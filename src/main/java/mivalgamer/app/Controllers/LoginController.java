package mivalgamer.app.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;

import mivalgamer.app.Autentificacion;
import mivalgamer.app.Usuario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel; // Label para mensajes de error en la interfaz

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
            autentificacion = null;
        }
    }

    @FXML
    private void initialize() {
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
                // Inicio de sesión exitoso: redirigir a Inicio.fxml
                try {
                    Parent inicioView = FXMLLoader.load(getClass().getResource("/Views/Inicio.fxml"));
                    Scene scene = new Scene(inicioView);
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    mostrarError("No se pudo cargar la vista de Inicio.");
                }
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