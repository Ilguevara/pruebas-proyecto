package mivalgamer.app.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.layout.Background;
import mivalgamer.app.MivalGamerInterfaz;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class MainController implements Initializable {

    @FXML
    private StackPane playstationCard;
    @FXML
    private StackPane xboxCard;
    @FXML
    private StackPane nintendoCard;
    @FXML
    private StackPane pcCard;
    @FXML
    private VBox cartItems;
    @FXML
    private Label totalAmount;
    @FXML
    private Button checkoutButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando MivalGamer...");
        setupHoverEffects();

        // Configurar transparencia del panel del carrito
        Platform.runLater(() -> {
            if (cartItems != null && cartItems.getParent() != null && cartItems.getParent().getParent() != null) {
                Region cartSection = (Region) cartItems.getParent().getParent();
                cartSection.setBackground(Background.EMPTY);
                cartSection.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15);");
            }
        });
    }

    private void setupHoverEffects() {
        if (playstationCard != null) setupCardHover(playstationCard);
        if (xboxCard != null) setupCardHover(xboxCard);
        if (nintendoCard != null) setupCardHover(nintendoCard);
        if (pcCard != null) setupCardHover(pcCard);
    }

    private void setupCardHover(StackPane card) {
        card.setOnMouseEntered(event -> animateCard(card, 1.05));
        card.setOnMouseExited(event -> animateCard(card, 1.0));
    }

    private void animateCard(StackPane card, double scale) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
        scaleTransition.setToX(scale);
        scaleTransition.setToY(scale);
        scaleTransition.play();
    }

    @FXML
    private void handlePlatformClick(MouseEvent event) {
        StackPane source = (StackPane) event.getSource();
        String platformId = source.getId();
        System.out.println("Plataforma seleccionada: " + platformId);

        switch (platformId) {
            case "playstationCard":
                navigateToPlayStation();
                break;
            case "xboxCard":
                navigateToXbox();
                break;
            case "nintendoCard":
                navigateToNintendo();
                break;
            case "pcCard":
                navigateToPc();
                break;
            default:
                System.out.println("Navegación a " + platformId + " aún no implementada");
        }
    }

    private void navigateToPc() {
        try {
            System.out.println("Intentando navegar a PC...");
            MivalGamerInterfaz.changeView("/com/example/mivalgamer/Pc.fxml");
            System.out.println("Navegación a PC completada");
        } catch (Exception e) {
            System.err.println("Error al cargar la vista de PC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToNintendo() {
        try {
            System.out.println("Intentando navegar a Nintendo...");
            MivalGamerInterfaz.changeView("/com/example/mivalgamer/Nintendo.fxml");
            System.out.println("Navegación a Nintendo completada");
        } catch (Exception e) {
            System.err.println("Error al cargar la vista de Nintendo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToXbox() {
        try {
            System.out.println("Intentando navegar a Xbox...");
            MivalGamerInterfaz.changeView("/com/example/mivalgamer/Xbox.fxml");
            System.out.println("Navegación a Xbox completada");
        } catch (Exception e) {
            System.err.println("Error al cargar la vista de Xbox: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToPlayStation() {
        try {
            System.out.println("Intentando navegar a PlayStation...");
            MivalGamerInterfaz.changeView("/com/example/mivalgamer/PlayStation.fxml");
            System.out.println("Navegación a PlayStation completada");
        } catch (Exception e) {
            System.err.println("Error al cargar la vista de PlayStation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Métodos para el carrito de compras
    public void addToCart(String gameName, double price) {
        if (cartItems == null) {
            System.err.println("Error: cartItems es null");
            return;
        }

        HBox cartItem = new HBox();
        cartItem.setSpacing(10);
        cartItem.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(gameName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Museo Sans';");

        Label priceLabel = new Label(String.format("$%.2f", price));
        priceLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Museo Sans'; -fx-font-weight: bold;");

        Button removeButton = new Button("×");
        removeButton.setStyle("-fx-text-fill: white; -fx-background-color: transparent; -fx-cursor: hand;");
        removeButton.setOnAction(e -> removeFromCart(cartItem, price));

        cartItem.getChildren().addAll(nameLabel, new Region(), priceLabel, removeButton);
        cartItems.getChildren().add(cartItem);

        updateTotal(price);
    }

    private void removeFromCart(HBox item, double price) {
        if (cartItems != null) {
            cartItems.getChildren().remove(item);
            updateTotal(-price);
        }
    }

    private void updateTotal(double amount) {
        if (totalAmount != null) {
            String currentText = totalAmount.getText();
            double currentTotal = 0.0;

            if (currentText != null && currentText.startsWith("$")) {
                try {
                    currentTotal = Double.parseDouble(currentText.substring(1));
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear el total: " + e.getMessage());
                }
            }

            double newTotal = currentTotal + amount;
            totalAmount.setText(String.format("$%.2f", newTotal));
        }
    }

    @FXML
    private void handleCheckout() {
        System.out.println("Procesando pago...");
        // Aquí puedes agregar la lógica para procesar el pago
    }

    @FXML
    private void handleUserButton(ActionEvent event) {
        System.out.println("Botón de usuario clickeado");
        // Aquí puedes agregar la lógica para el menú de usuario
    }

    @FXML
    public void handleBackButton(MouseEvent event) {
        try {
            System.out.println("Volviendo a la vista principal...");
            MivalGamerInterfaz.changeView("/com/example/mivalgamer/Main.fxml");
            System.out.println("Regreso a la vista principal completado");
        } catch (Exception e) {
            System.err.println("Error al volver a la vista principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBibliotecaClick(MouseEvent event) {
        try {
            // Cargar la vista de Biblioteca
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Biblioteca.fxml"));
            Parent bibliotecaView = loader.load();

            // Cambiar la vista actual por la vista de Biblioteca
            mivalgamer.app.MivalGamerInterfaz.changeView(bibliotecaView);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de Biblioteca: " + e.getMessage());
        }
    }
}
