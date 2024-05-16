package com.example.recipes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyRecipesActivity extends AppCompatActivity {

    private ListView recipeListView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;

    private DatabaseReference databaseReference;
    private EditText searchEditText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        recipeListView = findViewById(R.id.recipe_listview);
        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, recipeList);
        recipeListView.setAdapter(recipeAdapter);

        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);

        // Получаем данные пользователя из SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail", null);

        if (userEmail != null) {
            // Инициализируем базу данных Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Recipes");

            loadUserRecipes(userEmail);

            // Слушатель для кнопки поиска
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String query = searchEditText.getText().toString().trim();
                    if (!query.isEmpty()) {
                        searchRecipesByTitle(userEmail, query);
                    } else {
                        loadUserRecipes(userEmail);
                    }
                }
            });
        } else {
            // Если пользователь не вошел в систему, перенаправляем его на страницу входа
            Toast.makeText(MyRecipesActivity.this, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MyRecipesActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Установка обработчика нажатия на элементы списка рецептов
        recipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Получаем данные о выбранном рецепте
                Recipe selectedRecipe = recipeList.get(position);

                // Создаем интент для перехода к RecipeDetailsActivity и передачи информации о рецепте
                Intent intent = new Intent(MyRecipesActivity.this, RecipeDetailsActivity.class);
                intent.putExtra("id", selectedRecipe.getId());
                intent.putExtra("recipe_title", selectedRecipe.getTitle());
                intent.putExtra("ingredients", selectedRecipe.getIngredients());
                intent.putExtra("instructions", selectedRecipe.getInstructions());
                intent.putExtra("author", selectedRecipe.getAuthor());
                startActivity(intent);
            }
        });
    }

    private void loadUserRecipes(String userEmail) {
        Query myRecipesQuery = databaseReference.orderByChild("author").equalTo(userEmail);
        myRecipesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList.clear();
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    recipeList.add(recipe);
                }
                Collections.reverse(recipeList); // Сортировка списка рецептов по времени добавления (от новых к старым)
                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyRecipesActivity.this, "Ошибка при загрузке рецептов", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchRecipesByTitle(String userEmail, final String title) {
        Query myRecipesQuery = databaseReference.orderByChild("author").equalTo(userEmail);
        myRecipesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList.clear();
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null && recipe.getTitle().toLowerCase().contains(title.toLowerCase())) {
                        recipeList.add(recipe);
                    }
                }
                Collections.reverse(recipeList); // Сортировка списка рецептов по времени добавления (от новых к старым)
                recipeAdapter.notifyDataSetChanged();

                if (recipeList.isEmpty()) {
                    Toast.makeText(MyRecipesActivity.this, "Рецепты с таким названием не найдены.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyRecipesActivity.this, "Ошибка при поиске рецептов.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
