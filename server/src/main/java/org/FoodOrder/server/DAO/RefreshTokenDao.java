package org.FoodOrder.server.DAO;
import org.FoodOrder.server.models.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.FoodOrder.server.utils.HibernateUtil;
import java.util.List;
import java.util.Optional;

public class RefreshTokenDao implements RefreshTokenInterfaceDao {

    @Override
    public void save(RefreshToken token) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(token);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error saving refresh token", e);
        }
    }

    @Override
    public RefreshToken findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(RefreshToken.class, id);
        }
    }

    @Override
    public List<RefreshToken> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM RefreshToken", RefreshToken.class).list();
        }
    }

    @Override
    public void update(RefreshToken token) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(token);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error updating refresh token", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            RefreshToken token = session.get(RefreshToken.class, id);
            if (token != null) {
                session.remove(token);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error deleting refresh token by ID", e);
        }
    }

    @Override
    public void delete(RefreshToken token) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            if (!session.contains(token)) {
                token = (RefreshToken) session.merge(token);
            }
            session.remove(token);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error deleting refresh token", e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            RefreshToken token = session.get(RefreshToken.class, id);
            return token != null;
        }
    }

    @Override
    public RefreshToken findByToken(String tokenValue) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<RefreshToken> query = session.createQuery(
                    "FROM RefreshToken r WHERE r.token = :tokenValue", RefreshToken.class);
            query.setParameter("tokenValue", tokenValue);
            return query.uniqueResult();
        }
    }

    @Override
    public void deleteByUser(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query<RefreshToken> query = session.createQuery(
                    "FROM RefreshToken r WHERE r.user = :userValue", RefreshToken.class);
            query.setParameter("userValue", user);
            List<RefreshToken> tokens = query.list();
            for (RefreshToken token : tokens) {
                session.remove(token);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error deleting refresh tokens by user", e);
        }
    }

    public Optional<RefreshToken> findByTokenString(String tokenString) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<RefreshToken> query = session.createQuery(
                    "FROM RefreshToken r WHERE r.token = :tokenStringValue", RefreshToken.class);
            query.setParameter("tokenStringValue", tokenString);
            RefreshToken result = query.uniqueResult();
            return result == null ? Optional.empty() : Optional.of(result);
        }
    }
}
