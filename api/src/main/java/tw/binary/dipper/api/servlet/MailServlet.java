package tw.binary.dipper.api.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Created by eason on 2015/4/20.
@SuppressWarnings("serial")
public class MailServlet extends HttpServlet {
    //action=changeEmail&email=...&newEmail=...
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String fromAddr = "magiclotw@gmail.com";
        String sender = "Campfire";
        //String toAddr = req.getParameter("to");
        //String recipient = req.getParameter("recipient");
        //String subject = req.getParameter("subject");
        //String msgBody = req.getParameter("msgBody");
        String toAddr = "magiclotw@gmail.com";
        String recipient = "Magic Lo";
        String subject = "Test message";
        String msgBody = "No message";
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromAddr, sender));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddr, recipient));
            msg.setSubject(subject);
            msg.setText(msgBody);
            Transport.send(msg);

        } catch (Exception e) {
            resp.setContentType("text/plain");
            resp.getWriter().println("Something went wrong. Please try again.");
            throw new RuntimeException(e);
        }

        resp.setContentType("text/plain");
        resp.getWriter().println("Thank you for your feedback. An Email has been send out.");
    }
}
