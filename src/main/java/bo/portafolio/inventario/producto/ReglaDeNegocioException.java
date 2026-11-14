package bo.portafolio.inventario.producto;

/** Error esperado de negocio (se responde con 409 o 404 y un mensaje claro). */
public class ReglaDeNegocioException extends RuntimeException {

    private final boolean noEncontrado;

    public ReglaDeNegocioException(String mensaje) {
        this(mensaje, false);
    }

    public ReglaDeNegocioException(String mensaje, boolean noEncontrado) {
        super(mensaje);
        this.noEncontrado = noEncontrado;
    }

    public boolean isNoEncontrado() {
        return noEncontrado;
    }
}
