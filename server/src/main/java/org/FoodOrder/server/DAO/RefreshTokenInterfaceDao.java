package org.FoodOrder.server.DAO;
import org.FoodOrder.server.models.*;

public interface RefreshTokenInterfaceDao extends UserInterface<RefreshToken , Long> {
    RefreshToken findByToken(String token);
    void deleteByUser(User user);
}
