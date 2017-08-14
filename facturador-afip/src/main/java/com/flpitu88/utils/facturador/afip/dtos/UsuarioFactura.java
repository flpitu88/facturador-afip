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
public class UsuarioFactura {

    private String nombre;
    private String apellido;
    private String nombreUsuario;
    private String email;
    private Long cuil;
    private String nroTelefono;
    private Integer idUsuario;
    private String gcmId;

    public UsuarioFactura() {
    }

    public UsuarioFactura(String nombre, String apellido, String nombreUsuario,
            String email, Long cuil, String nroTelefono, Integer idUsuario, String gcmId) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.cuil = cuil;
        this.nroTelefono = nroTelefono;
        this.idUsuario = idUsuario;
        this.gcmId = gcmId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCuil() {
        return cuil;
    }

    public void setCuil(Long cuil) {
        this.cuil = cuil;
    }

    public String getNroTelefono() {
        return nroTelefono;
    }

    public void setNroTelefono(String nroTelefono) {
        this.nroTelefono = nroTelefono;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getGcmId() {
        return gcmId;
    }

    public void setGcmId(String gcmId) {
        this.gcmId = gcmId;
    }

}
