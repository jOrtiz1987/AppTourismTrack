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
import android.content.Context;
import android.util.Log;

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
        enviarDatosAlServidor(presupuesto, fechaInicio, fechaFin);
    }

    private void enviarDatosAlServidor(String presupuesto, String fechaInicio, String fechaFin) {
        // En base al backend el endpoint es /api/periodos
        String url = com.example.androidapp.config.ApiConfig.BASE_URL + "api/periodos";

        com.android.volley.RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(this);

        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            // El backend espera FechaInicioEstimada, FechaFinEstimada, Presupuesto (y
            // opcionalmente usuario y reales)
            jsonBody.put("fechaInicioEstimada", fechaInicio);
            jsonBody.put("fechaFinEstimada", fechaFin);
            jsonBody.put("presupuesto", Double.parseDouble(presupuesto));

            android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String savedUserId = prefs.getString("userId", "-1");

            if (!savedUserId.equals("-1")) {
                org.json.JSONObject usuarioObj = new org.json.JSONObject();
                usuarioObj.put("idUsuario", Integer.parseInt(savedUserId));
                jsonBody.put("usuario", usuarioObj);
            }

        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El presupuesto debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(PeriodoVacacionalActivity.this, "Datos guardados correctamente", Toast.LENGTH_SHORT)
                            .show();
                    // Continuar a la siguiente pantalla
                    Intent intent = new Intent(PeriodoVacacionalActivity.this, PreferenciasTuristicasActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    String errorMsg = "Error desconocido";
                    if (error.networkResponse != null) {
                        errorMsg = "HTTP Status: " + error.networkResponse.statusCode;
                        if (error.networkResponse.data != null) {
                            errorMsg += " Data: " + new String(error.networkResponse.data);
                        }
                    }
                    Log.e("Volley Error", "Fallo al guardar: " + errorMsg, error);
                    Toast.makeText(PeriodoVacacionalActivity.this, "Error al guardar los datos: " + errorMsg,
                            Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws com.android.volley.AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("jwtToken", "");
                if (!token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
}
