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
public class CAE {

    private String nroCae;
    private String fechaVto;
    private Integer nroComprobante;

    public CAE() {
    }

    public CAE(String nroCae, String fechaVto, Integer ultimoNro) {
        this.nroCae = nroCae;
        this.fechaVto = fechaVto;
        this.nroComprobante = ultimoNro;
    }

    public String getNroCae() {
        return nroCae;
    }

    public void setNroCae(String nroCae) {
        this.nroCae = nroCae;
    }

    public String getFechaVto() {
        return fechaVto;
    }

    public void setFechaVto(String fechaVto) {
        this.fechaVto = fechaVto;
    }

    public Integer getNroComprobante() {
        return nroComprobante;
    }

    public void setNroComprobante(Integer nroComprobante) {
        this.nroComprobante = nroComprobante;
    }

}
