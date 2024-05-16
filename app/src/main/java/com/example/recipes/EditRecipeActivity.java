package com.example.recipes;

import android.content.Intent;
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

public class EditRecipeActivity extends AppCompatActivity {

    private EditText edNameRecipe;
    private EditText edIngredients;
    private EditText edInstructions;
    private Button saveButton;

    private DatabaseReference databaseReference;
    private String recipeId;
    private String author; // Добавляем переменную для хранения автора

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        edNameRecipe = findViewById(R.id.edNameRecipe);
        edIngredients = findViewById(R.id.edIngredients);
        edInstructions = findViewById(R.id.edInstructions);
        saveButton = findViewById(R.id.save_button);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Recipes");

        // Получаем данные о рецепте из Intent
        Intent intent = getIntent();
        recipeId = intent.getStringExtra("recipe_id");
        String recipeTitle = intent.getStringExtra("recipe_title");
        String ingredients = intent.getStringExtra("ingredients");
        String instructions = intent.getStringExtra("instructions");
        author = intent.getStringExtra("author"); // Получаем автора

        // Устанавливаем полученные данные в поля для редактирования
        edNameRecipe.setText(recipeTitle);
        edIngredients.setText(ingredients);
        edInstructions.setText(instructions);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecipe();
            }
        });
    }

    private void updateRecipe() {
        String recipeName = edNameRecipe.getText().toString().trim();
        String ingredients = edIngredients.getText().toString().trim();
        String instructions = edInstructions.getText().toString().trim();

        if (recipeName.isEmpty() || ingredients.isEmpty() || instructions.isEmpty()) {
            Toast.makeText(EditRecipeActivity.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference recipeRef = databaseReference.child(recipeId);
            recipeRef.child("title").setValue(recipeName);
            recipeRef.child("ingredients").setValue(ingredients);
            recipeRef.child("instructions").setValue(instructions).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditRecipeActivity.this, "Рецепт успешно обновлен", Toast.LENGTH_SHORT).show();
                        // Переход обратно к RecipeDetailsActivity

                        Intent intent = new Intent(EditRecipeActivity.this, RecipeDetailsActivity.class);
                        intent.putExtra("recipe_id", recipeId);
                        intent.putExtra("recipe_title", recipeName);
                        intent.putExtra("ingredients", ingredients);
                        intent.putExtra("instructions", instructions);
                        intent.putExtra("author", author); // Передаем автора обратно

                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(EditRecipeActivity.this, "Ошибка при обновлении рецепта", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
