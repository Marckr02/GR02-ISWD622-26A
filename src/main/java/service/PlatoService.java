package service;

import dao.InsumoDao;
import dao.PlatoDao;
import dao.RestauranteDao;
import model.IngredientePlato;
import model.Insumo;
import model.Plato;
import model.Restaurante;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Logica de negocio de los platos del menu y su receta (F6.2): alta,
 * consulta, modificacion y eliminacion, con validacion de ingredientes.
 */
public class PlatoService {

    private static final String PATRON_NOMBRE = "[\\p{L}\\p{N} \\-]{2,100}";
    private static final List<String> UNIDADES_RECETA = List.of("g", "kg", "ml", "l", "unidades");

    private final PlatoDao platoDao;
    private final RestauranteDao restauranteDao;
    private final InsumoDao insumoDao;
    private final ConversionUnidades conversionUnidades = new ConversionUnidades();

    public PlatoService() {
        this(new PlatoDao(), new RestauranteDao(), new InsumoDao());
    }

    public PlatoService(PlatoDao platoDao, RestauranteDao restauranteDao, InsumoDao insumoDao) {
        this.platoDao = platoDao;
        this.restauranteDao = restauranteDao;
        this.insumoDao = insumoDao;
    }

    /** Listado ordenado alfabeticamente (HU31). */
    public List<Plato> listarPlatos() {
        return platoDao.listarTodos().stream()
                .sorted(Comparator.comparing(Plato::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Plato buscar(int id) {
        return platoDao.buscarPorId(id);
    }

    public Restaurante restauranteDe(Plato plato) {
        return restauranteDao.buscarPorId(plato.getRestauranteId());
    }

    public Insumo insumoDe(int insumoId) {
        return insumoDao.buscarPorId(insumoId);
    }

    public List<Restaurante> listarRestaurantesDisponibles() {
        return restauranteDao.listarTodos();
    }

    public List<Insumo> listarInsumosDisponibles() {
        return insumoDao.listarTodos();
    }

    /**
     * Registra un plato nuevo con su receta (HU30).
     * @throws IllegalArgumentException si el nombre, el restaurante o algun
     *         ingrediente son invalidos.
     */
    public Plato registrarPlato(String nombre, Integer restauranteId, List<IngredientePlato> ingredientes) {
        String nombreLimpio = validarNombre(nombre, -1);
        int restauranteValido = validarRestaurante(restauranteId);
        List<IngredientePlato> ingredientesValidos = validarIngredientes(ingredientes);
        return platoDao.guardar(new Plato(0, nombreLimpio, restauranteValido, ingredientesValidos));
    }

    /**
     * Actualiza un plato existente (HU32).
     * @throws IllegalArgumentException si el plato no existe o los datos son invalidos.
     */
    public Plato actualizarPlato(int id, String nombre, Integer restauranteId, List<IngredientePlato> ingredientes) {
        Plato plato = platoDao.buscarPorId(id);
        if (plato == null) {
            throw new IllegalArgumentException("El plato indicado no existe en el sistema");
        }
        String nombreLimpio = validarNombre(nombre, id);
        int restauranteValido = validarRestaurante(restauranteId);
        List<IngredientePlato> ingredientesValidos = validarIngredientes(ingredientes);
        plato.setNombre(nombreLimpio);
        plato.setRestauranteId(restauranteValido);
        plato.setIngredientes(ingredientesValidos);
        platoDao.actualizar(plato);
        return plato;
    }

    /**
     * Elimina un plato del menu (HU33).
     * @throws IllegalArgumentException si el plato no existe.
     */
    public void eliminarPlato(int id) {
        Plato plato = platoDao.buscarPorId(id);
        if (plato == null) {
            throw new IllegalArgumentException("El plato indicado no existe en el sistema");
        }
        platoDao.eliminar(id);
    }

    private String validarNombre(String nombre, int idActual) {
        String limpio = (nombre == null) ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre del plato no puede estar vacio");
        }
        if (!limpio.matches(PATRON_NOMBRE)) {
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras, numeros, espacios y guiones, "
                            + "con un maximo de 100 caracteres");
        }
        Plato duplicado = platoDao.buscarPorNombre(limpio);
        if (duplicado != null && duplicado.getId() != idActual) {
            throw new IllegalArgumentException("Ya existe un plato con ese nombre");
        }
        return limpio;
    }

    private int validarRestaurante(Integer restauranteId) {
        if (restauranteId == null || restauranteId <= 0 || restauranteDao.buscarPorId(restauranteId) == null) {
            throw new IllegalArgumentException("Debe seleccionar el restaurante al que pertenece este plato");
        }
        return restauranteId;
    }

    private List<IngredientePlato> validarIngredientes(List<IngredientePlato> ingredientes) {
        if (ingredientes == null || ingredientes.isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un insumo con su cantidad y unidad");
        }
        List<IngredientePlato> limpios = new ArrayList<>();
        Set<Integer> insumosUsados = new HashSet<>();
        for (IngredientePlato ingrediente : ingredientes) {
            if (ingrediente.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad del ingrediente debe ser un numero positivo mayor a cero");
            }
            String unidad = (ingrediente.getUnidadReceta() == null)
                    ? "" : ingrediente.getUnidadReceta().trim().toLowerCase();
            if (!UNIDADES_RECETA.contains(unidad)) {
                throw new IllegalArgumentException("Seleccione una unidad valida para el ingrediente");
            }
            Insumo insumo = insumoDao.buscarPorId(ingrediente.getInsumoId());
            if (insumo == null) {
                throw new IllegalArgumentException("Uno de los insumos seleccionados no existe en el sistema");
            }
            if (!insumosUsados.add(insumo.getId())) {
                throw new IllegalArgumentException(
                        "El insumo \"" + insumo.getNombre() + "\" esta repetido en la receta");
            }
            if (!unidad.equals(insumo.getUnidad())) {
                try {
                    conversionUnidades.convertir(ingrediente.getCantidad(), unidad, insumo.getUnidad());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException(
                            "La unidad de \"" + insumo.getNombre() + "\" no es compatible con su unidad "
                                    + "de almacenamiento en bodega (" + insumo.getUnidad() + ")");
                }
            }
            limpios.add(new IngredientePlato(insumo.getId(), ingrediente.getCantidad(), unidad));
        }
        return limpios;
    }
}
