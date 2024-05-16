package com.example.recipes;

public class Recipe {
    private String id;
    private String title;
    private String ingredients;
    private String instructions;
    private String author;
    private boolean favorite; // Поле для определения избранного рецепта

    public Recipe() {
        // Пустой конструктор нужен для Firebase
    }

    public Recipe(String id, String title, String ingredients, String instructions, String author, boolean favorite) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.author = author;
        this.favorite = favorite;
    }

    // Геттеры
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isFavorite() {
        return favorite;
    }

    // Сеттеры
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
