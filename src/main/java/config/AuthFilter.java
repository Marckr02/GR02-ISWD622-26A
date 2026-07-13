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
 * Control de acceso por rol. Cada seccion sensible solo admite los roles que
 * le corresponden:
 *   - Inventario (ver, registrar entrada, reducir stock, crear, editar,
 *     eliminar, vincular proveedor), menu, panel de monitoreo y
 *     proveedores: unicamente ADMIN_BODEGA.
 *   - Consulta de disponibilidad del cocinero: COCINERO.
 *   - Restaurantes, platos y el historial de alertas: unicamente ADMINISTRADOR.
 * El tablero (/pedidos) queda abierto como pantalla de inicio.
 *
 * El rol vigente se toma del parametro "rol" o del atributo de sesion "rol"
 * (el sistema no implementa login real).
 */
@WebFilter(urlPatterns = {
        "/insumos", "/menu", "/monitoreo", "/disponibilidad",
        "/proveedores", "/restaurantes", "/platos", "/alertas",
        "/metricas", "/reporte"
})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        Rol rol = resolverRol(request);

        if (!tienePermiso(request.getServletPath(), rol)) {
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
            case "/insumos":        // ver inventario, registrar entrada, reducir stock, crear, editar, eliminar, vincular proveedor
            case "/menu":           // menu y disponibilidad general
            case "/monitoreo":      // panel de monitoreo
            case "/proveedores":    // gestion de proveedores
                permitidos = EnumSet.of(Rol.ADMIN_BODEGA);
                break;
            case "/disponibilidad": // vista del cocinero
                permitidos = EnumSet.of(Rol.COCINERO);
                break;
            case "/restaurantes":   // gestion de restaurantes
            case "/platos":         // gestion de platos y recetas
            case "/alertas":        // historial de alertas de stock critico
            case "/metricas":       // metricas de rendimiento por restaurante (F5.1)
            case "/reporte":        // exportacion de metricas a PDF (F5.2)
                permitidos = EnumSet.of(Rol.ADMINISTRADOR);
                break;
            default:
                permitidos = EnumSet.allOf(Rol.class);
        }
        return permitidos.contains(rol);
    }

    /** Lee el rol del parametro de la peticion o, en su defecto, de la sesion. */
    private Rol resolverRol(HttpServletRequest request) {
        Rol rol = Rol.desde(request.getParameter("rol"));
        if (rol != null) {
            request.getSession().setAttribute("rol", rol.name());
            return rol;
        }
        if (request.getSession(false) != null) {
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