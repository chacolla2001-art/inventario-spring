package bo.portafolio.inventario.producto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Datos de entrada y salida de la API (records inmutables con validación). */
public final class Dtos {

    private Dtos() {
    }

    public record ProductoNuevo(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{3,40}", message = "SKU: 3 a 40 letras, números o guiones") String sku,
            @NotBlank @Size(max = 120) String nombre,
            @NotNull @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2) BigDecimal costoUnitario,
            @Min(0) int stockMinimo) {
    }

    public record ProductoEdicion(
            @NotBlank @Size(max = 120) String nombre,
            @NotNull @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2) BigDecimal costoUnitario,
            @Min(0) int stockMinimo) {
    }

    public record MovimientoNuevo(@Positive int cantidad, @Size(max = 200) String motivo) {
    }

    public record AjusteNuevo(@Min(0) int stockContado, @NotBlank @Size(max = 200) String motivo) {
    }

    public record ProductoVista(Long id, String sku, String nombre, BigDecimal costoUnitario, int stock, int stockMinimo,
                                boolean bajoMinimo, BigDecimal valorEnStock) {
        static ProductoVista de(Producto p) {
            return new ProductoVista(p.getId(), p.getSku(), p.getNombre(), p.getCostoUnitario(), p.getStock(),
                    p.getStockMinimo(), p.bajoMinimo(), p.valorEnStock());
        }
    }

    public record MovimientoVista(Long id, String tipo, int cantidad, int stockResultante, String motivo, Instant fecha) {
        static MovimientoVista de(Movimiento m) {
            return new MovimientoVista(m.getId(), m.getTipo().name(), m.getCantidad(), m.getStockResultante(), m.getMotivo(), m.getFecha());
        }
    }

    public record Resumen(long productos, long bajoMinimo, BigDecimal valorTotal, List<ProductoVista> reponerPrimero) {
    }
}
