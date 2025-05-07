package com.example.androidapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etCorreo, etPassword, etGenero, etFechaNacimiento, etCodigoPostal;
    private Button btnRegistrar;
    private RequestQueue requestQueue;
    private String urlBase = "http://10.1.37.40:8080/api/usuarios/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        etGenero = findViewById(R.id.etGenero);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etCodigoPostal = findViewById(R.id.etCodigoPostal);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        requestQueue = Volley.newRequestQueue(this);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String genero = etGenero.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        String codigoPostal = etCodigoPostal.getText().toString().trim();

        boolean cancel = false;

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty() ||
                genero.isEmpty() || fechaNacimiento.isEmpty() || codigoPostal.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for a valid password.
        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Password Invalido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for a valid email address.
        if (!isEmailValid(correo)) {
            Toast.makeText(this, "Correo Invalido", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("nombre", nombre);
            jsonBody.put("correo", correo);
            jsonBody.put("password", password);
            jsonBody.put("genero", genero);
            jsonBody.put("fechaDeNacimiento", fechaNacimiento);
            jsonBody.put("codigoPostal", codigoPostal);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, urlBase, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(RegisterActivity.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                        Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        RegisterActivity.this.startActivity(newIntent);
                        //startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RegisterActivity.this, "Error al registrar usuario: " + error.toString(), Toast.LENGTH_LONG).show();
                        Toast.makeText(RegisterActivity.this, "Volley " + error.toString(), Toast.LENGTH_SHORT).show();
                        Log.e("Volley", error.toString());
                    }
                });

        requestQueue.add(request);
    }

    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

}
