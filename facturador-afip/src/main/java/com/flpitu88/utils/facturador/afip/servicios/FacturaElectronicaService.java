/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.utils.facturador.afip.servicios;

import com.flpitu88.utils.facturador.afip.dtos.CAE;
import com.flpitu88.utils.facturador.afip.dtos.DatosFacturacion;

/**
 *
 * @author flavio
 */
public interface FacturaElectronicaService {
    
    public CAE obtenerCAEAfip(DatosFacturacion df);
}
