package mivalgamer.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;
import java.util.Arrays;

public class MivalGamerInterfaz extends Application {

    private static StackPane mainContainer;
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar fuentes personalizadas
            cargarFuentes("/RecursosGlobales/Tipografia/museo-sans/", Arrays.asList(
                    "MuseoSans_500.otf",
                    "MuseoSans_700.otf",
                    "MuseoSans_900.otf",
                    "MuseoSans-100.otf",
                    "MuseoSans-300.otf"
            ));

            // Cargar pantalla de login como inicial
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"));
            StackPane root = (StackPane) loader.load();

            mainContainer = root; // Asignar el contenedor raíz para manejo dinámico
            MivalGamerInterfaz.primaryStage = primaryStage;

            Scene scene = new Scene(mainContainer, 1280, 720);

            // Aplicar el CSS de la aplicación
            scene.getStylesheets().add(getClass().getResource("/Views/Estilos.css").toExternalForm());

            primaryStage.setTitle("MivalGamer - Acceso");
            primaryStage.setScene(scene);

            // Pantalla completa
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");

            // Establecer tamaño mínimo (opcional)
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Hacer responsive para que ocupe siempre todo el espacio
            root.prefWidthProperty().bind(scene.widthProperty());
            root.prefHeightProperty().bind(scene.heightProperty());

            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Carga varias fuentes desde la carpeta indicada bajo resources **/
    private void cargarFuentes(String carpeta, List<String> archivos) {
        for (String archivo : archivos) {
            try (InputStream is = getClass().getResourceAsStream(carpeta + archivo)) {
                if (is != null) {
                    Font.loadFont(is, 12);
                } else {
                    System.err.println("Fuente no encontrada: " + archivo);
                }
            } catch (Exception e) {
                System.err.println("Error al cargar fuente: " + archivo + " - " + e.getMessage());
            }
        }
    }

    /**
     * Cambia la vista principal por el contenido de otro archivo FXML.
     * @param fxmlPath Ruta del archivo FXML a cargar.
     */
    public static void changeView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MivalGamerInterfaz.class.getResource(fxmlPath));
            Parent newRoot = loader.load();
            if (mainContainer != null) {
                mainContainer.getChildren().setAll(newRoot);
            } else if (primaryStage != null) {
                // fallback: Si mainContainer aún no está asignado, crear una nueva escena
                Scene newScene = new Scene(newRoot, 1280, 720);
                newScene.getStylesheets().add(MivalGamerInterfaz.class.getResource("/Views/Estilos.css").toExternalForm());
                primaryStage.setScene(newScene);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cambiar la vista a: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}