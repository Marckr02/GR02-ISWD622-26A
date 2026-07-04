package darkkitchen;

import dao.PlatoDao;
import dao.RestauranteDao;
import model.Restaurante;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.RestauranteService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU26): registrarRestaurante() persiste el restaurante con datos
 * validos y lanza excepcion para nombre duplicado, descripcion mayor a 255
 * caracteres y caracteres especiales en el nombre.
 */
@ExtendWith(MockitoExtension.class)
class RestauranteRegistroTest {

    @Mock
    private RestauranteDao restauranteDao;

    @Mock
    private PlatoDao platoDao;

    @InjectMocks
    private RestauranteService restauranteService;

    @Test
    void persisteRestauranteConDatosValidos() {
        when(restauranteDao.buscarPorNombre("Green Bowl")).thenReturn(null);
        when(restauranteDao.guardar(any(Restaurante.class))).thenAnswer(inv -> inv.getArgument(0));

        Restaurante creado = restauranteService.registrarRestaurante("Green Bowl", "Bowls saludables y veganos.");

        assertEquals("Green Bowl", creado.getNombre());
        assertEquals("Bowls saludables y veganos.", creado.getDescripcion());
        verify(restauranteDao).guardar(any(Restaurante.class));
    }

    @Test
    void lanzaExcepcionSiNombreDuplicado() {
        when(restauranteDao.buscarPorNombre("Napoli")).thenReturn(new Restaurante(1, "Napoli", "Pizzeria"));

        assertThrows(IllegalArgumentException.class,
                () -> restauranteService.registrarRestaurante("Napoli", "Otra pizzeria"));
    }

    @Test
    void lanzaExcepcionSiDescripcionSuperaLosDoscientosCincuentaYCincoCaracteres() {
        when(restauranteDao.buscarPorNombre("Crispy House")).thenReturn(null);
        String descripcionLarga = "a".repeat(256);

        assertThrows(IllegalArgumentException.class,
                () -> restauranteService.registrarRestaurante("Crispy House", descripcionLarga));
    }

    @Test
    void lanzaExcepcionConCaracteresEspecialesEnElNombre() {
        assertThrows(IllegalArgumentException.class,
                () -> restauranteService.registrarRestaurante("Cocina <>{}", "Descripcion valida"));
    }
}
