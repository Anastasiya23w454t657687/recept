package com.example.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private Button loginButton;
    private Button backButton;
    private Button registerButton;

    private Button favoritesButton;
    private Button myRecipesButton;
    private Button logoutButton;

    private TextView userNameTextView, userEmailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        loginButton = findViewById(R.id.login_button);
        backButton = findViewById(R.id.back_button);
        registerButton = findViewById(R.id.register_button);

        userNameTextView = findViewById(R.id.user_name);
        userEmailTextView = findViewById(R.id.user_email);

        logoutButton = findViewById(R.id.logout_button);

        favoritesButton = findViewById(R.id.favorites_button);
        myRecipesButton = findViewById(R.id.my_recipes_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(MenuActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(MenuActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Получаем данные пользователя из SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", null);
        String userEmail = sharedPreferences.getString("userEmail", null);


        // Если данные о пользователе получены, отображаем их и скрываем кнопки "Войти" и "Зарегистрироваться"
        if (userName != null && userEmail != null) {
            userNameTextView.setText(userName);
            userEmailTextView.setText(userEmail);
            loginButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.GONE);
            myRecipesButton.setVisibility(View.VISIBLE);
            favoritesButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
        }


        // Обработчик нажатия на кнопку "Выйти"
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Удаляем данные о пользователе из SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences("UserData", MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();

                // Переходим на главную страницу
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Закрываем текущую активность
            }
        });

        myRecipesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MyRecipesActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, FavoriteRecipesActivity.class);
                startActivity(intent);
            }
        });
    }
}
