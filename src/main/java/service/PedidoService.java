package service;

import dao.InsumoDao;
import dao.PedidoDao;
import dao.PlatoDao;
import dao.RestauranteDao;
import model.EstadoPedido;
import model.IngredientePlato;
import model.Insumo;
import model.Pedido;
import model.Plato;
import model.Restaurante;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Logica de negocio del tablero Kanban de pedidos: consulta por estado
 * para alimentar las columnas y avance/retroceso del ciclo de vida.
 */
public class PedidoService {

    private final PedidoDao pedidoDao;
    private final PlatoDao platoDao;
    private final InsumoDao insumoDao;
    private final RestauranteDao restauranteDao;
    private final ConversionUnidades conversionUnidades;
    private final AlertaStockService alertaStockService;

    public PedidoService() {
        this(new PedidoDao());
    }

    public PedidoService(PedidoDao pedidoDao) {
        this(pedidoDao, new PlatoDao(), new InsumoDao(), new RestauranteDao());
    }

    public PedidoService(PedidoDao pedidoDao, PlatoDao platoDao, InsumoDao insumoDao) {
        this(pedidoDao, platoDao, insumoDao, new RestauranteDao());
    }

    public PedidoService(PedidoDao pedidoDao, PlatoDao platoDao, InsumoDao insumoDao, RestauranteDao restauranteDao) {
        this(pedidoDao, platoDao, insumoDao, restauranteDao, new AlertaStockService());
    }

    public PedidoService(PedidoDao pedidoDao, PlatoDao platoDao, InsumoDao insumoDao,
                          RestauranteDao restauranteDao, AlertaStockService alertaStockService) {
        this.pedidoDao = pedidoDao;
        this.platoDao = platoDao;
        this.insumoDao = insumoDao;
        this.restauranteDao = restauranteDao;
        this.conversionUnidades = new ConversionUnidades();
        this.alertaStockService = alertaStockService;
    }

