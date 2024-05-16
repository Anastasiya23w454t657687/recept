package com.example.recipes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RecipeDetailsActivity extends AppCompatActivity {

    private static final String TAG = "RecipeDetailsActivity";
    private String recipeId;
    private String authorEmail;
    private Button backButton;
    private Button favoriteButtonAdd;
    private Button favoriteButtonRemove;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_details);

        Button editButton = findViewById(R.id.button_edit);
        Button deleteButton = findViewById(R.id.button_delete);
        backButton = findViewById(R.id.back_button);
        favoriteButtonAdd = findViewById(R.id.button_favorite1);
        favoriteButtonRemove = findViewById(R.id.button_favorite2);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String recipeTitle = extras.getString("recipe_title");
            String ingredients = extras.getString("ingredients");
            String instructions = extras.getString("instructions");
            String author = extras.getString("author");
            recipeId = extras.getString("id");
            authorEmail = extras.getString("author");

            TextView titleTextView = findViewById(R.id.text_recipe_title);
            TextView ingredientsTextView = findViewById(R.id.text_ingredients);
            TextView instructionsTextView = findViewById(R.id.text_instructions);
            TextView authorTextView = findViewById(R.id.text_author);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            titleTextView.setText(recipeTitle);
            ingredientsTextView.setText("Ингредиенты: " + ingredients);
            instructionsTextView.setText("Инструкции: " + instructions);
            authorTextView.setText("Автор: " + author);

            SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
            String currentUserEmail = sharedPreferences.getString("userEmail", null);
            String currentUserID = sharedPreferences.getString("userID", null);
            Log.d(TAG, "userID = " + currentUserID + ", userEmail = " + currentUserEmail);

            if (currentUserEmail != null && currentUserEmail.equals(authorEmail)) {
                Log.d(TAG, "Current user is the author, showing buttons.");
                editButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);

                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(RecipeDetailsActivity.this, EditRecipeActivity.class);
                        intent.putExtra("recipe_id", recipeId);
                        intent.putExtra("recipe_title", recipeTitle);
                        intent.putExtra("ingredients", ingredients);
                        intent.putExtra("instructions", instructions);
                        intent.putExtra("author", author);
                        startActivity(intent);
                    }
                });

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(RecipeDetailsActivity.this)
                                .setTitle("Удаление рецепта")
                                .setMessage("Вы уверены, что хотите удалить этот рецепт?")
                                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteRecipe(recipeId))
                                .setNegativeButton(android.R.string.no, null)
                                .show();
                    }
                });
            } else {
                Log.d(TAG, "Current user is not the author, hiding buttons.");
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }

            boolean fromFavorites = extras.getBoolean("fromFavorites", false);

            if (fromFavorites) {
                favoriteButtonAdd.setVisibility(View.GONE);
                favoriteButtonRemove.setVisibility(View.VISIBLE);
            } else {
                favoriteButtonAdd.setVisibility(View.VISIBLE);
                favoriteButtonRemove.setVisibility(View.GONE);
            }

            if (currentUserID != null) {
                favoriteButtonAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Adding to favorites: UserID = " + currentUserID + ", RecipeID = " + recipeId);
                        addRecipeToFavorites(currentUserID, recipeId);
                    }
                });

                favoriteButtonRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Removing from favorites: UserID = " + currentUserID + ", RecipeID = " + recipeId);
                        removeRecipeFromFavorites(currentUserID, recipeId);
                    }
                });
            }
        }
    }

    private void deleteRecipe(String recipeId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Recipes").child(recipeId);
        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RecipeDetailsActivity.this, "Рецепт успешно удален", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RecipeDetailsActivity.this, "Ошибка при удалении рецепта", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addRecipeToFavorites(String currentUserID, String recipeId) {
        DatabaseReference favoritesReference = FirebaseDatabase.getInstance().getReference().child("Favorites").child(currentUserID).child(recipeId);
        favoritesReference.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Recipe added to favorites");
                    Toast.makeText(RecipeDetailsActivity.this, "Рецепт добавлен в избранное", Toast.LENGTH_SHORT).show();
                    favoriteButtonAdd.setVisibility(View.GONE);
                    favoriteButtonRemove.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "Error adding recipe to favorites: " + task.getException().getMessage());
                    Toast.makeText(RecipeDetailsActivity.this, "Ошибка при добавлении в избранное", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void removeRecipeFromFavorites(String currentUserID, String recipeId) {
        DatabaseReference favoritesReference = FirebaseDatabase.getInstance().getReference().child("Favorites").child(currentUserID).child(recipeId);
        favoritesReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Recipe removed from favorites");
                    Toast.makeText(RecipeDetailsActivity.this, "Рецепт удален из избранного", Toast.LENGTH_SHORT).show();
                    favoriteButtonAdd.setVisibility(View.VISIBLE);
                    favoriteButtonRemove.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "Error removing recipe from favorites: " + task.getException().getMessage());
                    Toast.makeText(RecipeDetailsActivity.this, "Ошибка при удалении из избранного", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
