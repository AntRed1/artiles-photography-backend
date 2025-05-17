package com.artiles_photography_backend.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artiles_photography_backend.models.HttpLog;

/**
 * @author arojas
 */
public interface HttpLogRepository extends JpaRepository<HttpLog, Long> {

    // Encontrar logs con filtros (método, URI, estado, rango de fechas)
    @Query("SELECT h FROM HttpLog h WHERE " +
            "(:method IS NULL OR h.method = :method) AND " +
            "(:uri IS NULL OR h.uri LIKE %:uri%) AND " +
            "(:status IS NULL OR h.status = :status) AND " +
            "(:startDate IS NULL OR h.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR h.timestamp <= :endDate)")
    Page<HttpLog> findLogsByFilters(
            @Param("method") String method,
            @Param("uri") String uri,
            @Param("status") Integer status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Eliminar logs anteriores a una fecha
    @Modifying
    @Query("DELETE FROM HttpLog h WHERE h.timestamp < :date")
    void deleteLogsBefore(@Param("date") LocalDateTime date);
}