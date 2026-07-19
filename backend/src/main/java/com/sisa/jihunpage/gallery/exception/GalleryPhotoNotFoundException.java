package com.sisa.jihunpage.gallery.exception;

public class GalleryPhotoNotFoundException extends RuntimeException {

	public GalleryPhotoNotFoundException(Long photoId) {
		super("갤러리 사진을 찾을 수 없습니다: " + photoId);
	}
}