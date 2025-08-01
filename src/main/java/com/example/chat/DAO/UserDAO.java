package com.example.chat.DAO;

import com.example.chat.Entity.User;
import com.example.chat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction; // Thêm import này nếu cần dùng Transaction tường minh
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List; // Thêm import này cho getAllUsers

public class UserDAO {
    public boolean registerUser (String username, String passwordHash) {
        Transaction transaction = null; // Khai báo transaction
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction(); // Bắt đầu transaction
            User user = new User(username, passwordHash);
            session.persist(user);
            transaction.commit(); // Commit transaction
            return true;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) { // Kiểm tra transaction trước khi rollback
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public User getUserByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Sử dụng createQuery trực tiếp từ Session (khuyến nghị từ Hibernate 5.2+)
            return session.createQuery("FROM User WHERE username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ----- CÁC PHƯƠNG THỨC CẦN THIẾT CHO ChatServlet NHƯNG CHƯA CÓ -----
    // ChatServlet.java sử dụng userDAO.getAllUsers() và userDAO.getUserById()
    // Dưới đây là gợi ý triển khai cho các phương thức đó:
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            users = query.getResultList();
            // DEBUG: In ra số lượng user tìm thấy
            System.out.println("DEBUG [UserDAO]: Found " + users.size() + " users in the database.");
        } catch (Exception e) {
            System.err.println("ERROR [UserDAO]: Failed to get all users.");
            e.printStackTrace();
        }
        return users;
    }

    public User getUserById(Long id) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        User user = session.get(User.class, id);
        System.out.println("getUserById(" + id + ") result: " + (user != null ? user.getUsername() : "null"));
        return user;
    } catch (Exception e) {
        System.out.println("Error in getUserById(" + id + "): " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}
}