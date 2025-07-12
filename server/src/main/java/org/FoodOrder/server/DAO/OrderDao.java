package org.FoodOrder.server.DAO;

import org.FoodOrder.server.enums.OrderStatus;
import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class OrderDao {
    public Order findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Order.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void update(Order order) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(order);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    public List<Order> findByStatus(OrderStatus status) throws Exception {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("SELECT o FROM Order o WHERE o.status = :status");
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
