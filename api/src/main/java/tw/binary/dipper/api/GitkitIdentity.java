package tw.binary.dipper.api;

// Created by eason on 2015/4/22.

import com.google.gson.JsonObject;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClient.OobAction;
import com.google.identitytoolkit.GitkitClient.OobResponse;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.HttpSender;
import com.google.identitytoolkit.JsonTokenHelper;
import com.google.identitytoolkit.RpcHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*import org.json.JSONException;
import org.json.JSONObject;*/

public class GitkitIdentity {
    static String cookieName = "gtoken";
    static String apiKey = "AIzaSyAfArNl_B45xEk0dM6wOhoUPgfVielVpiU";
    static String serviceAccountEmail = "789806255860-ufrghvsss6japmlnj0cmrsds1okp5o9c@developer.gserviceaccount.com";
    static String widgetUrl = "http://localhost:8080/gitkit";
    static String googleClientId = "789806255860-ufrghvsss6japmlnj0cmrsds1okp5o9c.apps.googleusercontent.com";

    static GitkitClient getGitkitClient() {
        return GitkitClient.newBuilder()
                .setCookieName(cookieName)
                .setServiceAccountEmail(serviceAccountEmail)
                .setWidgetUrl(widgetUrl)
                .setGoogleClientId(googleClientId)
                .setKeyStream(new ByteArrayInputStream(apiKey.getBytes()))
                .build();
    }

    // TODO: Remove this hack and figure out a way for Gitkit to natively support the "verified" email property.
    static String getAuthTokenFromRequest(HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }
            for (Cookie cookie : cookies) {
                if ("gtoken".equals(cookie.getName())) {
                    RpcHelper rpcHelper = new RpcHelper(new HttpSender()
                            , "https://www.googleapis.com/identitytoolkit/v3/relyingparty/"
                            , serviceAccountEmail
                            , new ByteArrayInputStream(apiKey.getBytes()));
                    JsonTokenHelper tokenHelper = new JsonTokenHelper(googleClientId
                            , rpcHelper
                            , null);
                    JsonObject jsonToken = tokenHelper.verifyAndDeserialize(cookie.getValue()).getPayloadAsJsonObject();
                    return jsonToken.toString();
                }
            }
        } catch (SignatureException se) {
            throw new RuntimeException(se);
        }
        return null;
    }

    static boolean doesUserHaveVerifiedEmail(HttpServletRequest request) {
        try {
            String authToken = getAuthTokenFromRequest(request);
            if (authToken == null) return false;
            JSONObject theToken = new JSONObject(authToken);
            return theToken.getBoolean("verified");
        } catch (JSONException e) {
            return false;
        }
    }

    public static GitkitUser getUser(HttpServletRequest request) {
        try {
            GitkitClient gitkitClient = getGitkitClient();
            GitkitUser gitkitUser = gitkitClient.validateTokenInRequest(request);

            if (gitkitUser != null && !GitkitIdentity.doesUserHaveVerifiedEmail(request)) {
                gitkitUser.setEmail(null);  //沒有eMail
            }

            return gitkitUser;
        } catch (GitkitClientException gce) {
            return null;
        }
    }

    //Gitkit Widget
    public static void handleOauthCallback(HttpServletRequest request, HttpServletResponse resp) {
        resp.setContentType("text/html");

        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = request.getReader().readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String postBody;
        try {
            postBody = URLEncoder.encode(builder.toString(), "UTF-8");
            resp.getWriter().print(new Scanner(new File("gitkit-widget.html"), "UTF-8")
                    .useDelimiter("\\A").next()
                    .replaceAll("JAVASCRIPT_ESCAPED_POST_BODY", postBody));
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try {
                resp.getWriter().print(e.toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    //Email Endpoint
    public static void sendEmail(HttpServletRequest request, HttpServletResponse resp) {
        try {
            GitkitClient gitkitClient = getGitkitClient();
            OobResponse oobResponse = gitkitClient.getOobResponse(request);

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress("magiclotw@gmail.com", "Campfire Accounts"));
                //msg.addRecipient(Message.RecipientType.TO, new InternetAddress(oobResponse.getEmail(), oobResponse.getRecipient()));
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress("magiclo99@yahoo.com.tw", "Magic Lo"));
                if (oobResponse.getOobAction().equals(OobAction.CHANGE_EMAIL)) {
                    msg.setSubject("Email address change for Campfire account");
                    msg.setText("Hello!\n\n The email address for your Campfire account will be changed from " + oobResponse.getEmail() + " to " + oobResponse.getNewEmail() + " when you click this confirmation link:\n\n " + oobResponse.getOobUrl().get() + "\n\nIf you didn't request an email address change for this account, please disregard this message.");
                } else if (oobResponse.getOobAction().equals(OobAction.RESET_PASSWORD)) {
                    msg.setSubject("Password change for Campfire account");
                    msg.setText("Hello!\n\n The password for your Campfire account will be reset when you click this confirmation link:\n\n " + oobResponse.getOobUrl().get() + "\n\nIf you didn't request a password change for this account, please disregard this message.");
                }
                Transport.send(msg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            resp.getWriter().write(oobResponse.getResponseBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

