package com.example.chat.DAO;

import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class messagedao {
    
    public void saveMessage(Message message) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(message);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public Message getMessageById(Long messageId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Message.class, messageId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Message> getMessagesBetweenUsers(User user1, User user2, int limit, int offset) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // This query fetches messages where user1 is sender and user2 is recipient, OR user2 is sender and user1 is recipient
            String hql = "FROM Message m WHERE (m.sender = :user1 AND m.recipient = :user2) OR (m.sender = :user2 AND m.recipient = :user1) ORDER BY m.timestamp ASC";
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("user1", user1);
            query.setParameter("user2", user2);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Message> getRecentMessagesForUser(User user, int limit) {
         // This is a simplified example. A real implementation might involve more complex logic
         // to determine "recent" conversations (e.g., distinct conversations, last message per conversation).
         // This query gets the latest 'limit' messages sent or received by the user.
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Message m WHERE m.sender = :user OR m.recipient = :user ORDER BY m.timestamp DESC";
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("user", user);
            query.setMaxResults(limit);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Message> getUnreadMessages(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Message> query = session.createQuery(
                "FROM Message WHERE receiver.id = :userId AND isRead = false ORDER BY sentAt ASC", 
                Message.class
            );
            query.setParameter("userId", userId);
            return query.list();
        }
    }
    
    public void markMessageAsRead(Long messageId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Message message = session.get(Message.class, messageId);
            if (message != null) {
                message.setRead(true);
                message.setReadAt(LocalDateTime.now());
                session.update(message);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public List<Message> getRecentConversations(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT m FROM Message m WHERE " +
                        "(m.sender.id = :userId OR m.receiver.id = :userId) " +
                        "AND m.sentAt = (SELECT MAX(m2.sentAt) FROM Message m2 WHERE " +
                        "((m2.sender.id = m.sender.id AND m2.receiver.id = m.receiver.id) OR " +
                        "(m2.sender.id = m.receiver.id AND m2.receiver.id = m.sender.id)))";
            Query<Message> query = session.createQuery(hql, Message.class);
            query.setParameter("userId", userId);
            return query.list();
        }
    }
}
