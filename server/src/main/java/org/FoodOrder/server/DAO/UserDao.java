package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.Menu;
import org.FoodOrder.server.models.Restaurant;
import org.FoodOrder.server.models.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.FoodOrder.server.utils.HibernateUtil;

import java.util.List;
//import exception.UserAlreadyExistsException;


public class UserDao implements UserInterface<User,Long> {
    @Override
    public void save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    @Override
    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    @Override
    public User findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT u FROM User u WHERE u.id = :id";
            return session.createQuery(sql, User.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public boolean existsById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT u FROM User u WHERE u.id = :id";
            if(session.createQuery(sql, User.class).setParameter("id", id).getSingleResult() == null){
                return false;
            }else {
                return true;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static User findByPhoneNumber(String phoneNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber";
            return session.createQuery(hql, User.class)
                    .setParameter("phoneNumber", phoneNumber)
                    .getSingleResult();
        }
    }
    public User findByPublicId(String publicId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT u FROM User u WHERE u.publicId = :publicId";
            return session.createQuery(hql, User.class)
                    .setParameter("publicId", publicId)
                    .getSingleResult();
        }
    }
    public User findByEmail(String email) {
        try(Session session = HibernateUtil.getSessionFactory().openSession();){
            String hql = "SELECT u FROM User u WHERE u.email = :email";
            return session.createQuery(hql ,User.class)
                    .setParameter("email",email)
                    .getSingleResult();
        }
    }
    @Override
    public void deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    @Override
    public List<User> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("from User ", User.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}



