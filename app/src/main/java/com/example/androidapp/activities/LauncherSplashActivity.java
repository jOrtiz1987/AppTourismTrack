package com.example.androidapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherSplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Comprobamos la bandera de primer uso en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean hasCompletedOnboarding = prefs.getBoolean("hasCompletedOnboarding", false);

        // Sin importar si completó el onboarding, vamos al Login como default a
        // petición
        Intent targetIntent = new Intent(this, LoginActivity.class);

        startActivity(targetIntent);
        finish(); // Cierra esta actividad para no volver atrás
    }
}
