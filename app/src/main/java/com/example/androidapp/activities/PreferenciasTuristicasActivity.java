package com.example.androidapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidapp.R;
import com.example.androidapp.adapters.CategoriaAdapter;
import com.example.androidapp.config.ApiConfig;
import com.example.androidapp.models.Categoria;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PreferenciasTuristicasActivity extends AppCompatActivity {

    private RecyclerView rvCategorias;
    private View progressBar;
    private Button btnFinalizar;

    private CategoriaAdapter categoriaAdapter;
    private List<Categoria> categoriasList;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias_turisticas);

        rvCategorias = findViewById(R.id.rvCategorias);
        progressBar = findViewById(R.id.progressBar);
        btnFinalizar = findViewById(R.id.btnFinalizarPreferencias);

        categoriasList = new ArrayList<>();
        categoriaAdapter = new CategoriaAdapter(categoriasList);
        rvCategorias.setLayoutManager(new LinearLayoutManager(this));
        rvCategorias.setAdapter(categoriaAdapter);

        requestQueue = Volley.newRequestQueue(this);

        btnFinalizar.setOnClickListener(v -> guardarPreferencias());

        cargarCategorias();
    }

    private void cargarCategorias() {
        progressBar.setVisibility(View.VISIBLE);
        String url = ApiConfig.BASE_URL + "api/categorias";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String nombre = jsonObject.getString("descripcion");
                            categoriasList.add(new Categoria(id, nombre));
                        }
                        categoriaAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error procesando categorías", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar categorías", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws com.android.volley.AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String token = prefs.getString("jwtToken", "");
                if (!token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void guardarPreferencias() {
        List<Categoria> seleccionadas = categoriaAdapter.getSelectedCategorias();
        if (seleccionadas.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona al menos una preferencia", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int idPeriodoVacacional = prefs.getInt("idPeriodoVacacional", -1);

        if (idPeriodoVacacional == -1) {
            Toast.makeText(this, "Error: No se encontró el periodo vacacional", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.BASE_URL + "api/lugares-usuario";

        final int totalRequests = seleccionadas.size();
        final int[] completedRequests = { 0 };
        final boolean[] hasError = { false };

        progressBar.setVisibility(View.VISIBLE);
        btnFinalizar.setEnabled(false);

        for (Categoria categoria : seleccionadas) {
            JSONObject jsonBody = new JSONObject();
            try {
                JSONObject catObj = new JSONObject();
                catObj.put("id", categoria.getId());
                jsonBody.put("categoria", catObj);

                JSONObject periodoObj = new JSONObject();
                periodoObj.put("idPeriodoVacacional", idPeriodoVacacional);
                jsonBody.put("idPeriodoVacacional", periodoObj);
            } catch (JSONException e) {
                e.printStackTrace();
                completedRequests[0]++;
                checkAllRequestsCompleted(completedRequests[0], totalRequests, hasError[0]);
                continue;
            }

            com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    response -> {
                        completedRequests[0]++;
                        checkAllRequestsCompleted(completedRequests[0], totalRequests, hasError[0]);
                    },
                    error -> {
                        error.printStackTrace();
                        hasError[0] = true;
                        completedRequests[0]++;
                        checkAllRequestsCompleted(completedRequests[0], totalRequests, hasError[0]);
                    }) {
                @Override
                public java.util.Map<String, String> getHeaders() throws com.android.volley.AuthFailureError {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
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

    private void checkAllRequestsCompleted(int completed, int total, boolean hasError) {
        if (completed == total) {
            progressBar.setVisibility(View.GONE);
            btnFinalizar.setEnabled(true);

            if (hasError) {
                Toast.makeText(this, "Algunas preferencias no se guardaron correctamente, pero puedes continuar.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Preferencias guardadas con éxito", Toast.LENGTH_SHORT).show();
            }

            android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("hasCompletedOnboarding", true).apply();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
