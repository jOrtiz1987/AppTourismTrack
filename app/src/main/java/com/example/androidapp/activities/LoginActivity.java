package com.example.androidapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidapp.R;
import com.example.androidapp.config.ApiConfig;
import com.example.androidapp.services.LocationService;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity {

    private EditText mUsername;
    private EditText mPassword;
    private Button mLoginButton;
    private Button btnRegistro;

    // PERMISOS
    private static final int PERMISSION_LOCATION = 100;
    private static final int PERMISSION_CAMERA = 101;
    private static final int PERMISSION_NOTIFICATIONS = 102;

    private int mIdUsuario = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLoginButton = findViewById(R.id.email_sign_in_button);
        btnRegistro = findViewById(R.id.btnRegistro);

        if (btnRegistro != null) {
            btnRegistro.setOnClickListener(v -> {
                Intent newIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(newIntent);
            });
        }

        mLoginButton.setOnClickListener(view -> verifyPermissionsBeforeLogin());

        // Auto-login check
        checkExistingSession();
    }

    private void checkExistingSession() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedUserId = prefs.getString("userId", "-1");

        if (!savedUserId.equals("-1")) {
            Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(dashboardIntent);
            finish();
        }
    }

    /**
     * ================================
     * 🔐 VALIDACIÓN DE PERMISOS
     * ================================
     */
    private void verifyPermissionsBeforeLogin() {

        // 1. Ubicación
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSION_LOCATION);

            return;
        }

        // 2. Cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA },
                    PERMISSION_CAMERA);

            return;
        }

        // 3. Notificaciones Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS },
                        PERMISSION_NOTIFICATIONS);

                return;
            }
        }

        // Si todo está OK → intentar login
        attemptLogin();
    }

    /**
     * ================================
     * 🔑 LOGIN
     * ================================
     */
    private void attemptLogin() {

        String username = mUsername.getText().toString().trim();
        String password = mPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        callLoginApi(username, password);
    }

    private void callLoginApi(String username, String password) {

        // Endpoint según el AuthController proporcionado por ti
        String url = ApiConfig.BASE_URL + "api/auth/login";

        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);

        JSONObject bodyParams = new JSONObject();
        try {
            // El backend AuthController espera "correo" en el Request
            bodyParams.put("correo", username);
            bodyParams.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                bodyParams,
                response -> {
                    try {
                        // El backend AuthController retorna la llave "jwt"
                        String jwtToken = response.getString("jwt");

                        // IMPORTANTE: Tu backend actual en AuthController NO retorna idUsuario.
                        // Usará -1 por defecto, lo que podría afectar servicios que lo requieran.
                        mIdUsuario = response.optInt("idUsuario", -1);

                        // Guardamos el usuario y el nuevo token JWT
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        prefs.edit()
                                .putString("userId", String.valueOf(mIdUsuario))
                                .putString("jwtToken", jwtToken)
                                .apply();

                        // Iniciar LocationService
                        Intent serviceIntent = new Intent(getApplicationContext(), LocationService.class);
                        serviceIntent.putExtra("idUsuario", String.valueOf(mIdUsuario));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent);
                        } else {
                            startService(serviceIntent);
                        }

                        Toast.makeText(this, "Login correcto. Servicio de ubicación iniciado", Toast.LENGTH_SHORT)
                                .show();

                        // Iniciar DashboardActivity y cerrar Login
                        Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(dashboardIntent);
                        finish();

                    } catch (JSONException e) {
                        Log.e("Volley", "Invalid JSON", e);
                        Toast.makeText(this, "Error: respuesta inválida del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Volley Error", "API error: " + error.toString());
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonObjectRequest);
    }

    /**
     * ===============================================
     * 📌 RESPUESTAS DE LOS PERMISOS
     * ===============================================
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length == 0)
            return;

        switch (requestCode) {

            case PERMISSION_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    verifyPermissionsBeforeLogin();
                } else {
                    Toast.makeText(this, "La ubicación es necesaria para continuar", Toast.LENGTH_LONG).show();
                }
                break;

            case PERMISSION_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    verifyPermissionsBeforeLogin();
                } else {
                    Toast.makeText(this, "La cámara es necesaria para realidad aumentada", Toast.LENGTH_LONG).show();
                }
                break;

            case PERMISSION_NOTIFICATIONS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    verifyPermissionsBeforeLogin();
                }
                break;
        }
    }
}
