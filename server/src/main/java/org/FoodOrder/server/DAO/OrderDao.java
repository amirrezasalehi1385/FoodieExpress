package org.FoodOrder.server.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.FoodOrder.server.enums.OrderStatus;
import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.utils.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import java.util.*;

public class OrderDao {
    public static Order findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Order.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static List<Order> findByCourierIdWithFilters(Long courierId, String search, String vendor, String user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder(
                    "SELECT o FROM Order o " +
                            "LEFT JOIN FETCH o.items " +
                            "WHERE o.courier.id = :courierId " +
                            "AND o.status IN (:activeStatuses)");
            Map<String, Object> params = new HashMap<>();
            params.put("courierId", courierId);
            params.put("activeStatuses", List.of(org.FoodOrder.server.enums.OrderStatus.COMPLETED));

            if (search != null && !search.isEmpty()) {
                hql.append(" AND (o.id LIKE :search OR o.deliveryAddress LIKE :search)");
                params.put("search", "%" + search + "%");
            }
            if (vendor != null && !vendor.isEmpty()) {
                hql.append(" AND o.restaurantId = :vendor");
                params.put("vendor", Long.parseLong(vendor));
            }
            if (user != null && !user.isEmpty()) {
                hql.append(" AND o.customerId = :user");
                params.put("user", Long.parseLong(user));
            }

            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            params.forEach(query::setParameter);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    public static List<Order> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("from Order ", Order.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void update(Order order) {
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

    public List<Order> findByCourierId(Long courierId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            List<Order> orders = session.createQuery(
                            "FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.food WHERE o.courier.id = :courierId", Order.class)
                    .setParameter("courierId", courierId)
                    .list();
            transaction.commit();
            return orders;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<Order> findByStatus(OrderStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery(
                    "SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.food WHERE o.status = :status", Order.class);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Order> findByRestaurantId(Long restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery(
                    "SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.food LEFT JOIN FETCH o.restaurant WHERE o.restaurant.id = :restaurantId",
                    Order.class);
            query.setParameter("restaurantId", restaurantId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Order> findByCustomerId(Long customerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery(
                    "SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.food LEFT JOIN FETCH o.restaurant WHERE o.customer.id = :customerId",
                    Order.class);
            query.setParameter("customerId", customerId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void save(Order order) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(order);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static List<Order> getOrdersByRestaurantIdWithFilters(Long restaurantId, Map<String, String> filters) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        List<Order> orders = new ArrayList<>();

        try {
            tx = session.beginTransaction();

            StringBuilder hql = new StringBuilder(
                    "SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.food WHERE o.restaurant.id = :restaurantId");
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("restaurantId", restaurantId);

            if (filters != null) {
                if (filters.containsKey("status") && !filters.get("status").isEmpty()) {
                    hql.append(" AND o.status = :status");
                    parameters.put("status", OrderStatus.valueOf(filters.get("status").toUpperCase()));
                }
                if (filters.containsKey("user") && !filters.get("user").isEmpty()) {
                    hql.append(" AND LOWER(o.customer.fullName) LIKE LOWER(:customerFullName)");
                    parameters.put("customerFullName", "%" + filters.get("user") + "%");
                }
                if (filters.containsKey("courier") && !filters.get("courier").isEmpty()) {
                    hql.append(" AND o.courier IS NOT NULL AND LOWER(o.courier.fullName) LIKE LOWER(:courierFullName)");
                    parameters.put("courierFullName", "%" + filters.get("courier") + "%");
                }
                if (filters.containsKey("search") && !filters.get("search").isEmpty()) {
                    hql.append(" AND EXISTS (SELECT 1 FROM OrderItem oi WHERE oi.order = o AND LOWER(oi.food.name) LIKE LOWER(:itemName))");
                    parameters.put("itemName", "%" + filters.get("search") + "%");
                }
            }

            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            parameters.forEach(query::setParameter);

            orders = query.getResultList();

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return orders;
    }

    public List<Order> findHistoryForAdmin(String searchFilter, String vendorFilter, String courierFilter,
                                           String customerFilter, String statusFilter) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT DISTINCT o.* FROM orders o " +
                            "LEFT JOIN order_item oi ON o.id = oi.order_id " +
                            "LEFT JOIN fooditem fi ON oi.food_id = fi.id " +
                            "LEFT JOIN restaurant r ON o.restaurant_id = r.id " +
                            "LEFT JOIN users c ON o.customer_id = c.id " +
                            "LEFT JOIN users cr ON o.deliveryman_id = cr.id "
            );

            List<String> conditions = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();

            if (vendorFilter != null && !vendorFilter.isBlank()) {
                conditions.add("LOWER(r.title) LIKE :vendorFilter");
                parameters.put("vendorFilter", "%" + vendorFilter.toLowerCase() + "%");
            }

            if (courierFilter != null && !courierFilter.isBlank()) {
                conditions.add("LOWER(cr.full_name) LIKE :courierFilter");
                parameters.put("courierFilter", "%" + courierFilter.toLowerCase() + "%");
            }

            if (customerFilter != null && !customerFilter.isBlank()) {
                conditions.add("LOWER(c.full_name) LIKE :customerFilter");
                parameters.put("customerFilter", "%" + customerFilter.toLowerCase() + "%");
            }

            if (statusFilter != null && !statusFilter.isBlank()) {
                conditions.add("LOWER(o.status) LIKE :statusFilter");
                parameters.put("statusFilter", "%" + statusFilter.toLowerCase() + "%");
            }

            if (searchFilter != null && !searchFilter.isBlank()) {
                conditions.add("LOWER(fi.name) LIKE :searchFilter");
                parameters.put("searchFilter", "%" + searchFilter.toLowerCase() + "%");
            }

            if (!conditions.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            sql.append(" ORDER BY o.created_at DESC");

            NativeQuery<Order> query = session.createNativeQuery(sql.toString(), Order.class);
            parameters.forEach(query::setParameter);
            List<Order> orders = query.getResultList();
            for (Order order : orders) {
                Hibernate.initialize(order.getItems());
            }
            return orders;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}