package com.example.androidapp.models;

public class Visita {

    private Integer idVisita;
    private Usuario usuario;
    private EdificioHistorico edificioHistorico;

    public Integer getIdVisita() {
        return idVisita;
    }

    public void setIdVisita(Integer idVisita) {
        this.idVisita = idVisita;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public EdificioHistorico getEdificioHistorico() {
        return edificioHistorico;
    }

    public void setEdificioHistorico(EdificioHistorico edificioHistorico) {
        this.edificioHistorico = edificioHistorico;
    }
}
