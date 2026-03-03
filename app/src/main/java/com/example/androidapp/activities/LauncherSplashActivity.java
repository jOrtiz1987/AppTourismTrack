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

        Intent targetIntent;

        if (!hasCompletedOnboarding) {
            // Es la primera vez (o no ha terminado el onboarding), va al Registro
            targetIntent = new Intent(this, RegisterActivity.class);
        } else {
            // Ya completó el ciclo, va al Login
            targetIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(targetIntent);
        finish(); // Cierra esta actividad para no volver atrás
    }
}
