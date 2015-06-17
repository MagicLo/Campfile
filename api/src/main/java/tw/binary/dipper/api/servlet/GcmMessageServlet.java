package tw.binary.dipper.api.servlet;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tw.binary.dipper.api.CFUser;
import tw.binary.dipper.api.GcmMessage;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by eason on 2015/5/15.
 */
public class GcmMessageServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(GcmMessageServlet.class.getCanonicalName());

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(CFUser.class);
        ObjectifyService.register(GcmMessage.class);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Sends a message to the GCM server.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String from = req.getParameter(Constants.MESSAGE_COL_FROM);
        String to = req.getParameter(Constants.MESSAGE_COL_TO);
        String msg = req.getParameter(Constants.MESSAGE_COL_MSG);
        String sentTime = req.getParameter(Constants.MESSAGE_COL_SENT);

        //利用传近来的参数:from找到User的資料
        CFUser fromCFUser = ofy().load().type(CFUser.class).id(from).now();
        if (fromCFUser == null) return;

        //取得收件人的資料
        CFUser toCFUser = ofy().load().type(CFUser.class).id(to).now();
        if (toCFUser == null) return;

        //产生一个传送器
        Sender sender = new Sender(Constants.API_KEY);
        Message message = new Message.Builder()
//			.delayWhileIdle(true)
                .addData(Constants.MESSAGE_COL_MSG, msg)
                .addData(Constants.MESSAGE_COL_FROM, from)
                .addData(Constants.MESSAGE_COL_TO, to)
                .addData(Constants.MESSAGE_COL_DISPLAYNAME, fromCFUser.getDisplayName())
                .addData(Constants.MESSAGE_COL_PHOTOURL, fromCFUser.getPhotoURL())
                .addData(Constants.MESSAGE_COL_SENT, sentTime)
                .build();

        try {
            Result result = sender.send(message, toCFUser.getGcmRegId(), 5); //送出訊息
            GcmMessage gcmMessage = new GcmMessage(result.getMessageId(), from, to, msg, sentTime);
            //寫入Cloud DB
            ofy().save().entity(gcmMessage).now();
            resp.setContentType("text/plain");
            resp.getWriter().println(result.getMessageId());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

}

