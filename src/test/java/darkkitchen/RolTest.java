package darkkitchen;

import model.Rol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TDD (Tarea Tecnica de correccion de roles): Rol.desde() reconoce los tres
 * roles validos de forma tolerante a mayusculas/minusculas y espacios, y
 * devuelve null para cualquier valor que no coincida con ninguno de ellos.
 */
class RolTest {

    @ParameterizedTest
    @EnumSource(Rol.class)
    void desdeReconoceElNombreExactoDeCadaRolValido(Rol rol) {
        assertEquals(rol, Rol.desde(rol.name()));
    }

    @ParameterizedTest
    @CsvSource({
            "cocinero, COCINERO",
            "Cocinero, COCINERO",
            "admin_bodega, ADMIN_BODEGA",
            "'admin bodega', ADMIN_BODEGA",
            "'Admin Bodega', ADMIN_BODEGA",
            "administrador, ADMINISTRADOR",
            "ADMINISTRADOR, ADMINISTRADOR",
            "'  cocinero  ', COCINERO"
    })
    void desdeEsTolerantePorEspaciosYMayusculas(String entrada, Rol esperado) {
        assertEquals(esperado, Rol.desde(entrada));
    }

    @ParameterizedTest
    @ValueSource(strings = {"repartidor", "invitado", "", "   ", "ADMIN-BODEGA"})
    void desdeDevuelveNullParaTextosInvalidos(String entrada) {
        assertNull(Rol.desde(entrada));
    }

    @ParameterizedTest
    @NullSource
    void desdeDevuelveNullParaValorNulo(String entrada) {
        assertNull(Rol.desde(entrada));
    }

    @Test
    void getEtiquetaDevuelveElTextoLegibleDeCadaRol() {
        assertEquals("Cocinero", Rol.COCINERO.getEtiqueta());
        assertEquals("Administrador de bodega", Rol.ADMIN_BODEGA.getEtiqueta());
        assertEquals("Administrador", Rol.ADMINISTRADOR.getEtiqueta());
    }
}
