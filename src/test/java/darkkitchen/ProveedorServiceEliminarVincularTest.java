package darkkitchen;

import dao.InsumoDao;
import dao.ProveedorDao;
import model.Insumo;
import model.Proveedor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.ProveedorService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU6/HU25): eliminarProveedor() bloquea la eliminacion si tiene
 * insumos vinculados, vincularAInsumo() valida que ambos existan, y
 * obtenerProveedorDe() devuelve null si el insumo no tiene proveedor
 * asociado.
 */
@ExtendWith(MockitoExtension.class)
class ProveedorServiceEliminarVincularTest {

    @Mock
    private ProveedorDao proveedorDao;

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private ProveedorService proveedorService;

    @Test
    void listarProveedoresDelegaEnElDao() {
        List<Proveedor> lista = List.of(new Proveedor(1, "Andina", "0991234567", "a@a.com"));
        when(proveedorDao.listarTodos()).thenReturn(lista);

        assertEquals(lista, proveedorService.listarProveedores());
    }

    @Test
    void buscarDelegaEnElDao() {
        Proveedor proveedor = new Proveedor(2, "AgroFresh", "0987654321", "b@b.com");
        when(proveedorDao.buscarPorId(2)).thenReturn(proveedor);

        assertEquals(proveedor, proveedorService.buscar(2));
    }

    @Test
    void eliminarProveedorLanzaExcepcionSiNoExiste() {
        when(proveedorDao.buscarPorId(404)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> proveedorService.eliminarProveedor(404));
    }

    @Test
    void eliminarProveedorLanzaExcepcionSiTieneInsumosVinculados() {
        Proveedor proveedor = new Proveedor(3, "Con Vinculos", "0991112222", "c@c.com");
        when(proveedorDao.buscarPorId(3)).thenReturn(proveedor);
        when(proveedorDao.tieneInsumosVinculados(3)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> proveedorService.eliminarProveedor(3));
    }

    @Test
    void eliminarProveedorEliminaSiNoTieneVinculos() {
        Proveedor proveedor = new Proveedor(4, "Sin Vinculos", "0991113333", "d@d.com");
        when(proveedorDao.buscarPorId(4)).thenReturn(proveedor);
        when(proveedorDao.tieneInsumosVinculados(4)).thenReturn(false);

        proveedorService.eliminarProveedor(4);

        verify(proveedorDao).eliminar(4);
    }

    @Test
    void vincularAInsumoLanzaExcepcionSiElInsumoNoExiste() {
        when(insumoDao.buscarPorId(10)).thenReturn(null);
        when(proveedorDao.buscarPorId(5)).thenReturn(new Proveedor(5, "Proveedor", "0991114444", "e@e.com"));

        assertThrows(IllegalArgumentException.class, () -> proveedorService.vincularAInsumo(10, 5));
    }

    @Test
    void vincularAInsumoLanzaExcepcionSiElProveedorNoExiste() {
        when(insumoDao.buscarPorId(11)).thenReturn(new Insumo(11, "Harina", "kg", 5.0, 1.0));
        when(proveedorDao.buscarPorId(99)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> proveedorService.vincularAInsumo(11, 99));
    }

    @Test
    void vincularAInsumoVinculaCuandoAmbosExisten() {
        when(insumoDao.buscarPorId(12)).thenReturn(new Insumo(12, "Queso", "kg", 5.0, 1.0));
        when(proveedorDao.buscarPorId(6)).thenReturn(new Proveedor(6, "Proveedor", "0991115555", "f@f.com"));

        proveedorService.vincularAInsumo(12, 6);

        verify(proveedorDao).vincularInsumo(12, 6);
    }

    @Test
    void obtenerProveedorDeDevuelveNullSiNoHayVinculo() {
        when(proveedorDao.obtenerProveedorIdDeInsumo(13)).thenReturn(null);

        assertNull(proveedorService.obtenerProveedorDe(13));
    }

    @Test
    void obtenerProveedorDeDevuelveElProveedorVinculado() {
        Proveedor proveedor = new Proveedor(7, "Vinculado", "0991116666", "g@g.com");
        when(proveedorDao.obtenerProveedorIdDeInsumo(14)).thenReturn(7);
        when(proveedorDao.buscarPorId(7)).thenReturn(proveedor);

        assertEquals(proveedor, proveedorService.obtenerProveedorDe(14));
    }
}
