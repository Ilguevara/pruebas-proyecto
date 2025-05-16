package mivalgamer.app.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import mivalgamer.app.Autentificacion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField nombreField;
    @FXML private TextField apellidoField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private Autentificacion autentificacion;

    // Inicializa la conexión y Autentificacion (puedes adaptar para usar un singleton/pool)
    public RegisterController() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://bkgshsddffwp1gdiqqcl-mysql.services.clever-cloud.com/bkgshsddffwp1gdiqqcl", "uzjpyykc41cm3273", "oq4aVRScMPmmRQS6SciH");
            autentificacion = new Autentificacion(conn);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error de conexión", e.getMessage());
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String nombre = nombreField.getText().trim();
        String apellido = apellidoField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos vacíos", "Completa todos los campos para registrarte.");
            return;
        }

        try {
            autentificacion.registrarUsuario(nombre + " " + apellido, email, password);
            showAlert(Alert.AlertType.INFORMATION, "Registro exitoso", "¡Te has registrado correctamente!");

            // Redirige a la pantalla de login
            goToLogin();
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.ERROR, "Registro fallido", ex.getMessage());
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Error SQL", ex.getMessage());
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nombreField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navegación", "No se pudo abrir la pantalla de inicio de sesión.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}