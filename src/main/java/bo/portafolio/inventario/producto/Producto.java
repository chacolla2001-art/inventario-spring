package bo.portafolio.inventario.producto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import java.math.BigDecimal;

/** Producto del inventario. El stock solo cambia a través de movimientos (entradas, salidas y ajustes). */
@Entity
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private int stockMinimo;

    /** Bloqueo optimista: dos actualizaciones simultáneas no se pisan. */
    @Version
    private Long version;

    protected Producto() {
    }

    public Producto(String sku, String nombre, BigDecimal costoUnitario, int stockMinimo) {
        this.sku = sku;
        this.nombre = nombre;
        this.costoUnitario = costoUnitario;
        this.stockMinimo = stockMinimo;
        this.stock = 0;
    }

    public boolean bajoMinimo() {
        return stock <= stockMinimo;
    }

    public BigDecimal valorEnStock() {
        return costoUnitario.multiply(BigDecimal.valueOf(stock));
    }

    void sumarStock(int cantidad) {
        this.stock += cantidad;
    }

    void restarStock(int cantidad) {
        if (cantidad > stock) {
            throw new ReglaDeNegocioException("Stock insuficiente de " + sku + ": hay " + stock + " y se piden " + cantidad + ".");
        }
        this.stock -= cantidad;
    }

    void fijarStock(int nuevo) {
        if (nuevo < 0) {
            throw new ReglaDeNegocioException("El stock no puede ser negativo.");
        }
        this.stock = nuevo;
    }

    void actualizar(String nombre, BigDecimal costoUnitario, int stockMinimo) {
        this.nombre = nombre;
        this.costoUnitario = costoUnitario;
        this.stockMinimo = stockMinimo;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public int getStock() { return stock; }
    public int getStockMinimo() { return stockMinimo; }
}
