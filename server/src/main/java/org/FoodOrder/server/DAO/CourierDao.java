package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.Courier;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.FoodOrder.server.utils.HibernateUtil;

import java.util.List;

public class CourierDao implements UserInterface<Courier, Long> {

    @Override
    public void save(Courier entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    @Override
    public  Courier findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Courier.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Courier> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Courier", Courier.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public void update(Courier entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    @Override
    public void deleteById(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Courier entity = session.get(Courier.class, id);
            if (entity != null) {
                session.remove(entity);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Courier entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            if (!session.contains(entity)) {
                entity = (Courier) session.merge(entity);
            }
            session.remove(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    @Override
    public boolean existsById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Courier.class, id) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Courier findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Courier c WHERE c.email = :email", Courier.class)
                    .setParameter("email", email)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Courier findByPublicId(String publicId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Courier c WHERE c.publicId = :publicId", Courier.class)
                    .setParameter("publicId", publicId)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Courier findByPhone(String phone) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Courier c WHERE c.phone = :phone", Courier.class)
                    .setParameter("phone", phone)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
