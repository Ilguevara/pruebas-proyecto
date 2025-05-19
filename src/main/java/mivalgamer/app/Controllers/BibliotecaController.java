package mivalgamer.app.Controllers;

import mivalgamer.app.MivalGamerInterfaz;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class BibliotecaController implements Initializable {

    @FXML
    private TextField searchInput;

    @FXML
    private Label totalGamesLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización del controlador
        System.out.println("Inicializando vista de Biblioteca...");
    }

    @FXML
    private void handleGameCardClick(MouseEvent event) {
        if (event.getSource() instanceof VBox) {
            VBox gameCard = (VBox) event.getSource();

            // Obtener el título del juego
            String gameTitle = "";
            for (javafx.scene.Node node : gameCard.getChildren()) {
                if (node instanceof Label && ((Label) node).getStyleClass().contains("game-title")) {
                    gameTitle = ((Label) node).getText();
                    break;
                }
            }

            System.out.println("Juego seleccionado: " + gameTitle);

            // Aquí puedes implementar la navegación a la vista de detalles del juego
            // o cualquier otra acción que desees realizar al hacer clic en un juego
        }
    }

    @FXML
    private void handleSearchAction() {
        String searchTerm = searchInput.getText();
        System.out.println("Buscando: " + searchTerm);

        // Implementar lógica de búsqueda
    }

    @FXML
    private void handleManagePurchasesClick() {
        System.out.println("Administrar compras");

        // Implementar navegación a la vista de administración de compras
    }

    @FXML
    public void handleBackButton(MouseEvent event) {
        try {
            System.out.println("Volviendo a la vista principal...");
            mivalgamer.app.MivalGamerInterfaz.changeView("/Views/Main.fxml");
            System.out.println("Regreso a la vista principal completado");
        } catch (Exception e) {
            System.err.println("Error al volver a la vista principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleInicioButton(MouseEvent event) {
        try {
            System.out.println("Yendo a Inicio...");
            MivalGamerInterfaz.changeView("/Views/Inicio.fxml");
        } catch (Exception e) {
            System.err.println("Error al ir a Inicio: " + e.getMessage());
            e.printStackTrace();
        }
    }
}