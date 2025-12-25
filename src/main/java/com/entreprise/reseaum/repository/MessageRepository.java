package com.entreprise.reseaum.repository;

import com.entreprise.reseaum.model.Message;
import com.entreprise.reseaum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
	List<Message> findByReceiverAndLuFalseOrderByDateDesc(User receiver);

	List<Message> findBySenderOrderByDateDesc(User sender);

	List<Message> findByReceiverOrderByDateDesc(User receiver);
}
