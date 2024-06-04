package com.example.recipes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

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
        ImageView recipeImageView = findViewById(R.id.recipe_image);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String recipeTitle = extras.getString("recipe_title");
            String ingredients = extras.getString("ingredients");
            String instructions = extras.getString("instructions");
            String author = extras.getString("author");
            recipeId = extras.getString("id");
            authorEmail = extras.getString("author");
            String imageUrl = extras.getString("image_url");

            TextView titleTextView = findViewById(R.id.text_recipe_title);
            TextView ingredientsTextView = findViewById(R.id.text_ingredients);
            TextView instructionsTextView = findViewById(R.id.text_instructions);
            TextView authorTextView = findViewById(R.id.text_author);

            titleTextView.setText(recipeTitle);
            ingredientsTextView.setText("Ингредиенты: " + ingredients);
            instructionsTextView.setText("Инструкции: " + instructions);
            authorTextView.setText("Автор: " + author);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(recipeImageView);
            } else {
                recipeImageView.setImageResource(R.drawable.default_image);
            }

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

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
                        intent.putExtra("image_url", imageUrl);
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

            checkIfRecipeIsFavorite(currentUserID, recipeId);

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
        } else {
            Log.e(TAG, "No recipe data received.");
            Toast.makeText(this, "Ошибка при загрузке данных рецепта", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void deleteRecipe(String recipeId) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes").child(recipeId);
        recipeRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Recipe deleted from database: " + recipeId);
                    Toast.makeText(RecipeDetailsActivity.this, "Рецепт успешно удален", Toast.LENGTH_SHORT).show();
                    deleteRecipeImage(recipeId);
                } else {
                    Log.e(TAG, "Failed to delete recipe: " + recipeId, task.getException());
                    Toast.makeText(RecipeDetailsActivity.this, "Ошибка при удалении рецепта", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteRecipeImage(String recipeId) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("RecipeImages").child(recipeId);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Recipe image deleted: " + recipeId);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to delete recipe image: " + recipeId, e);
                finish();
            }
        });
    }

    private void addRecipeToFavorites(String userId, String recipeId) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference().child("Favorites").child(userId).child(recipeId);
        favoritesRef.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Recipe added to favorites: " + recipeId);
                    Toast.makeText(RecipeDetailsActivity.this, "Рецепт добавлен в избранное", Toast.LENGTH_SHORT).show();
                    favoriteButtonAdd.setVisibility(View.GONE);
                    favoriteButtonRemove.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "Failed to add recipe to favorites: " + recipeId, task.getException());
                    Toast.makeText(RecipeDetailsActivity.this, "Ошибка при добавлении рецепта в избранное", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void removeRecipeFromFavorites(String userId, String recipeId) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference().child("Favorites").child(userId).child(recipeId);
        favoritesRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Recipe removed from favorites: " + recipeId);
                    Toast.makeText(RecipeDetailsActivity.this, "Рецепт удален из избранного", Toast.LENGTH_SHORT).show();
                    favoriteButtonAdd.setVisibility(View.VISIBLE);
                    favoriteButtonRemove.setVisibility(View.GONE);
                } else {
                    Log.e(TAG, "Failed to remove recipe from favorites: " + recipeId, task.getException());
                    Toast.makeText(RecipeDetailsActivity.this, "Ошибка при удалении рецепта из избранного", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkIfRecipeIsFavorite(String userId, String recipeId) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference().child("Favorites").child(userId).child(recipeId);
        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    favoriteButtonAdd.setVisibility(View.GONE);
                    favoriteButtonRemove.setVisibility(View.VISIBLE);
                } else {
                    favoriteButtonAdd.setVisibility(View.VISIBLE);
                    favoriteButtonRemove.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check if recipe is favorite: " + recipeId, error.toException());
            }
        });
    }
}
