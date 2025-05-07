package com.example.androidapp.services;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.androidapp.R;
import com.example.androidapp.activities.RealidadAumentadaActivity;

import org.json.JSONException;

public class LocationService extends IntentService {
    private LocationManager locationManager;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String CHANNEL_ID = "my_channel";
    private static final int NOTIFICATION_ID = 1;
    private int userId = 1; // Actualizar para usar el id del usuario logeado

    // Mapa para almacenar notificaciones enviadas
    private Map<String, Long> sentNotifications = new HashMap<>();
    public LocationService() {
        super("LocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Toast.makeText(getApplicationContext(), "ManejarIntent ", Toast.LENGTH_SHORT).show();
        //sendNotification();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                checkLocationAndNotify(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) { }
            public void onProviderEnabled(String provider) { }
            public void onProviderDisabled(String provider) { }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            checkLocationAndNotify(lastKnownLocation);
        }
    }

    private void checkLocationAndNotify(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        checkNotificationCondition(userId, currentLatitude, currentLongitude);
    }

    private void checkNotificationCondition(int userId, double latitude, double longitude) {
        String url = "http://10.1.37.53:8080/api/coordenadas/validarCoordenadas/" + userId + "/" + latitude + "/" + longitude;
        //String url = "http://192.168.1.2:8080/api/coordenadas/validarCoordenadas/" + userId + "/" + latitude + "/" + longitude;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response != null && response.length() > 0) {
                            String nombreLugar = response.getString("descripcion");
                            // Verificar si ya se envió una notificación en el último minuto
                            if (!hasNotificationBeenSentRecently(nombreLugar)) {
                                sendNotification(nombreLugar);
                                // Registrar la notificación enviada
                                sentNotifications.put(nombreLugar, System.currentTimeMillis());
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", "Error parsing JSON response", e);
                    }
                }, error -> Log.e("API Error", "Error en la respuesta de la API: " + error.toString())
        );

        queue.add(jsonObjectRequest);
    }

    private boolean hasNotificationBeenSentRecently(String nombreLugar) {
        if (sentNotifications.containsKey(nombreLugar)) {
            long lastSentTime = sentNotifications.get(nombreLugar);
            long currentTime = System.currentTimeMillis();
            // Verificar si ha pasado menos de un minuto (60,000 milisegundos)
            if (currentTime - lastSentTime < 60000) {
                return true; // Ya se envió una notificación en el último minuto
            }
        }
        return false; // No se ha enviado una notificación recientemente
    }

    private void sendNotification(String nombre) {
        Toast.makeText(this, "Enviar Notificacion", Toast.LENGTH_SHORT).show();
        createNotificationChannel();

        Intent intent = new Intent(this, RealidadAumentadaActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_message) + " " + nombre)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
