package com.example.androidapp.models;

public class RegistroCoordendas {

    private Integer idRegistroCoordendas;
    private Double latitud;
    private Double longitud;
    private Usuario usuario;

    public Integer getIdRegistroCoordendas() {
        return idRegistroCoordendas;
    }

    public void setIdRegistroCoordendas(Integer idRegistroCoordendas) {
        this.idRegistroCoordendas = idRegistroCoordendas;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
