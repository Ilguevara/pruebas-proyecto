package mivalgamer.app.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import mivalgamer.app.Autentificacion;
import mivalgamer.app.Usuario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel; // Nuevo: Label para mensajes de error en la interfaz

    private Autentificacion autentificacion;

    public LoginController() {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://bkgshsddffwp1gdiqqcl-mysql.services.clever-cloud.com/bkgshsddffwp1gdiqqcl",
                    "uzjpyykc41cm3273",
                    "oq4aVRScMPmmRQS6SciH"
            );
            autentificacion = new Autentificacion(conn);
        } catch (SQLException e) {
            // Si ocurre un error de conexión, podría informarse de alguna otra manera en la interfaz
            autentificacion = null;
        }
    }

    @FXML
    private void initialize() {
        // Asegúrate de ocultar el label de error al iniciar
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Escribe tu correo y contraseña para ingresar.");
            return;
        }

        if (autentificacion == null) {
            mostrarError("No se pudo conectar con el servidor. Intenta más tarde.");
            return;
        }

        try {
            Usuario usuario = autentificacion.iniciarSesion(email, password);
            if (usuario != null) {
                // Aquí podrías redirigir a la pantalla principal
                // Por ejemplo: irAMenuPrincipal(usuario);
                // Limpiar mensaje de error si lo hubiera
                mostrarError("");   // o errorLabel.setVisible(false);
            } else {
                mostrarError("Correo o contraseña incorrectos.");
            }
        } catch (SQLException ex) {
            mostrarError("Error al conectar con la base de datos.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        goToRegister();
    }

    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            mostrarError("No se pudo abrir la pantalla de registro.");
        }
    }

    private void mostrarError(String mensaje) {
        if (errorLabel != null) {
            errorLabel.setText(mensaje);
            errorLabel.setVisible(mensaje != null && !mensaje.isEmpty());
        }
    }
}