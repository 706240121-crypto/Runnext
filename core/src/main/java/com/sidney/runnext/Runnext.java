package com.sidney.runnext;

import com.badlogic.gdx.Game;                        // Classe base do LibGDX para apps com múltiplos ecrãs
import com.sidney.runnext.screens.MenuScreen;         // O ecrã de menu do jogo, definido noutra classe

// Classe principal do jogo. Estende Game (LibGDX), que já implementa
// a interface ApplicationListener e gere o ciclo de vida da aplicação.
public class Runnext extends Game {

    // Chamado UMA VEZ, quando a aplicação é criada (equivalente a um "onCreate").
    // É aqui que se define o primeiro ecrã a ser mostrado.
    @Override
    public void create() {
        // Define o MenuScreen como o ecrã ativo, passando "this" (a instância
        // do jogo) para que o MenuScreen possa trocar de ecrã mais tarde.
        setScreen(new MenuScreen(this));
    }

    // Chamado a cada frame (muitas vezes por segundo) para desenhar e atualizar o jogo.
    @Override
    public void render() {
        // super.render() delega a chamada ao ecrã ativo no momento
        // (chama automaticamente o render() do MenuScreen ou de outro ecrã atual).
        super.render();
    }

    // Chamado quando a aplicação é encerrada, para libertar recursos (memória, texturas, etc.).
    @Override
    public void dispose() {
        // super.dispose() liberta os recursos do ecrã atualmente ativo.
        super.dispose();
    }
}
