package org.FoodOrder.server.observers;
import org.FoodOrder.server.models.User;

public interface ForgetPasswordObserver {
    void onForgetPassword(User user, int resetCode);
}
