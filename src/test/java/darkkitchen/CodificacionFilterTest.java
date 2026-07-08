package darkkitchen;

import config.CodificacionFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * TDD (tarea tecnica de codificacion): CodificacionFilter fuerza UTF-8 tanto
 * en la peticion como en la respuesta y siempre continua la cadena de
 * filtros, sin importar el tipo concreto de request/response.
 */
@ExtendWith(MockitoExtension.class)
class CodificacionFilterTest {

    private final CodificacionFilter filtro = new CodificacionFilter();

    @Mock
    private ServletRequest request;

    @Mock
    private ServletResponse response;

    @Mock
    private FilterChain chain;

    @Test
    void fuerzaUtf8EnRequestYResponseYContinuaLaCadena() throws Exception {
        filtro.doFilter(request, response, chain);

        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
        verify(chain).doFilter(request, response);
        verifyNoMoreInteractions(chain);
    }
}
