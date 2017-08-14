/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.utils.facturador.afip.servicios;

import afip.services.data.AlicIva;
import afip.services.data.ArrayOfAlicIva;
import afip.services.data.ArrayOfFECAEDetRequest;
import afip.services.data.FEAuthRequest;
import afip.services.data.FECAECabRequest;
import afip.services.data.FECAEDetRequest;
import afip.services.data.FECAERequest;
import afip.services.data.FECAEResponse;
import afip.services.data.FECAESolicitar;
import afip.services.data.FECAESolicitarResponse;
import afip.services.data.FECompUltimoAutorizado;
import afip.services.data.FECompUltimoAutorizadoResponse;
import afip.services.data.FERecuperaLastCbteResponse;
import com.flpitu88.utils.facturador.afip.dtos.CAE;
import com.flpitu88.utils.facturador.afip.dtos.DatosFacturacion;
import com.flpitu88.utils.facturador.afip.dtos.TicketAccesoAfip;
import com.flpitu88.utils.facturador.afip.excepciones.ErrorUltimoNumeroException;
import com.flpitu88.utils.facturador.afip.utils.HoraArgentina;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.rpc.ParameterMode;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.Base64;
import org.apache.axis.encoding.XMLType;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 *
 * @author flavio
 */
public class FacturaElectronicaServiceImpl implements FacturaElectronicaService {

    @Autowired
    private Environment env;

    private static final Logger logger
            = Logger.getLogger(FacturaElectronicaServiceImpl.class.getName());
    
    @Override
    public CAE obtenerCAEAfip(DatosFacturacion df) {
        TicketAccesoAfip ta = obtenerTicketDeAcceso();

        logger.log(java.util.logging.Level.INFO, "Token: {0}\nSign: {1}",
                new Object[]{ta.getToken(), ta.getSign()});

        CAE respuestaCAE = null;
        try {
            int ultimoNro = obtenerUltimoNroComprobante(ta);

            logger.log(java.util.logging.Level.INFO,
                    "Ultimo numero de comprobante: {0}", ultimoNro);

            String soapXml = generarXMLPedidoCAE(ta, ultimoNro, df);
            URL url = new URL("https://wswhomo.afip.gov.ar/wsfev1/service.asmx");
            URLConnection conn = url.openConnection();

            // Set the necessary header fields
            conn.setRequestProperty("SOAPAction", "https://ar.gov.afip.dif.FEV1/FECAESolicitar");
            conn.setRequestProperty("Content-type", "application/soap+xml;charset=UTF-8;action=\"http://ar.gov.afip.dif.FEV1/FECAESolicitar\"");
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setDoOutput(true);
            // Send the request
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(soapXml);
            wr.flush();
            // Read the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            line = rd.readLine();

            logger.log(Level.INFO, "####   RESPUESTA AFIP CAE  : " + line);

            XMLInputFactory xif = XMLInputFactory.newFactory();
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(line));
            xsr.nextTag(); // Advance to Envelope tag
            xsr.nextTag(); // Advance to Body tag
            xsr.nextTag(); // Advance to getNumberResponse tag

            JAXBContext jc = JAXBContext.newInstance(FECAESolicitarResponse.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            JAXBElement<FECAESolicitarResponse> je = unmarshaller.unmarshal(xsr, FECAESolicitarResponse.class);

            FECAESolicitarResponse resp = je.getValue();

            FECAEResponse fresp = resp.getFECAESolicitarResult();

            respuestaCAE = new CAE(
                    fresp.getFeDetResp().getFECAEDetResponse().get(0).getCAE(),
                    fresp.getFeDetResp().getFECAEDetResponse().get(0).getCAEFchVto(),
                    ultimoNro + 1);

        } catch (ErrorUltimoNumeroException e) {
            logger.log(java.util.logging.Level.SEVERE, "---### Exception ###---: {0}", e);
        } catch (JAXBException | XMLStreamException | IOException ex) {
            logger.log(java.util.logging.Level.SEVERE, "---### Exception ###---: {0}", ex);
        }
        return respuestaCAE;
    }

