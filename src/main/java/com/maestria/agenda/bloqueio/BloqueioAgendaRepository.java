package com.maestria.agenda.bloqueio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maestria.agenda.profissional.Profissional;

public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, Long> {
    
    List<BloqueioAgenda> findByProfissional(Profissional profissional);
    
    @Query("SELECT b FROM BloqueioAgenda b WHERE b.profissional = :profissional AND " +
           "((b.dataInicio <= :data AND b.dataFim >= :data) OR " +
           "(b.dataInicio = :data OR b.dataFim = :data))")
    List<BloqueioAgenda> findByProfissionalAndData(
        @Param("profissional") Profissional profissional, 
        @Param("data") LocalDate data
    );
    
    @Query("SELECT b FROM BloqueioAgenda b WHERE " +
           "((b.dataInicio <= :data AND b.dataFim >= :data) OR " +
           "(b.dataInicio = :data OR b.dataFim = :data))")
    List<BloqueioAgenda> findByData(@Param("data") LocalDate data);
    
    @Query("SELECT b FROM BloqueioAgenda b WHERE " +
           "b.profissional.id = :profissionalId AND " +
           "((b.dataInicio BETWEEN :dataInicio AND :dataFim) OR " +
           "(b.dataFim BETWEEN :dataInicio AND :dataFim) OR " +
           "(b.dataInicio <= :dataInicio AND b.dataFim >= :dataFim))")
    List<BloqueioAgenda> findByProfissionalAndPeriodo(
        @Param("profissionalId") Long profissionalId,
        @Param("dataInicio") LocalDate dataInicio, 
        @Param("dataFim") LocalDate dataFim
    );

    boolean existsByProfissionalAndDataInicio(Profissional profissional, LocalDate dataInicio);
}