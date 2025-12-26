package com.entreprise.reseaum.repository;

import com.entreprise.reseaum.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
	List<Announcement> findTop5ByOrderByDatePublicationDesc();
}
