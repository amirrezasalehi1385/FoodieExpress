package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.Restaurant;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.FoodOrder.server.utils.HibernateUtil;

import java.util.List;

public class RestaurantDao {

    public void save(Restaurant restaurant) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(restaurant);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public Restaurant findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Restaurant.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Restaurant findByVendorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Restaurant WHERE vendor_id = :vendorId";
            return session.createQuery(hql, Restaurant.class).setParameter("vendorId", id).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void update(Restaurant restaurant) {
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

    public void delete(Long id) {
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

    public List<Restaurant> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Restaurant> query = session.createQuery("from Restaurant", Restaurant.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
