package com.example.androidapp.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidapp.activities.LoginActivity;
import com.example.androidapp.activities.RealidadAumentadaActivity;
import com.example.androidapp.config.ApiConfig;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service implements LocationListener {

    private static final String TAG = "LocationService";
    private static final long LOCATION_INTERVAL = 60000; // 1 minuto
    private static final float LOCATION_DISTANCE = 10f; // 10 metros
    private static final int NOTIFICATION_ID = 101;

    private LocationManager locationManager;
    private String userId;
    private String lugarActual;

    // Mapa en memoria
    private final Map<String, Long> sentNotifications = new HashMap<>();

    // Persistencia
    private static final String PREFS_NOTIF = "NotifPrefs";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(NOTIFICATION_ID, buildForegroundNotification());
        return START_STICKY;
    }

    private Notification buildForegroundNotification() {
        String channelId = "location_foreground_channel";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Servicio de Rastreo Activo",
                    NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, com.example.androidapp.activities.DashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Servicio de Ubicación Activo")
                .setContentText("Monitoreando tu ubicación…")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("userId", "0");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        cargarNotificacionesPrevias();
        iniciarGPS();
    }

    private void iniciarGPS() {
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    this);
        } catch (SecurityException e) {
            Log.e(TAG, "Permisos de ubicación no otorgados", e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        sendCoordinatesToServer(lat, lon);
    }

    private void sendCoordinatesToServer(double latitude, double longitude) {

        String url = ApiConfig.BASE_URL + "coordenadas/validarCoordenadas/"
                + userId + "/" + latitude + "/" + longitude;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response != null && response.length() > 0) {

                            // NORMALIZAMOS LA LLAVE
                            String nombreLugar = response.getString("descripcion")
                                    .trim().toLowerCase();

                            String idLugar = response.getString("id");

                            if (!hasNotificationBeenSentRecently(nombreLugar)) {
                                sendNotification(nombreLugar, idLugar);
                                guardarNotificacion(nombreLugar);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "JSON error", e);
                    }
                },
                error -> Log.e(TAG, "API error: " + error));

        queue.add(jsonObjectRequest);
    }

    private boolean hasNotificationBeenSentRecently(String lugar) {
        Long lastSent = sentNotifications.get(lugar);

        if (lastSent == null)
            return false;

        // 1 minuto
        return System.currentTimeMillis() - lastSent < 60000;
    }

    private void guardarNotificacion(String lugar) {
        long now = System.currentTimeMillis();
        sentNotifications.put(lugar, now);

        SharedPreferences prefs = getSharedPreferences(PREFS_NOTIF, MODE_PRIVATE);
        prefs.edit().putLong(lugar, now).apply();
    }

    private void cargarNotificacionesPrevias() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NOTIF, MODE_PRIVATE);
        Map<String, ?> all = prefs.getAll();

        for (String key : all.keySet()) {
            long timestamp = prefs.getLong(key, 0);
            sentNotifications.put(key, timestamp);
        }
    }

    private void sendNotification(String lugar, String idLugar) {

        SharedPreferences prefs = getSharedPreferences("VisitPrefs", MODE_PRIVATE);
        prefs.edit().putString("currentEdificioId", idLugar).apply();

        String channelId = "location_channel";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Ubicación",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, RealidadAumentadaActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Estás cerca de:")
                .setContentText(lugar)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        manager.notify(lugar.hashCode(), builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
