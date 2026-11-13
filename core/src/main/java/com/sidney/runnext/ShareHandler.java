package com.sidney.runnext;

/**
 * Interface que permite ao jogo (módulo core) pedir ao Android
 * que partilhe conteúdo via WhatsApp, Facebook, etc.
 */
public interface ShareHandler {
    /**
     * Partilha um texto (ex: score final) via aplicações instaladas.
     * @param titulo Título da partilha
     * @param mensagem Texto a partilhar
     */
    void shareScore(String titulo, String mensagem);
}
