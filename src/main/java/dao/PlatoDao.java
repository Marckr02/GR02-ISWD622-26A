package dao;

import model.IngredientePlato;
import model.Plato;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistencia de los platos del menu y su receta (insumos con cantidad y
 * unidad, tabla plato_ingredientes) en la BD H2 (ver {@link ConexionBD}).
 * Los datos de ejemplo (platos de la gastronomia ecuatoriana) se cargan una
 * sola vez desde src/main/resources/db/seed.sql.
 */
public class PlatoDao {

    public Plato guardar(Plato plato) {
        String sql = "INSERT INTO platos (nombre, restaurante_id) VALUES (?, ?)";
        try (Connection con = ConexionBD.obtenerConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, plato.getNombre());
                ps.setInt(2, plato.getRestauranteId());
                ps.executeUpdate();
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        plato.setId(claves.getInt(1));
                    }
                }
            }
            insertarIngredientes(con, plato.getId(), plato.getIngredientes());
            return plato;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo guardar el plato", ex);
        }
    }

    public Plato buscarPorId(int id) {
        String sql = "SELECT id, nombre, restaurante_id FROM platos WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapear(con, rs);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar el plato", ex);
        }
    }

    /** Busca un plato por nombre ignorando mayusculas/minusculas. */
    public Plato buscarPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        String sql = "SELECT id, nombre, restaurante_id FROM platos WHERE LOWER(nombre) = LOWER(?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapear(con, rs);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar el plato", ex);
        }
    }

    /**
     * Lista todos los platos con su receta completa en solo 2 consultas (en
     * vez de 1 + N, una por cada plato para cargar sus ingredientes), que es
     * lo que hacia lenta esta pantalla con catalogos grandes.
     */
    public List<Plato> listarTodos() {
        try (Connection con = ConexionBD.obtenerConexion()) {
            Map<Integer, List<IngredientePlato>> ingredientesPorPlato = cargarTodosLosIngredientes(con);

            List<Plato> resultado = new ArrayList<>();
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, nombre, restaurante_id FROM platos ORDER BY id")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    List<IngredientePlato> ingredientes = ingredientesPorPlato.getOrDefault(id, List.of());
                    resultado.add(new Plato(id, rs.getString("nombre"), rs.getInt("restaurante_id"), ingredientes));
                }
            }
            return resultado;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo listar los platos", ex);
        }
    }

    public void actualizar(Plato plato) {
        String sql = "UPDATE platos SET nombre = ?, restaurante_id = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, plato.getNombre());
                ps.setInt(2, plato.getRestauranteId());
                ps.setInt(3, plato.getId());
                ps.executeUpdate();
            }
            eliminarIngredientes(con, plato.getId());
            insertarIngredientes(con, plato.getId(), plato.getIngredientes());
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo actualizar el plato", ex);
        }
    }

    public void eliminar(int id) {
        try (Connection con = ConexionBD.obtenerConexion()) {
            eliminarIngredientes(con, id);
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM platos WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo eliminar el plato", ex);
        }
    }

    /** True si algun plato pertenece al restaurante indicado (bloquea su eliminacion, HU29). */
    public boolean existePlatoConRestaurante(int restauranteId) {
        String sql = "SELECT COUNT(*) FROM platos WHERE restaurante_id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, restauranteId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo verificar los platos del restaurante", ex);
        }
    }

    /** True si algun plato usa el insumo indicado en su receta (bloquea la eliminacion del insumo). */
    public boolean existePlatoConIngrediente(int insumoId) {
        String sql = "SELECT COUNT(*) FROM plato_ingredientes WHERE insumo_id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, insumoId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo verificar el uso del insumo en los platos", ex);
        }
    }

    private void insertarIngredientes(Connection con, int platoId, List<IngredientePlato> ingredientes) throws SQLException {
        if (ingredientes == null || ingredientes.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO plato_ingredientes (plato_id, insumo_id, cantidad, unidad_receta) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (IngredientePlato ingrediente : ingredientes) {
                ps.setInt(1, platoId);
                ps.setInt(2, ingrediente.getInsumoId());
                ps.setDouble(3, ingrediente.getCantidad());
                ps.setString(4, ingrediente.getUnidadReceta());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void eliminarIngredientes(Connection con, int platoId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM plato_ingredientes WHERE plato_id = ?")) {
            ps.setInt(1, platoId);
            ps.executeUpdate();
        }
    }

    /** Todos los ingredientes de todos los platos, agrupados por plato_id, en una sola consulta. */
    private Map<Integer, List<IngredientePlato>> cargarTodosLosIngredientes(Connection con) throws SQLException {
        Map<Integer, List<IngredientePlato>> resultado = new LinkedHashMap<>();
        String sql = "SELECT plato_id, insumo_id, cantidad, unidad_receta FROM plato_ingredientes ORDER BY plato_id, id";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int platoId = rs.getInt("plato_id");
                IngredientePlato ingrediente = new IngredientePlato(
                        rs.getInt("insumo_id"), rs.getDouble("cantidad"), rs.getString("unidad_receta"));
                resultado.computeIfAbsent(platoId, k -> new ArrayList<>()).add(ingrediente);
            }
        }
        return resultado;
    }

    private List<IngredientePlato> cargarIngredientes(Connection con, int platoId) throws SQLException {
        String sql = "SELECT insumo_id, cantidad, unidad_receta FROM plato_ingredientes WHERE plato_id = ? ORDER BY id";
        List<IngredientePlato> ingredientes = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, platoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ingredientes.add(new IngredientePlato(
                            rs.getInt("insumo_id"), rs.getDouble("cantidad"), rs.getString("unidad_receta")));
                }
            }
        }
        return ingredientes;
    }

    private Plato mapear(Connection con, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        return new Plato(id, rs.getString("nombre"), rs.getInt("restaurante_id"), cargarIngredientes(con, id));
    }
}
