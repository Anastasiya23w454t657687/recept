package com.example.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView recipeListView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;

    private DatabaseReference databaseReference;
    private EditText searchEditText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipeListView = findViewById(R.id.recipe_listview);
        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, recipeList);
        recipeListView.setAdapter(recipeAdapter);

        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        Button addButton = findViewById(R.id.add_recipe_button);
        Button menuButton = findViewById(R.id.menu_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewRecipeActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });

        // Инициализация базы данных Firebase
        FirebaseApp.initializeApp(this);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Recipes");
        loadAllRecipes();

        // Установка обработчика нажатия на элементы списка рецептов
        recipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Получаем данные о выбранном рецепте
                Recipe selectedRecipe = recipeList.get(position);

                // Создаем интент для перехода к RecipeDetailsActivity и передачи информации о рецепте
                Intent intent = new Intent(MainActivity.this, RecipeDetailsActivity.class);
                intent.putExtra("id", selectedRecipe.getId());
                intent.putExtra("recipe_title", selectedRecipe.getTitle());
                intent.putExtra("ingredients", selectedRecipe.getIngredients());
                intent.putExtra("instructions", selectedRecipe.getInstructions());
                intent.putExtra("author", selectedRecipe.getAuthor());
                startActivity(intent);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchRecipesByTitle(query);
                } else {
                    loadAllRecipes();
                }
            }
        });
    }

    private void loadAllRecipes() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList.clear();
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    recipeList.add(recipe);
                }
                // Сортировка списка рецептов по времени добавления (от новых к старым)
                Collections.reverse(recipeList);
                recipeAdapter.notifyDataSetChanged();

                // Вывод данных в журнал логов для проверки
                Log.d("Recipe", "Number of recipes: " + recipeList.size());
                for (Recipe recipe : recipeList) {
                    Log.d("Recipe", "Title: " + recipe.getTitle());
                    // Выводите другие данные о рецептах по необходимости
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load recipes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchRecipesByTitle(final String title) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList.clear();
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null && recipe.getTitle().toLowerCase().contains(title.toLowerCase())) {
                        recipeList.add(recipe);
                    }
                }
                // Сортировка списка рецептов по времени добавления (от новых к старым)
                Collections.reverse(recipeList);
                recipeAdapter.notifyDataSetChanged();

                if (recipeList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Рецепты с таким названием не найдены.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Ошибка при поиске рецептов.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
