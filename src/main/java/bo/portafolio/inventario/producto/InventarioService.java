package bo.portafolio.inventario.producto;

import bo.portafolio.inventario.producto.Dtos.AjusteNuevo;
import bo.portafolio.inventario.producto.Dtos.MovimientoNuevo;
import bo.portafolio.inventario.producto.Dtos.MovimientoVista;
import bo.portafolio.inventario.producto.Dtos.ProductoEdicion;
import bo.portafolio.inventario.producto.Dtos.ProductoNuevo;
import bo.portafolio.inventario.producto.Dtos.ProductoVista;
import bo.portafolio.inventario.producto.Dtos.Resumen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Reglas del inventario. Cada cambio de stock queda registrado como movimiento dentro de la misma transacción. */
@Service
@Transactional
public class InventarioService {

    private final ProductoRepository productos;
    private final MovimientoRepository movimientos;

    public InventarioService(ProductoRepository productos, MovimientoRepository movimientos) {
        this.productos = productos;
        this.movimientos = movimientos;
    }

    public ProductoVista crear(ProductoNuevo datos) {
        if (productos.existsBySkuIgnoreCase(datos.sku())) {
            throw new ReglaDeNegocioException("Ya existe un producto con el SKU " + datos.sku() + ".");
        }
        Producto p = new Producto(datos.sku().toUpperCase(), datos.nombre().trim(), datos.costoUnitario(), datos.stockMinimo());
        return ProductoVista.de(productos.save(p));
    }

    public ProductoVista editar(Long id, ProductoEdicion datos) {
        Producto p = buscar(id);
        p.actualizar(datos.nombre().trim(), datos.costoUnitario(), datos.stockMinimo());
        return ProductoVista.de(p);
    }

    @Transactional(readOnly = true)
    public ProductoVista ver(Long id) {
        return ProductoVista.de(buscar(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductoVista> listar(String q, Pageable pagina) {
        Page<Producto> resultado = (q == null || q.isBlank())
                ? productos.findAll(pagina)
                : productos.findByNombreContainingIgnoreCaseOrSkuContainingIgnoreCase(q, q, pagina);
        return resultado.map(ProductoVista::de);
    }

    public MovimientoVista entrada(Long id, MovimientoNuevo datos) {
        Producto p = buscar(id);
        p.sumarStock(datos.cantidad());
        return MovimientoVista.de(movimientos.save(new Movimiento(p, Movimiento.Tipo.ENTRADA, datos.cantidad(), datos.motivo())));
    }

    public MovimientoVista salida(Long id, MovimientoNuevo datos) {
        Producto p = buscar(id);
        p.restarStock(datos.cantidad());
        return MovimientoVista.de(movimientos.save(new Movimiento(p, Movimiento.Tipo.SALIDA, datos.cantidad(), datos.motivo())));
    }

    /** Ajuste por conteo físico: fija el stock al valor contado y registra la diferencia. */
    public MovimientoVista ajuste(Long id, AjusteNuevo datos) {
        Producto p = buscar(id);
        int diferencia = datos.stockContado() - p.getStock();
        p.fijarStock(datos.stockContado());
        return MovimientoVista.de(movimientos.save(new Movimiento(p, Movimiento.Tipo.AJUSTE, diferencia, datos.motivo())));
    }

    @Transactional(readOnly = true)
    public List<MovimientoVista> kardex(Long id) {
        buscar(id);
        return movimientos.findByProductoIdOrderByIdDesc(id).stream().map(MovimientoVista::de).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoVista> bajoMinimo() {
        return productos.findBajoMinimo().stream().map(ProductoVista::de).toList();
    }

    @Transactional(readOnly = true)
    public Resumen resumen() {
        List<Producto> todos = productos.findAll();
        BigDecimal valor = todos.stream().map(Producto::valorEnStock).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<ProductoVista> reponer = bajoMinimo();
        return new Resumen(todos.size(), reponer.size(), valor, reponer.stream().limit(5).toList());
    }

    private Producto buscar(Long id) {
        return productos.findById(id).orElseThrow(() -> new ReglaDeNegocioException("El producto " + id + " no existe.", true));
    }
}
