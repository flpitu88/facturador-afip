/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.utils.facturador.afip.excepciones;

/**
 *
 * @author flpitu88
 */
public class ErrorUltimoNumeroException extends RuntimeException {

    private String error;

    public ErrorUltimoNumeroException(String mensaje) {
        this.error = mensaje;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
