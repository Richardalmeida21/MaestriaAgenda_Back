package com.maestria.agenda.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
