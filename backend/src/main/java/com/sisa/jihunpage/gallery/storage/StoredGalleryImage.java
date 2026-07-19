package com.sisa.jihunpage.gallery.storage;

public record StoredGalleryImage(
	String storedFileName,
	String originalFileName,
	String imageUrl
) {
}
