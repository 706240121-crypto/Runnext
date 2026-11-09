package com.sidney.runnext.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sidney.runnext.Audio;
import com.sidney.runnext.Progress;
import com.sidney.runnext.Settings;

public class MenuScreen implements Screen {

    // Os 3 ecrãs do menu
    private static final int STATE_MAIN = 0;      // JOGAR / OPÇÕES / SAIR
    private static final int STATE_LEVELS = 1;    // escolha do nível
    private static final int STATE_OPTIONS = 2;   // música e sons

    private final Game game;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;

    private int menuState;

    private final int maxUnlocked;

    // menu principal
    private final Rectangle btnJogar;
    private final Rectangle btnOpcoes;
    private final Rectangle btnSair;

    // escolha de nível
    private final Rectangle[] btnNivel = new Rectangle[Progress.TOTAL_LEVELS];
    private final Rectangle btnVoltarNiveis;

    // opções
    private final Rectangle btnMusica;
    private final Rectangle btnSons;
    private final Rectangle btnVoltarOpcoes;

    public MenuScreen(Game game) {
        this(game, STATE_MAIN);
    }

    public MenuScreen(Game game, int menuState) {
        this.game = game;
        this.menuState = menuState;

        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        maxUnlocked = Progress.getMaxUnlocked();

        btnJogar = MenuStyle.button(0);
        btnOpcoes = MenuStyle.button(1);
        btnSair = MenuStyle.button(2);

        for (int i = 0; i < btnNivel.length; i++) {
            btnNivel[i] = MenuStyle.button(i);
        }
        btnVoltarNiveis = MenuStyle.button(Progress.TOTAL_LEVELS);

        btnMusica = MenuStyle.button(0);
        btnSons = MenuStyle.button(1);
        btnVoltarOpcoes = MenuStyle.button(2);
    }

    @Override
    public void show() {
        // Garante que nenhum ecrã antigo (já destruído) continua a receber toques
        Gdx.input.setInputProcessor(null);
        Audio.playMusic("audio/music/menu.ogg", true);
    }

    @Override
    public void render(float delta) {
        MenuStyle.clear();

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        if (menuState == STATE_MAIN) {
            renderMenuPrincipal();
        } else if (menuState == STATE_LEVELS) {
            renderSelecaoNivel();
        } else {
            renderOpcoes();
        }

        // Tem de ser a ÚLTIMA coisa do render: pode trocar de ecrã e destruir este.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (menuState == STATE_MAIN) menuState = STATE_LEVELS;
            else if (menuState == STATE_LEVELS) iniciarNivel(1);
        } else if (Gdx.input.justTouched()) {
            handleTouchInput();
        }
    }

    private void renderMenuPrincipal() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        MenuStyle.drawButton(shapeRenderer, btnJogar, true);
        MenuStyle.drawButton(shapeRenderer, btnOpcoes, true);
        MenuStyle.drawButton(shapeRenderer, btnSair, true);
        shapeRenderer.end();

        batch.begin();
        MenuStyle.drawTitle(batch, font, "RUNNEXT", MenuStyle.TITLE_Y);
        MenuStyle.drawButtonLabel(batch, font, "JOGAR", btnJogar, true);
        MenuStyle.drawButtonLabel(batch, font, "OPÇÕES", btnOpcoes, true);
        MenuStyle.drawButtonLabel(batch, font, "SAIR", btnSair, true);
        batch.end();
    }

    private void renderSelecaoNivel() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < btnNivel.length; i++) {
            boolean livre = (i + 1) <= maxUnlocked;
            MenuStyle.drawButton(shapeRenderer, btnNivel[i], livre);
            if (!livre) MenuStyle.drawLock(shapeRenderer, btnNivel[i]);
        }
        MenuStyle.drawButton(shapeRenderer, btnVoltarNiveis, true);
        shapeRenderer.end();

        batch.begin();
        MenuStyle.drawTitle(batch, font, "RUNNEXT", MenuStyle.TITLE_Y);
        for (int i = 0; i < btnNivel.length; i++) {
            boolean livre = (i + 1) <= maxUnlocked;
            MenuStyle.drawButtonLabel(batch, font, "NIVEL " + (i + 1), btnNivel[i], livre);
        }
        MenuStyle.drawButtonLabel(batch, font, "VOLTAR", btnVoltarNiveis, true);
        batch.end();
    }

    private void renderOpcoes() {
        boolean musica = Settings.isMusicOn();
        boolean sons = Settings.isSfxOn();

        // azul = ligado, cinzento = desligado
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        MenuStyle.drawButton(shapeRenderer, btnMusica, musica);
        MenuStyle.drawButton(shapeRenderer, btnSons, sons);
        MenuStyle.drawButton(shapeRenderer, btnVoltarOpcoes, true);
        shapeRenderer.end();

        batch.begin();
        MenuStyle.drawTitle(batch, font, "OPÇÕES", MenuStyle.TITLE_Y);
        MenuStyle.drawButtonLabel(batch, font, musica ? "MÚSICA: LIGADA" : "MÚSICA: DESLIGADA", btnMusica, musica);
        MenuStyle.drawButtonLabel(batch, font, sons ? "SONS: LIGADOS" : "SONS: DESLIGADOS", btnSons, sons);
        MenuStyle.drawButtonLabel(batch, font, "VOLTAR", btnVoltarOpcoes, true);
        batch.end();
    }

    private void handleTouchInput() {
        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPos);
        float x = touchPos.x;
        float y = touchPos.y;

        if (menuState == STATE_MAIN) {
            if (btnJogar.contains(x, y)) {
                menuState = STATE_LEVELS;
            } else if (btnOpcoes.contains(x, y)) {
                menuState = STATE_OPTIONS;
            } else if (btnSair.contains(x, y)) {
                Gdx.app.exit();
            }
        } else if (menuState == STATE_LEVELS) {
            if (btnVoltarNiveis.contains(x, y)) {
                menuState = STATE_MAIN;
                return;
            }
            for (int i = 0; i < btnNivel.length; i++) {
                boolean livre = (i + 1) <= maxUnlocked;
                if (livre && btnNivel[i].contains(x, y)) {
                    iniciarNivel(i + 1);
                    return;
                }
            }
        } else {
            if (btnMusica.contains(x, y)) {
                Settings.setMusicOn(!Settings.isMusicOn());
                Audio.applyMusicSetting();   // liga ou desliga logo a música que está a tocar
            } else if (btnSons.contains(x, y)) {
                Settings.setSfxOn(!Settings.isSfxOn());
            } else if (btnVoltarOpcoes.contains(x, y)) {
                menuState = STATE_MAIN;
            }
        }
    }

    private void iniciarNivel(int nivel) {
        game.setScreen(new GameScreen(game, nivel));
        dispose();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
