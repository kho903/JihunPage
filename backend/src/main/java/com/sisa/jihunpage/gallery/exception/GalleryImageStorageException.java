package com.sisa.jihunpage.gallery.exception;

public class GalleryImageStorageException extends RuntimeException {
	public GalleryImageStorageException(String message) {
		super(message);
	}

	public GalleryImageStorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
