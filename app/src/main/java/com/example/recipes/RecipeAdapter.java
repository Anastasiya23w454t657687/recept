package com.example.recipes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecipeAdapter extends ArrayAdapter<Recipe> {

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        super(context, 0, recipes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Получаем данные о рецепте для текущей позиции
        Recipe recipe = getItem(position);

        // Проверяем, используется ли существующее представление; если нет, то заполняем его
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_recipe, parent, false);
        }

        // Находим и заполняем элементы представления данными о рецепте
        TextView nameTextView = convertView.findViewById(R.id.recipe_name);
        ImageView recipeImageView = convertView.findViewById(R.id.recipe_image);

        nameTextView.setText(recipe.getTitle());

        // Используем Glide для загрузки изображения из URL
        String imageUrl = recipe.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getContext()).load(imageUrl).into(recipeImageView);
        } else {
            // Устанавливаем изображение по умолчанию, если URL не задан
            recipeImageView.setImageResource(R.drawable.default_image);
        }

        return convertView;
    }
}
