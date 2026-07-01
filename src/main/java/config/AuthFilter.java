package config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Rol;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/**
 * Control de acceso por rol (Tarea Tecnica de roles + HU9/HU10/HU23). Protege
 * las rutas sensibles permitiendo solo los roles autorizados; si el rol actual
 * no tiene permiso, reenvia a la pantalla de acceso denegado.
 *
 * El rol vigente se toma del parametro "rol" o del atributo de sesion "rol"
 * (el sistema no implementa login real). Solo se reconocen los tres roles
 * validos: COCINERO, ADMIN_BODEGA y ADMINISTRADOR.
 */
@WebFilter(urlPatterns = {"/monitoreo", "/disponibilidad", "/insumos/crear"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ruta = request.getServletPath();
        Rol rol = resolverRol(request);

        if (!tienePermiso(ruta, rol)) {
            request.setAttribute("mensajeAcceso", "No tienes permisos para acceder a esta seccion");
            request.getRequestDispatcher("/views/acceso-denegado.jsp").forward(request, response);
            return;
        }
        chain.doFilter(req, res);
    }

    /** Determina si el rol puede acceder a la ruta protegida. */
    private boolean tienePermiso(String ruta, Rol rol) {
        if (rol == null) {
            return false;
        }
        Set<Rol> permitidos;
        switch (ruta) {
            case "/monitoreo":          // HU9: panel de bodega (COCINERO denegado)
                permitidos = EnumSet.of(Rol.ADMIN_BODEGA, Rol.ADMINISTRADOR);
                break;
            case "/disponibilidad":     // HU10: vista del cocinero (ADMIN_BODEGA denegado)
                permitidos = EnumSet.of(Rol.COCINERO, Rol.ADMINISTRADOR);
                break;
            case "/insumos/crear":      // HU23: alta de insumos (COCINERO denegado)
                permitidos = EnumSet.of(Rol.ADMIN_BODEGA, Rol.ADMINISTRADOR);
                break;
            default:
                permitidos = EnumSet.allOf(Rol.class);
        }
        return permitidos.contains(rol);
    }

    /** Lee el rol del parametro de la peticion o, en su defecto, de la sesion. */
    private Rol resolverRol(HttpServletRequest request) {
        Rol rol = Rol.desde(request.getParameter("rol"));
        if (rol == null && request.getSession(false) != null) {
            rol = Rol.desde((String) request.getSession().getAttribute("rol"));
        }
        return rol;
    }

    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