    /** Pedidos de un estado concreto, usado para pintar cada columna del tablero. */
    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        return pedidoDao.buscarPorEstado(estado);
    }

    /** Columna inicial del tablero. */
    public List<Pedido> listarPedidosRecibidos() {
        return listarPorEstado(EstadoPedido.RECIBIDO);
    }

    /** Columna ENTREGADO del tablero: solo los mas recientes, para no sobrecargarla. */
    public List<Pedido> listarEntregadosRecientes(int limite) {
        List<Pedido> todos = pedidoDao.buscarPorEstadoRecientePrimero(EstadoPedido.ENTREGADO);
        return todos.size() > limite ? todos.subList(0, limite) : todos;
    }

    /** Historial completo de entregados (mas reciente primero), para el modal de historial. */
    public List<Pedido> listarHistorialEntregados() {
        return pedidoDao.buscarPorEstadoRecientePrimero(EstadoPedido.ENTREGADO);
    }

    public List<Pedido> listarTodos() {
        return pedidoDao.listarTodos();
    }

    public Pedido buscar(int id) {
        return pedidoDao.buscarPorId(id);
    }

    /**
     * Crea manualmente un pedido nuevo a partir de un plato existente,
     * simulando la llegada de un pedido desde el pase de cocina. Queda
     * en estado RECIBIDO, igual que un pedido real.
     * @throws IllegalArgumentException si el plato no existe.
     */
    public Pedido crearPedidoManual(int platoId) {
        Plato plato = platoDao.buscarPorId(platoId);
        if (plato == null) {
            throw new IllegalArgumentException("Debe seleccionar un plato valido");
        }
        Restaurante restaurante = restauranteDao.buscarPorId(plato.getRestauranteId());
        String marca = (restaurante != null) ? restaurante.getNombre() : "Generica";
        Pedido pedido = new Pedido(0, plato.getNombre(), marca, EstadoPedido.RECIBIDO, plato.getId());
        return pedidoDao.guardar(pedido);
    }

    /**
     * Avanza el pedido al siguiente estado del flujo y persiste el cambio.
     * @throws IllegalArgumentException si el pedido no existe.
     * @throws IllegalStateException    si el pedido ya esta ENTREGADO.
     */
    public Pedido avanzarEstado(int pedidoId) {
        Pedido pedido = pedidoDao.buscarPorId(pedidoId);
        if (pedido == null) {
            throw new IllegalArgumentException("No existe el pedido " + pedidoId);
        }
        validarYDescontarStockParaPreparacion(pedido);
        pedido.setEstado(pedido.getEstado().siguiente());
        pedidoDao.actualizar(pedido);
        return pedido;
    }

    /** Alias de negocio usado por la accion "mover" del tablero. */
    public Pedido moverPedido(int pedidoId) {
        return avanzarEstado(pedidoId);
    }

    /**
     * Retrocede el pedido al estado anterior cuando se avanzo por error (HU20)
     * y persiste el cambio.
     * @throws IllegalArgumentException si el pedido no existe ("Pedido no encontrado").
     * @throws IllegalStateException    si esta en RECIBIDO o ENTREGADO.
     */
    public Pedido retrocederEstado(int pedidoId) {
        Pedido pedido = pedidoDao.buscarPorId(pedidoId);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido no encontrado");
        }
        pedido.setEstado(pedido.getEstado().anterior());
        pedidoDao.actualizar(pedido);
        return pedido;
    }

    /**
     * Valida que haya stock suficiente para cada ingrediente de la receta
     * del plato (convirtiendo la unidad de la receta a la unidad del insumo
     * cuando es necesario) y, si alcanza, descuenta esa cantidad del
     * inventario (HU30: "el sistema... descuente el inventario al
     * prepararlo"). No hace nada si el pedido no esta en RECIBIDO o no
     * tiene un plato asociado.
     * @throws StockInsuficienteException si falta stock de algun ingrediente;
     *         en ese caso no se descuenta nada.
     */
    private void validarYDescontarStockParaPreparacion(Pedido pedido) {
        if (pedido.getEstado() != EstadoPedido.RECIBIDO || pedido.getPlatoId() <= 0) {
            return;
        }
        Plato plato = platoDao.buscarPorId(pedido.getPlatoId());
        if (plato == null) {
            return;
        }
        List<String> faltantes = new ArrayList<>();
        Map<Insumo, Double> aDescontar = new LinkedHashMap<>();

        for (IngredientePlato ingrediente : plato.getIngredientes()) {
            Insumo insumo = insumoDao.buscarPorId(ingrediente.getInsumoId());
            if (insumo == null) {
                faltantes.add("insumo #" + ingrediente.getInsumoId());
                continue;
            }
            double cantidadRequerida = convertirALaUnidadDelInsumo(ingrediente, insumo);
            if (Numeros.redondear(insumo.getStock()) < Numeros.redondear(cantidadRequerida)) {
                faltantes.add(insumo.getNombre() + " (disponible: "
                        + formatear(insumo.getStock()) + " " + insumo.getUnidad()
                        + ", requerido: " + formatear(cantidadRequerida) + " " + insumo.getUnidad() + ")");
            } else {
                aDescontar.merge(insumo, cantidadRequerida, Double::sum);
            }
        }

        if (!faltantes.isEmpty()) {
            throw new StockInsuficienteException(plato.getNombre(), faltantes);
        }

        for (Map.Entry<Insumo, Double> entry : aDescontar.entrySet()) {
            Insumo insumo = entry.getKey();
            insumo.setStock(Numeros.redondear(insumo.getStock() - entry.getValue()));
            insumoDao.actualizar(insumo);
            if (alertaStockService != null) {
                alertaStockService.evaluarYRegistrar(insumo);
            }
        }
    }

    /** Convierte la cantidad de la receta a la unidad en la que se almacena el insumo. */
    private double convertirALaUnidadDelInsumo(IngredientePlato ingrediente, Insumo insumo) {
        String unidadReceta = ingrediente.getUnidadReceta();
        if (unidadReceta == null || unidadReceta.equals(insumo.getUnidad())) {
            return ingrediente.getCantidad();
        }
        return conversionUnidades.convertir(ingrediente.getCantidad(), unidadReceta, insumo.getUnidad());
    }

    private String formatear(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.format(java.util.Locale.US, "%.2f", valor);
    }
}