    /////////////////////////////////////////////////////////////
    // Metodos privados utilitarios para obtener el CAE
    /////////////////////////////////////////////////////////////
    //
    private TicketAccesoAfip obtenerTicketDeAcceso() {

        TicketAccesoAfip ticket = deboTramitarTicket();

        if (ticket != null) {
            return ticket;
        } else {
            String LoginTicketResponse = null;

            System.setProperty("http.proxyHost", "");
            System.setProperty("http.proxyPort", "80");

            String endpoint = env.getProperty("endpoint");
            String service = env.getProperty("service");
            String dstDN = env.getProperty("dstdn");

            String p12file = env.getProperty("keystore");
            String signer = env.getProperty("keystore-signer");
            String p12pass = env.getProperty("keystore-password");

            // Set proxy system vars
            System.setProperty("http.proxyHost", "");
            System.setProperty("http.proxyPort", "");
            System.setProperty("http.proxyUser", "");
            System.setProperty("http.proxyPassword", "");

            // Set the keystore used by SSL
            System.setProperty("javax.net.ssl.trustStore", env.getProperty("trustStore"));
            System.setProperty("javax.net.ssl.trustStorePassword", env.getProperty("trustStore_password"));

            Long TicketTime = new Long(env.getProperty("TicketTime"));

            // Create LoginTicketRequest_xml_cms
            byte[] LoginTicketRequest_xml_cms = create_cms(p12file, p12pass,
                    signer, dstDN, service, TicketTime);

            // Invoke AFIP wsaa and get LoginTicketResponse
            try {
                LoginTicketResponse = invoke_wsaa(LoginTicketRequest_xml_cms, endpoint);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error al invocar web service de Afip", e);
                logger.log(Level.SEVERE, "---= Exception =---:{0}", e);
            }

            // Get token & sign from LoginTicketResponse
            try {
                Reader tokenReader = new StringReader(LoginTicketResponse);
                Document tokenDoc = new SAXReader(false).read(tokenReader);

                String token = tokenDoc.valueOf("/loginTicketResponse/credentials/token");
                String sign = tokenDoc.valueOf("/loginTicketResponse/credentials/sign");

                ticket = new TicketAccesoAfip(token, sign);

                /**
                 * Guardo los datos del ticket de acceso en un archivo para
                 * revisar si tengo que volver a tramitarlo
                 */
                Properties prop = new Properties();
                OutputStream output = null;
                output = new FileOutputStream(env.getProperty("pathTicketAcceso") + "ticketAcceso.properties");
                prop.setProperty("token", token);
                prop.setProperty("sign", sign);
                prop.setProperty("time", HoraArgentina.getFechaYHoraActualArgentinaString());
                prop.store(output, null);

            } catch (Exception e) {
                logger.log(Level.SEVERE, "---### Exception ###---: {0}", e);
            }
            return ticket;
        }
    }

    private String invoke_wsaa(byte[] LoginTicketRequest_xml_cms, String endpoint) throws Exception {

        String LoginTicketResponse = null;
        try {

            Service service = new Service();
            Call call = (Call) service.createCall();

            //
            // Prepare the call for the Web service
            //
            call.setTargetEndpointAddress(new java.net.URL(endpoint));
            call.setOperationName("loginCms");
            call.addParameter("request", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.XSD_STRING);

            //
            // Make the actual call and assign the answer to a String
            //
            LoginTicketResponse = (String) call.invoke(new Object[]{
                Base64.encode(LoginTicketRequest_xml_cms)});

        } catch (Exception e) {
            logger.log(Level.SEVERE, "---### Exception ###---: {0}", e);
        }
        return (LoginTicketResponse);
    }

