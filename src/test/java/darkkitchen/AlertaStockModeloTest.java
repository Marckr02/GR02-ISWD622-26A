package darkkitchen;

import model.AlertaStock;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TDD (tarea tecnica model): AlertaStock expone getters/setters para todos
 * sus campos (id, insumo, stock al momento y timestamp de la alerta).
 */
class AlertaStockModeloTest {

    @Test
    void constructorConDatosAsignaTodosLosCampos() {
        LocalDateTime ahora = LocalDateTime.now();
        AlertaStock alerta = new AlertaStock(1, 2, "Tomate", 3.5, ahora);

        assertEquals(1, alerta.getId());
        assertEquals(2, alerta.getInsumoId());
        assertEquals("Tomate", alerta.getInsumoNombre());
        assertEquals(3.5, alerta.getStockAlMomento(), 0.0001);
        assertEquals(ahora, alerta.getTimestamp());
    }

    @Test
    void constructorVacioPermiteUsarSetters() {
        AlertaStock alerta = new AlertaStock();
        LocalDateTime fecha = LocalDateTime.of(2024, 3, 15, 8, 30);

        alerta.setId(5);
        alerta.setInsumoId(6);
        alerta.setInsumoNombre("Queso");
        alerta.setStockAlMomento(1.2);
        alerta.setTimestamp(fecha);

        assertEquals(5, alerta.getId());
        assertEquals(6, alerta.getInsumoId());
        assertEquals("Queso", alerta.getInsumoNombre());
        assertEquals(1.2, alerta.getStockAlMomento(), 0.0001);
        assertEquals(fecha, alerta.getTimestamp());
    }
}
