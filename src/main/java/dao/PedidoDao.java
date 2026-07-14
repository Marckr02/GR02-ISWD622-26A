package dao;

import model.EstadoPedido;
import model.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia de pedidos en la BD H2 (ver {@link ConexionBD}). Los datos de
 * ejemplo (pedidos ENTREGADO variados por restaurante/plato, mas algunos en
 * otros estados para el tablero) se cargan una sola vez desde
 * src/main/resources/db/seed.sql.
 */
public class PedidoDao {

    public Pedido guardar(Pedido pedido) {
        String sql = "INSERT INTO pedidos (descripcion, marca, plato_id, estado, creado_en) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pedido.getDescripcion());
            ps.setString(2, pedido.getMarca());
            ps.setInt(3, pedido.getPlatoId());
            ps.setString(4, pedido.getEstado().name());
            ps.setTimestamp(5, Timestamp.valueOf(pedido.getCreadoEn()));
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    pedido.setId(claves.getInt(1));
                }
            }
            return pedido;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo guardar el pedido", ex);
        }
    }

    public Pedido buscarPorId(int id) {
        String sql = "SELECT id, descripcion, marca, plato_id, estado, creado_en FROM pedidos WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar el pedido", ex);
        }
    }

    public List<Pedido> listarTodos() {
        String sql = "SELECT id, descripcion, marca, plato_id, estado, creado_en FROM pedidos";
        List<Pedido> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo listar los pedidos", ex);
        }
    }

    public List<Pedido> buscarPorEstado(EstadoPedido estado) {
        String sql = "SELECT id, descripcion, marca, plato_id, estado, creado_en FROM pedidos WHERE estado = ? ORDER BY id";
        List<Pedido> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar los pedidos por estado", ex);
        }
    }

    /** Igual que {@link #buscarPorEstado}, pero mas reciente primero (usado por la columna
     *  ENTREGADO del tablero y su modal de historial completo). */
    public List<Pedido> buscarPorEstadoRecientePrimero(EstadoPedido estado) {
        String sql = "SELECT id, descripcion, marca, plato_id, estado, creado_en FROM pedidos "
                + "WHERE estado = ? ORDER BY creado_en DESC, id DESC";
        List<Pedido> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar los pedidos por estado", ex);
        }
    }

    /**
     * Renombra la marca de todos los pedidos existentes que la tengan (el nombre del
     * restaurante se copia en "marca" al crear el pedido, no es una referencia viva),
     * para que el tablero Kanban refleje el nombre nuevo tras editar un restaurante.
     * No hace nada si nombreAnterior es null o no cambio.
     */
    public void renombrarMarca(String nombreAnterior, String nombreNuevo) {
        if (nombreAnterior == null || nombreAnterior.equals(nombreNuevo)) {
            return;
        }
        String sql = "UPDATE pedidos SET marca = ? WHERE marca = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreNuevo);
            ps.setString(2, nombreAnterior);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo actualizar la marca de los pedidos", ex);
        }
    }

    public void actualizar(Pedido pedido) {
        String sql = "UPDATE pedidos SET descripcion = ?, marca = ?, plato_id = ?, estado = ?, creado_en = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pedido.getDescripcion());
            ps.setString(2, pedido.getMarca());
            ps.setInt(3, pedido.getPlatoId());
            ps.setString(4, pedido.getEstado().name());
            ps.setTimestamp(5, Timestamp.valueOf(pedido.getCreadoEn()));
            ps.setInt(6, pedido.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo actualizar el pedido", ex);
        }
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido(rs.getInt("id"), rs.getString("descripcion"), rs.getString("marca"),
                EstadoPedido.valueOf(rs.getString("estado")), rs.getInt("plato_id"));
        pedido.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
        return pedido;
    }
}
