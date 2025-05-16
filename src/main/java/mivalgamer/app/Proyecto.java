package mivalgamer.app;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.InputMismatchException;

public class Proyecto {
    private static final Logger LOGGER = Logger.getLogger(Proyecto.class.getName());
    private static Connection connection;
    static Usuario usuarioActual;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%6$s%n");
        Logger.getLogger("proyecto").setLevel(Level.WARNING);

        try {
            ConexionBaseDatos conexionBD = new ConexionBaseDatos();
            connection = conexionBD.conectar();

            if (connection == null || connection.isClosed()) {
                LOGGER.severe("No se pudo establecer conexion con la base de datos");
                return;
            }

            mostrarMenuPrincipal();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error critico", e);
            System.out.println("Error critico: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error al cerrar conexion", ex);
                }
            }
        }
    }

    private static int leerEntero() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Entrada invalida. Por favor ingrese un numero: ");
                scanner.next();
            }
        }
    }

    private static void mostrarMenuPrincipal() {
        while (true) {
            System.out.println("\n=== MivalGamer ===");
            System.out.println("1. Iniciar sesion");
            System.out.println("2. Registrarse");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opcion: ");

            int opcion = leerEntero();
            scanner.nextLine();
            switch (opcion) {
                case 1 -> iniciarSesion();
                case 2 -> registrarUsuario();
                case 3 -> {
                    System.out.println("Hasta pronto!");
                    return;
                }
                default -> System.out.println("Opcion invalida");
            }
        }
    }

    private static void iniciarSesion() {
        System.out.print("\nEmail: ");
        String email = scanner.nextLine();
        System.out.print("Contrasena: ");
        String password = scanner.nextLine();

        try {
            Autentificacion auth = new Autentificacion(connection);
            usuarioActual = auth.iniciarSesion(email, password);

            if (usuarioActual != null) {
                System.out.println("\nBienvenido, " + usuarioActual.getNombre() + "!");
                mostrarMenuUsuario();
            } else {
                System.out.println("\nCredenciales incorrectas");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de autenticacion", e);
            System.out.println("\nError al iniciar sesion");
        }
    }

    private static void registrarUsuario() {
        if (connection == null) {
            System.out.println("Error: No hay conexion a la base de datos");
            return;
        }

        Autentificacion auth = new Autentificacion(connection);

        System.out.print("\nNombre: ");
        String nombre = scanner.nextLine();

        String email;
        boolean emailValido;
        do {
            System.out.print("Email: ");
            email = scanner.nextLine();
            emailValido = auth.validarFormatoEmail(email);

            if (!emailValido) {
                System.out.println("Error: Formato de correo invalido.");
            }
        } while (!emailValido);

        String password;
        boolean contrasenaValida;
        do {
            System.out.print("Contrasena: ");
            password = scanner.nextLine();
            contrasenaValida = auth.esContraseñaSegura(password);

            if (!contrasenaValida) {
                System.out.println("La contrasena debe cumplir:");
                System.out.println("- Minimo 8 caracteres");
                System.out.println("- Al menos una letra mayuscula");
                System.out.println("- Al menos una letra minuscula");
                System.out.println("- Al menos un numero");
            }
        } while (!contrasenaValida);

        try {
            usuarioActual = auth.registrarUsuario(nombre, email, password);
            System.out.println("\nRegistro exitoso! Ahora puedes iniciar sesion");
        } catch (IllegalArgumentException e) {
            System.out.println("\nError: " + e.getMessage());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.out.println("\nError: Este correo ya esta registrado");
            } else {
                LOGGER.log(Level.SEVERE, "Error en registro", e);
                System.out.println("\nError tecnico: " + e.getMessage());
            }
        }
    }

    private static void mostrarMenuUsuario() throws SQLException {
        Autentificacion auth = new Autentificacion(connection);

        while (true) {
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("1. Ver catalogo de juegos");
            System.out.println("2. Buscar juegos por plataforma");
            System.out.println("3. Ver juegos en descuento");
            System.out.println("4. Ver mi carrito");
            System.out.println("5. Ver mi biblioteca");
            System.out.println("6. Ver pedidos");
            System.out.println("7. Administrar metodos de pago");
            System.out.println("8. Cerrar sesion");
            System.out.println("9. Cambiar contrasena");
            System.out.print("Seleccione una opcion: ");

            int opcion = leerEntero();
            scanner.nextLine();
            switch (opcion) {
                case 1 -> mostrarCatalogoCompleto();
                case 2 -> mostrarPorPlataforma();
                case 3 -> mostrarJuegosEnDescuento();
                case 4 -> gestionarCarrito();
                case 5 -> mostrarBiblioteca();
                case 6 -> mostrarPedidos();
                case 7 -> gestionarMetodosPago();
                case 8 -> { usuarioActual = null; return; }
                case 9 -> {
                    System.out.print("\nContrasena actual: ");
                    String actual = scanner.nextLine();
                    System.out.print("Nueva contrasena: ");
                    String nueva = scanner.nextLine();
                    System.out.print("Confirmar nueva contrasena: ");
                    String confirmacion = scanner.nextLine();

                    try {
                        if (!nueva.equals(confirmacion)) {
                            System.out.println("Las contrasenas no coinciden");
                            break;
                        }

                        if (!auth.verificarCredenciales(usuarioActual.getEmail(), actual)) {
                            System.out.println("Contrasena actual incorrecta");
                            break;
                        }

                        auth.cambiarContrasena(usuarioActual.getIdUsuario(), nueva);
                        System.out.println("Contrasena cambiada exitosamente");

                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    } catch (SQLException e) {
                        System.out.println("Error al cambiar contrasena: " + e.getMessage());
                    }
                }
                default -> System.out.println("Opcion invalida");
            }
        }
    }



    private static void mostrarCatalogoCompleto() {
        try {
            List<Videojuego> juegos = Videojuego.obtenerTodos(connection);
            for (Videojuego juego : juegos) {
                // Obtener todas las plataformas del videojuego
                List<Plataforma> plataformas = Plataforma.obtenerPorVideojuego(connection, juego.getIdVideojuego());
                System.out.print(juego.getTitulo() + " | Plataformas: ");
                for (int i = 0; i < plataformas.size(); i++) {
                    System.out.print(plataformas.get(i).getNombreComercial());
                    if (i < plataformas.size() - 1) {
                        System.out.print(", ");
                    }
                }
                // Aquí agregamos el estado a la impresión
                System.out.print(" | Estado: ");
                if (juego.getEstado() != null) {
                    System.out.print(juego.getEstado().name());
                } else {
                    System.out.print("Desconocido");
                }
                // También puedes incluir precio si lo necesitas
                System.out.printf(" | Precio: $%.2f", juego.getPrecio());
                System.out.println();
            }

            while (true) {
                System.out.print("\nSelecciona un juego para ver detalles (0 para volver): ");
                int seleccion = leerEntero();
                scanner.nextLine();

                if (seleccion == 0) {
                    break;
                }
                if (seleccion > 0 && seleccion <= juegos.size()) {
                    mostrarDetallesJuego(juegos.get(seleccion - 1));
                    break;
                } else {
                    System.out.println("Ingrese un juego válido.");
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error mostrando catálogo", ex);
        }
    }

    private static void mostrarDetallesJuego(Videojuego juego) throws SQLException {
        // Obtener género
        String nombreGenero = "";
        try {
            // Consulta para obtener el nombre del género
            String sqlGenero = "SELECT nombre FROM genero WHERE id_genero = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sqlGenero)) {
                stmt.setLong(1, juego.getIdGenero());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        nombreGenero = rs.getString("nombre");
                    }
                }
            }
        } catch (SQLException e) {
            nombreGenero = "Desconocido";
            LOGGER.log(Level.WARNING, "Error al obtener género del juego", e);
        }

        // Obtener plataformas
        List<Plataforma> plataformas = juego.obtenerPlataformas(connection);
        StringBuilder plataformasStr = new StringBuilder();
        for (int i = 0; i < plataformas.size(); i++) {
            plataformasStr.append(plataformas.get(i).getNombreComercial());
            if (i < plataformas.size() - 1) {
                plataformasStr.append(", ");
            }
        }

        // Mostrar detalles
        System.out.println("\nDETALLES DEL JUEGO");
        System.out.println("=============================");
        System.out.println("Título: " + juego.getTitulo());
        System.out.println("Género: " + nombreGenero);
        System.out.println("Plataforma: " + plataformasStr);
        System.out.println("Precio: $" + String.format("%.2f", juego.getPrecio()) + " USD");
        System.out.println("Estudio: " + juego.getEstudio());
        System.out.println("Estado: " + juego.getEstado());

        System.out.println("\nDescripción:");
        System.out.println(juego.getDescripcion());

        System.out.println("\n1. Agregar al carrito");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        int opcion = leerEntero();
        scanner.nextLine();

        if (opcion == 1) {
            agregarAlCarrito(juego);
        }
    }

    private static void mostrarPorPlataforma() {
        try {
            List<Plataforma> plataformas = Plataforma.obtenerTodas(connection);
            System.out.println("\n=== PLATAFORMAS DISPONIBLES ===");
            for (int i = 0; i < plataformas.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, plataformas.get(i).getNombreComercial());
            }

            System.out.print("\nSeleccione una plataforma (0 para volver): ");
            int seleccion = leerEntero();
            if (seleccion > 0 && seleccion <= plataformas.size()) {
                List<Videojuego> juegos = Videojuego.obtenerPorPlataforma(
                        connection, plataformas.get(seleccion - 1).getIdPlataforma());

                System.out.println("\n=== JUEGOS DISPONIBLES ===");
                mostrarListaJuegos(juegos);

                System.out.println("\n1. Seleccionar juego para ver detalles");
                System.out.println("2. Seleccionar juego para agregar al carrito");
                System.out.println("0. Volver");
                System.out.print("Seleccione una opción: ");
                int opcion = leerEntero();
                scanner.nextLine();

                if (opcion == 1) {
                    while (true) {
                        System.out.print("\nSeleccione un juego para ver detalles (0 para volver): ");
                        int juegoSeleccionado = leerEntero();
                        scanner.nextLine();
                        if (juegoSeleccionado == 0) break;
                        if (juegoSeleccionado > 0 && juegoSeleccionado <= juegos.size()) {
                            mostrarDetallesJuego(juegos.get(juegoSeleccionado - 1));
                            break;
                        } else {
                            System.out.println("Ingrese un juego válido.");
                        }
                    }
                } else if (opcion == 2) {
                    while (true) {
                        System.out.print("\nSeleccione un juego para agregar al carrito (0 para volver): ");
                        int juegoSeleccionado = leerEntero();
                        scanner.nextLine();
                        if (juegoSeleccionado == 0) break;
                        if (juegoSeleccionado > 0 && juegoSeleccionado <= juegos.size()) {
                            agregarAlCarrito(juegos.get(juegoSeleccionado - 1));
                            break;
                        } else {
                            System.out.println("Ingrese un juego válido.");
                        }
                    }
                }

            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener juegos por plataforma", e);
            System.out.println("Error al cargar los juegos");
        }
    }

    private static void mostrarJuegosEnDescuento() {
        try {
            List<Videojuego> juegos = Videojuego.obtenerEnDescuento(connection);
            System.out.println("\n=== JUEGOS EN DESCUENTO ===");
            mostrarListaJuegos(juegos);

            while (true) {
                System.out.println("\n1. Seleccionar juego para ver detalles");
                System.out.println("2. Seleccionar juego para agregar al carrito");
                System.out.println("0. Volver");
                System.out.print("Seleccione una opción: ");
                int opcion = leerEntero();
                scanner.nextLine();

                if (opcion == 1) {
                    while (true) {
                        System.out.print("\nSeleccione un juego para ver detalles (0 para volver): ");
                        int seleccion = leerEntero();
                        scanner.nextLine();
                        if (seleccion == 0) break;
                        if (seleccion > 0 && seleccion <= juegos.size()) {
                            mostrarDetallesJuego(juegos.get(seleccion - 1));
                            break;
                        } else {
                            System.out.println("Ingrese un juego válido.");
                        }
                    }
                    break;
                } else if (opcion == 2) {
                    while (true) {
                        System.out.print("\nSeleccione un juego para agregar al carrito (0 para volver): ");
                        int seleccion = leerEntero();
                        scanner.nextLine();
                        if (seleccion == 0) break;
                        if (seleccion > 0 && seleccion <= juegos.size()) {
                            agregarAlCarrito(juegos.get(seleccion - 1));
                            break;
                        } else {
                            System.out.println("Ingrese un juego válido.");
                        }
                    }
                    break;
                } else if (opcion == 0) {
                    break;
                } else {
                    System.out.println("Ingrese una opción válida.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener juegos en descuento", e);
            System.out.println("Error al cargar los juegos en descuento");
        }
    }

    private static void mostrarListaJuegos(List<Videojuego> juegos) {
        int i = 1;
        for (Videojuego juego : juegos) {
            // Ejemplo de cómo mostrar título, plataformas y estado
            try {
                List<Plataforma> plataformas = juego.obtenerPlataformas(connection);
                String plataformasStr = plataformas.isEmpty() ? "Sin plataformas" :
                        plataformas.stream().map(Plataforma::getNombreComercial).reduce((a,b) -> a + ", " + b).orElse("");
                System.out.printf("%d) %s | Plataformas: %s | Estado: %s | Precio: $%.2f\n",
                        i++, juego.getTitulo(), plataformasStr, juego.getEstado().name(), juego.getPrecio());
            } catch (SQLException e) {
                System.out.println(i++ + ") " + juego.getTitulo() + " (Error cargando plataformas)");
            }
        }
    }

    private static void agregarAlCarrito(Videojuego juego) {
        System.out.print("Cantidad: ");
        int cantidad = leerEntero();
        scanner.nextLine();

        try {
            // Verificar y crear carrito si no existe
            if (usuarioActual.getCarrito() == null) {
                usuarioActual.setCarrito(new CarritoCompra(usuarioActual, connection));
            }

            CarritoCompra carrito = usuarioActual.getCarrito();
            carrito.agregarItem(juego, cantidad);
            System.out.println("\nJuego agregado al carrito!");
        } catch (IllegalStateException e) {
            System.out.println("\nError: " + e.getMessage());
        } catch (RuntimeException e) { // Captura excepciones de SQL convertidas en RuntimeException
            LOGGER.log(Level.SEVERE, "Error al agregar al carrito", e);
            System.out.println("\nError al agregar al carrito: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            System.out.println("\nError inesperado al agregar al carrito");
        }
    }

    private static void mostrarBiblioteca() {
        try {
            if (usuarioActual == null) {
                System.out.println("No hay usuario autenticado");
                return;
            }

            Biblioteca biblioteca = usuarioActual.getBiblioteca();
            List<Biblioteca.ItemBiblioteca> items = biblioteca.getJuegos();

            System.out.println("\n=== TU BIBLIOTECA ===");
            if (items.isEmpty()) {
                System.out.println("No tienes juegos en tu biblioteca");
            } else {
                for (int i = 0; i < items.size(); i++) {
                    Biblioteca.ItemBiblioteca item = items.get(i);
                    System.out.printf("%d. %s - Key: %s | Comprado: %s%n",
                            i + 1,
                            item.getJuego().getTitulo(),
                            item.getKeyActivacion(),
                            item.getFechaCompra().toLocalDate());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar la biblioteca", e);
            System.out.println("\nError al cargar la biblioteca: " + e.getMessage());
        }
    }

    private static void mostrarPedidos() {
        try {
            if (usuarioActual == null) {
                System.out.println("No hay usuario autenticado");
                return;
            }

            HistorialCompras historial = new HistorialCompras(usuarioActual, connection);
            List<Pedido> pedidos = historial.getPedidosRecientes();

            if (pedidos.isEmpty()) {
                System.out.println("\nNo tienes compras registradas");
                return;
            }

            System.out.println("\n=== HISTORIAL DE COMPRAS ===");
            for (int i = 0; i < pedidos.size(); i++) {
                Pedido pedido = pedidos.get(i);
                System.out.printf("%d. Pedido #%s - Total: $%.2f - Fecha: %s - Estado: %s%n",
                        i + 1,
                        pedido.getIdPedido(),
                        pedido.getTotal(),
                        pedido.getFechaCreacion().toLocalDate(),
                        pedido.getEstado());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar el historial", e);
            System.out.println("\nError al cargar el historial de compras: " + e.getMessage());
        }
    }

    private static void eliminarMetodoPago(int idMetodo) {
        try {
            // Primero eliminar la relación usuario-metodo_pago
            String sql = "DELETE FROM usuario_metodo_pago WHERE id_usuario = ? AND id_metodo = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, usuarioActual.getIdUsuario());
                stmt.setInt(2, idMetodo);
                int filasAfectadas = stmt.executeUpdate();

                if (filasAfectadas > 0) {
                    System.out.println("Método de pago eliminado correctamente");

                    // Opcional: Eliminar también el método de pago de la tabla principal
                    // si no hay otras referencias a él
                    String sqlEliminarMetodo = "DELETE FROM metodo_pago WHERE id_metodo = ?";
                    try (PreparedStatement stmtMetodo = connection.prepareStatement(sqlEliminarMetodo)) {
                        stmtMetodo.setInt(1, idMetodo);
                        stmtMetodo.executeUpdate();
                    }
                } else {
                    System.out.println("No se encontró el método de pago");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar método de pago", e);
            System.out.println("Error al eliminar método de pago: " + e.getMessage());
        }
    }

    private static void gestionarMetodosPago() {
        try {
            if (usuarioActual == null) {
                System.out.println("No hay usuario autenticado");
                return;
            }

            List<MetodoPago> metodos = usuarioActual.getMetodosPago();

            System.out.println("\n=== TUS METODOS DE PAGO ===");
            for (int i = 0; i < metodos.size(); i++) {
                MetodoPago metodo = metodos.get(i);
                System.out.printf("%d. %s - %s%n",
                        i + 1, metodo.getTipo(), metodo.getNumero());
            }

            System.out.println("\n1. Agregar metodo de pago");
            System.out.println("2. Eliminar metodo de pago");
            System.out.println("3. Volver");
            System.out.print("Seleccione una opcion: ");

            int opcion = leerEntero();
            scanner.nextLine();
            switch (opcion) {
                case 1 -> agregarMetodoPago();
                case 2 -> {
                    if (metodos.isEmpty()) {
                        System.out.println("No tienes metodos de pago para eliminar");
                        break;
                    }
                    System.out.print("Seleccione metodo a eliminar (0 para cancelar): ");
                    int seleccion = leerEntero();
                    scanner.nextLine();
                    if (seleccion > 0 && seleccion <= metodos.size()) {
                        eliminarMetodoPago(metodos.get(seleccion - 1).getIdMetodo());
                    }
                }
                case 3 -> { return; }
                default -> System.out.println("Opcion invalida");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al gestionar metodos de pago", e);
            System.out.println("\nError al gestionar metodos de pago");
        }
    }



    private static void agregarMetodoPago() {
        System.out.println("\n=== AGREGAR METODO DE PAGO ===");
        System.out.println("1. Tarjeta de debito");
        System.out.println("2. Tarjeta de credito");
        System.out.print("Seleccione tipo: ");

        int tipo = leerEntero();
        scanner.nextLine();
        if (tipo < 1 || tipo > 2) {
            System.out.println("Opcion invalida");
            return;
        }

        System.out.print("Nombre del titular: ");
        String titular = scanner.nextLine();

        // Validación inmediata para número de tarjeta
        String numero;
        while (true) {
            System.out.print("Numero de tarjeta: ");
            numero = scanner.nextLine();
            if (numero.matches("\\d{10}")) {
                break;
            } else {
                System.out.println("El número de tarjeta debe tener exactamente 10 dígitos numéricos.");
            }
        }

        // Validación inmediata para fecha de expiración (no pasada)
        Date fechaExpiracion;
        while (true) {
            System.out.print("Fecha de expiracion (YYYY-MM-DD): ");
            String fechaInput = scanner.nextLine();
            try {
                fechaExpiracion = Date.valueOf(fechaInput);
                LocalDate fechaIngresada = fechaExpiracion.toLocalDate();
                if (fechaIngresada.isAfter(LocalDate.now())) {
                    break;
                } else {
                    System.out.println("Tarjeta expirada.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Formato de fecha inválido. Intente de nuevo (YYYY-MM-DD).");
            }
        }

        // Validación inmediata para CVV
        String cvv;
        while (true) {
            System.out.print("CVV: ");
            cvv = scanner.nextLine();
            if (cvv.matches("\\d{3,4}")) {
                break;
            } else {
                System.out.println("CVV no valido.");
            }
        }

        try {
            MetodoPago metodo = null;
            if (tipo == 1) {
                // Si decides pedir el número de cuenta para débito, agrégalo aquí si hace falta
                metodo = new TarjetaDebito(connection, titular, numero, fechaExpiracion, cvv);
            } else {
                double limite;
                while (true) {
                    System.out.print("Limite de credito: ");
                    try {
                        limite = Double.parseDouble(scanner.nextLine());
                        if (limite > 0) {
                            break;
                        } else {
                            System.out.println("El límite debe ser un número positivo.");
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Ingrese un valor numérico válido para el límite.");
                    }
                }
                metodo = new TarjetaCredito(connection, titular, numero, fechaExpiracion, cvv, limite);
            }

            metodo.guardarEnBD();
            System.out.println("Metodo de pago agregado exitosamente!");

            String sql = "INSERT INTO usuario_metodo_pago (id_usuario, id_metodo) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, usuarioActual.getIdUsuario());
                stmt.setInt(2, metodo.getIdMetodo());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println("Error al guardar metodo de pago: " + e.getMessage());
        }
    }
    private static void finalizarCompra(CarritoCompra carrito) {
        try {
            List<MetodoPago> metodos = usuarioActual.getMetodosPago();
            if (metodos.isEmpty()) {
                System.out.println("\nNo tienes metodos de pago registrados");
                System.out.println("Deseas agregar uno ahora? (s/n)");
                String respuesta = scanner.nextLine();
                if (respuesta.equalsIgnoreCase("s")) {
                    agregarMetodoPago();
                    metodos = usuarioActual.getMetodosPago();
                    if (metodos.isEmpty()) return;
                } else {
                    return;
                }
            }

            System.out.println("\n=== SELECCIONA METODO DE PAGO ===");
            for (int i = 0; i < metodos.size(); i++) {
                MetodoPago metodo = metodos.get(i);
                System.out.printf("%d. %s - %s%n",
                        i + 1, metodo.getTipo(), metodo.getNumero());
            }

            System.out.print("\nSeleccione metodo de pago (0 para agregar nuevo): ");
            int seleccion = leerEntero();
            scanner.nextLine();

            if (seleccion == 0) {
                agregarMetodoPago();
                return;
            }

            if (seleccion < 1 || seleccion > metodos.size()) {
                System.out.println("Seleccion invalida");
                return;
            }

            int metodoPagoId = metodos.get(seleccion - 1).getIdMetodo();

            // Aquí ya no se pregunta ni se usa el código de descuento

            Pedido pedido = PedidoFactory.crearPedidoDesdeCarrito(
                    connection,
                    carrito.getItems(),
                    metodoPagoId,
                    null,  // El parámetro del código de descuento se pone null si es necesario
                    usuarioActual);

            System.out.println("Pedido creado exitosamente con ID: " + pedido.getIdPedido());
            System.out.println("\n¡Gracias por tu compra! Aquí tienes las claves de activación de tus juegos:");
            int index = 1;
            for (ItemPedido item : pedido.getItems()) {
                String key = "";
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT key_activacion FROM biblioteca WHERE id_usuario = ? AND id_videojuego = ?")) {
                    stmt.setString(1, usuarioActual.getIdUsuario());
                    stmt.setLong(2, item.getJuego().getIdVideojuego());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            key = rs.getString("key_activacion");
                        } else {
                            key = "(No se encontró la clave de activación)";
                        }
                    }
                } catch (SQLException ex) {
                    key = "(Error al obtener la clave)";
                }

                System.out.printf("%d. %s - Key: %s%n", index++, item.getJuego().getTitulo(), key);
            }
            carrito.cambiarEstado(EstadoCarrito.FINALIZADO);
            usuarioActual.setCarrito(null); // Vaciar carrito al finalizar la compra

        } catch (Exception e) {
            //System.out.println("Error al finalizar la compra: " + e.getMessage());
        }
    }

    private static void gestionarCarrito() {
        CarritoCompra carrito = usuarioActual.getCarrito();
        if (carrito == null || carrito.getItems().isEmpty()) {
            System.out.println("\nNo tienes items en tu carrito");
            return;
        }

        List<ItemCarrito> items = carrito.getItems();
        System.out.println("\n=== TU CARRITO ===");
        for (int i = 0; i < items.size(); i++) {
            ItemCarrito item = items.get(i);
            System.out.printf("%d. %s x%d - $%.2f%n",
                    i + 1,
                    item.getVideojuego().getTitulo(),
                    item.getCantidad(),
                    item.getSubtotal()
            );
        }

        System.out.printf("\nTotal: $%.2f%n", carrito.calcularTotal());

        System.out.println("\n1. Finalizar compra");
        System.out.println("2. Eliminar item");
        System.out.println("3. Volver");
        System.out.print("Seleccione una opcion: ");

        int opcion = leerEntero();
        scanner.nextLine();
        switch (opcion) {
            case 1 -> finalizarCompra(carrito);
            case 2 -> eliminarItemDelCarrito(items);
            case 3 -> { return; }
            default -> System.out.println("Opcion invalida");
        }
    }

    private static void eliminarItemDelCarrito(List<ItemCarrito> items) {
        System.out.print("Indice del item a eliminar: ");
        int indice = leerEntero() - 1;
        scanner.nextLine();

        if (indice >= 0 && indice < items.size()) {
            ItemCarrito item = items.get(indice);
            usuarioActual.getCarrito().eliminarItem(item.getVideojuego().getIdVideojuego()); // CORREGIDO
            System.out.println("Item eliminado");
        } else {
            System.out.println("Indice invalido");
        }
    }
}