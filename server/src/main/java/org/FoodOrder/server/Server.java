package org.FoodOrder.server;


import com.sun.net.httpserver.HttpServer;
import org.FoodOrder.server.HttpHandler.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
            server.createContext("/auth", AuthHandler.getInstance());
            server.createContext("/restaurants", RestaurantHandler.getInstance());
            server.createContext("/vendors", BuyerHandler.getInstance());
            server.createContext("/orders", OrderHandler.getInstance());
            server.createContext("/favorites", FavoriteHandler.getInstance());
            server.createContext("/ratings", RatingHandler.getInstance());
            server.createContext("/deliveries", DeliveryHandler.getInstance());
            server.createContext("/admin", AdminHandler.getInstance());
            server.createContext("/cart", CartHandler.getInstance());
//            server.createContext("/transactions", TransactionHandler.getInstance());
//            server.createContext("/wallet/top-up", WalletTopUpHandler.getInstance());
//            server.createContext("/wallet/balance", WalletBalanceHandler.getInstance());
//            server.createContext("/payment/online", PaymentHandler.getInstance());
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}