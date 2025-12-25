package com.entreprise.reseaum.repository;

import com.entreprise.reseaum.model.Message;
import com.entreprise.reseaum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

	List<Message> findByReceiverAndLuFalseOrderByDateDesc(User receiver);

	@Query("SELECT m FROM Message m " + "LEFT JOIN FETCH m.sender s " + "LEFT JOIN FETCH m.receiver r "
			+ "WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR "
			+ "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " + "ORDER BY m.date ASC")
	List<Message> findConversationBetween(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

	@Modifying
	@Transactional
	@Query("UPDATE Message m SET m.lu = true WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId")
	void markAsReadBetween(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

	List<Message> findBySenderOrderByDateDesc(User sender);

	List<Message> findByReceiverOrderByDateDesc(User receiver);
}
