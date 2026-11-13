package com.sidney.runnext;

import com.badlogic.gdx.Game;
import com.sidney.runnext.screens.MenuScreen;

/**
 * Classe principal do jogo. Estende Game (LibGDX), que já implementa
 * a interface ApplicationListener e gere o ciclo de vida da aplicação.
 */
public class Runnext extends Game {

    private final ShareHandler shareHandler;

    /**
     * Construtor que recebe o ShareHandler do Android.
     * O AndroidLauncher.java chama isto com: new Runnext(this)
     */
    public Runnext(ShareHandler shareHandler) {
        this.shareHandler = shareHandler;
    }

    @Override
    public void create() {
        // Define o MenuScreen como o ecrã ativo e passa-lhe o shareHandler
        // para que ele possa repassar ao GameScreen quando o jogo iniciar.
        setScreen(new MenuScreen(this, shareHandler));
    }

    @Override
    public void render() {
        // super.render() delega a chamada ao ecrã ativo no momento
        super.render();
    }

    @Override
    public void dispose() {
        // super.dispose() liberta os recursos do ecrã atualmente ativo.
        super.dispose();
    }
}
