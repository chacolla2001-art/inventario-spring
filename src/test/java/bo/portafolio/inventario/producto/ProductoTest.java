package bo.portafolio.inventario.producto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas unitarias de las reglas del producto (sin Spring). */
class ProductoTest {

    private Producto nuevo() {
        return new Producto("TEC-01", "Teclado", new BigDecimal("120.50"), 2);
    }

    @Test
    void empiezaSinStockYBajoElMinimo() {
        Producto p = nuevo();
        assertThat(p.getStock()).isZero();
        assertThat(p.bajoMinimo()).isTrue();
    }

    @Test
    void noPermiteSacarMasDeLoQueHay() {
        Producto p = nuevo();
        p.sumarStock(3);
        assertThatThrownBy(() -> p.restarStock(4))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("Stock insuficiente");
        assertThat(p.getStock()).isEqualTo(3);
    }

    @Test
    void valorizaConDecimalesExactos() {
        Producto p = nuevo();
        p.sumarStock(3);
        assertThat(p.valorEnStock()).isEqualByComparingTo("361.50");
        assertThat(p.bajoMinimo()).isFalse();
    }

    @Test
    void elAjusteNoAceptaNegativos() {
        assertThatThrownBy(() -> nuevo().fijarStock(-1)).isInstanceOf(ReglaDeNegocioException.class);
    }
}
