package com.example.androidapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.config.ApiConfig;
import com.example.androidapp.models.EdificioHistorico;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.androidapp.activities.RealidadAumentadaActivity;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {

    private Context context;
    private List<EdificioHistorico> placeList;

    public PlacesAdapter(Context context, List<EdificioHistorico> placeList) {
        this.context = context;
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        EdificioHistorico place = placeList.get(position);

        holder.tvNombre.setText(place.getDescripcion());
        holder.tvDescripcion.setText(place.getContenido());

        // Manejar click para abrir RealidadAumentadaActivity (con fallback si no tiene modelo)
        holder.itemView.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("VisitPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("currentEdificioId", String.valueOf(place.getIdEdificioHistorico())).apply();
            
            Intent intent = new Intent(context, RealidadAumentadaActivity.class);
            intent.putExtra("fromNotification", false);
            context.startActivity(intent);
        });

        // Debugging strategy: Load placeholder if null.
        if (place.getReferenciaImagen() != null && !place.getReferenciaImagen().isEmpty()) {
            String imageUrl = place.getReferenciaImagen();
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

            Glide.with(context)
                    .load(glideUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivLugar);
        }
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

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLugar;
        TextView tvNombre;
        TextView tvDescripcion;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLugar = itemView.findViewById(R.id.ivLugar);
            tvNombre = itemView.findViewById(R.id.tvNombreLugar);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionLugar);
        }
    }
}
