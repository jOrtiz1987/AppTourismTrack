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

        // TODO: Enviar preferencias al servidor si es necesario (ej. POST
        // /api/usuarios/{id}/preferencias)

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
