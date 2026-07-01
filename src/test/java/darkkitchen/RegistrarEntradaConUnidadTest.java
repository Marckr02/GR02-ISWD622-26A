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

@ExtendWith(MockitoExtension.class)
class RegistrarEntradaConUnidadTest {

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private InsumoService insumoService;

    @Test
    void registraEntradaDecimalEnKilogramos() {
        Insumo insumo = new Insumo(1, "Harina", "kg", 10.0, 0.85);
        when(insumoDao.buscarPorId(1)).thenReturn(insumo);

        Insumo resultado = insumoService.registrarEntradaSimplificada(1, "2.5");

        assertEquals(12.5, resultado.getStock(), 0.0001);
        assertEquals("kg", resultado.getUnidad());
    }

    @Test
    void rechazaDecimalesCuandoLaUnidadEsUnidades() {
        Insumo insumo = new Insumo(2, "Pan", "unidades", 10.0, 0.35);
        when(insumoDao.buscarPorId(2)).thenReturn(insumo);

        assertThrows(IllegalArgumentException.class,
                () -> insumoService.registrarEntradaSimplificada(2, "1.5", "unidades"));
    }

    @Test
    void rechazaUnidadDistintaALaDefinidaEnElInsumo() {
        Insumo insumo = new Insumo(3, "Aceite", "l", 10.0, 2.10);
        when(insumoDao.buscarPorId(3)).thenReturn(insumo);

        assertThrows(IllegalArgumentException.class,
                () -> insumoService.registrarEntradaSimplificada(3, "2", "kg"));
    }
}
