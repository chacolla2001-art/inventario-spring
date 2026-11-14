package bo.portafolio.inventario.producto;

import bo.portafolio.inventario.producto.Dtos.AjusteNuevo;
import bo.portafolio.inventario.producto.Dtos.MovimientoNuevo;
import bo.portafolio.inventario.producto.Dtos.MovimientoVista;
import bo.portafolio.inventario.producto.Dtos.ProductoEdicion;
import bo.portafolio.inventario.producto.Dtos.ProductoNuevo;
import bo.portafolio.inventario.producto.Dtos.ProductoVista;
import bo.portafolio.inventario.producto.Dtos.Resumen;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InventarioController {

    private final InventarioService servicio;

    public InventarioController(InventarioService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoVista crear(@Valid @RequestBody ProductoNuevo datos) {
        return servicio.crear(datos);
    }

    @GetMapping("/productos")
    public Page<ProductoVista> listar(@RequestParam(required = false) String q, @PageableDefault(size = 20, sort = "nombre") Pageable pagina) {
        return servicio.listar(q, pagina);
    }

    @GetMapping("/productos/{id}")
    public ProductoVista ver(@PathVariable Long id) {
        return servicio.ver(id);
    }

    @PutMapping("/productos/{id}")
    public ProductoVista editar(@PathVariable Long id, @Valid @RequestBody ProductoEdicion datos) {
        return servicio.editar(id, datos);
    }

    @PostMapping("/productos/{id}/entradas")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoVista entrada(@PathVariable Long id, @Valid @RequestBody MovimientoNuevo datos) {
        return servicio.entrada(id, datos);
    }

    @PostMapping("/productos/{id}/salidas")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoVista salida(@PathVariable Long id, @Valid @RequestBody MovimientoNuevo datos) {
        return servicio.salida(id, datos);
    }

    @PostMapping("/productos/{id}/ajustes")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoVista ajuste(@PathVariable Long id, @Valid @RequestBody AjusteNuevo datos) {
        return servicio.ajuste(id, datos);
    }

    @GetMapping("/productos/{id}/kardex")
    public List<MovimientoVista> kardex(@PathVariable Long id) {
        return servicio.kardex(id);
    }

    @GetMapping("/alertas/stock-minimo")
    public List<ProductoVista> bajoMinimo() {
        return servicio.bajoMinimo();
    }

    @GetMapping("/resumen")
    public Resumen resumen() {
        return servicio.resumen();
    }
}
