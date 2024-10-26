package com.example.lab5_iot_20180444;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab5_iot_20180444.activity.FoodActivity;
import com.example.lab5_iot_20180444.databinding.ActivityMainBinding;
import com.example.lab5_iot_20180444.notificaciones.NotificationHelper;
import com.example.lab5_iot_20180444.notificaciones.ReminderReceiver;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText etIntervaloMinutos;
    private Button btnConfigurarNotificacion;
    private TextView tvCaloriasRestantes;

    private ActivityMainBinding binding;
    private NotificationHelper notificationHelper;
    private double caloriasRecomendadas = 0.0; //caalorías calculadas recomendadas
    private double caloriasConsumidas = 0.0; //calorías que el usuario ha registrado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //inicializamos el NotificationHelper
        notificationHelper = new NotificationHelper(this);

        //configurar recordatorios
        configurarRecordatorios();

        //configurar la notificación de motivación
        configurarNotificacionMotivacion();

        binding.btnCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcularCaloriasDiarias();
            }
        });
        //configuración del botón para añadir alimentos
        binding.btnAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FoodActivity.class);
                intent.putExtra("caloriasRecomendadas", caloriasRecomendadas);
                startActivity(intent);
            }
        });
    }

    private void calcularCaloriasDiarias() {
        try {
            double peso = Double.parseDouble(binding.etPeso.getText().toString());
            double altura = Double.parseDouble(binding.etAltura.getText().toString());
            int edad = Integer.parseInt(binding.etEdad.getText().toString());
            String genero = binding.spinnerGenero.getSelectedItem().toString();
            String nivelActividad = binding.spinnerActividad.getSelectedItem().toString();
            String objetivo = binding.spinnerObjetivo.getSelectedItem().toString();

            double tmb = (genero.equals("Hombre"))
                    ? (10 * peso + 6.25 * altura - 5 * edad + 5)
                    : (10 * peso + 6.25 * altura - 5 * edad - 161);

            //ajustamos TMB según nivel de actividad física
            double factorActividad = 1.0;
            switch (nivelActividad) {
                case "Sedentario":
                    factorActividad = 1.2;
                    break;
                case "Moderado":
                    factorActividad = 1.55;
                    break;
                case "Activo":
                    factorActividad = 1.725;
                    break;
                case "Muy Activo":
                    factorActividad = 1.9;
                    break;
            }

            double caloriasDiarias = tmb * factorActividad;

            //ajustamos según el objetivo
            double ajusteObjetivo = caloriasDiarias;
            switch (objetivo) {
                case "Subir Peso":
                    ajusteObjetivo += 500;
                    break;
                case "Bajar Peso":
                    ajusteObjetivo -= 500;
                    break;
            }

            binding.tvResultado.setText("Calorías diarias recomendadas: " + (int) ajusteObjetivo + " kcal");

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, completa todos los campos correctamente.", Toast.LENGTH_SHORT).show();
        }
    }

    //creamos el método para verificar si hay exceso de calorías y enviar notificación
    private void verificarExcesoCalorias() {
        if (caloriasConsumidas > caloriasRecomendadas) {
            String title = "¡Cuidado! Exceso de calorías";
            String message = "Has consumido " + (int)(caloriasConsumidas - caloriasRecomendadas) + " calorías más de las recomendadas. Considera hacer ejercicio o reducir la próxima comida.";
            notificationHelper.sendNotification(title, message, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            caloriasConsumidas = data.getIntExtra("caloriasConsumidas", 0);
            actualizarCaloriasRestantes();
        }
    }

    private void configurarRecordatorios() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Configurar alarmas para desayuno, almuerzo y cena
        configurarAlarma(alarmManager, 8, 0, "Recordatorio de Desayuno", "¡Es hora de registrar tu desayuno!");
        configurarAlarma(alarmManager, 13, 0, "Recordatorio de Almuerzo", "¡Es hora de registrar tu almuerzo!");
        configurarAlarma(alarmManager, 19, 0, "Recordatorio de Cena", "¡Es hora de registrar tu cena!");

        // Configurar alarma para revisar si se han registrado comidas al final del día
        configurarAlarma(alarmManager, 21, 0, "Alerta de Registro de Comidas", "No has registrado ninguna comida hoy. ¡Recuerda hacerlo!");
    }

    // Método auxiliar para configurar una alarma
    private void configurarAlarma(AlarmManager alarmManager, int hour, int minute, String title, String message) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, hour * 100 + minute, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Configurar la hora de la alarma
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Establecer la alarma
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void configurarAlarmaMotivacion(int minutos) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", "¡Motivación!");
        intent.putExtra("message", "Sigue trabajando en tus objetivos de salud.");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2000, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long intervaloMillis = minutos * 60 * 1000; // Convertir minutos a milisegundos
        long triggerTime = System.currentTimeMillis() + intervaloMillis;

        if (alarmManager != null) {
            // Configurar alarma repetitiva
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, intervaloMillis, pendingIntent);
        }
    }

    private void configurarNotificacionMotivacion() {
        etIntervaloMinutos = findViewById(R.id.etIntervaloMinutos);
        btnConfigurarNotificacion = findViewById(R.id.btnConfigurarNotificacion);

        btnConfigurarNotificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String intervaloStr = etIntervaloMinutos.getText().toString();
                if (!intervaloStr.isEmpty()) {
                    int minutos = Integer.parseInt(intervaloStr);
                    if (minutos > 0) {
                        configurarAlarmaMotivacion(minutos);
                        Toast.makeText(MainActivity.this, "Notificación de motivación configurada cada " + minutos + " minutos.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Por favor, ingresa un valor mayor a 0.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Por favor, ingresa un intervalo en minutos.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void actualizarCaloriasRestantes() {
        double caloriasRestantes = caloriasRecomendadas - caloriasConsumidas;


        if (caloriasRestantes > 0) {
            tvCaloriasRestantes.setText("Calorías restantes para hoy: " + (int) caloriasRestantes + " kcal");
            tvCaloriasRestantes.setTextColor(Color.parseColor("#00FF00")); // Verde si faltan calorías
        } else {
            tvCaloriasRestantes.setText("¡Meta superada! Has excedido por " + Math.abs((int) caloriasRestantes) + " kcal");
            tvCaloriasRestantes.setTextColor(Color.parseColor("#FF0000")); // Rojo si se ha excedido
        }
    }

}