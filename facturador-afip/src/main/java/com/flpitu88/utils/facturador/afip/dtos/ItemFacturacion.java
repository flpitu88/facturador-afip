/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.utils.facturador.afip.dtos;

/**
 *
 * @author flavio
 */
public class ItemFacturacion {

    private String descripcion;
    private Double costoNexo;
    private Double costoIva;
    private String fechaEnvio;

    public ItemFacturacion() {
    }

    public ItemFacturacion(String descripcion,
            Double costoNexo, String fechaEnvio) {
        this.descripcion = descripcion;
        this.costoNexo = costoNexo;
        this.fechaEnvio = fechaEnvio;
        this.costoIva = costoNexo * 0.21;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getCostoNexo() {
        return costoNexo;
    }

    public void setCostoNexo(Double costoNexo) {
        this.costoNexo = costoNexo;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Double getCostoIva() {
        return costoIva;
    }

    public void setCostoIva(Double costoIva) {
        this.costoIva = costoIva;
    }

    public String getCostoNexoString() {
        Double itemConIva = costoNexo + costoIva;
        return itemConIva.toString();
    }

    public String getCostoIvaString() {
        return costoIva.toString();
    }
}
