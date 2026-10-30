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
import com.android.volley.AuthFailureError;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class RealidadAumentadaActivity extends AppCompatActivity {

    private FrameLayout arContainer;
    private LottieAnimationView animationView;
    private boolean arSoportado;
    private ArFragment arFragment;
    private String userId;
    private RequestQueue requestQueue;
    private String placeId;

    private ScrollView noArDetailsView;
    private ImageView ivEdificioNoAr;
    private TextView tvNombreNoAr;
    private TextView tvContenidoNoAr;

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
        noArDetailsView = findViewById(R.id.no_ar_details_view);
        ivEdificioNoAr = findViewById(R.id.iv_edificio_no_ar);
        tvNombreNoAr = findViewById(R.id.tv_nombre_no_ar);
        tvContenidoNoAr = findViewById(R.id.tv_contenido_no_ar);

        requestQueue = Volley.newRequestQueue(this);
        // Puedes obtener el userId de SharedPreferences si lo guardas tras el login
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "0");
        placeId = getSharedPreferences("VisitPrefs", MODE_PRIVATE).getString("currentEdificioId", "0");

        // Verificación simplificada que no muestra diálogos de instalación
        verificarCompatibilidadAR();
    }

    /**
     * Modelo 3D de realidad aumentada de un lugar de interés: recurso raw y escala.
     * La escala deja cada maqueta de aproximadamente 1.6 a 2 m sobre el plano detectado
     * (los modelos están en metros reales).
     */
    private static final class ModeloRA {
        final int recurso;
        final float escala;

        ModeloRA(int recurso, float escala) {
            this.recurso = recurso;
            this.escala = escala;
        }
    }

    /**
     * Relaciona cada idLugarInteres (tabla lugarinteres) con su modelo 3D.
     * Retorna null si el lugar no tiene modelo (p. ej. 16 = "Casa").
     */
    private ModeloRA getModeloRA(String placeId) {
        if (placeId == null) return null;
        switch (placeId) {
            case "1":  return new ModeloRA(R.raw.catedral_basilica_de_zacatecas, 0.02f);        // Catedral Basílica (~80 m)
            case "2":  return new ModeloRA(R.raw.mina_el_eden_zacatecas, 0.06f);                // Mina El Edén (~29 m)
            case "3":  return new ModeloRA(R.raw.cerro_de_la_bufa_zacatecas, 0.022f);           // Cerro de la Bufa (~80 m)
            case "4":  return new ModeloRA(R.raw.teatro_calderon_zacatecas, 0.06f);             // Teatro Calderón (~29 m)
            case "5":  return new ModeloRA(R.raw.museo_rafael_coronel_zacatecas, 0.025f);       // Museo Rafael Coronel (~70 m)
            case "6":  return new ModeloRA(R.raw.museo_pedro_coronel_zacatecas, 0.018f);        // Museo Pedro Coronel (~95 m)
            case "7":  return new ModeloRA(R.raw.plazuela_miguel_auza_zacatecas, 0.025f);       // Plazuela Miguel Auza (~66 m)
            case "8":  return new ModeloRA(R.raw.alameda_garcia_de_la_cadena_zacatecas, 0.016f); // Alameda García de la Cadena (~110 m)
            case "9":  return new ModeloRA(R.raw.acueducto_el_cubo_zacatecas, 0.011f);          // Acueducto El Cubo (~160 m)
            case "10": return new ModeloRA(R.raw.mercado_gonzalez_ortega_zacatecas, 0.02f);     // Mercado González Ortega (~84 m)
            case "11": return new ModeloRA(R.raw.portal_de_rosales_zacatecas, 0.018f);          // Portal de Rosales (~96 m)
            case "12": return new ModeloRA(R.raw.jardin_juarez_zacatecas, 0.025f);              // Jardín Juárez (~68 m)
            case "13": return new ModeloRA(R.raw.plaza_de_armas_zacatecas, 0.02f);              // Plaza de Armas (~80 m)
            case "14": return new ModeloRA(R.raw.museo_zacatecano_zacatecas, 0.032f);           // Museo Zacatecano (~52 m)
            case "15": return new ModeloRA(R.raw.ex_templo_san_agustin_zacatecas, 0.025f);      // Ex Templo de San Agustín (~70 m)
            case "17": return new ModeloRA(R.raw.parroquia_de_fatima, 0.1f);                    // Templo de Fátima (~18 m)
            case "18": return new ModeloRA(R.raw.museo_manuel_felguerez_zacatecas, 0.024f);     // Museo Manuel Felguérez (~72 m)
            case "19": return new ModeloRA(R.raw.plaza_bicentenario_zacatecas, 0.02f);          // Plaza Bicentenario (~80 m)
            case "20": return new ModeloRA(R.raw.museo_francisco_goitia_zacatecas, 0.024f);     // Museo Francisco Goitia (~72 m)
            case "21": return new ModeloRA(R.raw.obelisco_plaza_de_armas_zacatecas, 0.07f);     // Obelisco Plaza de Armas (~24 m)
            case "22": return new ModeloRA(R.raw.santuario_de_guadalupe_zacatecas, 0.02f);      // Santuario de Guadalupe (~88 m)
            default:   return null;
        }
    }

    /**
     * Retorna el ID del recurso raw para el modelo 3D correspondiente al edificio.
     * Si no hay un modelo 3D definido para el edificio, retorna 0.
     */
    private int getModelRawResource(String placeId) {
        ModeloRA modelo = getModeloRA(placeId);
        return modelo != null ? modelo.recurso : 0;
    }

    /**
     * Verifica compatibilidad con ARCore sin mostrar diálogos de instalación
     */
    private void verificarCompatibilidadAR() {
        ArCoreApk.Availability disponibilidad = ArCoreApk.getInstance().checkAvailability(this);

        // Solo activa AR si ya está instalado y listo para usar
        arSoportado = (disponibilidad == ArCoreApk.Availability.SUPPORTED_INSTALLED);

        boolean fromNotification = getIntent().getBooleanExtra("fromNotification", false);
        if (fromNotification) {
            registerVisit(userId, placeId);
        }

        int modelResId = getModelRawResource(placeId);

        if (modelResId != 0 && arSoportado) {
            iniciarArFragment(modelResId);
        } else {
            mostrarDetallesEdificio();
        }
    }

    /**
     * Carga ArFragment dinámicamente y configura el listener para instanciar el modelo AR
     */
    private void iniciarArFragment(int modelResId) {
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
                ModelRenderable.builder()
                        .setSource(this, modelResId)
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
            mostrarDetallesEdificio();
        }
    }

    /**
     * Muestra la vista alternativa con los detalles de imagen y descripción del edificio
     */
    private void mostrarDetallesEdificio() {
        arContainer.setVisibility(View.GONE);
        animationView.setVisibility(View.GONE);
        noArDetailsView.setVisibility(View.VISIBLE);

        loadPlaceDetails(placeId);
    }

    /**
     * Carga los detalles del edificio desde el servidor web de Spring Boot
     */
    private void loadPlaceDetails(String placeId) {
        String url = ApiConfig.BASE_URL + "api/edificios/" + placeId;
        String jwtToken = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("jwtToken", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String descripcion = response.optString("descripcion", "Edificio Histórico");
                        String contenido = response.optString("contenido", "No hay contenido disponible.");
                        String referenciaImagen = response.optString("referenciaImagen", "");

                        tvNombreNoAr.setText(descripcion);
                        tvContenidoNoAr.setText(contenido);

                        if (!referenciaImagen.isEmpty()) {
                              String imageUrl = referenciaImagen;
                              if (!imageUrl.startsWith("http")) {
                                  imageUrl = ApiConfig.BASE_URL.replace("api/", "") + imageUrl;
                              } else {
                                  imageUrl = getWikimediaThumbnail(imageUrl);
                              }

                              com.bumptech.glide.load.model.GlideUrl glideUrl;
                              try {
                                  // Pasando un objeto URL evitamos que Glide decodifique e intente codificar de nuevo caracteres ya válidos como %2C y %C3
                                  glideUrl = new com.bumptech.glide.load.model.GlideUrl(new java.net.URL(imageUrl), 
                                      new com.bumptech.glide.load.model.LazyHeaders.Builder()
                                          .addHeader("User-Agent", "TourismTrackingApp/1.0 (icjorge@uaz.edu.mx; Mobile App for Historic Tourism)")
                                          .build());
                              } catch (java.net.MalformedURLException e) {
                                  glideUrl = new com.bumptech.glide.load.model.GlideUrl(imageUrl, 
                                      new com.bumptech.glide.load.model.LazyHeaders.Builder()
                                          .addHeader("User-Agent", "TourismTrackingApp/1.0 (icjorge@uaz.edu.mx; Mobile App for Historic Tourism)")
                                          .build());
                              }
                              Glide.with(this)
                                      .load(glideUrl)
                                      .placeholder(android.R.drawable.ic_menu_gallery)
                                      .error(android.R.drawable.ic_menu_report_image)
                                      .into(ivEdificioNoAr);
                          }
                    } catch (Exception e) {
                        Log.e("ARDetails", "Error parsing place details", e);
                    }
                },
                error -> {
                    Log.e("ARDetails", "Error fetching place details: " + error);
                    Toast.makeText(this, "Error al cargar detalles del edificio", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + jwtToken);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    private String getWikimediaThumbnail(String url) {
        if (url != null && url.startsWith("https://upload.wikimedia.org/wikipedia/commons/")) {
            if (!url.contains("/thumb/")) {
                String filename = url.substring(url.lastIndexOf("/") + 1);
                String rest = url.substring("https://upload.wikimedia.org/wikipedia/commons/".length());
                return "https://upload.wikimedia.org/wikipedia/commons/thumb/" + rest + "/500px-" + filename;
            }
        }
        return url;
    }

    /**
     * Muestra animación alternativa cuando AR no está disponible (mantenido por compatibilidad)
     */
    private void mostrarAnimacionAlternativa() {
        arContainer.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();
        Toast.makeText(this, "Realidad aumentada no disponible", Toast.LENGTH_SHORT).show();
    }

    /**
     * Agrega modelo 3D a la escena
     */
    /*
     * private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable)
     * {
     * AnchorNode anchorNode = new AnchorNode(anchor);
     * anchorNode.setParent(arFragment.getArSceneView().getScene());
     * 
     * TransformableNode model = new
     * TransformableNode(arFragment.getTransformationSystem());
     * model.setParent(anchorNode);
     * model.setRenderable(modelRenderable);
     * model.setLocalScale(new Vector3(10.0f, 10.0f, 10.0f));
     * model.select();
     * }
     */

    /**
     * Agrega modelo 3D a la escena. Aplica la escala al AnchorNode para garantizar
     * el tamaño.
     */
    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Escala según el lugar (ver getModeloRA) para verlo como maqueta.
        ModeloRA modelo = getModeloRA(placeId);
        float scaleFactor = modelo != null ? modelo.escala : 1.0f;

        anchorNode.setLocalScale(new Vector3(scaleFactor, scaleFactor, scaleFactor));

        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(modelRenderable);

        model.select();
    }

    /**
     * Registra la visita al web service mediante una petición POST.
     * Utiliza JsonObjectRequest para enviar el userId y el contenido (nombre del
     * lugar).
     */
    private void registerVisit(String userId, String placeId) {
        String url = ApiConfig.BASE_URL + "api/visitas/";
        /*
         * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
         * Locale.US);
         * sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         * String fechaActual = sdf.format(new Date());
         */

        Date miFecha = new Date();
        long fechaEpoch = miFecha.getTime();
        String fechaEpochString = String.valueOf(fechaEpoch);

        /*
         * Date miFecha = new Date();
         * String fechaISO = miFecha.toInstant().toString();
         */

        // Toast.makeText(this, "Inicio registro visita.", Toast.LENGTH_SHORT).show();
        try {
            JSONObject jsonBody = new JSONObject();
            // El userId y el contenido (nombre del lugar) forman el cuerpo de la Visita
            // jsonBody.put("idUsuario", 1);
            jsonBody.put("idUsuario", userId);
            // jsonBody.put("idEdificioHistorico", 2);
            jsonBody.put("idEdificioHistorico", Integer.parseInt(placeId));
            jsonBody.put("fecha", fechaEpochString);
            jsonBody.put("llevaNinos", false);
            Toast.makeText(this, jsonBody.toString(), Toast.LENGTH_LONG).show();

            // Obtenemos el token guardado
            String jwtToken = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("jwtToken", "");

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Toast.makeText(this, "Visita registrada exitosamente.", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        // Mostrar Toast para debug
                        String errorMessage = parseVolleyError(error);

                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show();
                            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
                            finish();
                        } else {
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + jwtToken);
                    return headers;
                }
            };

            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            Log.e("Volley", "Invalid JSON Object.", e);
        }
    }

    /**
     * Extrae el mensaje de error del cuerpo de la respuesta de Volley (usualmente
     * JSON).
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