/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.utils.facturador.afip.dtos;

/**
 *
 * @author flpitu88
 */
public class TicketAccesoAfip {

    private String token;
    private String sign;

    public TicketAccesoAfip() {
    }

    public TicketAccesoAfip(String token, String sign) {
        this.token = token;
        this.sign = sign;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

}
