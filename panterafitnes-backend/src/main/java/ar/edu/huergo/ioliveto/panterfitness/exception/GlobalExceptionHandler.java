package ar.edu.huergo.ioliveto.panterfitness.exception;

import ar.edu.huergo.ioliveto.panterfitness.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleBusiness(BusinessException exception, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleUnauthorized(UnauthorizedException exception, HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleAuthentication(AuthenticationException exception, HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, "Credenciales invalidas o token no valido.", request);
	}

	@ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorResponse handleForbidden(Exception exception, HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		String message = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return build(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleUnexpected(Exception exception, HttpServletRequest request) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor.", request);
	}

	private ErrorResponse build(HttpStatus status, String message, HttpServletRequest request) {
		return new ErrorResponse(
				LocalDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		);
	}
}
