package com.example.chat.DAO;

import com.example.chat.Entity.Message;
import com.example.chat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class MessageDAO {

    /** Lưu message mới, trả về true nếu thành công */
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

    /** Lấy danh sách tin nhắn cho người dùng cụ thể */
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
}
