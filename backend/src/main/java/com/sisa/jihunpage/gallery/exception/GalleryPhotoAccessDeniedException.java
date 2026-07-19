package com.sisa.jihunpage.gallery.exception;

public class GalleryPhotoAccessDeniedException extends RuntimeException {

	public GalleryPhotoAccessDeniedException() {
		super("본인의 갤러리 사진만 삭제할 수 있습니다.");
	}
}