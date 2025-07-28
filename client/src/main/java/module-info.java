module org.FoodOrder.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    exports org.FoodOrder.client.enums;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    requires jjwt.api;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;


    opens org.FoodOrder.client to javafx.fxml;
    opens org.FoodOrder.client.controllers to javafx.fxml;
    opens org.FoodOrder.client.models to com.fasterxml.jackson.databind;

    exports org.FoodOrder.client;
    exports org.FoodOrder.client.controllers;
    exports org.FoodOrder.client.models;
}
