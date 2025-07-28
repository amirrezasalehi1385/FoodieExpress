package org.FoodOrder.server.DAO;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.FoodOrder.server.models.FoodItem;
import org.FoodOrder.server.models.Menu;
import org.FoodOrder.server.models.Restaurant;
import org.FoodOrder.server.models.User;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.FoodOrder.server.utils.HibernateUtil;

import java.util.List;

public class FoodDao {

    public static void save(FoodItem foodItem) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(foodItem);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }


    public static FoodItem findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            FoodItem foodItem = session.get(FoodItem.class, id);
            if (foodItem != null) {
                Hibernate.initialize(foodItem.getCategories()); // 👈 این خط بسیار مهمه
            }
            return foodItem;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public static void update(FoodItem foodItem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(foodItem);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static void deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            FoodItem foodItem = session.get(FoodItem.class, id);
            if (foodItem != null) {
                session.delete(foodItem);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    public static List<FoodItem> findByRestaurantId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM FoodItem f WHERE f.restaurant.id = :restaurantId";
            List<FoodItem> items = session.createQuery(hql, FoodItem.class)
                    .setParameter("restaurantId", id)
                    .getResultList();
            for (FoodItem item : items) {
                item.getCategories().size(); // triggers lazy loading
            }

            return items;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<FoodItem> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<FoodItem> query = session.createQuery("from FoodItem ", FoodItem.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
