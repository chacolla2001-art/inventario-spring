package bo.portafolio.inventario.producto;

import bo.portafolio.inventario.producto.Dtos.MovimientoNuevo;
import bo.portafolio.inventario.producto.Dtos.ProductoNuevo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Carga productos de ejemplo al iniciar (desactivable con inventario.demo=false). */
@Component
@ConditionalOnProperty(name = "inventario.demo", havingValue = "true")
public class DatosDemo implements CommandLineRunner {

    private final InventarioService servicio;
    private final ProductoRepository productos;

    public DatosDemo(InventarioService servicio, ProductoRepository productos) {
        this.servicio = servicio;
        this.productos = productos;
    }

    @Override
    public void run(String... args) {
        if (productos.count() > 0) {
            return;
        }
        var laptop = servicio.crear(new ProductoNuevo("LAP-001", "Laptop 14 pulgadas", new BigDecimal("4200.00"), 3));
        var mouse = servicio.crear(new ProductoNuevo("MOU-010", "Mouse inalámbrico", new BigDecimal("85.50"), 10));
        var cable = servicio.crear(new ProductoNuevo("CAB-UTP6", "Cable UTP cat. 6 (metro)", new BigDecimal("4.20"), 100));
        servicio.entrada(laptop.id(), new MovimientoNuevo(5, "Compra inicial"));
        servicio.entrada(mouse.id(), new MovimientoNuevo(30, "Compra inicial"));
        servicio.salida(mouse.id(), new MovimientoNuevo(22, "Entrega a oficinas"));
        servicio.entrada(cable.id(), new MovimientoNuevo(80, "Compra inicial"));
    }
}
