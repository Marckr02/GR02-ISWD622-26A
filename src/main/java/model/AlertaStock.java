package model;

import java.time.LocalDateTime;

/**
 * Registro historico de una alerta de stock critico: conserva el insumo,
 * el stock que tenia en el momento de detectarse y cuando se genero.
 */
public class AlertaStock {

    private int id;
    private int insumoId;
    private String insumoNombre;
    private double stockAlMomento;
    private LocalDateTime timestamp;

    public AlertaStock() {
    }

    public AlertaStock(int id, int insumoId, String insumoNombre, double stockAlMomento, LocalDateTime timestamp) {
        this.id = id;
        this.insumoId = insumoId;
        this.insumoNombre = insumoNombre;
        this.stockAlMomento = stockAlMomento;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(int insumoId) {
        this.insumoId = insumoId;
    }

    public String getInsumoNombre() {
        return insumoNombre;
    }

    public void setInsumoNombre(String insumoNombre) {
        this.insumoNombre = insumoNombre;
    }

    public double getStockAlMomento() {
        return stockAlMomento;
    }

    public void setStockAlMomento(double stockAlMomento) {
        this.stockAlMomento = stockAlMomento;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
