package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.Payment;
import org.FoodOrder.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDao {

    public void save(Payment t) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            org.hibernate.Transaction hibernateTx = session.beginTransaction();
            session.persist(t);
            hibernateTx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Transaction save failed", e);
        }
    }

    public List<Payment> findByUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM org.FoodOrder.server.models.Payment t WHERE t.user.id = :userId ORDER BY t.createdAt DESC";
            Query<Payment> query = session.createQuery(hql, Payment.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Payment> findHistoryForAdmin(String searchFilter, String userFilter,
                                             String methodFilter, String statusFilter) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT t FROM org.FoodOrder.server.models.Transaction t ");
            Map<String, Object> params = new HashMap<>();
            List<String> conditions = new ArrayList<>();

            if (searchFilter != null && !searchFilter.isBlank()) {
                hql.append("JOIN t.order o ");
                conditions.add("EXISTS (SELECT 1 FROM OrderItem oi WHERE oi.order = o AND LOWER(oi.itemName) LIKE :searchFilter)");
                params.put("searchFilter", "%" + searchFilter.toLowerCase() + "%");
            }

            if (userFilter != null && !userFilter.isBlank()) {
                conditions.add("LOWER(t.user.fullName) LIKE :userFilter");
                params.put("userFilter", "%" + userFilter.toLowerCase() + "%");
            }

            if (statusFilter != null && !statusFilter.isBlank()) {
                conditions.add("LOWER(t.status) LIKE :statusFilter");
                params.put("statusFilter", "%" + statusFilter.toLowerCase() + "%");
            }

            if (methodFilter != null && !methodFilter.isBlank()) {
                conditions.add("LOWER(t.method) LIKE :methodFilter");
                params.put("methodFilter", "%" + methodFilter.toLowerCase() + "%");
            }

            if (!conditions.isEmpty()) {
                hql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            hql.append(" ORDER BY t.createdAt DESC");

            Query<Payment> query = session.createQuery(hql.toString(), Payment.class);
            params.forEach(query::setParameter);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}