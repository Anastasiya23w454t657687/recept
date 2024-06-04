package com.example.recipes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NewRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText edNameRecipe;
    private EditText edIngredients;
    private EditText edInstructions;
    private Button saveButton;
    private Button selectImageButton;
    private ImageView recipeImageView;
    private ProgressBar progressBar;

    private Uri imageUri;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newrecipeactivity);

        edNameRecipe = findViewById(R.id.edNameRecipe);
        edIngredients = findViewById(R.id.edIngredients);
        edInstructions = findViewById(R.id.edInstructions);
        saveButton = findViewById(R.id.save_button);
        selectImageButton = findViewById(R.id.selectImageButton);
        recipeImageView = findViewById(R.id.recipeImageView);
        progressBar = findViewById(R.id.progressBar);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Recipes");
        storageReference = FirebaseStorage.getInstance().getReference().child("RecipeImages");

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

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (imageUri != null) {
                    uploadImage(userName, userEmail);
                } else {
                    saveRecipe(userName, userEmail, null);
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            recipeImageView.setImageURI(imageUri);
        }
    }

    private void uploadImage(String userName, String userEmail) {
        final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
        fileReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            saveRecipe(userName, userEmail, uri.toString());
                        }
                    });
                } else {
                    Toast.makeText(NewRecipeActivity.this, "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveRecipe(String userName, String userEmail, String imageUrl) {

        String recipeName = edNameRecipe.getText().toString().trim();
        String ingredients = edIngredients.getText().toString().trim();
        String instructions = edInstructions.getText().toString().trim();

        if (recipeName.isEmpty() || ingredients.isEmpty() || instructions.isEmpty()) {
            Toast.makeText(NewRecipeActivity.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
        } else {
            String recipeId = databaseReference.push().getKey();
            boolean favorite = false; // Устанавливаем значение favorite по умолчанию
            Recipe recipe = new Recipe(recipeId, recipeName, ingredients, instructions, userEmail, favorite, imageUrl);

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
                        intent.putExtra("image_url", imageUrl);
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
