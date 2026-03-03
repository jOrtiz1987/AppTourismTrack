package com.example.androidapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;

public class PreferenciasTuristicasActivity extends AppCompatActivity {

    private CheckBox cbRestaurantes, cbAtracciones, cbMuseos, cbNaturaleza, cbVidaNocturna;
    private Button btnFinalizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias_turisticas);

        cbRestaurantes = findViewById(R.id.cbRestaurantes);
        cbAtracciones = findViewById(R.id.cbAtracciones);
        cbMuseos = findViewById(R.id.cbMuseos);
        cbNaturaleza = findViewById(R.id.cbNaturaleza);
        cbVidaNocturna = findViewById(R.id.cbVidaNocturna);
        btnFinalizar = findViewById(R.id.btnFinalizarPreferencias);

        btnFinalizar.setOnClickListener(v -> guardarPreferencias());
    }

    private void guardarPreferencias() {
        // TODO: Enviar preferencias al servidor si es necesario

        Toast.makeText(this, "Preferencias guardadas con éxito", Toast.LENGTH_SHORT).show();

        // Guardamos que el usuario ya completó el registro inicial completo
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("hasCompletedOnboarding", true).apply();

        // Redirigir al Login para que el usuario inicie sesión con su nueva cuenta
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
