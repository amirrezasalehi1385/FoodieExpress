package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.Review;
import org.FoodOrder.server.models.User;
import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class ReviewDao {

    private SessionFactory sessionFactory;

    public ReviewDao() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    /**
     * ذخیره یک Review جدید
     */
    public Review save(Review review) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(review);
            transaction.commit();
            return review;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * به‌روزرسانی یک Review موجود
     */
    public Review update(Review review) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.update(review);
            transaction.commit();
            return review;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * حذف Review با ID
     */
    public boolean delete(Long id) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Review review = session.get(Review.class, id);
            if (review != null) {
                session.delete(review);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * جستجوی Review با ID
     */
    public Review findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Review.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    /**
//     * دریافت تمام Reviews
//     */
//    public List<Review> getAll() {
//        try (Session session = sessionFactory.openSession()) {
//            Query<Review> query = session.createQuery("FROM Review ORDER BY createdAt DESC", Review.class);
//            return query.getResultList();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * دریافت Reviews بر اساس User ID
//     */
//    public List<Review> findByUserId(Long userId) {
//        try (Session session = sessionFactory.openSession()) {
//            Query<Review> query = session.createQuery(
//                    "FROM Review WHERE user.id = :userId ORDER BY createdAt DESC", Review.class);
//            query.setParameter("userId", userId);
//            return query.getResultList();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * دریافت Review بر اساس Order ID
//     */
    public Review findByOrderId(Long orderId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Review> query = session.createQuery(
                    "FROM Review WHERE order.id = :orderId", Review.class);
            query.setParameter("orderId", orderId);
            List<Review> reviews = query.getResultList();
            return reviews.isEmpty() ? null : reviews.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
//
//    /**
//     * دریافت Reviews بر اساس Restaurant ID
//     */
//    public List<Review> findByRestaurantId(Long restaurantId) {
//        try (Session session = sessionFactory.openSession()) {
//            Query<Review> query = session.createQuery(
//                    "FROM Review WHERE order.restaurant.id = :restaurantId ORDER BY createdAt DESC",
//                    Review.class);
//            query.setParameter("restaurantId", restaurantId);
//            return query.getResultList();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * دریافت Reviews بر اساس امتیاز
//     */
//    public List<Review> findByRating(Integer rating) {
//        try (Session session = sessionFactory.openSession()) {
//            Query<Review> query = session.createQuery(
//                    "FROM Review WHERE rating = :rating ORDER BY createdAt DESC", Review.class);
//            query.setParameter("rating", rating);
//            return query.getResultList();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * دریافت Reviews بر اساس محدوده امتیاز
//     */
//    public List<Review> findByRatingRange(Integer minRating, Integer maxRating) {
//        try (Session session = sessionFactory.openSession()) {
//            Query<Review> query = session.createQuery(
//                    "FROM Review WHERE rating >= :minRating AND rating <= :maxRating ORDER BY createdAt DESC",
//                    Review.class);
//            query.setParameter("minRating", minRating);
//            query.setParameter("maxRating", maxRating);
//            return query.getResultList();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }

    /**
     * محاسبه میانگین امتیاز یک رستوران
     */
    public Double getAverageRatingByRestaurant(Long restaurantId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Double> query = session.createQuery(
                    "SELECT AVG(rating) FROM Review WHERE order.restaurant.id = :restaurantId",
                    Double.class);
            query.setParameter("restaurantId", restaurantId);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * شمارش تعداد Reviews یک رستوران
     */
    public Long countByRestaurant(Long restaurantId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM Review WHERE order.restaurant.id = :restaurantId",
                    Long.class);
            query.setParameter("restaurantId", restaurantId);
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }


//
//    /**
//     * دریافت Reviews اخیر (محدود به تعداد مشخص)
//     */
//    public List<Review> getRecentReviews(int limit) {
//        try (Session session = sessionFactory.openSession()) {
//            Query<Review> query = session.createQuery(
//                    "FROM Review ORDER BY createdAt DESC", Review.class);
//            query.setMaxResults(limit);
//            return query.getResultList();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * دریافت Reviews بر اساس بازه زمانی
//     */
//    public List<Review> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
//        try (Session session = sessionFactory.openSession()) {
//            Query<Review> query = session.createQuery(
//                    "FROM Review WHERE createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC",
//                    Review.class);
//            query.setParameter("startDate", startDate);
//            query.setParameter("endDate", endDate);
//            return query.getResultList();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * دریافت Reviews بر اساس User و Restaurant
//     */
//    public List<Review> findByUserAndRestaurant(Long userId, Long restaurantId) {
//        try (Session session = sessionFactory.openSession()) {
//            Query<Review> query = session.createQuery(
//                    "FROM Review WHERE user.id = :userId AND order.restaurant.id = :restaurantId ORDER BY createdAt DESC",
//                    Review.class);
//            query.setParameter("userId", userId);
//            query.setParameter("restaurantId", restaurantId);
//            return query.getResultList();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
}