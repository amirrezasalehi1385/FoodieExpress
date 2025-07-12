package org.FoodOrder.server.observers;

import org.FoodOrder.server.models.User;

public interface SignUpObserver {
    void onUserRegistered(User user);
}
