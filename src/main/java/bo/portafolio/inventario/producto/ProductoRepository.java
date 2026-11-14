package bo.portafolio.inventario.producto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    Page<Producto> findByNombreContainingIgnoreCaseOrSkuContainingIgnoreCase(String nombre, String sku, Pageable pagina);

    @Query("select p from Producto p where p.stock <= p.stockMinimo order by (p.stock - p.stockMinimo), p.nombre")
    List<Producto> findBajoMinimo();
}
