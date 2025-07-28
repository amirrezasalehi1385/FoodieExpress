package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.models.Restaurant;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.FoodOrder.server.utils.HibernateUtil;

import java.util.List;

public class RestaurantDao {

    public static void save(Restaurant restaurant) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(restaurant);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            throw e;
        }
    }


    public static Restaurant findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Restaurant.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static List<Restaurant> findByVendorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT r FROM Restaurant r LEFT JOIN FETCH r.reviews WHERE r.seller.id = :vendorId";
            return session.createQuery(hql, Restaurant.class)
                    .setParameter("vendorId", id)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void update(Restaurant restaurant) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(restaurant);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant restaurant = session.get(Restaurant.class, id);
            if (restaurant != null) {
                session.delete(restaurant);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static List<Restaurant> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Restaurant> query = session.createQuery("from Restaurant", Restaurant.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static List<Restaurant> findByPhoneNumber(String phone) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from Restaurant where phone = :phone";
            Query query = session.createQuery(hql);
            query.setParameter("phone", phone);
            return query.list();
        }
    }


}
