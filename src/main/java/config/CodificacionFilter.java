package config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

/**
 * Fuerza la codificacion UTF-8 en todas las peticiones y respuestas
 * para que los acentos del dominio (En preparacion, etc.) se muestren bien.
 * Registrado en web.xml (no con @WebFilter) para garantizar que se ejecute
 * antes que AuthFilter: setCharacterEncoding() no tiene efecto si algun otro
 * filtro ya leyo un parametro de la peticion antes que este.
 */
public class CodificacionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
