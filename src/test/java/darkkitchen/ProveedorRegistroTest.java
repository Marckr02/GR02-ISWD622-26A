package darkkitchen;

import dao.InsumoDao;
import dao.ProveedorDao;
import model.Proveedor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.ProveedorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU12): registrarProveedor() persiste el proveedor con datos validos
 * y lanza excepcion para nombre duplicado, telefono invalido y correo invalido.
 */
@ExtendWith(MockitoExtension.class)
class ProveedorRegistroTest {

    @Mock
    private ProveedorDao proveedorDao;

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private ProveedorService proveedorService;

    @Test
    void persisteProveedorConDatosValidos() {
        when(proveedorDao.buscarPorNombre("Distribuidora Sierra")).thenReturn(null);
        when(proveedorDao.guardar(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        Proveedor creado = proveedorService.registrarProveedor(
                "Distribuidora Sierra", "0987654321", "contacto@sierra.com");

        assertEquals("Distribuidora Sierra", creado.getNombre());
        assertEquals("0987654321", creado.getTelefono());
        assertEquals("contacto@sierra.com", creado.getCorreo());
        verify(proveedorDao).guardar(any(Proveedor.class));
    }

    @Test
    void lanzaExcepcionSiNombreDuplicado() {
        when(proveedorDao.buscarPorNombre("Andina")).thenReturn(new Proveedor(1, "Andina", "0991234567", "a@a.com"));

        assertThrows(IllegalArgumentException.class,
                () -> proveedorService.registrarProveedor("Andina", "0991234567", "a@a.com"));
    }

    @Test
    void lanzaExcepcionConTelefonoInvalido() {
        when(proveedorDao.buscarPorNombre("Verde Suministros")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> proveedorService.registrarProveedor("Verde Suministros", "abc123", "verde@correo.com"));
    }

    @Test
    void lanzaExcepcionConCorreoInvalido() {
        when(proveedorDao.buscarPorNombre("Norte Insumos")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> proveedorService.registrarProveedor("Norte Insumos", "0987001122", "correo-invalido"));
    }
}
