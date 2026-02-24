package com.skillmatch.backend.repository;

import com.skillmatch.backend.model.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    
    // Buscar conexión entre dos usuarios (bidireccional)
    @Query("SELECT c FROM Connection c WHERE " +
           "(c.user.id = ?1 AND c.connectedUser.id = ?2) OR " +
           "(c.user.id = ?2 AND c.connectedUser.id = ?1)")
    Optional<Connection> findConnectionBetweenUsers(Long userId1, Long userId2);
    
    // Conexiones aceptadas de un usuario
    @Query("SELECT c FROM Connection c WHERE " +
           "(c.user.id = ?1 OR c.connectedUser.id = ?1) AND c.status = 'accepted'")
    List<Connection> findAcceptedConnectionsByUserId(Long userId);
    
    // Solicitudes pendientes recibidas
    @Query("SELECT c FROM Connection c WHERE c.connectedUser.id = ?1 AND c.status = 'pending'")
    List<Connection> findPendingRequestsByUserId(Long userId);
    
    // Solicitudes pendientes enviadas
    @Query("SELECT c FROM Connection c WHERE c.user.id = ?1 AND c.status = 'pending'")
    List<Connection> findSentPendingRequestsByUserId(Long userId);
    
    // Verificar si existe conexión
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Connection c WHERE " +
           "((c.user.id = ?1 AND c.connectedUser.id = ?2) OR " +
           "(c.user.id = ?2 AND c.connectedUser.id = ?1)) AND c.status = 'accepted'")
    boolean areUsersConnected(Long userId1, Long userId2);
    
    // Contar conexiones de un usuario
    @Query("SELECT COUNT(c) FROM Connection c WHERE " +
           "(c.user.id = ?1 OR c.connectedUser.id = ?1) AND c.status = 'accepted'")
    long countConnectionsByUserId(Long userId);
}
