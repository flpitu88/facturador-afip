/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.utils.facturador.afip.dtos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author flavio
 */
public class DatosFacturacion {

    private String fechaPrimera;
    private String fechaUltima;
    private List<ItemFacturacion> items;
    private Double importeTotal;
    private Double importeServicio;
    private Double importeIva;
    private UsuarioFactura usuario;
    private String fechaCbte;

    public DatosFacturacion() {
        items = new ArrayList<>();
    }

    public DatosFacturacion(String fechaPrimera, String fechaUltima,
            List<ItemFacturacion> items, Double importeTotal,
            Double importeServicio, Double importeIva, UsuarioFactura usuario) {
        this.fechaPrimera = fechaPrimera;
        this.fechaUltima = fechaUltima;
        this.items = items;
        this.importeTotal = importeTotal;
        this.importeServicio = importeServicio;
        this.importeIva = importeIva;
        this.usuario = usuario;
    }

    public String getFechaPrimera() {
        return fechaPrimera;
    }

    public void setFechaPrimera(String fechaPrimera) {
        this.fechaPrimera = fechaPrimera;
    }

    public String getFechaUltima() {
        return fechaUltima;
    }

    public void setFechaUltima(String fechaUltima) {
        this.fechaUltima = fechaUltima;
    }

    public List<ItemFacturacion> getItems() {
        return items;
    }

    public void setItems(List<ItemFacturacion> items) {
        this.items = items;
    }

    public Double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(Double importeTotal) {
        this.importeTotal = importeTotal;
    }

    public Double getImporteServicio() {
        return importeServicio;
    }

    public void setImporteServicio(Double importeServicio) {
        this.importeServicio = importeServicio;
    }

    public Double getImporteIva() {
        return importeIva;
    }

    public void setImporteIva(Double importeIva) {
        this.importeIva = importeIva;
    }

    public UsuarioFactura getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioFactura usuario) {
        this.usuario = usuario;
    }

    public void calcularImportes() {
        Double importeServicioAcum = 0d;

        for (ItemFacturacion item : items) {
            importeServicioAcum += item.getCostoNexo();
        }
        importeServicio = redondear(importeServicioAcum);
        importeIva = redondear(0.21 * importeServicio);
        importeTotal = redondear(importeServicio + importeIva);
    }

    public void obtenerFechas() {
        DateTimeFormatter formatterParaCAE = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter formatterParaLDT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fechaPrim = LocalDate.now().plusYears(1); // Me aseguro que lo cambia
        LocalDate fechaUlt = LocalDate.now().minusYears(1); // Me aseguro que lo cambia
        for (ItemFacturacion item : items) {
            String fechaItemString = item.getFechaEnvio();
            LocalDate fechaLd = LocalDate.parse(fechaItemString, formatterParaLDT);
            if (fechaLd.isBefore(fechaPrim)) {
                fechaPrim = fechaLd;
            }
            if (fechaLd.isAfter(fechaUlt)) {
                fechaUlt = fechaLd;
            }
        }
        fechaPrimera = fechaPrim.format(formatterParaCAE);
        fechaUltima = fechaUlt.format(formatterParaCAE);
    }

    public String getFechaCbte() {
        return fechaCbte;
    }

    public void setFechaCbte(String fechaCbte) {
        this.fechaCbte = fechaCbte;
    }

    public Double redondear(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
