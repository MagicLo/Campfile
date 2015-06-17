package tw.binary.dipper.api.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tw.binary.dipper.api.GitkitIdentity;

// Created by eason on 2015/4/21.
public class GitKitServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            doGet(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //response.setCharacterEncoding("utf-8");

        if (request.getRequestURI().startsWith("/g/gitkit1")) {
            GitkitIdentity.handleOauthCallback(request, response);
            return;
        }
        if (request.getRequestURI().startsWith("/g/m")) {
            //GitkitIdentity.sendEmail(request, response);
            new MailServlet().doPost(request, response);
            return;
        }
    }
}
