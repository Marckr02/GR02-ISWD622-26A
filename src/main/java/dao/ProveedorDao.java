package dao;

import model.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia de proveedores y de su vinculacion con insumos del
 * inventario (tabla proveedor_insumo) en la BD H2 (ver {@link ConexionBD}).
 * Los datos de ejemplo (proveedores ecuatorianos y sus vinculos) se cargan
 * una sola vez desde src/main/resources/db/seed.sql.
 */
public class ProveedorDao {

    public Proveedor guardar(Proveedor proveedor) {
        String sql = "INSERT INTO proveedores (nombre, telefono, correo) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getTelefono());
            ps.setString(3, proveedor.getCorreo());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    proveedor.setId(claves.getInt(1));
                }
            }
            return proveedor;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo guardar el proveedor", ex);
        }
    }

    public Proveedor buscarPorId(int id) {
        String sql = "SELECT id, nombre, telefono, correo FROM proveedores WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar el proveedor", ex);
        }
    }

    /** Busca un proveedor por nombre ignorando mayusculas/minusculas. */
    public Proveedor buscarPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        String sql = "SELECT id, nombre, telefono, correo FROM proveedores WHERE LOWER(nombre) = LOWER(?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar el proveedor", ex);
        }
    }

    /** Listado ordenado alfabeticamente por nombre (HU13). */
    public List<Proveedor> listarTodos() {
        String sql = "SELECT id, nombre, telefono, correo FROM proveedores ORDER BY LOWER(nombre)";
        List<Proveedor> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo listar los proveedores", ex);
        }
    }

    /** Reemplaza los datos de un proveedor existente (HU28: editar proveedor). */
    public Proveedor actualizar(Proveedor proveedor) {
        String sql = "UPDATE proveedores SET nombre = ?, telefono = ?, correo = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getTelefono());
            ps.setString(3, proveedor.getCorreo());
            ps.setInt(4, proveedor.getId());
            ps.executeUpdate();
            return proveedor;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo actualizar el proveedor", ex);
        }
    }

    public void eliminar(int id) {
        try (Connection con = ConexionBD.obtenerConexion()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM proveedor_insumo WHERE proveedor_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM proveedores WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo eliminar el proveedor", ex);
        }
    }

    /** Vincula un insumo con un proveedor (HU6), reemplazando el vinculo previo si existia. */
    public void vincularInsumo(int insumoId, int proveedorId) {
        String sql = "MERGE INTO proveedor_insumo (insumo_id, proveedor_id) KEY (insumo_id) VALUES (?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, insumoId);
            ps.setInt(2, proveedorId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo vincular el insumo al proveedor", ex);
        }
    }

    public void desvincularInsumo(int insumoId) {
        String sql = "DELETE FROM proveedor_insumo WHERE insumo_id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, insumoId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo desvincular el insumo", ex);
        }
    }

    public Integer obtenerProveedorIdDeInsumo(int insumoId) {
        String sql = "SELECT proveedor_id FROM proveedor_insumo WHERE insumo_id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, insumoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo obtener el proveedor del insumo", ex);
        }
    }

    /** True si el proveedor tiene al menos un insumo vinculado (bloquea su eliminacion, HU25). */
    public boolean tieneInsumosVinculados(int proveedorId) {
        String sql = "SELECT COUNT(*) FROM proveedor_insumo WHERE proveedor_id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo verificar los insumos vinculados", ex);
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(rs.getInt("id"), rs.getString("nombre"), rs.getString("telefono"), rs.getString("correo"));
    }
}
