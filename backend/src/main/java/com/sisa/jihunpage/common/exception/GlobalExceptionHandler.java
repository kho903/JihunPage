package com.sisa.jihunpage.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.sisa.jihunpage.auth.exception.InvalidCredentialsException;
import com.sisa.jihunpage.auth.exception.UnauthenticatedException;
import com.sisa.jihunpage.gallery.exception.GalleryImageStorageException;
import com.sisa.jihunpage.gallery.exception.GalleryOwnerNotFoundException;
import com.sisa.jihunpage.gallery.exception.GalleryPhotoAccessDeniedException;
import com.sisa.jihunpage.gallery.exception.GalleryPhotoNotFoundException;
import com.sisa.jihunpage.gallery.exception.InvalidGalleryImageException;
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

	@ExceptionHandler(GalleryOwnerNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleGalleryOwnerNotFoundException(
		GalleryOwnerNotFoundException exception
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			"GALLERY_OWNER_NOT_FOUND",
			exception.getMessage(),
			Map.of()
		);

		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.body(response);
	}

	@ExceptionHandler(GalleryPhotoNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleGalleryPhotoNotFoundException(
		GalleryPhotoNotFoundException exception
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			"GALLERY_PHOTO_NOT_FOUND",
			exception.getMessage(),
			Map.of()
		);

		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.body(response);
	}

	@ExceptionHandler(GalleryPhotoAccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleGalleryPhotoAccessDeniedException(
		GalleryPhotoAccessDeniedException exception
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			"GALLERY_PHOTO_ACCESS_DENIED",
			exception.getMessage(),
			Map.of()
		);

		return ResponseEntity
			.status(HttpStatus.FORBIDDEN)
			.body(response);
	}

	@ExceptionHandler(InvalidGalleryImageException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidGalleryImageException(
		InvalidGalleryImageException exception
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			"INVALID_GALLERY_IMAGE",
			exception.getMessage(),
			Map.of()
		);

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceededException(
		MaxUploadSizeExceededException exception
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			"GALLERY_IMAGE_TOO_LARGE",
			"이미지 파일은 5MB 이하만 업로드할 수 있습니다.",
			Map.of()
		);

		return ResponseEntity
			.status(HttpStatus.PAYLOAD_TOO_LARGE)
			.body(response);
	}

	@ExceptionHandler(GalleryImageStorageException.class)
	public ResponseEntity<ApiErrorResponse> handleGalleryImageStorageException(
		GalleryImageStorageException exception
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			"GALLERY_IMAGE_STORAGE_FAILED",
			"갤러리 이미지를 처리하는 중 오류가 발생했습니다.",
			Map.of()
		);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
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
