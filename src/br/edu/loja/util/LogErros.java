package br.edu.loja.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogErros {

    private static final String ARQUIVO = "erros.log";

    private LogErros() {
    }

    public static void registrar(Exception e) {
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String mensagem = e.getMessage() == null ? "" : e.getMessage().replace("\n", " ");
        String linha = "[" + dataHora + "] " + e.getClass().getSimpleName() + " - " + mensagem;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO, true))) {
            writer.write(linha);
            writer.newLine();
        } catch (IOException ex) {
            System.err.println("Não foi possível gravar no arquivo de log: " + ex.getMessage());
        }
    }
}