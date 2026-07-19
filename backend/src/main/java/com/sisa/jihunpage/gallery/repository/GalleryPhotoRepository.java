package com.sisa.jihunpage.gallery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sisa.jihunpage.gallery.domain.GalleryPhoto;

public interface GalleryPhotoRepository extends JpaRepository<GalleryPhoto, Long> {

	List<GalleryPhoto> findAllByMember_IdOrderByCreatedAtDesc(Long memberId);
}