    //
    // Create the CMS Message
    //
    private byte[] create_cms(String p12file, String p12pass, String signer, String dstDN, String service, Long TicketTime) {

        PrivateKey pKey = null;
        X509Certificate pCertificate = null;
        byte[] asn1_cms = null;
        CertStore cstore = null;
        String LoginTicketRequest_xml;
        String SignerDN = null;

        ArrayList<X509Certificate> certList = null;

        //
        // Manage Keys & Certificates
        //
        try {
            // Create a keystore using keys from the pkcs#12 p12file
            KeyStore ks = KeyStore.getInstance("pkcs12");
            InputStream p12stream = getClass().getResourceAsStream(p12file);
            ks.load(p12stream, p12pass.toCharArray());
            p12stream.close();

            // Get Certificate & Private key from KeyStore
            pKey = (PrivateKey) ks.getKey(signer, p12pass.toCharArray());
            pCertificate = (X509Certificate) ks.getCertificate(signer);
            SignerDN = pCertificate.getSubjectDN().toString();

            // Create a list of Certificates to include in the final CMS
            certList = new ArrayList<X509Certificate>();
            certList.add(pCertificate);

            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }

            cstore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), "BC");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "---### Exception ###---: {0}", e);
        }

        //
        // Create XML Message
        // 
        LoginTicketRequest_xml = create_LoginTicketRequest(SignerDN, dstDN, service, TicketTime);

        //
        // Create CMS Message
        //
        try {
            // Create a new empty CMS Message
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            // Add a Signer to the Message
            gen.addSigner(pKey, pCertificate, CMSSignedDataGenerator.DIGEST_SHA1);

            // Add the Certificate to the Message
            gen.addCertificatesAndCRLs(cstore);

            // Add the data (XML) to the Message
            CMSProcessable data = new CMSProcessableByteArray(LoginTicketRequest_xml.getBytes());

            // Add a Sign of the Data to the Message
            CMSSignedData signed = gen.generate(data, true, "BC");

            asn1_cms = signed.getEncoded();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "---### Exception ###---: {0}", e);
        }

        return (asn1_cms);
    }

    private int obtenerUltimoNroComprobante(TicketAccesoAfip ta) throws ErrorUltimoNumeroException {

        int ultComprobante = -1;
        try {
            String soapXml = generarXMLUltimoComprobante(ta);
            URL url = new URL("https://wswhomo.afip.gov.ar/wsfev1/service.asmx");
            URLConnection conn = url.openConnection();

            // Set the necessary header fields
            conn.setRequestProperty("SOAPAction", "http://ar.gov.afip.dif.FEV1/FECompUltimoAutorizado");
            conn.setRequestProperty("Content-type", "application/soap+xml;charset=UTF-8;action=\"http://ar.gov.afip.dif.FEV1/FECompUltimoAutorizado");
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setDoOutput(true);
            // Send the request
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(soapXml);
            wr.flush();
            // Read the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            line = rd.readLine();

            XMLInputFactory xif = XMLInputFactory.newFactory();
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(line));
            xsr.nextTag();
            while (!xsr.getLocalName().equals("FECompUltimoAutorizadoResponse")) {
                xsr.nextTag();
            }

            JAXBContext jc = JAXBContext.newInstance(FECompUltimoAutorizadoResponse.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            JAXBElement<FECompUltimoAutorizadoResponse> je = unmarshaller.unmarshal(xsr, FECompUltimoAutorizadoResponse.class);

            FECompUltimoAutorizadoResponse resp = je.getValue();

            FERecuperaLastCbteResponse fresp = resp.getFECompUltimoAutorizadoResult();

            ultComprobante = fresp.getCbteNro();
        } catch (Exception e) {
            throw new ErrorUltimoNumeroException(e.getStackTrace().toString());
        }
        return ultComprobante;
    }

    /**
     * Metodo auxiliar que verifica si el Ticket de Acceso todavia esta vigente
     * o si se debe gestionar uno nuevo.
     *
     * @return
     */
    private TicketAccesoAfip deboTramitarTicket() {
        try {
            Properties prop = new Properties();
            InputStream input = null;
            input = new FileInputStream(env.getProperty("pathTicketAcceso") + "ticketAcceso.properties");
            // load a properties file
            prop.load(input);

            String time = prop.getProperty("time");
            DateTimeFormatter formatterConHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime fecha = LocalDateTime.parse(time, formatterConHora);
            if (HoraArgentina.getFechaYHoraActualArgentina().isAfter(fecha.plusHours(12))) {
                logger.info("Debo tramitar nuevamente el Ticket de acceso por estar vencido");
                return null;
            } else {
                TicketAccesoAfip ticket = new TicketAccesoAfip(
                        prop.getProperty("token"),
                        prop.getProperty("sign"));
                logger.info("El ticket de acceso aún es válido");
                return ticket;
            }
        } catch (IOException ex) {
            logger.info("Debo tramitar nuevamente el Ticket de acceso porque no hay archivo");
            return null;
        }
    }

    // ------------------------------------------------------------------------------------
    // Metodos para obtener los Strings de los XML correspondientes
    // ------------------------------------------------------------------------------------
    private String create_LoginTicketRequest(String SignerDN, String dstDN, String service, Long TicketTime) {

        String LoginTicketRequest_xml;

        Date GenTime = new Date();
        GregorianCalendar gentime = new GregorianCalendar();
        GregorianCalendar exptime = new GregorianCalendar();
        String UniqueId = new Long(GenTime.getTime() / 1000).toString();

        exptime.setTime(new Date(GenTime.getTime() + TicketTime));

        XMLGregorianCalendarImpl XMLGenTime = new XMLGregorianCalendarImpl(gentime);
        XMLGregorianCalendarImpl XMLExpTime = new XMLGregorianCalendarImpl(exptime);

        LoginTicketRequest_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<loginTicketRequest version=\"1.0\">"
                + "<header>"
                + "<source>" + SignerDN + "</source>"
                + "<destination>" + dstDN + "</destination>"
                + "<uniqueId>" + UniqueId + "</uniqueId>"
                + "<generationTime>" + XMLGenTime + "</generationTime>"
                + "<expirationTime>" + XMLExpTime + "</expirationTime>"
                + "</header>"
                + "<service>" + service + "</service>"
                + "</loginTicketRequest>";

        return (LoginTicketRequest_xml);
    }

    private String generarXMLUltimoComprobante(TicketAccesoAfip ta) throws JAXBException {
        FECompUltimoAutorizado req = new FECompUltimoAutorizado();
        FEAuthRequest auth = new FEAuthRequest();
        // Configuracion de autorizacion
        auth.setCuit(20334428878l);
        auth.setToken(ta.getToken());
        auth.setSign(ta.getSign());
        req.setAuth(auth);

        req.setCbteTipo(6);
        req.setPtoVta(12);

        JAXBContext jaxbContext = JAXBContext.newInstance(FECompUltimoAutorizado.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(req, sw);
        String xmlString = sw.toString();

        String nuevoEncabezado = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:ar=\"http://ar.gov.afip.dif.FEV1/\"><soap:Header/><soap:Body>";
        String exEncabezado = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        String pedido = xmlString.replace(exEncabezado, nuevoEncabezado);

        return pedido + "</soap:Body></soap:Envelope>";
    }

    private String generarXMLPedidoCAE(TicketAccesoAfip ta, int ultCompro, DatosFacturacion df)
            throws PropertyException, JAXBException {
        FECAESolicitar req = new FECAESolicitar();
        FECAERequest feCAEReq = new FECAERequest();
        FEAuthRequest auth = new FEAuthRequest();

        FECAECabRequest cabReq = new FECAECabRequest();

        ArrayOfFECAEDetRequest array = new ArrayOfFECAEDetRequest();

        FECAEDetRequest detail1 = new FECAEDetRequest();

        // Configuracion de autorizacion
        auth.setCuit(20334428878l);
        auth.setToken(ta.getToken());
        auth.setSign(ta.getSign());
        req.setAuth(auth);

        // Configuracion de cabecera de pedido
        cabReq.setCbteTipo(6); // Factura B
        cabReq.setPtoVta(12); // Nro de Punto de Venta
        cabReq.setCantReg(1); // 1 solo registro de factura
        feCAEReq.setFeCabReq(cabReq);

        // Configuracion de un item de detalle
        detail1.setConcepto(2); // Servicios
        if (df.getUsuario().getCuil() < 10000000000l) {
            detail1.setDocTipo(99); // DNI
            detail1.setDocNro(0l);
        } else {
            detail1.setDocTipo(86); // CUIL
            detail1.setDocNro(df.getUsuario().getCuil()); // Nro. de CUIL
        }
        detail1.setCbteDesde(ultCompro + 1); // Proximo numero disponible
        detail1.setCbteHasta(ultCompro + 1);
        detail1.setImpTotal(df.getImporteTotal()); // Importe Total de la Fact
        detail1.setImpTotConc(0);
        detail1.setImpNeto(df.getImporteServicio()); // Importe neto de los nexos
        detail1.setImpOpEx(0);
        detail1.setImpIVA(df.getImporteIva()); // Iva por el nexo
        detail1.setImpTrib(0);
        detail1.setMonId("PES"); // Pesos Argentinos
        detail1.setMonCotiz(1); // Cuando son pesos argentinos va 1
        detail1.setCbteFch(HoraArgentina.getFechaActualArgentinaParaFactura());
        detail1.setFchServDesde(df.getFechaPrimera());
        detail1.setFchServHasta(df.getFechaUltima());
        detail1.setFchVtoPago(HoraArgentina.getFechaVencimientoArgentinaParaWSAfip());
        ArrayOfAlicIva listIva = new ArrayOfAlicIva();
        AlicIva iva = new AlicIva();
        iva.setBaseImp(df.getImporteServicio()); // Base imponible sobre el que se calcula
        iva.setId(5);
        iva.setImporte(df.getImporteIva()); // Importe total de IVA calculado
        listIva.getAlicIva().add(iva);
        detail1.setIva(listIva);

        array.getFECAEDetRequest().add(detail1);
        feCAEReq.setFeDetReq(array);

        req.setFeCAEReq(feCAEReq);

        JAXBContext jaxbContext = JAXBContext.newInstance(FECAESolicitar.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(req, sw);
//        jaxbMarshaller.marshal(req, System.out);
        String xmlString = sw.toString();

        String nuevoEncabezado = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:ar=\"http://ar.gov.afip.dif.FEV1/\"><soap:Header/><soap:Body>";
        String exEncabezado = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        String pedido = xmlString.replace(exEncabezado, nuevoEncabezado);

        return pedido + "</soap:Body></soap:Envelope>";
    }
}
