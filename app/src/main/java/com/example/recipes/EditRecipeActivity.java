package com.example.recipes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class EditRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText edNameRecipe;
    private EditText edIngredients;
    private EditText edInstructions;
    private Button saveButton;
    private Button changeImageButton;
    private ImageView recipeImageView;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private String recipeId;
    private String author;
    private String currentImageUrl;
    private Uri newImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        edNameRecipe = findViewById(R.id.edNameRecipe);
        edIngredients = findViewById(R.id.edIngredients);
        edInstructions = findViewById(R.id.edInstructions);
        saveButton = findViewById(R.id.save_button);
        changeImageButton = findViewById(R.id.changeImageButton);
        recipeImageView = findViewById(R.id.recipeImageView);
        progressBar = findViewById(R.id.progressBar);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Recipes");
        storageReference = FirebaseStorage.getInstance().getReference();

        // Получаем данные о рецепте из Intent
        Intent intent = getIntent();
        recipeId = intent.getStringExtra("recipe_id");
        String recipeTitle = intent.getStringExtra("recipe_title");
        String ingredients = intent.getStringExtra("ingredients");
        String instructions = intent.getStringExtra("instructions");
        author = intent.getStringExtra("author");
        currentImageUrl = intent.getStringExtra("image_url");

        // Устанавливаем полученные данные в поля для редактирования
        edNameRecipe.setText(recipeTitle);
        edIngredients.setText(ingredients);
        edInstructions.setText(instructions);

        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Glide.with(this).load(currentImageUrl).into(recipeImageView);
        }

        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                updateRecipe();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            newImageUri = data.getData();
            Glide.with(this).load(newImageUri).into(recipeImageView);
        }
    }

    private void updateRecipe() {
        String recipeName = edNameRecipe.getText().toString().trim();
        String ingredients = edIngredients.getText().toString().trim();
        String instructions = edInstructions.getText().toString().trim();

        if (recipeName.isEmpty() || ingredients.isEmpty() || instructions.isEmpty()) {
            Toast.makeText(EditRecipeActivity.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
        } else {
            if (newImageUri != null) {
                deleteOldImageAndUploadNew(recipeName, ingredients, instructions);
            } else {
                saveRecipeDetails(recipeName, ingredients, instructions, currentImageUrl);
            }
        }
    }

    private void deleteOldImageAndUploadNew(final String recipeName, final String ingredients, final String instructions) {
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentImageUrl);
            oldImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    uploadNewImage(recipeName, ingredients, instructions);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditRecipeActivity.this, "Ошибка при удалении старого изображения", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            uploadNewImage(recipeName, ingredients, instructions);
        }
    }

    private void uploadNewImage(final String recipeName, final String ingredients, final String instructions) {
        final StorageReference newImageRef = storageReference.child("images/" + UUID.randomUUID().toString());
        newImageRef.putFile(newImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    newImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String newImageUrl = uri.toString();
                            saveRecipeDetails(recipeName, ingredients, instructions, newImageUrl);
                        }
                    });
                } else {
                    Toast.makeText(EditRecipeActivity.this, "Ошибка при загрузке нового изображения", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveRecipeDetails(String recipeName, String ingredients, String instructions, String imageUrl) {

        DatabaseReference recipeRef = databaseReference.child(recipeId);
        recipeRef.child("title").setValue(recipeName);
        recipeRef.child("ingredients").setValue(ingredients);
        recipeRef.child("instructions").setValue(instructions);
        recipeRef.child("imageUrl").setValue(imageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EditRecipeActivity.this, "Рецепт успешно обновлен", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditRecipeActivity.this, RecipeDetailsActivity.class);
                    intent.putExtra("recipe_id", recipeId);
                    intent.putExtra("recipe_title", recipeName);
                    intent.putExtra("ingredients", ingredients);
                    intent.putExtra("instructions", instructions);
                    intent.putExtra("author", author);
                    intent.putExtra("image_url", imageUrl);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(EditRecipeActivity.this, "Ошибка при обновлении рецепта", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
