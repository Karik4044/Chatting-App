package com.example.chat.DAO;

import com.example.chat.Entity.Message;
import com.example.chat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class MessageDAO {
    
    public void saveMessage(Message message) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(message);
            transaction.commit();
            System.out.println("Message saved successfully");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("Error saving message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Message> getMessagesBetweenUsers(Long user1Id, Long user2Id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Message m WHERE " +
                "(m.senderId = :user1 AND m.receiverId = :user2) OR " +
                "(m.senderId = :user2 AND m.receiverId = :user1) " +
                "ORDER BY m.timeStamp ASC", Message.class)
                .setParameter("user1", user1Id)
                .setParameter("user2", user2Id)
                .list();
        }
    }

    public List<Message> getMessagesForGroup(String groupName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Message m WHERE m.groupName = :groupName ORDER BY m.timeStamp ASC", 
                Message.class)
                .setParameter("groupName", groupName)
                .list();
        }
    }
}