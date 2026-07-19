package com.sisa.jihunpage.gallery.exception;

public class GalleryOwnerNotFoundException extends RuntimeException {
	public GalleryOwnerNotFoundException(String userId) {
		super("갤러리 회원을 찾을 수 없습니다 : " + userId);
	}
}
