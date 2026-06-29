package darkkitchen;

import dao.InsumoDao;
import model.Insumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.InsumoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * TDD (HU21): registrarEntradaSimplificada() valida que la cantidad sea un
 * numero entero positivo. Lanza excepcion para 0, negativo y no numerico, y
 * suma al stock cuando la cantidad es valida.
 */
@ExtendWith(MockitoExtension.class)
class RegistrarEntradaSimplificadaTest {

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private InsumoService insumoService;

    @Test
    void sumaAlStockConCantidadEnteraValida() {
        Insumo insumo = new Insumo(1, "Harina de trigo", "kg", 10.0, 0.85);
        when(insumoDao.buscarPorId(1)).thenReturn(insumo);

        Insumo resultado = insumoService.registrarEntradaSimplificada(1, "5");

        assertEquals(15.0, resultado.getStock(), 0.0001);
    }

    @Test
    void lanzaExcepcionConCantidadCero() {
        assertThrows(IllegalArgumentException.class,
                () -> insumoService.registrarEntradaSimplificada(1, "0"));
    }

    @Test
    void lanzaExcepcionConCantidadNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> insumoService.registrarEntradaSimplificada(1, "-5"));
    }

    @Test
    void lanzaExcepcionConCantidadNoNumerica() {
        assertThrows(IllegalArgumentException.class,
                () -> insumoService.registrarEntradaSimplificada(1, "abc"));
    }
}
