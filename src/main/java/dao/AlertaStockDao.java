package dao;

import model.AlertaStock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia del historial de alertas de stock critico en la BD H2 (ver
 * {@link ConexionBD}). Al migrar de memoria a BD, el historial ya no se
 * pierde al reiniciar la aplicacion: es un registro historico real (F3.3).
 */
public class AlertaStockDao {

    public AlertaStock guardar(AlertaStock alerta) {
        String sql = "INSERT INTO alertas_stock (insumo_id, insumo_nombre, stock_al_momento, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, alerta.getInsumoId());
            ps.setString(2, alerta.getInsumoNombre());
            ps.setDouble(3, alerta.getStockAlMomento());
            ps.setTimestamp(4, Timestamp.valueOf(alerta.getTimestamp()));
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    alerta.setId(claves.getInt(1));
                }
            }
            return alerta;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo guardar la alerta", ex);
        }
    }

    /** Ultima alerta registrada para un insumo, o null si nunca se registro una. */
    public AlertaStock buscarUltimaPorInsumo(int insumoId) {
        String sql = "SELECT id, insumo_id, insumo_nombre, stock_al_momento, timestamp FROM alertas_stock "
                + "WHERE insumo_id = ? ORDER BY timestamp DESC LIMIT 1";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, insumoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo buscar la ultima alerta del insumo", ex);
        }
    }

    /** Historial completo ordenado de la mas reciente a la mas antigua (HU11). */
    public List<AlertaStock> listarOrdenadoDesc() {
        String sql = "SELECT id, insumo_id, insumo_nombre, stock_al_momento, timestamp FROM alertas_stock ORDER BY timestamp DESC";
        List<AlertaStock> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo listar el historial de alertas", ex);
        }
    }

    private AlertaStock mapear(ResultSet rs) throws SQLException {
        return new AlertaStock(rs.getInt("id"), rs.getInt("insumo_id"), rs.getString("insumo_nombre"),
                rs.getDouble("stock_al_momento"), rs.getTimestamp("timestamp").toLocalDateTime());
    }
}
