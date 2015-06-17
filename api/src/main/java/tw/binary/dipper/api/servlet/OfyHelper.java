package tw.binary.dipper.api.servlet;

/**
 * Created by eason on 2015/5/15.
 */

import com.googlecode.objectify.ObjectifyService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import tw.binary.dipper.api.GcmMessage;
import tw.binary.dipper.api.MyResource;


/**
 * In order to use Objectify in a JSP, we need a helper class that registers the MODEL classes
 * in the JSP servlet context
 * <p/>
 * OfyHelper, a ServletContextListener, is setup in web.xml to run before a JSP is run.  This is
 * required to let JSP's access Ofy.
 */
public class OfyHelper implements ServletContextListener {
    public void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warm up request, or the first user
        // request if no warm up request was invoked.
        ObjectifyService.register(GcmMessage.class);
        //ObjectifyService.register(CFUser.class);
        ObjectifyService.register(MyResource.class);
    }

    public void contextDestroyed(ServletContextEvent event) {
        // App Engine does not currently invoke this method.
    }
}

