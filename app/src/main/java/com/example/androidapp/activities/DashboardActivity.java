package com.example.androidapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidapp.R;
import com.example.androidapp.adapters.PlacesAdapter;
import com.example.androidapp.config.ApiConfig;
import com.example.androidapp.models.EdificioHistorico;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PlacesAdapter adapter;
    private List<EdificioHistorico> placesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI components
        Button btnMinimizar = findViewById(R.id.btnMinimizar);
        recyclerView = findViewById(R.id.recyclerViewPlaces);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        placesList = new ArrayList<>();
        adapter = new PlacesAdapter(this, placesList);
        recyclerView.setAdapter(adapter);

        // Setup Logout Button
        android.widget.ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // 1. Clear Preferences
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("VisitPrefs", MODE_PRIVATE).edit().clear().apply();

            // 2. Stop Service
            android.content.Intent serviceIntent = new android.content.Intent(getApplicationContext(),
                    com.example.androidapp.services.LocationService.class);
            stopService(serviceIntent);

            // 3. Redirect to Login
            android.content.Intent loginIntent = new android.content.Intent(DashboardActivity.this,
                    LoginActivity.class);
            startActivity(loginIntent);
            finish();
        });

        // Setup Minimize Button
        btnMinimizar.setOnClickListener(v -> {
            moveTaskToBack(true);
            Toast.makeText(DashboardActivity.this, "Aplicación en segundo plano", Toast.LENGTH_SHORT).show();
        });

        // Fetch Data
        fetchPlaces();
    }

    private void fetchPlaces() {
        String url = ApiConfig.BASE_URL + "edificios/"; // Assuming endpoint is /edificios/

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            EdificioHistorico place = new EdificioHistorico();

                            // Map JSON fields to Model
                            // Note: Keys must match JSON response exactly.
                            if (jsonObject.has("idEdificioHistorico"))
                                place.setIdEdificioHistorico(jsonObject.getInt("idEdificioHistorico"));
                            else if (jsonObject.has("id")) // Spring Data REST often uses 'id' implies 'id' in DTO
                                place.setIdEdificioHistorico(jsonObject.getInt("id"));

                            place.setDescripcion(jsonObject.optString("descripcion", "Sin nombre"));
                            place.setContenido(jsonObject.optString("contenido", ""));
                            place.setReferenciaImagen(jsonObject.optString("referenciaImagen", ""));

                            placesList.add(place);
                        }
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e("Dashboard", "JSON Parsing error", e);
                        Toast.makeText(DashboardActivity.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Dashboard", "Volley error: " + error.toString());
                    Toast.makeText(DashboardActivity.this, "Error al cargar lugares: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonArrayRequest);
    }
}
