package com.dalila.entity;

public class Municipio {
    private int id;
    private String nombre;

    public Municipio() {}

    public Municipio(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public long getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
