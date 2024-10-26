    package com.example.lab5_iot_20180444.activity;

    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.graphics.Color;
    import android.os.Bundle;
    import android.view.View;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.recyclerview.widget.LinearLayoutManager;

    import com.example.lab5_iot_20180444.R;
    import com.example.lab5_iot_20180444.adapter.FoodAdapter;
    import com.example.lab5_iot_20180444.databinding.ActivityFoodBinding;
    import com.example.lab5_iot_20180444.entity.FoodItem;
    import com.example.lab5_iot_20180444.notificaciones.NotificationHelper;
    import com.google.gson.Gson;
    import com.google.gson.reflect.TypeToken;

    import java.lang.reflect.Type;
    import java.util.ArrayList;
    import java.util.List;

    public class FoodActivity extends AppCompatActivity implements FoodAdapter.OnFoodItemDeleteListener{

        private ActivityFoodBinding binding;
        private List<FoodItem> foodList;
        private FoodAdapter adapter;
        private int totalCaloriasConsumidas = 0;
        private double caloriasRecomendadas = 0.0;

        private SharedPreferences sharedPreferences;
        private Gson gson;
        private NotificationHelper notificationHelper;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            //nicialización de View Binding
            binding = ActivityFoodBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            //Inicialización de NotificationHelper
            notificationHelper = new NotificationHelper(this);

            //Recibir las calorías recomendadas desde MainActivity
            caloriasRecomendadas = getIntent().getDoubleExtra("caloriasRecomendadas", 0.0);

            sharedPreferences = getSharedPreferences("FoodData", MODE_PRIVATE);
            gson = new Gson();
            cargarDatosGuardados();

            adapter = new FoodAdapter(foodList, this);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerView.setAdapter(adapter);


            binding.btnAddFood.setOnClickListener(view -> {
                String nombre = binding.etNombreComida.getText().toString();
                int calorias = Integer.parseInt(binding.etCaloriasComida.getText().toString());

                foodList.add(new FoodItem(nombre, calorias));

                totalCaloriasConsumidas += calorias;
                actualizarCaloriasTotales();
                verificarExcesoCalorias();

                actualizarCaloriasRestantes();

                guardarDatos();

                adapter.notifyDataSetChanged();

                binding.etNombreComida.setText("");
                binding.etCaloriasComida.setText("");
            });

            binding.btnResetCalorias.setOnClickListener(view -> {

                totalCaloriasConsumidas = 0;
                foodList.clear();
                actualizarCaloriasTotales();

                guardarDatos();

                adapter.notifyDataSetChanged();
            });

            binding.btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        private void actualizarCaloriasTotales() {
            binding.tvTotalCalorias.setText("Calorías consumidas: " + totalCaloriasConsumidas + " kcal");
        }

        private void guardarDatos() {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            String jsonFoodList = gson.toJson(foodList);
            editor.putString("foodList", jsonFoodList);

            editor.putInt("totalCalorias", totalCaloriasConsumidas);

            editor.apply();
        }

        private void cargarDatosGuardados() {
            String jsonFoodList = sharedPreferences.getString("foodList", null);
            Type type = new TypeToken<List<FoodItem>>() {}.getType();

            if (jsonFoodList != null) {
                foodList = gson.fromJson(jsonFoodList, type);
            } else {
                foodList = new ArrayList<>();
            }

            totalCaloriasConsumidas = sharedPreferences.getInt("totalCalorias", 0);

            actualizarCaloriasTotales();
        }

        private void verificarExcesoCalorias() {
            if (totalCaloriasConsumidas > caloriasRecomendadas) {
                String title = "¡Cuidado! Exceso de calorías";
                String message = "Has consumido " + (int)(totalCaloriasConsumidas - caloriasRecomendadas) + " calorías más de las recomendadas. Considera hacer ejercicio o reducir la próxima comida.";
                notificationHelper.sendNotification(title, message, 1);
            }
        }

        @Override
        public void onFoodItemDeleted(int position, int calorias) {
            totalCaloriasConsumidas -= calorias;
            actualizarCaloriasTotales();

            actualizarCaloriasRestantes();

            foodList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, foodList.size());

            guardarDatos();
        }

        @Override
        public void onBackPressed() {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("caloriasConsumidas", totalCaloriasConsumidas);
            setResult(RESULT_OK, resultIntent);
            super.onBackPressed();
        }

        private void actualizarCaloriasRestantes() {
            double caloriasRestantes = caloriasRecomendadas - totalCaloriasConsumidas;

            if (caloriasRestantes > 0) {
                binding.tvTotalCalorias.setText("Calorías restantes para hoy: " + (int) caloriasRestantes + " kcal");
                binding.tvTotalCalorias.setTextColor(Color.parseColor("#00FF00")); // Verde si faltan calorías
            } else {
                binding.tvTotalCalorias.setText("¡Meta superada! Has excedido por " + Math.abs((int) caloriasRestantes) + " kcal");
                binding.tvTotalCalorias.setTextColor(Color.parseColor("#FF0000")); // Rojo si se ha excedido
            }
        }
    }