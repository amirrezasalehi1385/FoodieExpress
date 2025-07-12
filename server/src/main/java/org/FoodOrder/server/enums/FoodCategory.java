package org.FoodOrder.server.enums;
public enum FoodCategory {
    IRANIAN("Iranian"),
    FAST_FOOD("Fast Food"),
    PIZZA("Pizza"),
    BURGER("Burger"),
    SANDWICH("Sandwich"),
    KEBAB("Kebab"),
    SEAFOOD("Seafood"),
    VEGETARIAN("Vegetarian"),
    ASIAN("Asian"),
    ITALIAN("Italian"),
    DESSERT("Dessert"),
    BREAKFAST("Breakfast"),
    SALAD("Salad"),
    DRINK("Drink"),
    SNACK("Snack");

    private final String displayName;

    FoodCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FoodCategory makeFoodCategory(String displayName) {
        for (FoodCategory foodCategory : FoodCategory.values()) {
            if (foodCategory.getDisplayName().equals(displayName)) {
                return foodCategory;
            }
        }
        return null;
    }
    public static FoodCategory buildCategory(String category) {
        switch (category) {
            case "Iranian":
                return FoodCategory.IRANIAN;
            case "Fast Food":
                return FoodCategory.FAST_FOOD;
            case "Pizza":
                return FoodCategory.PIZZA;
            case "Burger":
                return FoodCategory.BURGER;
            case "Sandwich":
                return FoodCategory.SANDWICH;
            case "Kebab":
                return FoodCategory.KEBAB;
            case "Seafood":
                return FoodCategory.SEAFOOD;
            case "Vegetarian":
                return FoodCategory.VEGETARIAN;
            default:
                return null;
        }
    }
}