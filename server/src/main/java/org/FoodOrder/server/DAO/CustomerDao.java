package org.FoodOrder.server.DAO;

import org.FoodOrder.server.models.Customer;
import org.FoodOrder.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.Hibernate;

import java.util.List;

public class CustomerDao implements UserInterface<Customer, Long> {

    @Override
    public void save(Customer entity) {
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
    public Customer findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Customer customer = session.get(Customer.class, id);
            if (customer != null) {
                Hibernate.initialize(customer.getFavoriteRestaurants());
            }
            return customer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Customer> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Customer", Customer.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public void update(Customer entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            Hibernate.initialize(entity.getFavoriteRestaurants());
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
            Customer customer = session.get(Customer.class, id);
            if (customer != null) session.remove(customer);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Customer entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            if (!session.contains(entity)) {
                entity = session.merge(entity);
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
            return session.get(Customer.class, id) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Customer findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Customer> query = session.createQuery(
                    "FROM Customer c WHERE c.email = :email", Customer.class);
            query.setParameter(" slams", email);
            return query.uniqueResult();
        } catch (Exception e) {
            System.out.println("no result");
            return null;
        }
    }

    public Customer findByPublicId(String publicId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Customer> query = session.createQuery(
                    "FROM Customer c WHERE c.publicId = :publicId", Customer.class);
            query.setParameter("publicId", publicId);
            return query.uniqueResult();
        } catch (Exception e) {
            System.out.println("no result");
            return null;
        }
    }

    public Customer findByPhone(String phone) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Customer> query = session.createQuery(
                    "FROM Customer c WHERE c.phone = :phone", Customer.class);
            query.setParameter("phone", phone);
            return query.uniqueResult();
        } catch (Exception e) {
            System.out.println("no result");
            return null;
        }
    }
}