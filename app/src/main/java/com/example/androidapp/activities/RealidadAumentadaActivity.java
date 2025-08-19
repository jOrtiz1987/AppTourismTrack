package com.example.androidapp.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.example.androidapp.R;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class RealidadAumentadaActivity extends AppCompatActivity {

    private FrameLayout arContainer;
    private LottieAnimationView animationView;
    private boolean arSoportado;
    private ArFragment arFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realidad_aumentada_activity);

        arContainer = findViewById(R.id.ar_container);
        animationView = findViewById(R.id.animation_view);

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
    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(modelRenderable);
        model.setLocalScale(new Vector3(10.0f, 10.0f, 10.0f));
        model.select();
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