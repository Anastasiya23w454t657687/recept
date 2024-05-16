package com.example.recipes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteRecipesActivity extends AppCompatActivity {

    private ListView favoriteRecipesListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> favoriteRecipesList;
    private ArrayList<Recipe> recipeList;

    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_recipes);

        favoriteRecipesListView = findViewById(R.id.favorite_recipes_list_view);
        favoriteRecipesList = new ArrayList<>();
        recipeList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, favoriteRecipesList);
        favoriteRecipesListView.setAdapter(adapter);

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        userId = sharedPreferences.getString("userID", null);

        if (userId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Favorites").child(userId);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    favoriteRecipesList.clear();
                    recipeList.clear();
                    for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                        String recipeId = recipeSnapshot.getKey();
                        fetchRecipeDetails(recipeId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FavoriteRecipesActivity.this, "Ошибка при загрузке избранных рецептов", Toast.LENGTH_SHORT).show();
                }
            });

            favoriteRecipesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Recipe selectedRecipe = recipeList.get(position);
                    Intent intent = new Intent(FavoriteRecipesActivity.this, RecipeDetailsActivity.class);
                    intent.putExtra("recipe_title", selectedRecipe.getTitle());
                    intent.putExtra("ingredients", selectedRecipe.getIngredients());
                    intent.putExtra("instructions", selectedRecipe.getInstructions());
                    intent.putExtra("author", selectedRecipe.getAuthor());
                    intent.putExtra("id", selectedRecipe.getId());
                    intent.putExtra("fromFavorites", true);  // Передаем флаг
                    startActivity(intent);
                }
            });
        } else {
            Toast.makeText(this, "Ошибка при получении данных пользователя", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchRecipeDetails(String recipeId) {
        DatabaseReference recipeReference = FirebaseDatabase.getInstance().getReference().child("Recipes").child(recipeId);
        recipeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    favoriteRecipesList.add(recipe.getTitle());
                    recipeList.add(recipe);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavoriteRecipesActivity.this, "Ошибка при загрузке деталей рецепта", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
