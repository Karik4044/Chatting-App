package com.example.chat.DAO;

import com.example.chat.Entity.Message;
import com.example.chat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

public class MessageDAO {

    public void saveMessage(Message message) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(message);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public List<Message> getMessagesBetweenUsers(Long userId1, Long userId2) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Message m JOIN FETCH m.sender " +
                    "WHERE (m.sender.id = :userId1 AND m.receiverId = :userId2) OR " +
                    "(m.sender.id = :userId2 AND m.receiverId = :userId1) " +
                    "ORDER BY m.timeStamp ASC";
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("userId1", userId1);
            query.setParameter("userId2", userId2);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Message> getMessagesBetweenUsers(Long userId1, Long userId2, String afterTimestampStr) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime afterTimestamp;
            try {
                afterTimestamp = LocalDateTime.parse(afterTimestampStr);
            } catch (DateTimeParseException e) {
                System.err.println("Invalid timestamp format: " + afterTimestampStr);
                return Collections.emptyList();
            }

            String hql = "FROM Message m JOIN FETCH m.sender " +
                    "WHERE ((m.sender.id = :userId1 AND m.receiverId = :userId2) OR " +
                    "(m.sender.id = :userId2 AND m.receiverId = :userId1)) " +
                    "AND m.timeStamp > :afterTimestamp " +
                    "ORDER BY m.timeStamp ASC";

            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("userId1", userId1);
            query.setParameter("userId2", userId2);
            query.setParameter("afterTimestamp", afterTimestamp);

            List<Message> messages = query.getResultList();
            System.out.println("DAO: Found " + messages.size() + " messages between user " + userId1 + " and " + userId2 + " after " + afterTimestampStr);
            return messages;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Message> getMessagesForGroup(String groupName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Message m JOIN FETCH m.sender WHERE m.groupName = :groupName ORDER BY m.timeStamp ASC";
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("groupName", groupName);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Message> getMessagesForGroup(String groupName, String afterTimestampStr) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime afterTimestamp;
            try {
                afterTimestamp = LocalDateTime.parse(afterTimestampStr);
            } catch (DateTimeParseException e) {
                System.err.println("Invalid timestamp format for group chat: " + afterTimestampStr);
                return Collections.emptyList();
            }

            String hql = "FROM Message m JOIN FETCH m.sender " +
                    "WHERE m.groupName = :groupName AND m.timeStamp > :afterTimestamp " +
                    "ORDER BY m.timeStamp ASC";
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("groupName", groupName);
            query.setParameter("afterTimestamp", afterTimestamp);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Message> getMessagesAfter(LocalDateTime timestamp) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Message m JOIN FETCH m.sender WHERE m.timeStamp > :timestamp ORDER BY m.timeStamp ASC";
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("timestamp", timestamp);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Optimized method for polling - get recent messages efficiently
    public List<Message> getRecentMessages(LocalDateTime afterTimestamp) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Get messages in the last 5 minutes for efficiency
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            LocalDateTime startTime = afterTimestamp != null ? afterTimestamp : fiveMinutesAgo;

            String hql = "FROM Message m JOIN FETCH m.sender " +
                    "WHERE m.timeStamp > :startTime " +
                    "ORDER BY m.timeStamp ASC";

            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("startTime", startTime);

            List<Message> messages = query.getResultList();
            System.out.println("Found " + messages.size() + " recent messages after: " + startTime);
            return messages;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // NEW: Method to get new messages for specific user
    public List<Message> getNewMessagesForUser(String currentUsername, LocalDateTime afterTimestamp) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Get messages where current user is sender or receiver
            String hql = "FROM Message m JOIN FETCH m.sender " +
                    "WHERE m.timeStamp > :afterTimestamp " +
                    "AND (m.sender.username = :currentUser " +
                    "OR m.receiverId IN (SELECT u.id FROM User u WHERE u.username = :currentUser) " +
                    "OR m.groupName IS NOT NULL) " +
                    "ORDER BY m.timeStamp ASC";

            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("afterTimestamp", afterTimestamp);
            query.setParameter("currentUser", currentUsername);

            List<Message> messages = query.getResultList();
            System.out.println("Found " + messages.size() + " new messages for user: " + currentUsername + " after: " + afterTimestamp);

            // Debug: Print each message
            for (Message msg : messages) {
                System.out.println("Message: " + msg.getSender().getUsername() + " -> " + msg.getContent() + " at " + msg.getTimeStamp());
            }

            return messages;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // NEW: Method to get latest message timestamp for user
    public LocalDateTime getLatestMessageTimestampForUser(String currentUsername) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT MAX(m.timeStamp) FROM Message m " +
                    "WHERE m.sender.username = :currentUser " +
                    "OR m.receiverId IN (SELECT u.id FROM User u WHERE u.username = :currentUser) " +
                    "OR m.groupName IS NOT NULL";

            Query<LocalDateTime> query = session.createQuery(hql, LocalDateTime.class);
            query.setParameter("currentUser", currentUsername);

            LocalDateTime result = query.uniqueResult();
            return result != null ? result : LocalDateTime.now().minusMinutes(1);
        } catch (Exception e) {
            e.printStackTrace();
            return LocalDateTime.now().minusMinutes(1);
        }
    }
}