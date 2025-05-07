package com.example.androidapp.models;

public class EdificioHistorico {

    private Integer idEdificioHistorico;
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private String contenido;
    private String referenciaImagen;

    public Integer getIdEdificioHistorico() {
        return idEdificioHistorico;
    }

    public void setIdEdificioHistorico(Integer idEdificioHistorico) {
        this.idEdificioHistorico = idEdificioHistorico;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getReferenciaImagen() {
        return referenciaImagen;
    }

    public void setReferenciaImagen(String referenciaImagen) {
        this.referenciaImagen = referenciaImagen;
    }
}
