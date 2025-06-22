package com.example.chat.DAO;

import com.example.chat.Entity.Message;
import com.example.chat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public void saveMessage(Message message) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

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

    // Method with timestamp filtering for polling
    public List<Message> getMessagesBetweenUsers(Long userId1, Long userId2, String afterTimestamp) {
        List<Message> messages = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT m FROM Message m " +
                    "JOIN FETCH m.sender " +
                    "WHERE (m.sender.id = :userId1 AND m.receiverId = :userId2) " +
                    "OR (m.sender.id = :userId2 AND m.receiverId = :userId1)";
            
            // Add timestamp filter if provided
            if (afterTimestamp != null && !afterTimestamp.trim().isEmpty()) {
                hql += " AND m.timeStamp > :afterTime";
            }
            
            hql += " ORDER BY m.timeStamp ASC";
            
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("userId1", userId1);
            query.setParameter("userId2", userId2);
            
            // Set timestamp parameter if provided
            if (afterTimestamp != null && !afterTimestamp.trim().isEmpty()) {
                try {
                    // Parse ISO datetime format from JavaScript
                    LocalDateTime afterTime = LocalDateTime.parse(afterTimestamp.replace("Z", ""));
                    query.setParameter("afterTime", afterTime);
                    System.out.println("Filtering messages after: " + afterTime); // DEBUG
                } catch (Exception e) {
                    System.err.println("Invalid timestamp format: " + afterTimestamp);
                    // If timestamp is invalid, ignore the filter
                }
            }
            
            messages = query.list();
            System.out.println("DAO: Found " + messages.size() + " messages between user " + userId1 + " and " + userId2 
                             + (afterTimestamp != null ? " after " + afterTimestamp : ""));
        } catch (Exception e) {
            System.err.println("Error retrieving messages between users: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    // Original method for backward compatibility
    public List<Message> getMessagesBetweenUsers(Long userId1, Long userId2) {
        return getMessagesBetweenUsers(userId1, userId2, null);
    }

    // NEW: Method with timestamp filtering for group messages
    public List<Message> getMessagesForGroup(String groupName, String afterTimestamp) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT m FROM Message m " +
                    "JOIN FETCH m.sender " +
                    "WHERE m.groupName = :groupName";
            
            // Add timestamp filter if provided
            if (afterTimestamp != null && !afterTimestamp.trim().isEmpty()) {
                hql += " AND m.timeStamp > :afterTime";
            }
            
            hql += " ORDER BY m.timeStamp ASC";
            
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("groupName", groupName);
            
            // Set timestamp parameter if provided
            if (afterTimestamp != null && !afterTimestamp.trim().isEmpty()) {
                try {
                    LocalDateTime afterTime = LocalDateTime.parse(afterTimestamp.replace("Z", ""));
                    query.setParameter("afterTime", afterTime);
                } catch (Exception e) {
                    System.err.println("Invalid timestamp format: " + afterTimestamp);
                    // If timestamp is invalid, ignore the filter
                }
            }
            
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Original method for backward compatibility
    public List<Message> getMessagesForGroup(String groupName) {
        return getMessagesForGroup(groupName, null);
    }

    public List<Message> getNewMessages() {
        List<Message> messages = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Lấy tin nhắn trong 1 phút gần đây
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            
            String hql = "SELECT DISTINCT m FROM Message m " +
                    "JOIN FETCH m.sender " +
                    "WHERE m.timeStamp > :oneMinuteAgo " +
                    "ORDER BY m.timeStamp ASC";
            
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("oneMinuteAgo", oneMinuteAgo);
            
            messages = query.list();
            System.out.println("SSE: Found " + messages.size() + " new messages in the last minute");
        } catch (Exception e) {
            System.err.println("Error retrieving new messages: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }
}