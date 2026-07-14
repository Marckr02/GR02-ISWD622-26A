package dao;

import model.Restaurante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia de restaurantes (marcas) de la Dark Kitchen en la BD H2
 * (ver {@link ConexionBD}). Los datos de ejemplo (restaurantes ecuatorianos)
 * se cargan una sola vez desde src/main/resources/db/seed.sql.
 */
public class RestauranteDao {

    public Restaurante guardar(Restaurante restaurante) {
        String sql = "INSERT INTO restaurantes (nombre, descripcion) VALUES (?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, restaurante.getNombre());
            ps.setString(2, restaurante.getDescripcion());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    restaurante.setId(claves.getInt(1));
                }
            }
            return restaurante;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo guardar el restaurante", ex);
        }
    }

    public Restaurante buscarPorId(int id) {
        String sql = "SELECT id, nombre, descripcion FROM restaurantes WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar el restaurante", ex);
        }
    }

    /** Busca un restaurante por nombre ignorando mayusculas/minusculas. */
    public Restaurante buscarPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        String sql = "SELECT id, nombre, descripcion FROM restaurantes WHERE LOWER(nombre) = LOWER(?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar el restaurante", ex);
        }
    }

    /** Listado ordenado alfabeticamente por nombre (HU27). */
    public List<Restaurante> listarTodos() {
        String sql = "SELECT id, nombre, descripcion FROM restaurantes ORDER BY LOWER(nombre)";
        List<Restaurante> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo listar los restaurantes", ex);
        }
    }

    public void actualizar(Restaurante restaurante) {
        String sql = "UPDATE restaurantes SET nombre = ?, descripcion = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, restaurante.getNombre());
            ps.setString(2, restaurante.getDescripcion());
            ps.setInt(3, restaurante.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo actualizar el restaurante", ex);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM restaurantes WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo eliminar el restaurante", ex);
        }
    }

    private Restaurante mapear(ResultSet rs) throws SQLException {
        return new Restaurante(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion"));
    }
}
