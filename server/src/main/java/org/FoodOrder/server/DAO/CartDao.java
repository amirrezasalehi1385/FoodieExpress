package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.Cart;
import org.FoodOrder.server.models.FoodItem;
import org.FoodOrder.server.models.Restaurant;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.FoodOrder.server.utils.HibernateUtil;

public class CartDao {

    public Cart findByCustomerId(Long customerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Cart WHERE customer.id = :customerId";
            return session.createQuery(hql, Cart.class)
                    .setParameter("customerId", customerId)
                    .uniqueResult();
        }
    }

    public void save(Cart cart) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(cart);
            tx.commit();
        }
    }
}

