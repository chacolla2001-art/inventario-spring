package bo.portafolio.inventario.producto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByProductoIdOrderByIdDesc(Long productoId);
}
