package bo.portafolio.inventario.producto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

/** Registro inmutable de cada cambio de stock (kardex). */
@Entity
public class Movimiento {

    public enum Tipo { ENTRADA, SALIDA, AJUSTE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Tipo tipo;

    /** Cantidad del movimiento; en un ajuste es la diferencia (puede ser negativa). */
    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private int stockResultante;

    @Column(length = 200)
    private String motivo;

    @Column(nullable = false)
    private Instant fecha;

    protected Movimiento() {
    }

    public Movimiento(Producto producto, Tipo tipo, int cantidad, String motivo) {
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockResultante = producto.getStock();
        this.motivo = motivo;
        this.fecha = Instant.now();
    }

    public Long getId() { return id; }
    public Producto getProducto() { return producto; }
    public Tipo getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public int getStockResultante() { return stockResultante; }
    public String getMotivo() { return motivo; }
    public Instant getFecha() { return fecha; }
}
