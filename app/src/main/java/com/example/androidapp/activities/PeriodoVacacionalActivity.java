package com.example.androidapp.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;

import java.util.Calendar;

public class PeriodoVacacionalActivity extends AppCompatActivity {

    private EditText etPresupuesto, etFechaInicio, etFechaFin;
    private Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_periodo_vacacional);

        etPresupuesto = findViewById(R.id.etPresupuesto);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etFechaFin = findViewById(R.id.etFechaFin);
        btnContinuar = findViewById(R.id.btnContinuarVacaciones);

        etFechaInicio.setOnClickListener(v -> mostrarDatePicker(etFechaInicio));
        etFechaFin.setOnClickListener(v -> mostrarDatePicker(etFechaFin));

        btnContinuar.setOnClickListener(v -> guardarPeriodoVacacional());
    }

    private void mostrarDatePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> editText
                        .setText(String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)),
                mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void guardarPeriodoVacacional() {
        String presupuesto = etPresupuesto.getText().toString().trim();
        String fechaInicio = etFechaInicio.getText().toString().trim();
        String fechaFin = etFechaFin.getText().toString().trim();

        if (presupuesto.isEmpty() || fechaInicio.isEmpty() || fechaFin.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Enviar datos al servidor si es necesario

        // Continuar a la siguiente pantalla
        Intent intent = new Intent(this, PreferenciasTuristicasActivity.class);
        startActivity(intent);
        finish();
    }
}
