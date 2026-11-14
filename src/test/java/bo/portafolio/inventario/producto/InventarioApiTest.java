package bo.portafolio.inventario.producto;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de punta a punta de la API (cada prueba corre en una transacción que se deshace al final). */
@SpringBootTest(properties = "inventario.demo=false")
@AutoConfigureMockMvc
@Transactional
class InventarioApiTest {

    @Autowired
    private MockMvc mvc;

    private ResultActions enviar(String ruta, String json) throws Exception {
        return mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long crearProducto(String sku, String costo, int minimo) throws Exception {
        String cuerpo = enviar("/api/productos", """
                {"sku": "%s", "nombre": "Producto %s", "costoUnitario": %s, "stockMinimo": %d}
                """.formatted(sku, sku, costo, minimo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(cuerpo, "$.id")).longValue();
    }

    @Test
    void creaProductoConSkuEnMayusculasYSinStock() throws Exception {
        enviar("/api/productos", """
                {"sku": "imp-01", "nombre": "Impresora", "costoUnitario": 1500.00, "stockMinimo": 1}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("IMP-01"))
                .andExpect(jsonPath("$.stock").value(0))
                .andExpect(jsonPath("$.bajoMinimo").value(true));
    }

    @Test
    void rechazaSkuDuplicadoYDatosInvalidos() throws Exception {
        crearProducto("SW-24", "300.00", 2);
        enviar("/api/productos", """
                {"sku": "sw-24", "nombre": "Otro switch", "costoUnitario": 10, "stockMinimo": 0}
                """).andExpect(status().isConflict());
        enviar("/api/productos", """
                {"sku": "x", "nombre": "", "costoUnitario": -1, "stockMinimo": 0}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("sku")));
    }

    @Test
    void entradasYSalidasActualizanElStock() throws Exception {
        long id = crearProducto("RTR-01", "650.00", 2);
        enviar("/api/productos/" + id + "/entradas", "{\"cantidad\": 10, \"motivo\": \"Compra\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockResultante").value(10));
        enviar("/api/productos/" + id + "/salidas", "{\"cantidad\": 4, \"motivo\": \"Instalación en sucursal\"}")
                .andExpect(jsonPath("$.stockResultante").value(6));
        mvc.perform(get("/api/productos/" + id))
                .andExpect(jsonPath("$.stock").value(6))
                .andExpect(jsonPath("$.valorEnStock").value(3900.00));
    }

    @Test
    void noPermiteSalidasMayoresAlStock() throws Exception {
        long id = crearProducto("AP-05", "420.00", 1);
        enviar("/api/productos/" + id + "/entradas", "{\"cantidad\": 2}").andExpect(status().isCreated());
        enviar("/api/productos/" + id + "/salidas", "{\"cantidad\": 3}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("Stock insuficiente")));
        enviar("/api/productos/" + id + "/salidas", "{\"cantidad\": 0}").andExpect(status().isBadRequest());
    }

    @Test
    void elAjustePorConteoRegistraLaDiferenciaEnElKardex() throws Exception {
        long id = crearProducto("CAM-02", "250.00", 1);
        enviar("/api/productos/" + id + "/entradas", "{\"cantidad\": 8}");
        enviar("/api/productos/" + id + "/ajustes", "{\"stockContado\": 5, \"motivo\": \"Conteo físico mensual\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidad").value(-3))
                .andExpect(jsonPath("$.tipo").value("AJUSTE"));
        mvc.perform(get("/api/productos/" + id + "/kardex"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipo").value("AJUSTE"))
                .andExpect(jsonPath("$[1].tipo").value("ENTRADA"));
    }

    @Test
    void alertaDeStockMinimoYResumen() throws Exception {
        long conStock = crearProducto("UPS-01", "800.00", 1);
        crearProducto("BAT-01", "95.00", 5);
        enviar("/api/productos/" + conStock + "/entradas", "{\"cantidad\": 3}");
        mvc.perform(get("/api/alertas/stock-minimo"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku").value("BAT-01"));
        mvc.perform(get("/api/resumen"))
                .andExpect(jsonPath("$.productos").value(2))
                .andExpect(jsonPath("$.bajoMinimo").value(1))
                .andExpect(jsonPath("$.valorTotal").value(2400.00));
    }

    @Test
    void productoInexistenteResponde404() throws Exception {
        mvc.perform(get("/api/productos/99999")).andExpect(status().isNotFound());
        enviar("/api/productos/99999/entradas", "{\"cantidad\": 1}").andExpect(status().isNotFound());
    }
}
