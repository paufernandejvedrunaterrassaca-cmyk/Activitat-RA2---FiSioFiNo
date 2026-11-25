package com.fisiofino.controlador;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.fisiofino.LectorXML;
import com.fisiofino.database.GestioPacients;
import com.fisiofino.model.MalaltiaPacient;
import com.fisiofino.model.Pacient;
import com.fisiofino.vista.MenuView;

public class Controller {

    private final MenuView vista;
    private final GestioPacients gestiopacients;

    public Controller(MenuView vista, GestioPacients gestiopacients) {
        this.vista = vista;
        this.gestiopacients = gestiopacients;
    }

    public void inicia() {
        vista.mostraBenvinguda();
        boolean sortir = false;

        while (!sortir) {
            vista.MenuOpcions();
            try {
                String opcio = vista.demanaOpcio();
                if (opcio == null) break;

                switch (opcio.toUpperCase()) {
                    case "1":
                        carregarXml();
                        break;
                    case "2":
                        consultarInfoPersonal();
                        break;
                    case "3":
                        consultarInfoMedica();
                        break;
                    case "4":
                        consultarTots();
                        break;
                    case "5":
                        modificarEstat();
                        break;
                    case "X": sortir = true;                break;
                    default:  vista.mostraError("Opció no vàlida");
                }
            } catch (IOException e) {
                vista.mostraError("Error de lectura: " + e.getMessage());
                sortir = true;
            } catch (Exception e) {
                vista.mostraError(e.toString());
            }
        }
        
        vista.mostraMissatge("Fins aviat!");
    }

    private void carregarXml() throws Exception {
        Path ruta = Path.of("pacientes_bluebutton.xml");

        for (Pacient pacient : LectorXML.llegirPacients(ruta)) {
            gestiopacients.CreatePatient(pacient);
        }

        for (MalaltiaPacient malaltia : LectorXML.llegirMalalties(ruta)) {
            if (!gestiopacients.findMalaltia(malaltia.getCode())) {
                gestiopacients.CreateMalaltia(malaltia);
            }
            if (!gestiopacients.findMalaltiaPacient(
                    malaltia.getDniPacient(), malaltia.getCode(), malaltia.getStatus())) {
                gestiopacients.CreateMalaltiaPacient(malaltia);
            }
        }
    }

    private void consultarInfoPersonal() throws Exception {
        String dni = vista.demana("DNI/NIE: ");
        Pacient pacient = gestiopacients.findBydniNie(dni);
        if (pacient != null) {
            vista.mostraPacient(pacient);
        }
    }

    private void consultarInfoMedica() throws Exception {
        String dni = vista.demana("DNI/NIE: ");
        List<MalaltiaPacient> malalties = gestiopacients.findDiseaseBydniNie(dni);
        vista.mostraMalaltiesPacient(malalties);
    }

    private void consultarTots() throws Exception {
        vista.mostraPacients(gestiopacients.findAllPacients());
    }

    private void modificarEstat() throws Exception {
        String dni = vista.demana("DNI/NIE: ");
        String codi = vista.demana("Codi SNOMED: ");
        String estat = vista.demana("Nou estat: ");
        Pacient pacient = gestiopacients.findBydniNie(dni);
        if (pacient != null) {
            gestiopacients.updateStatus(pacient.getIdPatient(), codi, estat);
        }
    }
}
