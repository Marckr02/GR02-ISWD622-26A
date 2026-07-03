package service;

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

    private final RestauranteDao restauranteDao;
    private final PlatoDao platoDao;

    public RestauranteService() {
        this(new RestauranteDao(), new PlatoDao());
    }

    public RestauranteService(RestauranteDao restauranteDao, PlatoDao platoDao) {
        this.restauranteDao = restauranteDao;
        this.platoDao = platoDao;
    }

    public List<Restaurante> listarRestaurantes() {
        return restauranteDao.listarTodos();
    }

    public Restaurante buscar(int id) {
        return restauranteDao.buscarPorId(id);
    }

    /**
     * Registra un restaurante nuevo (HU26). Nombre obligatorio (2-100
     * caracteres, unico); descripcion opcional hasta 255 caracteres.
     */
    public Restaurante registrarRestaurante(String nombre, String descripcion) {
        String nombreLimpio = validarNombre(nombre, -1);
        String descripcionLimpia = validarDescripcion(descripcion);
        return restauranteDao.guardar(new Restaurante(0, nombreLimpio, descripcionLimpia));
    }

    /**
     * Actualiza un restaurante existente (HU28).
     * @throws IllegalArgumentException si el restaurante no existe o los datos son invalidos.
     */
    public Restaurante actualizarRestaurante(int id, String nombre, String descripcion) {
        Restaurante restaurante = restauranteDao.buscarPorId(id);
        if (restaurante == null) {
            throw new IllegalArgumentException("El restaurante indicado no existe en el sistema");
        }
        String nombreLimpio = validarNombre(nombre, id);
        String descripcionLimpia = validarDescripcion(descripcion);
        restaurante.setNombre(nombreLimpio);
        restaurante.setDescripcion(descripcionLimpia);
        restauranteDao.actualizar(restaurante);
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

    private String validarDescripcion(String descripcion) {
        String limpio = (descripcion == null) ? "" : descripcion.trim();
        if (limpio.length() > MAX_DESCRIPCION) {
            throw new IllegalArgumentException("La descripcion no puede superar los 255 caracteres");
        }
        return limpio;
    }
}
