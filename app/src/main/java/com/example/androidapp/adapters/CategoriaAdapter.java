package com.example.androidapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.Categoria;

import java.util.ArrayList;
import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder> {

    private List<Categoria> categorias;

    public CategoriaAdapter(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        Categoria categoria = categorias.get(position);
        holder.cbCategoria.setText(categoria.getNombre());
        holder.cbCategoria.setChecked(categoria.isSelected());

        holder.cbCategoria.setOnCheckedChangeListener((buttonView, isChecked) -> {
            categoria.setSelected(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public List<Categoria> getSelectedCategorias() {
        List<Categoria> selected = new ArrayList<>();
        for (Categoria c : categorias) {
            if (c.isSelected()) {
                selected.add(c);
            }
        }
        return selected;
    }

    public static class CategoriaViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCategoria;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCategoria = itemView.findViewById(R.id.cbCategoria);
        }
    }
}
