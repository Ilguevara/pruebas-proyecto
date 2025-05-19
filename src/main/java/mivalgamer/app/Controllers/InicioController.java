package mivalgamer.app.Controllers;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import mivalgamer.app.MivalGamerInterfaz;

public class InicioController {

    @FXML
    private void irABiblioteca(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Biblioteca.fxml"));
            Parent bibliotecaView = loader.load();
            MivalGamerInterfaz.changeView(bibliotecaView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de Biblioteca: " + e.getMessage());
        }
    }

    @FXML
    private void irAPlataformas(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Main.fxml"));
            Parent plataformasView = loader.load();
            MivalGamerInterfaz.changeView(plataformasView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de Plataformas: " + e.getMessage());
        }
    }

    @FXML
    private void irAInicio(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Inicio.fxml"));
            Parent inicioView = loader.load();
            MivalGamerInterfaz.changeView(inicioView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de Inicio: " + e.getMessage());
        }
    }
}