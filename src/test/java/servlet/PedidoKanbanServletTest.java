package servlet;

import dao.InsumoDao;
import dao.PedidoDao;
import dao.PlatoDao;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.EstadoPedido;
import model.IngredientePlato;
import model.Insumo;
import model.Pedido;
import model.Plato;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU20 / tablero Kanban): PedidoKanbanServlet arma las columnas del
 * tablero por estado en GET, y en POST procesa las acciones "mover" y
 * "retroceder", dejando el error en sesion cuando el id es invalido, cuando
 * falta stock (StockInsuficienteException) o ante cualquier otra regla de
 * negocio violada. Usa el PedidoDao real (estatico) para sembrar pedidos de
 * prueba propios y no depender de los ids sembrados por otras pruebas.
 */
@ExtendWith(MockitoExtension.class)
class PedidoKanbanServletTest {

    private final PedidoKanbanServlet servlet = new PedidoKanbanServlet();
    private final PedidoDao pedidoDao = new PedidoDao();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private HttpSession session;

    @Test
    void doGetArmaUnaColumnaPorEstadoYExponeLaPolicyYReenviaAlTablero() throws Exception {
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getRequestDispatcher("/views/cu2-pedidos-kanban.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        for (EstadoPedido estado : EstadoPedido.values()) {
            verify(request).setAttribute(eq("col_" + estado.name()), notNull());
        }
        verify(request).setAttribute(eq("policy"), notNull());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetConRolValidoLoGuardaEnSesion() throws Exception {
        when(request.getParameter("rol")).thenReturn("cocinero");
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("/views/cu2-pedidos-kanban.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(session).setAttribute("rol", "COCINERO");
    }

    @Test
    void doPostMoverAvanzaUnPedidoSinPlatoAsociadoYRedirige() throws Exception {
        Pedido pedido = pedidoDao.guardar(new Pedido(0, "Pedido de prueba sin plato", "Generica",
                EstadoPedido.RECIBIDO, 0));
        when(request.getParameter("accion")).thenReturn("mover");
        when(request.getParameter("pedidoId")).thenReturn(String.valueOf(pedido.getId()));
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/pedidos");
        org.junit.jupiter.api.Assertions.assertEquals(EstadoPedido.EN_PREPARACION,
                pedidoDao.buscarPorId(pedido.getId()).getEstado());
    }

    @Test
    void doPostMoverConStockInsuficienteDejaElErrorYFaltantesEnSesion() throws Exception {
        // Insumo y plato propios, con stock deliberadamente insuficiente para la receta.
        Insumo insumoEscaso = new InsumoDao().guardar(
                new Insumo(0, "Insumo escaso test " + System.nanoTime(), "kg", 1.0, 1.0, 1.0));
        Plato platoSinStock = new PlatoDao().guardar(new Plato(0, "Plato sin stock test " + System.nanoTime(),
                0, java.util.List.of(new IngredientePlato(insumoEscaso.getId(), 999, "kg"))));
        Pedido pedido = pedidoDao.guardar(new Pedido(0, "Pedido sin stock suficiente", "Marca test",
                EstadoPedido.RECIBIDO, platoSinStock.getId()));
        when(request.getParameter("accion")).thenReturn("mover");
        when(request.getParameter("pedidoId")).thenReturn(String.valueOf(pedido.getId()));
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("error"), anyString());
        verify(session).setAttribute(eq("stockFaltantes"), notNull());
        verify(response).sendRedirect("/pedidos");
    }

    @Test
    void doPostRetrocederDesdeEnPreparacionVuelveARecibido() throws Exception {
        Pedido pedido = pedidoDao.guardar(new Pedido(0, "Pedido en preparacion", "Generica",
                EstadoPedido.EN_PREPARACION, 0));
        when(request.getParameter("accion")).thenReturn("retroceder");
        when(request.getParameter("pedidoId")).thenReturn(String.valueOf(pedido.getId()));
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        org.junit.jupiter.api.Assertions.assertEquals(EstadoPedido.RECIBIDO,
                pedidoDao.buscarPorId(pedido.getId()).getEstado());
        verify(response).sendRedirect("/pedidos");
    }

    @Test
    void doPostRetrocederDesdeRecibidoDejaElErrorEnSesion() throws Exception {
        Pedido pedido = pedidoDao.guardar(new Pedido(0, "Pedido recien recibido", "Generica",
                EstadoPedido.RECIBIDO, 0));
        when(request.getParameter("accion")).thenReturn("retroceder");
        when(request.getParameter("pedidoId")).thenReturn(String.valueOf(pedido.getId()));
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("error"), anyString());
        verify(response).sendRedirect("/pedidos");
    }

    @Test
    void doPostConIdInvalidoDejaMensajeDeErrorYRedirigeConRol() throws Exception {
        when(request.getParameter("accion")).thenReturn("mover");
        when(request.getParameter("pedidoId")).thenReturn("no-es-numero");
        when(request.getParameter("rol")).thenReturn("administrador");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "Identificador de pedido invalido");
        verify(response).sendRedirect("/pedidos?rol=ADMINISTRADOR");
    }
}
