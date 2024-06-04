package com.example.recipes;

public class Recipe {
    private String id;
    private String title;
    private String ingredients;
    private String instructions;
    private String author;
    private boolean favorite;
    private String imageUrl;

    public Recipe() {
    }

    public Recipe(String id, String title, String ingredients, String instructions, String author, boolean favorite, String imageUrl) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.author = author;
        this.favorite = favorite;
        this.imageUrl = imageUrl;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) {
        this.instructions = instructions; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) {
        this.author = author; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) {
        this.favorite = favorite; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl; }
}
