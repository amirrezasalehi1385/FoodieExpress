package org.FoodOrder.server.enums;

public enum TransactionStatus {
    SUCCESS,        // تراکنش موفق
    FAILED,         // تراکنش ناموفق
    PENDING         // در حال پردازش (اختیاری برای آینده)
}