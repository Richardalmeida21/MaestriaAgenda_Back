package com.maestria.agenda.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

@RestController
public class PingController {

    // Criação do Logger
    private static final Logger logger = LoggerFactory.getLogger(PingController.class);

    @GetMapping("/ping")
    public String ping() {
        // Adicionando o log
        logger.info("Requisição recebida no endpoint /ping");
        return "API está funcionando!";
    }
    
    @GetMapping("/check-ip")
    public String checkIp() {
        try {
            URL url = new URL("https://checkip.amazonaws.com");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = br.readLine().trim();
            logger.info("IP atual do servidor: {}", ip);
            return "IP do servidor: " + ip;
        } catch (Exception e) {
            logger.error("Erro ao verificar IP: {}", e.getMessage());
            return "Erro ao verificar IP: " + e.getMessage();
        }
    }
}
