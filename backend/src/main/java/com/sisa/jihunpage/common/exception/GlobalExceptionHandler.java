package com.sisa.jihunpage.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sisa.jihunpage.auth.exception.InvalidCredentialsException;
import com.sisa.jihunpage.auth.exception.UnauthenticatedException;
import com.sisa.jihunpage.member.exception.DuplicateUserIdException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationException(
		MethodArgumentNotValidException exception
	) {
		Map<String, String> errors = new LinkedHashMap<>();

		for (FieldError fieldError
			: exception.getBindingResult().getFieldErrors()) {
			String fieldName = convertFieldName(fieldError.getField());

			errors.putIfAbsent(
				fieldName,
				fieldError.getDefaultMessage()
			);
		}

		ApiErrorResponse response = new ApiErrorResponse(
			"VALIDATION_FAILED",
			"입력값을 확인해주세요.",
			errors
		);
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}

	@ExceptionHandler(DuplicateUserIdException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateUserIdException(
		DuplicateUserIdException exception
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			"DUPLICATE_USER_ID",
			exception.getMessage(),
			Map.of()
		);

		return ResponseEntity
			.status(HttpStatus.CONFLICT)
			.body(response);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentialsException(
		InvalidCredentialsException exception
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			"INVALID_CREDENTIALS",
			exception.getMessage(),
			Map.of()
		);

		return ResponseEntity
			.status(HttpStatus.UNAUTHORIZED)
			.body(response);
	}

	@ExceptionHandler(UnauthenticatedException.class)
	public ResponseEntity<ApiErrorResponse> handleUnauthenticatedException(
		UnauthenticatedException exception
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			"UNAUTHENTICATED",
			exception.getMessage(),
			Map.of()
		);

		return ResponseEntity
			.status(HttpStatus.UNAUTHORIZED)
			.body(response);
	}


	private String convertFieldName(String fieldName) {
		return switch (fieldName) {
			case "userId" -> "userid";
			case "password" -> "userpwd";
			case "passwordConfirm", "passwordConfirmed" -> "userpwdConfirm";
			default -> fieldName;
		};
	}
}
