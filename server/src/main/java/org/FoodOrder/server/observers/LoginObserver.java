package org.FoodOrder.server.observers;

import org.FoodOrder.server.models.User;

public interface LoginObserver {
    void onUserLoggedIn(User user);
}
