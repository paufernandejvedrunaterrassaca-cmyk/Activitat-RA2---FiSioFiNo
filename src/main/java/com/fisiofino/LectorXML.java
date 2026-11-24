package com.fisiofino;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fisiofino.model.MalaltiaPacient;
import com.fisiofino.model.Pacient;

/**
 * Llegeix un XML amb DOM i en mostra el contingut sense conèixer-ne l'estructura.
 *
 * Important: amb DOM, builder.parse() llegeix TOT el fitxer i construeix l'arbre
 * sencer en memòria. Només després el recorrem i el mostrem.
 */
public class LectorXML {
    static Pacient p;

    static MalaltiaPacient mp;

    static String dniPacient;   // dni del pacient actual, per lligar-lo a cada malaltia

    // Peces del tractament actual (es reinicien en acabar cada <treatment>)
    static String medicacio = "";
    static String dosi = "";
    static String unitat = "";
    static String frequencia = "";

    public static List<Pacient> llegirPacients(Path entradaXML) throws IOException, ParserConfigurationException, SAXException {
            Document doc = obtenirDocument(entradaXML);
            Element arrel = doc.getDocumentElement();
            List<Pacient> pacients = new ArrayList<>();

            // Comencem el recorregut recursiu per l'arrel
            recorrer(arrel, pacients, new ArrayList<>());
            return pacients;
    }

    public static List<MalaltiaPacient> llegirMalalties(Path entradaXML) throws IOException, ParserConfigurationException, SAXException {
            Document doc = obtenirDocument(entradaXML);
            Element arrel = doc.getDocumentElement();
            List<MalaltiaPacient> malalties = new ArrayList<>();

            // Comencem el recorregut recursiu per l'arrel
            recorrer(arrel, new ArrayList<>(), malalties);
            return malalties;
    }

    /**
     * Carrega el fitxer XML (Path de NIO) i retorna l'arbre DOM.
     * parse() no accepta un Path, per això li passem un InputStream de NIO.
     */
    public static Document obtenirDocument(Path entradaXML)
            throws IOException, ParserConfigurationException, SAXException {

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        try (InputStream is = Files.newInputStream(entradaXML)) {
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();   // fusiona nodes de text adjacents
            return doc;
        }
    }

    /**
     * Recorre recursivament l'arbre a partir d'un node.
     *  - Cas base 1: el node no és un element (text de la indentació) -> no fem res.
     *  - Cas base 2: element fulla (només conté text) -> el mostrem i tornem.
     *  - Cas recursiu: element pare -> el mostrem i explorem els seus fills.
     *
     * node:   node que estem visitant
     */
    public static void recorrer(Node node, List<Pacient> pacients, List<MalaltiaPacient> malPacients) {

        // Cas base 1: només tractem elements; els nodes de text de la indentació s'ignoren
        if (node.getNodeType() != Node.ELEMENT_NODE) return;

        String tag = node.getNodeName();

        // Afegim els atributs de l'element (p. ex. id), sigui quin sigui el seu nom
        NamedNodeMap atributs = node.getAttributes();
        for (int i = 0; i < atributs.getLength(); i++) {
            Node at = atributs.item(i);
            String nomAt = at.getNodeName();
            String valor = at.getNodeValue();

            if (p != null) {
                if (tag.equals("telecom") && nomAt.equals("value")) {
                    if (valor.startsWith("tel:")) {
                        p.setTelephone(valor.substring(4));
                    }
                    if (valor.startsWith("mailto:")) {
                        p.setEmail(valor.substring(7));
                    }
                }
                if (tag.equals("birthTime") && nomAt.equals("value")) {
                    // 19850314 o 1985-03-14 -> LocalDate
                    String digits = valor.replaceAll("[^0-9]", "");
                    p.setBirthDate(LocalDate.parse(digits.substring(0, 8), DateTimeFormatter.BASIC_ISO_DATE));
                }
                if (tag.equals("administrativeGenderCode") && nomAt.equals("code")) {
                    p.setGender(valor);
                }
            }

            if (mp != null) {
                if (tag.equals("code") && nomAt.equals("code")) {
                    mp.setCode(valor);
                }
                // Dosi com a atributs: <dose value="10" unit="mg"/>
                if (tag.equals("dose") && nomAt.equals("value")) {
                    dosi = valor.trim();
                }
                if (tag.equals("dose") && nomAt.equals("unit")) {
                    unitat = valor.trim();
                }
            }
        }

        // Cas base 2: element fulla (nom, cognoms...) -> mostrem nom i text a la mateixa línia
        if (esFulla(node)) {
            String text = node.getTextContent().trim();

            if (p != null) {
                switch (tag) {
                    case "given" -> p.setGivenName(text);
                    case "family" -> p.setFamilyName(text);
                    case "dni" -> {
                        p.setDni(text);
                        dniPacient = text;
                    }
                    case "streetAddressLine" -> p.setAddress(text);
                    case "city" -> p.setCity(text);
                    case "postalCode" -> p.setPostalCode(text);
                    case "country" -> p.setCountry(text);
                    default -> { }
                }
            }

            if (mp != null) {
                switch (tag) {
                    case "code" -> mp.setName(text);
                    case "status" -> mp.setStatus(text);
                    case "medication" -> medicacio = text;
                    case "dose" -> {
                        if (!text.isEmpty()) dosi = text;   // si ve com a text; si no, ja ve dels atributs
                    }
                    case "frequency" -> frequencia = text;
                    default -> { }
                }
            }
            return;
        }

        // Element pare significa que creem un nou pacient
        if (tag.equals("recordTarget")) {
            p = new Pacient();
        }

        if (tag.equals("problem")) {
            mp = new MalaltiaPacient();
            mp.setDniPacient(dniPacient);   // la informació del pacient <recordTarget> apareix abans dels <problem>
        }

        // Cas recursiu: explorem els fills un nivell més avall
        NodeList fills = node.getChildNodes();
        for (int i = 0; i < fills.getLength(); i++) {
            recorrer(fills.item(i), pacients, malPacients);
        }

        // Tractament: "medicació dosi, freqüència"; si n'hi ha diversos, se separen amb "; "
        if (mp != null && tag.equals("treatment")) {
            String d = dosi;
            if (!unitat.isEmpty() && !d.endsWith(unitat)) {
                d = d + " " + unitat;
            }
            String t = medicacio + " " + d + ", " + frequencia;
            if (mp.getTreatment() == null) {
                mp.setTreatment(t);
            } else {
                mp.setTreatment(mp.getTreatment() + "; " + t);
            }
            medicacio = "";
            dosi = "";
            unitat = "";
            frequencia = "";
        }

        if (tag.equals("problem")) {
            malPacients.add(mp);
            mp = null;
        }
        if (tag.equals("recordTarget")) {
            pacients.add(p);
            p = null;
        }
    }

    /**
     * Un element és fulla si no té cap fill de tipus element (només text, o res).
     * Atenció: un element buit també es considera fulla.
     */
    static boolean esFulla(Node node) {
        NodeList fills = node.getChildNodes();
        for (int i = 0; i < fills.getLength(); i++) {
            if (fills.item(i).getNodeType() == Node.ELEMENT_NODE) return false;
        }
        return true;
    }
}