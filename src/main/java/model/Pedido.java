package model;

import java.time.LocalDateTime;

/**
 * Pedido gestionado en el tablero Kanban. Su ciclo de vida se mueve
 * a traves de los valores de {@link EstadoPedido}.
 */
public class Pedido {

    private int id;
    private String descripcion;
    private String marca;
    private int platoId;
    private EstadoPedido estado;
    private LocalDateTime creadoEn;

    public Pedido() {
        this.creadoEn = LocalDateTime.now();
        this.estado = EstadoPedido.RECIBIDO;
    }

    public Pedido(int id, String descripcion, String marca, EstadoPedido estado) {
        this(id, descripcion, marca, estado, 0);
    }

    public Pedido(int id, String descripcion, String marca, EstadoPedido estado, int platoId) {
        this();
        this.id = id;
        this.descripcion = descripcion;
        this.marca = marca;
        this.estado = estado;
        this.platoId = platoId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getPlatoId() {
        return platoId;
    }

    public void setPlatoId(int platoId) {
        this.platoId = platoId;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    /** Igualdad por id: dos instancias representan el mismo pedido si vienen de la misma fila en BD. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (!(obj instanceof Pedido)) { return false; }
        return id == ((Pedido) obj).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
