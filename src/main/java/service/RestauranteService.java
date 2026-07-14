package service;

import dao.PedidoDao;
import dao.PlatoDao;
import dao.RestauranteDao;
import model.Restaurante;

import java.util.List;

/**
 * Logica de negocio de restaurantes (marcas) que operan en la Dark Kitchen (F6.1).
 */
public class RestauranteService {

    private static final String PATRON_NOMBRE = "[\\p{L}\\p{N} \\-]{2,100}";
    private static final int MAX_DESCRIPCION = 255;
    private static final String PATRON_COLOR = "#[0-9A-Fa-f]{6}";

    private final RestauranteDao restauranteDao;
    private final PlatoDao platoDao;
    private final PedidoDao pedidoDao;

    public RestauranteService() {
        this(new RestauranteDao(), new PlatoDao(), new PedidoDao());
    }

    public RestauranteService(RestauranteDao restauranteDao, PlatoDao platoDao) {
        this(restauranteDao, platoDao, new PedidoDao());
    }

    public RestauranteService(RestauranteDao restauranteDao, PlatoDao platoDao, PedidoDao pedidoDao) {
        this.restauranteDao = restauranteDao;
        this.platoDao = platoDao;
        this.pedidoDao = pedidoDao;
    }

    public List<Restaurante> listarRestaurantes() {
        return restauranteDao.listarTodos();
    }

    public Restaurante buscar(int id) {
        return restauranteDao.buscarPorId(id);
    }

    /**
     * Registra un restaurante nuevo (HU26). Nombre obligatorio (2-100
     * caracteres, unico); descripcion opcional hasta 255 caracteres; sin color de marca.
     */
    public Restaurante registrarRestaurante(String nombre, String descripcion) {
        return registrarRestaurante(nombre, descripcion, null);
    }

    /**
     * Registra un restaurante nuevo con color de marca (HU26 + selector de color).
     * @param color hexadecimal "#RRGGBB", o null/vacio si no se asigna uno.
     */
    public Restaurante registrarRestaurante(String nombre, String descripcion, String color) {
        String nombreLimpio = validarNombre(nombre, -1);
        String descripcionLimpia = validarDescripcion(descripcion);
        String colorLimpio = validarColor(color);
        return restauranteDao.guardar(new Restaurante(0, nombreLimpio, descripcionLimpia, colorLimpio));
    }

    /**
     * Actualiza un restaurante existente (HU28), sin tocar su color de marca actual.
     * @throws IllegalArgumentException si el restaurante no existe o los datos son invalidos.
     */
    public Restaurante actualizarRestaurante(int id, String nombre, String descripcion) {
        Restaurante actual = restauranteDao.buscarPorId(id);
        if (actual == null) {
            throw new IllegalArgumentException("El restaurante indicado no existe en el sistema");
        }
        return actualizarRestaurante(id, nombre, descripcion, actual.getColor());
    }

    /**
     * Actualiza un restaurante existente, incluyendo su color de marca (HU28 + selector de color).
     * @param color hexadecimal "#RRGGBB", o null/vacio para dejar el restaurante sin color asignado.
     * @throws IllegalArgumentException si el restaurante no existe o los datos son invalidos.
     */
    public Restaurante actualizarRestaurante(int id, String nombre, String descripcion, String color) {
        Restaurante restaurante = restauranteDao.buscarPorId(id);
        if (restaurante == null) {
            throw new IllegalArgumentException("El restaurante indicado no existe en el sistema");
        }
        String nombreAnterior = restaurante.getNombre();
        String nombreLimpio = validarNombre(nombre, id);
        String descripcionLimpia = validarDescripcion(descripcion);
        String colorLimpio = validarColor(color);
        restaurante.setNombre(nombreLimpio);
        restaurante.setDescripcion(descripcionLimpia);
        restaurante.setColor(colorLimpio);
        restauranteDao.actualizar(restaurante);
        // El tablero Kanban guarda el nombre de la marca como texto en cada pedido
        // (no es una referencia viva): si el nombre cambio, hay que propagarlo a los
        // pedidos ya existentes para que sigan mostrando la marca correcta.
        if (!nombreLimpio.equals(nombreAnterior)) {
            pedidoDao.renombrarMarca(nombreAnterior, nombreLimpio);
        }
        return restaurante;
    }

    /**
     * Elimina un restaurante (HU29).
     * @throws IllegalArgumentException si no existe.
     * @throws IllegalStateException    si tiene platos asociados.
     */
    public void eliminarRestaurante(int id) {
        Restaurante restaurante = restauranteDao.buscarPorId(id);
        if (restaurante == null) {
            throw new IllegalArgumentException("El restaurante indicado no existe en el sistema");
        }
        if (platoDao.existePlatoConRestaurante(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar este restaurante porque tiene platos asociados. "
                            + "Elimine o reasigne los platos antes de eliminarlo");
        }
        restauranteDao.eliminar(id);
    }

    private String validarNombre(String nombre, int idActual) {
        String limpio = (nombre == null) ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (!limpio.matches(PATRON_NOMBRE)) {
            throw new IllegalArgumentException("Nombre invalido");
        }
        Restaurante duplicado = restauranteDao.buscarPorNombre(limpio);
        if (duplicado != null && duplicado.getId() != idActual) {
            throw new IllegalArgumentException("Ya existe un restaurante con ese nombre");
        }
        return limpio;
    }

    /** Color de marca opcional: si viene informado debe ser un hex "#RRGGBB" valido. */
    private String validarColor(String color) {
        String limpio = (color == null) ? "" : color.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        if (!limpio.matches(PATRON_COLOR)) {
            throw new IllegalArgumentException("El color debe ser hexadecimal, por ejemplo #F97316");
        }
        return limpio.toUpperCase(java.util.Locale.ROOT);
    }

    private String validarDescripcion(String descripcion) {
        String limpio = (descripcion == null) ? "" : descripcion.trim();
        if (limpio.length() > MAX_DESCRIPCION) {
            throw new IllegalArgumentException("La descripcion no puede superar los 255 caracteres");
        }
        return limpio;
    }
}
