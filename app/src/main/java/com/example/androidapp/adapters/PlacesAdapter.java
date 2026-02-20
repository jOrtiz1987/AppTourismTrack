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

        // Construct full image URL if reference is relative, or use as is if absolute
        // Assuming relative path for now based on typical setups, but can be adjusted
        // If referenciaImagen is just "image.jpg", we might need a base content URL.
        // For now, let's try to usage the ApiConfig.BASE_URL without "api/" if images
        // are static
        // Or just load what is in the string if it's a full URL.

        // Debugging strategy: Load placeholder if null.
        if (place.getReferenciaImagen() != null && !place.getReferenciaImagen().isEmpty()) {
            // START of image url logic - adjusting to potential local server logic
            // Common pattern: If it starts with http, use it. Else append to base.
            String imageUrl = place.getReferenciaImagen();
            if (!imageUrl.startsWith("http")) {
                imageUrl = ApiConfig.BASE_URL.replace("api/", "") + imageUrl;
            }

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivLugar);
        }
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
