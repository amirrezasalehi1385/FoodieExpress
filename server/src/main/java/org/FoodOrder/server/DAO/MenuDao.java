package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.FoodItem;
import org.FoodOrder.server.models.Menu;
import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.models.Restaurant;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.FoodOrder.server.utils.HibernateUtil;

import java.util.List;

public class MenuDao {

    public void save(Menu menu) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(menu);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void update(Menu menu) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(menu);
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
            Menu menu = session.get(Menu.class, id);
            if (menu != null) {
                session.delete(menu);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    List<Menu> findByRestaurantId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Menu WHERE restaurant.id = :restaurantId";
            return session.createQuery(hql, Menu.class)
                    .setParameter("restaurantId", id)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
