package org.FoodOrder.server.DAO;
import java.util.List;


public interface UserInterface<T, ID>{
    void save(T entity);
    T findById(ID id);
    List<T> getAll();
    void update(T entity);
    void deleteById(ID id);
    void delete(T entity);
    boolean existsById(ID id);
}
