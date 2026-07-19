package com.sisa.jihunpage.gallery.storage;

import org.springframework.web.multipart.MultipartFile;

public interface GalleryImageStorage {

	StoredGalleryImage store(MultipartFile image);

	void delete(String storedFileName);
}
