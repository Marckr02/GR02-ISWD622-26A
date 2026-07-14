package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Punto unico de acceso a la base de datos H2 embebida que reemplaza el
 * almacen en memoria de los DAO, a traves de un pool de conexiones
 * (HikariCP). Abrir una conexion fisica nueva en cada llamada a un DAO era
 * la causa real de la lentitud al cargar las vistas (p. ej. ~28 conexiones
 * nuevas solo para listar los platos); con el pool, {@link #obtenerConexion()}
 * entrega una conexion ya abierta y el "cierre" en el try-with-resources de
 * cada DAO simplemente la devuelve al pool.
 *
 * <p>En runtime real usa un archivo local (persiste entre reinicios: el
 * historial de alertas y pedidos es un historico real). En los tests, el
 * plugin surefire sobreescribe la propiedad de sistema "db.url" para apuntar
 * a una BD en memoria descartable (ver pom.xml), asi que los tests nunca
 * tocan el archivo de datos real. En Azure App Service (variable de entorno
 * WEBSITE_INSTANCE_ID presente), el archivo se ubica bajo el directorio
 * persistente "HOME" que la plataforma ya monta, sin aprovisionar nada
 * adicional.
 *
 * <p>El esquema (schema.sql) y los datos de ejemplo (seed.sql, solo si la
 * tabla de restaurantes esta vacia) se aplican una unica vez, al cargar esta
 * clase.
 */
public final class ConexionBD {

    private static final String USUARIO = "sa";
    private static final String CLAVE = "";

    private static final HikariDataSource DATA_SOURCE;

    static {
        String url = System.getProperty("db.url", urlPorDefecto());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(USUARIO);
        config.setPassword(CLAVE);
        config.setDriverClassName("org.h2.Driver");
        config.setPoolName("darkkitchen-pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        DATA_SOURCE = new HikariDataSource(config);

        try (Connection conexion = DATA_SOURCE.getConnection()) {
            ejecutarScript(conexion, "/db/schema.sql");
            if (tablaRestaurantesVacia(conexion)) {
                ejecutarScript(conexion, "/db/seed.sql");
            }
        } catch (SQLException ex) {
            throw new ExceptionInInitializerError("No se pudo inicializar la base de datos: " + ex.getMessage());
        }
    }

    private ConexionBD() {
    }

    public static Connection obtenerConexion() {
        try {
            return DATA_SOURCE.getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo obtener una conexion del pool", ex);
        }
    }

    /**
     * Archivo local por defecto. Si se detecta que la app corre en Azure App
     * Service (variable WEBSITE_INSTANCE_ID) usa el directorio persistente
     * "HOME" que la plataforma monta (sobrevive redeploys/reinicios, sin
     * necesidad de aprovisionar ninguna base de datos administrada); si no,
     * usa una carpeta relativa al directorio de trabajo (desarrollo local).
     */
    private static String urlPorDefecto() {
        String directorio = "./data";
        String enAzure = System.getenv("WEBSITE_INSTANCE_ID");
        String home = System.getenv("HOME");
        if (enAzure != null && !enAzure.isBlank() && home != null && !home.isBlank()) {
            directorio = home + "/data";
        }
        return "jdbc:h2:file:" + directorio + "/darkkitchen;AUTO_SERVER=TRUE";
    }

    private static boolean tablaRestaurantesVacia(Connection conexion) throws SQLException {
        try (Statement st = conexion.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM restaurantes")) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }

    private static void ejecutarScript(Connection conexion, String recurso) throws SQLException {
        String contenido;
        try (InputStream in = ConexionBD.class.getResourceAsStream(recurso)) {
            if (in == null) {
                throw new IllegalStateException("No se encontro el script " + recurso + " en el classpath");
            }
            try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
                contenido = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer " + recurso, ex);
        }

        try (Statement st = conexion.createStatement()) {
            for (String sentencia : dividirEnSentencias(contenido)) {
                if (!sentencia.isBlank()) {
                    st.execute(sentencia);
                }
            }
        }
    }

    /** Separa el script en sentencias por ";", ignorando lineas de comentario ("--"). */
    private static String[] dividirEnSentencias(String script) {
        StringBuilder limpio = new StringBuilder();
        for (String linea : script.split("\n")) {
            String sinComentario = linea.strip();
            if (sinComentario.startsWith("--") || sinComentario.isEmpty()) {
                continue;
            }
            limpio.append(linea).append('\n');
        }
        return limpio.toString().split(";");
    }
}
