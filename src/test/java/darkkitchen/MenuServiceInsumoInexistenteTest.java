package darkkitchen;

import dao.InsumoDao;
import dao.PlatoDao;
import model.DisponibilidadPlato;
import model.EstadoPlato;
import model.IngredientePlato;
import model.Plato;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.MenuService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * TDD (HU7): verificarDisponibilidadPlato() bloquea el plato con un motivo
 * que referencia "insumo #id" cuando alguno de sus ingredientes ya no
 * existe en el inventario (insumo eliminado o receta invalida).
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceInsumoInexistenteTest {

    @Mock
    private PlatoDao platoDao;

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private MenuService menuService;

    @Test
    void bloqueaConMotivoDeInsumoInexistenteCuandoElInsumoFueEliminado() {
        Plato plato = new Plato(1, "Plato Huerfano", 1, List.of(new IngredientePlato(99, 1, "g")));
        when(insumoDao.buscarPorId(99)).thenReturn(null);

        DisponibilidadPlato resultado = menuService.verificarDisponibilidadPlato(plato);

        assertEquals(EstadoPlato.BLOQUEADO, resultado.getEstado());
        assertTrue(resultado.getMotivo().contains("insumo #99"));
    }
}
