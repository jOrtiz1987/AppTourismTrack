package com.example.androidapp.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.example.androidapp.config.ApiConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RealidadAumentadaActivity extends AppCompatActivity {

    private FrameLayout arContainer;
    private LottieAnimationView animationView;
    private boolean arSoportado;
    private ArFragment arFragment;
    private String userId;
    private RequestQueue requestQueue;
    private String placeId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realidad_aumentada_activity);

        ImageButton btnCerrar = findViewById(R.id.btn_cerrar_ra);
        btnCerrar.setOnClickListener(v -> {
            finish();
        });

        arContainer = findViewById(R.id.ar_container);
        animationView = findViewById(R.id.animation_view);

        requestQueue = Volley.newRequestQueue(this);
        // Puedes obtener el userId de SharedPreferences si lo guardas tras el login
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "0");
        placeId = getSharedPreferences("VisitPrefs", MODE_PRIVATE).getString("currentEdificioId", "0");

        // Verificación simplificada que no muestra diálogos de instalación
        verificarCompatibilidadAR();
    }

    /**
     * Verifica compatibilidad con ARCore sin mostrar diálogos de instalación
     */
    private void verificarCompatibilidadAR() {
        ArCoreApk.Availability disponibilidad = ArCoreApk.getInstance().checkAvailability(this);

        // Solo activa AR si ya está instalado y listo para usar
        arSoportado = (disponibilidad == ArCoreApk.Availability.SUPPORTED_INSTALLED);

        registerVisit(userId, placeId);

        if (arSoportado) {
            iniciarArFragment();
        } else {
            mostrarAnimacionAlternativa();
        }
    }

    /**
     * Carga ArFragment dinámicamente
     */
    private void iniciarArFragment() {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            arFragment = new ArFragment();
            transaction.add(R.id.ar_container, arFragment);
            transaction.commit();

            arFragment.setOnTapArPlaneListener((HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING &&
                        plane.getType() != Plane.Type.HORIZONTAL_DOWNWARD_FACING) {
                    return;
                }

                Anchor anchor = hitResult.createAnchor();
                //Uri modeloUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.parroquiacentro22);
                ModelRenderable.builder()
                        .setSource(this, R.raw.parroquiacentro22)
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
                        .exceptionally(throwable -> {
                            Toast.makeText(this, "Error al cargar modelo 3D", Toast.LENGTH_LONG).show();
                            return null;
                        });
            });
        } catch (Exception e) {
            Log.e("ARError", "Error al iniciar AR", e);
            mostrarAnimacionAlternativa();
        }
    }

    /**
     * Muestra animación alternativa cuando AR no está disponible
     */
    private void mostrarAnimacionAlternativa() {
        arContainer.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();

        // Opcional: Personaliza el mensaje para no mencionar instalación
        Toast.makeText(this, "Realidad aumentada no disponible", Toast.LENGTH_SHORT).show();
    }

    /**
     * Agrega modelo 3D a la escena
     */
    /*private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(modelRenderable);
        model.setLocalScale(new Vector3(10.0f, 10.0f, 10.0f));
        model.select();
    }*/

    /**
     * Agrega modelo 3D a la escena. Aplica la escala al AnchorNode para garantizar el tamaño.
     */
    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // MODIFICACIÓN CRÍTICA: Aplicar escala masiva al AnchorNode (padre)
        // Esto compensa si el modelo .glb fue exportado con unidades minúsculas.
        anchorNode.setLocalScale(new Vector3(9.0f, 9.0f, 9.0f));

        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(modelRenderable);

        // Dejamos la escala en 1.0f para que herede la escala masiva del padre.
        // Si se define aquí, podría sobrescribir la del padre.
        // model.setLocalScale(new Vector3(1.0f, 1.0f, 1.0f));

        model.select();
    }

    /**
     * Registra la visita al web service mediante una petición POST.
     * Utiliza JsonObjectRequest para enviar el userId y el contenido (nombre del lugar).
     */
    private void registerVisit(String userId, String placeId) {
        String url = ApiConfig.BASE_URL + "visitas/";
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String fechaActual = sdf.format(new Date());*/

        Date miFecha = new Date();
        long fechaEpoch = miFecha.getTime();
        String fechaEpochString = String.valueOf(fechaEpoch);

        /*Date miFecha = new Date();
        String fechaISO = miFecha.toInstant().toString();*/

        //Toast.makeText(this, "Inicio registro visita.", Toast.LENGTH_SHORT).show();
        try {
            JSONObject jsonBody = new JSONObject();
            // El userId y el contenido (nombre del lugar) forman el cuerpo de la Visita
            //jsonBody.put("idUsuario", 1);
            jsonBody.put("idUsuario", userId);
            //jsonBody.put("idEdificioHistorico", 2);
            jsonBody.put("idEdificioHistorico", Integer.parseInt(placeId));
            jsonBody.put("fecha", fechaEpochString);
            jsonBody.put("llevaNinos", false);
            Toast.makeText(this, jsonBody.toString(), Toast.LENGTH_LONG).show();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Toast.makeText(this, "Visita registrada exitosamente.", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        // Mostrar Toast para debug
                        String errorMessage = parseVolleyError(error);

                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
            );

            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            Log.e("Volley", "Invalid JSON Object.", e);
        }
    }

    /**
     * Extrae el mensaje de error del cuerpo de la respuesta de Volley (usualmente JSON).
     */
    private String parseVolleyError(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                return new String(error.networkResponse.data, "UTF-8");
            } catch (Exception e) {
                return "Error al leer respuesta de error: " + error.getMessage();
            }
        }
        // Si no hay respuesta de red, devuelve el mensaje general
        return error.getMessage() != null ? error.getMessage() : "Error desconocido de red.";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arSoportado && arFragment != null) {
            try {
                arFragment.onResume();
            } catch (Exception e) {
                Log.e("ARError", "Error al reanudar AR", e);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (arFragment != null) {
            arFragment.onPause();
        }
    }
}