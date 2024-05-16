package com.example.recipes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewRecipeActivity extends AppCompatActivity {

    private EditText edNameRecipe;
    private EditText edIngredients;
    private EditText edInstructions;
    private Button saveButton;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newrecipeactivity);

        edNameRecipe = findViewById(R.id.edNameRecipe);
        edIngredients = findViewById(R.id.edIngredients);
        edInstructions = findViewById(R.id.edInstructions);
        saveButton = findViewById(R.id.save_button);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Recipes");

        // Проверка наличия данных о пользователе в SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", null);
        String userEmail = sharedPreferences.getString("userEmail", null);

        if (userName == null || userEmail == null) {
            // Если данных нет, направляем пользователя на экран входа или регистрации
            Intent intent = new Intent(NewRecipeActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipe(userName, userEmail);
            }
        });
    }

    private void saveRecipe(String userName, String userEmail) {
        String recipeName = edNameRecipe.getText().toString().trim();
        String ingredients = edIngredients.getText().toString().trim();
        String instructions = edInstructions.getText().toString().trim();

        if (recipeName.isEmpty() || ingredients.isEmpty() || instructions.isEmpty()) {
            Toast.makeText(NewRecipeActivity.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
        } else {
            String recipeId = databaseReference.push().getKey();
            boolean favorite = false; // Устанавливаем значение favorite по умолчанию
            Recipe recipe = new Recipe(recipeId, recipeName, ingredients, instructions, userEmail, favorite);

            databaseReference.child(recipeId).setValue(recipe).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(NewRecipeActivity.this, "Рецепт успешно добавлен", Toast.LENGTH_SHORT).show();
                        // Передаем данные о рецепте в RecipeDetailsActivity
                        Intent intent = new Intent(NewRecipeActivity.this, RecipeDetailsActivity.class);
                        intent.putExtra("recipe_id", recipeId);
                        intent.putExtra("recipe_title", recipeName);
                        intent.putExtra("ingredients", ingredients);
                        intent.putExtra("instructions", instructions);
                        intent.putExtra("author", userEmail);
                        startActivity(intent);
                        finish(); // Закрыть активность после сохранения рецепта
                    } else {
                        Toast.makeText(NewRecipeActivity.this, "Ошибка при добавлении рецепта", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
