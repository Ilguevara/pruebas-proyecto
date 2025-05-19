package mivalgamer.app.Controllers;


import mivalgamer.app.MivalGamerInterfaz;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PlataformasController implements Initializable {

    @FXML
    private List<VBox> gameCards;
    @FXML
    private VBox cartItems;
    @FXML
    private Label totalAmount;

    private List<GameItem> cartList = new ArrayList<>();



    private static class GameItem {
        private final String title;
        private final double price;
        private HBox cartItemView;

        public GameItem(String title, double price) {
            this.title = title;
            this.price = price;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (gameCards != null) {
            for (VBox gameCard : gameCards) {
                Button addButton = findAddButton(gameCard);
                if (addButton != null) {
                    addButton.setOnAction(this::handleAddToCartButton);
                }
            }
        }
    }

    @FXML
    public void handleAddToCartButton(ActionEvent event) {
        Button button = (Button) event.getSource();
        VBox gameCard = (VBox) button.getParent().getParent();
        Label titleLabel = findLabelInHierarchy(gameCard, "game-title");
        Label priceLabel = findLabelInHierarchy(gameCard, "game-price");

        if (titleLabel != null && priceLabel != null) {
            String title = titleLabel.getText();
            try {
                double price = Double.parseDouble(priceLabel.getText().replace(" COP", ""));
                System.out.println("Agregando al carrito: " + title + " - " + price);
                addToCart(title, price);
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear el precio: " + priceLabel.getText());
            }
        } else {
            System.err.println("No se encontró título o precio en el gameCard");
        }
    }

    private Button findAddButton(Node node) {
        if (node instanceof Button) {
            Button button = (Button) node;
            if (button.getText().equals("+")) {
                return button;
            }
        }

        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                Button found = findAddButton(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private Label findLabelInHierarchy(Node node, String styleClass) {
        if (node instanceof Label) {
            Label label = (Label) node;
            if (label.getStyleClass().contains(styleClass)) {
                return label;
            }
        }

        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                Label found = findLabelInHierarchy(child, styleClass);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void addToCart(String title, double price) {
        GameItem gameItem = new GameItem(title, price);
        HBox cartItemView = createCartItemView(gameItem);
        gameItem.cartItemView = cartItemView;

        cartList.add(gameItem);

        if (cartItems.getChildren().size() == 1 &&
                cartItems.getChildren().get(0) instanceof Label &&
                ((Label)cartItems.getChildren().get(0)).getText().contains("no has agregado")) {
            cartItems.getChildren().clear();
        }

        cartItems.getChildren().add(cartItemView);
        updateTotal();
    }

    private HBox createCartItemView(GameItem item) {
        HBox cartItemView = new HBox();
        cartItemView.getStyleClass().add("cart-item");
        cartItemView.setSpacing(10);
        cartItemView.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(item.title);
        titleLabel.getStyleClass().add("cart-item-title");

        Label priceLabel = new Label(String.format("%.2f COP", item.price));
        priceLabel.getStyleClass().add("cart-item-price");

        Button removeButton = new Button("X");
        removeButton.getStyleClass().add("remove-button");
        removeButton.setOnAction(e -> removeFromCart(item));

        cartItemView.getChildren().addAll(titleLabel, priceLabel, removeButton);
        return cartItemView;
    }

    private void removeFromCart(GameItem item) {
        cartList.remove(item);
        cartItems.getChildren().remove(item.cartItemView);

        if (cartList.isEmpty()) {
            Label emptyLabel = new Label("Aun no has agregado juegos a tu carrito");
            emptyLabel.getStyleClass().add("cart-empty");
            cartItems.getChildren().add(emptyLabel);
        }

        updateTotal();
    }

    private void updateTotal() {
        double total = cartList.stream()
                .mapToDouble(item -> item.price)
                .sum();
        totalAmount.setText(String.format("%.2f COP", total));
    }

    @FXML
    public void handleBackButton(MouseEvent event) {
        try {
            System.out.println("Volviendo a la vista principal...");
            MivalGamerInterfaz.changeView("/com/example/mivalgamer/MivalGamer.fxml");
            System.out.println("Regreso a la vista principal completado");
        } catch (Exception e) {
            System.err.println("Error al volver a la vista principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleGameCardClick(MouseEvent mouseEvent) {

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