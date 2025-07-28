package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.Cart;
import org.FoodOrder.server.models.FoodItem;
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
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving cart: " + e.getMessage());
        }
    }

    public void update(Cart cart) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(cart);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating cart: " + e.getMessage());
        }
    }

    public void remove(Cart cart) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.remove(session.contains(cart) ? cart : session.merge(cart));
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error removing cart: " + e.getMessage());
        }
    }

    public void removeByCustomerId(Long customerId) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            // پیدا کردن سبد خرید بر اساس customerId
            Cart cart = session.createQuery("FROM Cart WHERE customer.id = :customerId", Cart.class)
                    .setParameter("customerId", customerId)
                    .uniqueResult();

            if (cart != null) {
                // حذف همه آیتم‌های مرتبط در cart_item
                session.createQuery("DELETE FROM CartItem WHERE cart.id = :cartId")
                        .setParameter("cartId", cart.getId())
                        .executeUpdate();

                // حذف سبد خرید
                session.createQuery("DELETE FROM Cart WHERE id = :cartId")
                        .setParameter("cartId", cart.getId())
                        .executeUpdate();
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error removing cart by customer ID: " + e.getMessage(), e);
        }
    }

    public void removeCartItem(Long cartId, Long foodId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            String hql = "DELETE FROM CartItem WHERE cart.id = :cartId AND food.id = :foodId";
            Query query = session.createQuery(hql);
            query.setParameter("cartId", cartId);
            query.setParameter("foodId", foodId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error removing cart item: " + e.getMessage());
        }
    }
}