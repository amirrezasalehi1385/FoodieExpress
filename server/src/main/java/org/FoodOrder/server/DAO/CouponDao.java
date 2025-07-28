package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.Coupon;
import org.hibernate.Session;
import org.FoodOrder.server.utils.HibernateUtil;

import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CouponDao {

    public void save(Coupon coupon) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(coupon);
            tx.commit();
        }
    }

    public Coupon findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Coupon.class, id);
        }
    }

    public List<Coupon> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery("FROM Coupon", Coupon.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void update(Coupon coupon) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(coupon);
            tx.commit();
        }
    }

    public void deleteById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Coupon coupon = session.get(Coupon.class, id);
            if (coupon != null) {
                session.delete(coupon);
            }
            tx.commit();
        }
    }

    public void delete(Coupon coupon) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            if (!session.contains(coupon)) {
                coupon = (Coupon) session.merge(coupon);
            }
            session.delete(coupon);
            tx.commit();
        }
    }

    public boolean existsById(Integer id) {
        return findById(id) != null;
    }

    public Optional<Coupon> findByCode(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return Optional.empty();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery(
                    "FROM Coupon c WHERE c.couponCode = :code", Coupon.class);
            query.setParameter("code", couponCode);
            return Optional.of(query.uniqueResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
