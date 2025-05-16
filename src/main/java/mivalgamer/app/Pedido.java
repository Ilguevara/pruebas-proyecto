package mivalgamer.app;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pedido {
    private static final Logger LOGGER = Logger.getLogger(Pedido.class.getName());

    private final String idPedido;
    private final Usuario usuario;
    private final LocalDateTime fechaCreacion;
    private final int metodoPagoId;
    private final double descuentoTotal;
    private final double impuestos;
    private EstadoPedido estado;
    private List<ItemPedido> items;
    private final Connection connection;

    // Constructor principal
    public Pedido(String idPedido, Usuario usuario, LocalDateTime fechaCreacion, int metodoPagoId,
                  double descuentoTotal, double impuestos, EstadoPedido estado, Connection connection) {
        this.idPedido = idPedido;
        this.usuario = usuario;
        this.fechaCreacion = fechaCreacion;
        this.metodoPagoId = metodoPagoId;
        this.descuentoTotal = descuentoTotal;
        this.impuestos = impuestos;
        this.estado = estado;
        this.connection = connection;
    }

    // Constructor alternativo para nuevos pedidos
    public Pedido(Usuario usuario, int metodoPagoId,
                  double descuentoTotal, double impuestos, Connection connection) {
        this(generarIdPedido(), usuario, LocalDateTime.now(), metodoPagoId,
                descuentoTotal, impuestos, EstadoPedido.PENDIENTE, connection);
    }

    private static String generarIdPedido() {
        return "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void setItems(List<ItemPedido> items) {
        this.items = items;
    }

    public void guardarEnBD() throws SQLException {
        boolean autoCommitOriginal = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);
            guardarPedidoEnBD();
            guardarItemsPedido();
            connection.commit();
            //System.out.println("¡Tu pedido ha sido guardado exitosamente!");
        } catch (SQLException e) {
            connection.rollback();
            System.out.println("Ocurrió un error al guardar tu pedido. Por favor, inténtalo de nuevo.");
            throw e;
        } finally {
            connection.setAutoCommit(autoCommitOriginal);
        }
    }

    private void guardarPedidoEnBD() throws SQLException {
        String sql = "INSERT INTO pedido (id_pedido, id_usuario, fecha_creacion, " +
                "metodo_pago, descuento_total, impuestos, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, idPedido);
            stmt.setString(2, usuario.getIdUsuario());
            stmt.setTimestamp(3, Timestamp.valueOf(fechaCreacion));
            stmt.setInt(4, metodoPagoId);
            stmt.setDouble(5, descuentoTotal);
            stmt.setDouble(6, impuestos);
            stmt.setString(7, estado.name());
            stmt.executeUpdate();
        }
    }

    private void guardarItemsPedido() throws SQLException {
        if (items == null || items.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO item_pedido (id_pedido, id_videojuego, precio_unitario, cantidad, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (ItemPedido item : items) {
                stmt.setString(1, idPedido);
                stmt.setLong(2, item.getJuego().getIdVideojuego());
                stmt.setDouble(3, item.getPrecioUnitario());
                stmt.setInt(4, item.getCantidad());
                stmt.setDouble(5, item.getSubtotal());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public boolean procesarPago() throws SQLException {
        MetodoPago metodo = MetodoPago.cargarDesdeBD(metodoPagoId, connection);
        double total = calcularTotal();

        boolean hasStock=actualizarStock(getItems());
        if(hasStock){
            if (metodo.procesarPago(total)) {
                actualizarEstado(EstadoPedido.PAGADO);
                registrarEnHistorial(total);
                agregarABiblioteca();
                return true;
            }
        }
        System.out.println("El pago no pudo ser procesado. Por favor, intente de nuevo.");
        return false;
    }
    private boolean actualizarStock(List<ItemPedido> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            return false;
        }
        String sqlUpdate = "UPDATE videojuego SET stock = stock - ? WHERE id_videojuego = ?";
        String sql = "SELECT stock FROM videojuego WHERE id_videojuego = ?";
        for (ItemPedido item : items) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, item.getJuego().getIdVideojuego());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int stock = rs.getInt("stock");
                    if (stock < item.getCantidad()) {
                        System.out.println("No hay suficiente stock del juego "+ item.getJuego().getTitulo());
                        return false;
                    }
                }
            }
            try (PreparedStatement stmt = connection.prepareStatement(sqlUpdate)) {
                stmt.setInt(1, item.getCantidad());
                stmt.setLong(2, item.getJuego().getIdVideojuego());
                stmt.executeUpdate();
            }
        }
        return true;
    }

    private void registrarEnHistorial(double total) throws SQLException {
        String sql = "INSERT INTO historial_compras (id_usuario, id_pedido, fecha_compra, total) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, usuario.getIdUsuario());
            stmt.setString(2, idPedido);
            stmt.setDate(3, Date.valueOf(fechaCreacion.toLocalDate()));
            stmt.setDouble(4, total);
            stmt.executeUpdate();
        }
    }

    private void agregarABiblioteca() throws SQLException {
        if (items == null || items.isEmpty()) {
            return;
        }

        Biblioteca biblioteca = new Biblioteca(usuario, connection);
        String sql = "INSERT INTO biblioteca (id_usuario, id_videojuego, fecha_compra, key_activacion) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (ItemPedido item : items) {
                if (!biblioteca.contieneJuego(item.getJuego())) {
                    String keyActivacion = generarKeyActivacion(item.getJuego());

                    stmt.setString(1, usuario.getIdUsuario());
                    stmt.setLong(2, item.getJuego().getIdVideojuego());
                    stmt.setTimestamp(3, Timestamp.valueOf(fechaCreacion));
                    stmt.setString(4, keyActivacion);
                    stmt.addBatch();

                    System.out.println("Juego " + item.getJuego().getTitulo() + " agregado a biblioteca");
                } else {
                    System.out.println("Juego " + item.getJuego().getTitulo() + " ya está en la biblioteca");
                }
            }
            stmt.executeBatch();
        }
    }

    private String generarKeyActivacion(Videojuego juego) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int blockSize = 5;
        int blocks = 5;
        StringBuilder keyBuilder = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();

        for (int i = 0; i < blocks; i++) {
            if (i > 0) {
                keyBuilder.append("-");
            }
            for (int j = 0; j < blockSize; j++) {
                int index = random.nextInt(chars.length());
                keyBuilder.append(chars.charAt(index));
            }
        }
        return keyBuilder.toString();
    }

    public List<ItemPedido> getItems() throws SQLException {
        if (items == null) {
            items = ItemPedido.obtenerPorPedido(idPedido, connection);
        }
        return items;
    }

    public double calcularTotal() throws SQLException {
        double subtotal = getItems().stream()
                .mapToDouble(ItemPedido::getSubtotal)
                .sum();
        return (subtotal - descuentoTotal) * (1 + impuestos);
    }

    private void actualizarEstado(EstadoPedido nuevoEstado) throws SQLException {
        String sql = "UPDATE pedido SET estado = ? WHERE id_pedido = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado.name());
            stmt.setString(2, idPedido);
            stmt.executeUpdate();
            this.estado = nuevoEstado;
        }
    }

    // Getters
    public String getIdPedido() { return idPedido; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public int getMetodoPagoId() { return metodoPagoId; }
    public double getDescuentoTotal() { return descuentoTotal; }
    public double getImpuestos() { return impuestos; }
    public EstadoPedido getEstado() { return estado; }
    public double getTotal() {
        try {
            return calcularTotal();
        } catch (SQLException e) {
            throw new RuntimeException("Error al calcular total del pedido", e);
        }
    }
}