package darkkitchen;

import model.Proveedor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TDD (tarea tecnica model): Proveedor expone getters/setters para todos
 * sus campos (id, nombre, telefono y correo de contacto).
 */
class ProveedorModeloTest {

    @Test
    void constructorConDatosAsignaTodosLosCampos() {
        Proveedor proveedor = new Proveedor(1, "Distribuidora Sierra", "0987654321", "contacto@sierra.com");

        assertEquals(1, proveedor.getId());
        assertEquals("Distribuidora Sierra", proveedor.getNombre());
        assertEquals("0987654321", proveedor.getTelefono());
        assertEquals("contacto@sierra.com", proveedor.getCorreo());
    }

    @Test
    void constructorVacioPermiteUsarSetters() {
        Proveedor proveedor = new Proveedor();

        proveedor.setId(2);
        proveedor.setNombre("Proveedor Nuevo");
        proveedor.setTelefono("0991234567");
        proveedor.setCorreo("nuevo@correo.com");

        assertEquals(2, proveedor.getId());
        assertEquals("Proveedor Nuevo", proveedor.getNombre());
        assertEquals("0991234567", proveedor.getTelefono());
        assertEquals("nuevo@correo.com", proveedor.getCorreo());
    }
}
