package com.example.recipes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class RecipeAdapter extends ArrayAdapter<Recipe> {

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        super(context, 0, recipes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Получаем данные о рецепте для текущей позиции
        Recipe recipe = getItem(position);

        // Проверяем, используется ли существующее представление; если нет, то заполняем его
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_recipe, parent, false);
        }

        // Находим и заполняем элементы представления данными о рецепте
        TextView nameTextView = convertView.findViewById(R.id.recipe_name);
        nameTextView.setText(recipe.getTitle());

        return convertView;
    }
}