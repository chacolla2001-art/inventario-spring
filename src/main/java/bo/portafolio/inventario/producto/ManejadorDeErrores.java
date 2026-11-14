package bo.portafolio.inventario.producto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/** Respuestas de error con formato estándar (RFC 9457, ProblemDetail). */
@RestControllerAdvice
public class ManejadorDeErrores {

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ProblemDetail negocio(ReglaDeNegocioException e) {
        return ProblemDetail.forStatusAndDetail(e.isNoEncontrado() ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail concurrencia() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Otro usuario modificó este producto al mismo tiempo. Vuelve a intentarlo.");
    }
}
