package mivalgamer.app.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mivalgamer.app.Videojuego;
import mivalgamer.app.Plataforma;
import mivalgamer.app.ConexionBaseDatos; // Cambiado de ConexionBD a ConexionBaseDatos

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PlataformasController {
    private static final Logger LOGGER = Logger.getLogger(PlataformasController.class.getName());

    @FXML
    private FlowPane gameCardsContainer;

    @FXML
    private Label lblNombrePlataforma;

    private Plataforma plataformaActual;

    @FXML
    public void initialize() {
        seleccionarPlataforma(1L); // Ejemplo: PlayStation ID 1
    }

    public void seleccionarPlataforma(long idPlataforma) {
        ConexionBaseDatos cbd = new ConexionBaseDatos();
        Connection conn = null;
        try {
            conn = cbd.conectar();
            if (conn == null) {
                LOGGER.log(Level.SEVERE, "No se pudo obtener la conexión a la base de datos para seleccionar plataforma.");
                if (lblNombrePlataforma != null) {
                    lblNombrePlataforma.setText("Error de Conexión");
                }
                if (gameCardsContainer != null) gameCardsContainer.getChildren().clear();
                return;
            }

            this.plataformaActual = Plataforma.obtenerPorId(conn, idPlataforma);

            if (this.plataformaActual != null) {
                if (lblNombrePlataforma != null) {
                    lblNombrePlataforma.setText(this.plataformaActual.getNombreComercial());
                }
                cargarJuegosPorPlataforma(this.plataformaActual.getIdPlataforma()); // Carga juegos después de obtener la plataforma
            } else {
                LOGGER.log(Level.WARNING, "No se encontró la plataforma con ID: " + idPlataforma);
                if (lblNombrePlataforma != null) {
                    lblNombrePlataforma.setText("Plataforma no encontrada");
                }
                if (gameCardsContainer != null) gameCardsContainer.getChildren().clear();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de SQL al obtener la plataforma ID " + idPlataforma, e);
            if (lblNombrePlataforma != null) {
                lblNombrePlataforma.setText("Error al cargar plataforma");
            }
            if (gameCardsContainer != null) gameCardsContainer.getChildren().clear();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al seleccionar plataforma: " + idPlataforma, e);
            if (lblNombrePlataforma != null) {
                lblNombrePlataforma.setText("Error inesperado");
            }
            if (gameCardsContainer != null) gameCardsContainer.getChildren().clear();
        } finally {
            // Se cierra la conexión aquí porque cargarJuegosPorPlataforma abrirá la suya propia.
            // Si cargajuegos usara la misma 'conn', el cierre iría después de llamarlo.
            cbd.cerrarConexion();
        }
    }

    public void cargarJuegosPorPlataforma(long idPlataforma) {
        if (gameCardsContainer == null) {
            LOGGER.log(Level.SEVERE, "gameCardsContainer no ha sido inicializado.");
            return;
        }
        gameCardsContainer.getChildren().clear();
        List<Videojuego> videojuegos = new ArrayList<>();
        ConexionBaseDatos cbd = new ConexionBaseDatos();
        Connection conn = null;

        try {
            conn = cbd.conectar();
            if (conn == null) {
                LOGGER.log(Level.SEVERE, "No se pudo obtener la conexión a la base de datos para cargar juegos.");
                // Opcional: Mostrar mensaje en la UI
                Label errorLabel = new Label("Error al conectar con la base de datos.");
                errorLabel.setStyle("-fx-text-fill: red;");
                gameCardsContainer.getChildren().add(errorLabel);
                return;
            }
            videojuegos = Videojuego.obtenerPorPlataforma(conn, idPlataforma);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al obtener videojuegos para la plataforma ID " + idPlataforma, e);
            Label errorLabel = new Label("Error al cargar juegos desde la base de datos.");
            errorLabel.setStyle("-fx-text-fill: red;");
            gameCardsContainer.getChildren().add(errorLabel);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al cargar juegos para plataforma ID " + idPlataforma, e);
            Label errorLabel = new Label("Error inesperado al cargar juegos.");
            errorLabel.setStyle("-fx-text-fill: red;");
            gameCardsContainer.getChildren().add(errorLabel);
        } finally {
            cbd.cerrarConexion();
        }

        if (videojuegos.isEmpty() && gameCardsContainer.getChildren().isEmpty()) { // Solo muestra si no hay ya un error
            LOGGER.log(Level.INFO, "No se encontraron videojuegos para la plataforma ID: " + idPlataforma);
            Label noHayJuegos = new Label("No hay juegos disponibles para esta plataforma.");
            noHayJuegos.setStyle("-fx-text-fill: white; -fx-font-size: 1.2em;");
            gameCardsContainer.getChildren().add(noHayJuegos);
            return;
        }

        for (Videojuego juego : videojuegos) {
            try {
                URL fxmlUrl = getClass().getResource("/mivalgamer/app/GameCard.fxml");
                if (fxmlUrl == null) {
                    LOGGER.log(Level.SEVERE, "No se pudo encontrar GameCard.fxml. Verifica la ruta: /mivalgamer/app/GameCard.fxml");
                    continue;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                VBox gameCardNode = loader.load();
                GameCardController cardController = loader.getController();

                if (cardController == null) {
                    LOGGER.log(Level.SEVERE, "El controlador GameCardController no se pudo obtener para " + juego.getTitulo());
                    continue;
                }
                cardController.setData(juego);
                gameCardsContainer.getChildren().add(gameCardNode);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error al cargar GameCard.fxml para el juego '" + juego.getTitulo() + "'", e);
            } catch (IllegalStateException e) {
                LOGGER.log(Level.SEVERE, "Error de estado al cargar FXML para " + juego.getTitulo(), e);
            }
        }
    }

    @FXML
    private void handleBackButton() {
        LOGGER.log(Level.INFO, "Botón Volver presionado.");
        // Lógica de navegación
    }

    @FXML
    private void handleInicio() {
        LOGGER.log(Level.INFO, "Botón Inicio presionado.");
        // Lógica de navegación
    }

    @FXML
    private void handlePlataformas() {
        LOGGER.log(Level.INFO, "Botón Plataformas presionado.");
        // Lógica de navegación o recarga
    }

    @FXML
    private void handleBiblioteca() {
        LOGGER.log(Level.INFO, "Botón Biblioteca presionado.");
        // Lógica de navegación
    }
}