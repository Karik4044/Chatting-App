package com.example.chat.DAO;

import com.example.chat.Entity.Message;
import com.example.chat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageDAO {

    public boolean saveMessage(Message message) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(message);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public List<Message> getMessagesForUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select m " +
                         "from Message m " +
                         "join fetch m.sender " +
                         "where m.receiverId = :uid " +
                         "or m.senderId = :uid " +
                         "order by m.timeStamp asc";
            return session.createQuery(hql, Message.class)
                          .setParameter("uid", userId)
                          .getResultList();
        }
    }

    // Get messages between two users
    public List<Message> getMessagesBetweenUsers(Long userId1, Long userId2) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select m " +
                         "from Message m " +
                         "join fetch m.sender " +
                         "where (m.senderId = :id1 and m.receiverId = :id2) " +
                         "or (m.senderId = :id2 and m.receiverId = :id1) " +
                         "order by m.timeStamp asc";
            return session.createQuery(hql, Message.class)
                          .setParameter("id1", userId1)
                          .setParameter("id2", userId2)
                          .getResultList();
        }
    }

    // (Optional) Get messages for a group chat
    public List<Message> getMessagesForGroup(String groupName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select m " +
                         "from Message m " +
                         "join fetch m.sender " +
                         "where m.groupName = :gname " +
                         "order by m.timeStamp asc";
            return session.createQuery(hql, Message.class)
                          .setParameter("gname", groupName)
                          .getResultList();
        }
    }
}