package com.example.chat.DAO;

import com.example.chat.Entity.Message;
import com.example.chat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public void saveMessage(Message message) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // FIX: Kiểm tra và log thông tin trước khi lưu
            System.out.println("Saving message: " + message.toString());
            if (message.getSender() == null) {
                throw new IllegalArgumentException("Message sender cannot be null");
            }
            if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Message content cannot be null or empty");
            }

            session.persist(message);
            transaction.commit();

            System.out.println("Message saved successfully with ID: " + message.getId());

        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                    System.out.println("Transaction rolled back successfully");
                } catch (Exception rbEx) {
                    System.err.println("Lỗi khi rollback transaction: " + rbEx.getMessage());
                    rbEx.printStackTrace();
                }
            }
            System.err.println("Lỗi khi lưu tin nhắn: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save message", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public List<Message> getMessagesBetweenUsers(Long userId1, Long userId2) {
        List<Message> messages = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT m FROM Message m " +
                    "JOIN FETCH m.sender " +
                    "WHERE (m.sender.id = :userId1 AND m.receiverId = :userId2) " +
                    "OR (m.sender.id = :userId2 AND m.receiverId = :userId1) " +
                    "ORDER BY m.timeStamp ASC";
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("userId1", userId1);
            query.setParameter("userId2", userId2);
            messages = query.list();
            System.out.println("DAO: Found " + messages.size() + " messages between user " + userId1 + " and " + userId2);
        } catch (Exception e) {
            System.err.println("Error retrieving messages between users: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    public List<Message> getMessagesForGroup(String groupName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT m FROM Message m " +
                    "JOIN FETCH m.sender " +
                    "WHERE m.groupName = :groupName " +
                    "ORDER BY m.timeStamp ASC";
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("groupName", groupName);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}