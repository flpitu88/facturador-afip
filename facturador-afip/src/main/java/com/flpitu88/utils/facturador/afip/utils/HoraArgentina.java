/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.utils.facturador.afip.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 * @author flpitu88
 */
public class HoraArgentina {

    private static final String zoneId = "America/Buenos_Aires";

    public static LocalDateTime getFechaYHoraActualArgentina() {
        return LocalDateTime.now(ZoneId.of(zoneId));
    }

    public static LocalDateTime getFechaYHoraActualDefaultServer() {
        return LocalDateTime.now(ZoneId.systemDefault());
    }

    public static LocalDate getFechaActualArgentina() {
        return LocalDate.now(ZoneId.of(zoneId));
    }

    public static LocalDate getFechaActualDefaultServer() {
        return LocalDate.now(ZoneId.systemDefault());
    }

    public static String getFechaYHoraActualArgentinaString() {
        DateTimeFormatter formatterConHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime ldt = LocalDateTime.now(ZoneId.of(zoneId));
        return ldt.format(formatterConHora);
    }

    public static String getFechaActualArgentinaString() {
        DateTimeFormatter formatterSinHora = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate ldt = LocalDate.now(ZoneId.of(zoneId));
        return ldt.format(formatterSinHora);
    }

    public static String getFechaActualArgentinaParaFactura() {
        DateTimeFormatter formatterSinHora = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate ldt = LocalDate.now(ZoneId.of(zoneId));
        return ldt.format(formatterSinHora);
    }

    public static String getFechaVencimientoArgentinaParaWSAfip() {
        DateTimeFormatter formatterSinHora = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate ldt = LocalDate.now(ZoneId.of(zoneId)).plusDays(15);
        return ldt.format(formatterSinHora);
    }
    
    public static String getFechaVencimientoArgentinaParaFactura() {
        DateTimeFormatter formatterSinHora = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate ldt = LocalDate.now(ZoneId.of(zoneId)).plusDays(15);
        return ldt.format(formatterSinHora);
    }

    public static Date getFechaYHoraActualArgentinaComoDate() {
        return Date.from(getFechaYHoraActualArgentina()
                .atZone(ZoneId.of(zoneId)).toInstant());
    }

    public static String getFechaYHoraActualArgentinaStringAWS() {
        DateTimeFormatter formatterConHora = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
        LocalDateTime ldt = LocalDateTime.now(ZoneId.of(zoneId));
        return ldt.format(formatterConHora);
    }

    public static String getFechaDesdeLocalDateTime(LocalDateTime ldt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return ldt.format(formatter);
    }

    public static String getFechaArgentinaDesdeFechaFormatoFactura(String fechaFact) {
        DateTimeFormatter formatterFactura = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate ld = LocalDate.parse(fechaFact, formatterFactura);
        DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return ld.format(formatterFecha);
    }

}
