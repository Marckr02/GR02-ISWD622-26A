package dao;

import model.Insumo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia de insumos en la BD H2 (ver {@link ConexionBD}). Los datos de
 * ejemplo (insumos de la gastronomia ecuatoriana, con niveles de stock
 * variados: normal, critico y agotado) se cargan una sola vez desde
 * src/main/resources/db/seed.sql.
 */
public class InsumoDao {

    public Insumo guardar(Insumo insumo) {
        String sql = "INSERT INTO insumos (nombre, unidad, stock, costo_unitario, stock_minimo) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, insumo.getNombre());
            ps.setString(2, insumo.getUnidad());
            ps.setDouble(3, insumo.getStock());
            ps.setDouble(4, insumo.getCostoUnitario());
            ps.setDouble(5, insumo.getStockMinimo());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    insumo.setId(claves.getInt(1));
                }
            }
            return insumo;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo guardar el insumo", ex);
        }
    }

    public Insumo buscarPorId(int id) {
        String sql = "SELECT id, nombre, unidad, stock, costo_unitario, stock_minimo FROM insumos WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar el insumo", ex);
        }
    }

    /** Busca un insumo por nombre ignorando mayusculas/minusculas. */
    public Insumo buscarPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        String sql = "SELECT id, nombre, unidad, stock, costo_unitario, stock_minimo FROM insumos WHERE LOWER(nombre) = LOWER(?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar el insumo", ex);
        }
    }

    /** Listado ordenado alfabeticamente por nombre, para facilitar la busqueda visual en bodega. */
    public List<Insumo> listarTodos() {
        String sql = "SELECT id, nombre, unidad, stock, costo_unitario, stock_minimo FROM insumos ORDER BY LOWER(nombre)";
        List<Insumo> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo listar los insumos", ex);
        }
    }

    public void actualizar(Insumo insumo) {
        String sql = "UPDATE insumos SET nombre = ?, unidad = ?, stock = ?, costo_unitario = ?, stock_minimo = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, insumo.getNombre());
            ps.setString(2, insumo.getUnidad());
            ps.setDouble(3, insumo.getStock());
            ps.setDouble(4, insumo.getCostoUnitario());
            ps.setDouble(5, insumo.getStockMinimo());
            ps.setInt(6, insumo.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo actualizar el insumo", ex);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM insumos WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo eliminar el insumo", ex);
        }
    }

    private Insumo mapear(ResultSet rs) throws SQLException {
        return new Insumo(rs.getInt("id"), rs.getString("nombre"), rs.getString("unidad"),
                rs.getDouble("stock"), rs.getDouble("costo_unitario"), rs.getDouble("stock_minimo"));
    }
}
