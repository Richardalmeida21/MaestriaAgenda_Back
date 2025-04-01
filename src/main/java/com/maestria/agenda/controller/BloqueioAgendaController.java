package com.maestria.agenda.controller;

import com.maestria.agenda.bloqueio.BloqueioAgenda;
import com.maestria.agenda.bloqueio.BloqueioAgendaRepository;
import com.maestria.agenda.bloqueio.DadosCadastroBloqueio;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bloqueio")
@CrossOrigin(origins = "*")
public class BloqueioAgendaController {

    private static final Logger logger = LoggerFactory.getLogger(BloqueioAgendaController.class);

    private final BloqueioAgendaRepository bloqueioRepository;
    private final ProfissionalRepository profissionalRepository;

    public BloqueioAgendaController(BloqueioAgendaRepository bloqueioRepository,
                                  ProfissionalRepository profissionalRepository) {
        this.bloqueioRepository = bloqueioRepository;
        this.profissionalRepository = profissionalRepository;
    }

    /**
     * Cadastra um novo bloqueio de agenda
     * ADMIN pode bloquear para qualquer profissional
     * PROFISSIONAL pode bloquear apenas sua própria agenda
     */
    @PostMapping
public ResponseEntity<?> cadastrarBloqueio(
        @RequestBody Map<String, Object> rawData,
        @AuthenticationPrincipal UserDetails userDetails) {

    logger.info("🔍 Solicitação para criar bloqueio de agenda por: {}", userDetails.getUsername());

    try {
        // Parse manual dos dados do request
        Long profissionalId = Long.valueOf(rawData.get("profissionalId").toString());
        LocalDate dataInicio = LocalDate.parse((String) rawData.get("dataInicio"));
        LocalDate dataFim = rawData.get("dataFim") != null ? 
                          LocalDate.parse((String) rawData.get("dataFim")) : 
                          dataInicio;
        
        boolean diaTodo = Boolean.parseBoolean(rawData.get("diaTodo").toString());
        
        LocalTime horaInicio = null;
        LocalTime horaFim = null;
        
        if (!diaTodo) {
            horaInicio = LocalTime.parse((String) rawData.get("horaInicio"));
            horaFim = LocalTime.parse((String) rawData.get("horaFim"));
        } else {
            horaInicio = LocalTime.of(0, 0);
            horaFim = LocalTime.of(23, 59, 59);
        }
        
        String motivo = (String) rawData.get("motivo");

        // Verificação de segurança (código existente)...
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
        Profissional profissional;

        if (isAdmin) {
            // Admin pode bloquear para qualquer profissional
            profissional = profissionalRepository.findById(profissionalId)
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
            logger.info("✅ ADMIN criando bloqueio para o profissional: {}", profissional.getNome());
        } else {
            // Profissional só pode bloquear para si mesmo
            profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional == null) {
                logger.warn("❌ Profissional não encontrado para o usuário: {}", userDetails.getUsername());
                return ResponseEntity.status(403).body("Profissional não encontrado.");
            }
            
            // Verificar se o profissionalId no request corresponde ao profissional logado
            if (!Long.valueOf(profissional.getId()).equals(profissionalId)) {
                logger.warn("❌ Profissional tentando criar bloqueio para outro profissional: {}",
                        profissionalId);
                return ResponseEntity.status(403).body("Você só pode criar bloqueios para sua própria agenda.");
            }
            logger.info("✅ Profissional {} criando bloqueio para si mesmo", profissional.getNome());
        }

        // Criar um novo bloqueio de agenda
        BloqueioAgenda bloqueio = new BloqueioAgenda();
        bloqueio.setProfissional(profissional);
        bloqueio.setDataInicio(dataInicio);
        bloqueio.setDataFim(dataFim);
        bloqueio.setDiaTodo(diaTodo);
        bloqueio.setMotivo(motivo);
        bloqueio.setHoraInicio(horaInicio);
        bloqueio.setHoraFim(horaFim);
        
        if (dataFim.isBefore(dataInicio)) {
            return ResponseEntity.badRequest().body("A data final não pode ser anterior à data inicial.");
        }
        
        if (!diaTodo && horaFim.isBefore(horaInicio)) {
            return ResponseEntity.badRequest().body("O horário final não pode ser anterior ao horário inicial.");
        }
        
        // Salvar o bloqueio
        bloqueioRepository.save(bloqueio);
        
        logger.info("✅ Bloqueio de agenda criado com sucesso: {}", bloqueio);
        return ResponseEntity.ok(bloqueio);
    } catch (Exception e) {
        logger.error("❌ Erro ao criar bloqueio de agenda", e);
        return ResponseEntity.status(500).body("Erro ao criar bloqueio de agenda: " + e.getMessage());
    }
}
    
    /**
     * Lista todos os bloqueios de agenda
     * ADMIN vê todos os bloqueios
     * PROFISSIONAL vê apenas seus próprios bloqueios
     */
    @GetMapping
    public ResponseEntity<?> listarBloqueios(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para listar bloqueios de agenda por: {}", userDetails.getUsername());
        
        try {
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
            List<BloqueioAgenda> bloqueios;
            
            if (isAdmin) {
                // Admin vê todos os bloqueios
                bloqueios = bloqueioRepository.findAll();
                logger.info("✅ ADMIN listando todos os {} bloqueios de agenda", bloqueios.size());
            } else {
                // Profissional vê apenas seus próprios bloqueios
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null) {
                    logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                    return ResponseEntity.status(403).body("Profissional não encontrado.");
                }
                
                bloqueios = bloqueioRepository.findByProfissional(profissional);
                logger.info("✅ PROFISSIONAL {} listando seus {} bloqueios de agenda", 
                        profissional.getNome(), bloqueios.size());
            }
            
            return ResponseEntity.ok(bloqueios);
        } catch (Exception e) {
            logger.error("❌ Erro ao listar bloqueios de agenda", e);
            return ResponseEntity.status(500).body("Erro ao listar bloqueios de agenda: " + e.getMessage());
        }
    }
    
    /**
     * Busca bloqueios de agenda por profissional e período
     * ADMIN pode consultar bloqueios de qualquer profissional
     * PROFISSIONAL só pode consultar seus próprios bloqueios
     */
    @GetMapping("/periodo")
    public ResponseEntity<?> buscarBloqueiosPorPeriodo(
            @RequestParam Long profissionalId,
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("🔍 Solicitação para buscar bloqueios de {} entre {} e {} por {}", 
                profissionalId, dataInicio, dataFim, userDetails.getUsername());
        
        try {
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
            
            // Se não for admin, verificar se está consultando seus próprios bloqueios
            if (!isAdmin) {
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null) {
                    logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                    return ResponseEntity.status(403).body("Profissional não encontrado.");
                }
                
                if (profissional.getId() != profissionalId) {
                    logger.warn("❌ PROFISSIONAL {} tentando acessar bloqueios do profissional {}", 
                            profissional.getId(), profissionalId);
                    return ResponseEntity.status(403).body("Você só pode consultar seus próprios bloqueios.");
                }
                
                logger.info("✅ PROFISSIONAL {} verificando seus próprios bloqueios", profissional.getNome());
            }
            
            LocalDate inicio = LocalDate.parse(dataInicio);
            LocalDate fim = LocalDate.parse(dataFim);
            
            List<BloqueioAgenda> bloqueios = bloqueioRepository.findByProfissionalAndPeriodo(
                    profissionalId, inicio, fim);
            
            String usuarioTipo = isAdmin ? "ADMIN" : "PROFISSIONAL";
            logger.info("✅ {} encontrou {} bloqueios no período solicitado", usuarioTipo, bloqueios.size());
            return ResponseEntity.ok(bloqueios);
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar bloqueios por período", e);
            return ResponseEntity.status(500).body("Erro ao buscar bloqueios por período: " + e.getMessage());
        }
    }
    
    /**
     * Busca bloqueios de agenda para uma data específica
     * ADMIN vê todos os bloqueios do dia
     * PROFISSIONAL vê apenas bloqueios do dia para sua agenda
     */
    @GetMapping("/data")
    public ResponseEntity<?> buscarBloqueiosPorData(
            @RequestParam String data,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("🔍 Solicitação para buscar bloqueios na data {} por {}", 
                data, userDetails.getUsername());
        
        try {
            LocalDate dataFormatada = LocalDate.parse(data);
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
            List<BloqueioAgenda> bloqueios;
            
            if (isAdmin) {
                // Admin vê todos os bloqueios da data
                bloqueios = bloqueioRepository.findByData(dataFormatada);
                logger.info("✅ ADMIN encontrou {} bloqueios para a data {}", bloqueios.size(), data);
            } else {
                // Profissional vê apenas seus bloqueios da data
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null) {
                    logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                    return ResponseEntity.status(403).body("Profissional não encontrado.");
                }
                
                bloqueios = bloqueioRepository.findByProfissionalAndData(profissional, dataFormatada);
                logger.info("✅ PROFISSIONAL {} encontrou {} bloqueios para a data {}", 
                        profissional.getNome(), bloqueios.size(), data);
            }
            
            return ResponseEntity.ok(bloqueios);
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar bloqueios por data", e);
            return ResponseEntity.status(500).body("Erro ao buscar bloqueios por data: " + e.getMessage());
        }
    }
    
    /**
     * Exclui um bloqueio de agenda
     * ADMIN pode excluir qualquer bloqueio
     * PROFISSIONAL pode excluir apenas seus próprios bloqueios
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirBloqueio(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("🔍 Solicitação para excluir bloqueio ID {} por: {}", id, userDetails.getUsername());
        
        try {
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
            
            // Verifica se o bloqueio existe
            BloqueioAgenda bloqueio = bloqueioRepository.findById(id)
                    .orElse(null);
                    
            if (bloqueio == null) {
                logger.warn("❌ Bloqueio não encontrado. ID: {}", id);
                return ResponseEntity.status(404).body("Bloqueio não encontrado.");
            }
            
            // Se não é admin, verificar se é o profissional deste bloqueio
            if (!isAdmin) {
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null) {
                    logger.warn("❌ Profissional não encontrado para o usuário: {}", userDetails.getUsername());
                    return ResponseEntity.status(403).body("Profissional não encontrado.");
                }
                
                if (bloqueio.getProfissional().getId() != profissional.getId()) {
                    logger.warn("❌ Profissional {} tentando excluir bloqueio de outro profissional: {}", 
                            profissional.getId(), bloqueio.getProfissional().getId());
                    return ResponseEntity.status(403).body("Você só pode excluir seus próprios bloqueios.");
                }
                
                logger.info("✅ PROFISSIONAL {} excluindo seu próprio bloqueio", profissional.getNome());
            } else {
                logger.info("✅ ADMIN excluindo bloqueio ID {}", id);
            }
            
            bloqueioRepository.deleteById(id);
            
            String usuarioTipo = isAdmin ? "ADMIN" : "PROFISSIONAL";
            logger.info("✅ Bloqueio excluído com sucesso por {}. ID: {}", usuarioTipo, id);
            return ResponseEntity.ok("Bloqueio excluído com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao excluir bloqueio", e);
            return ResponseEntity.status(500).body("Erro ao excluir bloqueio: " + e.getMessage());
        }
    }
}