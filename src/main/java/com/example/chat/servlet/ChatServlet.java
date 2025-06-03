package com.example.chat.servlet;

import com.example.chat.DAO.MessageDAO;
import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.Message;
import com.example.chat.Entity.User;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    private final MessageDAO messageDAO = new MessageDAO();
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // ?target=foo&type=private|group
        String target = req.getParameter("target");
        String type = req.getParameter("type");
        HttpSession session = req.getSession();
        User current = (User) session.getAttribute("user");
        List<Message> history;
        if ("group".equals(type)) {
            history = messageDAO.getMessagesForGroup(target);
        } else {
            User u = userDAO.getUserByUsername(target);
            history = messageDAO.getMessagesBetweenUsers(current.getId(), u.getId());
        }
        resp.setContentType("application/json;charset=UTF-8");
        gson.toJson(history, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // gửi tin
        String target = req.getParameter("target");
        String type = req.getParameter("type");
        String content = req.getParameter("message");
        HttpSession session = req.getSession();
        User current = (User) session.getAttribute("user");
        Message m;
        if ("group".equals(type)) {
            m = new Message(current.getId(), null, content);
            m.setGroupName(target);
            messageDAO.saveMessage(m);
        } else {
            User u = userDAO.getUserByUsername(target);
            m = new Message(current.getId(), u.getId(), content);
            messageDAO.saveMessage(m);
        }
        // trả về chính tin nhắn vừa gửi để render ngay
        resp.setContentType("application/json;charset=UTF-8");
        gson.toJson(m, resp.getWriter());
    }
}