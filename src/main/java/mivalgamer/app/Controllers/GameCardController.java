// Archivo: mivalgamer/app/Controllers/GameCardController.java
package mivalgamer.app.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import mivalgamer.app.Videojuego; // Asegúrate que la clase Videojuego esté en el paquete correcto o importa

public class GameCardController {

    @FXML
    private VBox cardVBox;

    @FXML
    private ImageView imgIcono;

    @FXML
    private ImageView imgPortada;

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblPrecio;

    @FXML
    private Button btnVerDetalles;

    private Videojuego videojuego;

    public void setData(Videojuego videojuego) {
        this.videojuego = videojuego;
        lblTitulo.setText(videojuego.getTitulo());
        lblPrecio.setText(String.format("$%.2f", videojuego.getPrecio())); // Formatea el precio

        // Cargar imagen de icono (tomando la primera URL de la lista)
        if (videojuego.getIcono() != null && !videojuego.getIcono().isEmpty()) {
            String iconoUrl = videojuego.getIcono().get(0);
            if (iconoUrl != null && !iconoUrl.isEmpty()) {
                try {
                    // El 'true' en el constructor de Image habilita la carga en segundo plano
                    imgIcono.setImage(new Image(iconoUrl, true));
                } catch (IllegalArgumentException e) {
                    System.err.println("URL de icono inválida o no se pudo cargar: " + iconoUrl + " - " + e.getMessage());
                    // Opcional: Establecer una imagen por defecto si falla la carga
                    // imgIcono.setImage(new Image(getClass().getResourceAsStream("/ruta/a/tu/icono_placeholder.png")));
                }
            }
        }

        // Cargar imagen de portada (tomando la primera URL de la lista)
        if (videojuego.getPortada() != null && !videojuego.getPortada().isEmpty()) {
            String portadaUrl = videojuego.getPortada().get(0);
            if (portadaUrl != null && !portadaUrl.isEmpty()) {
                try {
                    imgPortada.setImage(new Image(portadaUrl, true));
                } catch (IllegalArgumentException e) {
                    System.err.println("URL de portada inválida o no se pudo cargar: " + portadaUrl + " - " + e.getMessage());
                    // Opcional: Establecer una imagen por defecto
                    // imgPortada.setImage(new Image(getClass().getResourceAsStream("/ruta/a/tu/portada_placeholder.png")));
                }
            }
        }
    }

    @FXML
    private void handleVerDetalles() {
        if (videojuego != null) {
            System.out.println("Ver detalles presionado para: " + videojuego.getTitulo());
            // Aquí puedes implementar la lógica para mostrar más detalles del videojuego
            // Por ejemplo, abrir una nueva ventana, cambiar a otra vista, etc.
        }
    }

    // Este método no es estrictamente necesario si añades el nodo directamente desde el loader,
    // pero puede ser útil si necesitas referenciar el VBox raíz desde fuera.
    public VBox getCardView() {
        return cardVBox;
    }
}