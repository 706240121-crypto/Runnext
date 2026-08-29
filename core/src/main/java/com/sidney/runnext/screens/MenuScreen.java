package com.sidney.runnext.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuScreen implements Screen {

    private final Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final BitmapFont fontGrande;
    private final BitmapFont fontMedia;
    private final BitmapFont fontPequena;

    public MenuScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        batch = new SpriteBatch();

        // Três tamanhos de fonte
        fontGrande = new BitmapFont();
        fontGrande.getData().setScale(4f);
        fontGrande.setColor(Color.WHITE);

        fontMedia = new BitmapFont();
        fontMedia.getData().setScale(2f);
        fontMedia.setColor(Color.WHITE);

        fontPequena = new BitmapFont();
        fontPequena.getData().setScale(1.5f);
        fontPequena.setColor(Color.YELLOW);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // Transição para o jogo
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.justTouched()) {
            game.setScreen(new GameScreen());
            return;
        }

        // Fundo escuro
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Coordenadas centrais
        float centroX = 400; // metade de 800

        batch.begin();

        // 1. TÍTULO - RUNNEXT (topo)
        fontGrande.setColor(Color.WHITE);
        fontGrande.draw(batch, "RUNNEXT", centroX - 140, 420);

        // 2. SUBTÍTULO - MENU
        fontPequena.setColor(Color.GRAY);
        fontPequena.draw(batch, "M E N U", centroX - 60, 370);

        // 3. TEXTO "JOGAR" (sem retângulo azul)
        fontMedia.setColor(Color.WHITE);
        fontMedia.draw(batch, "JOGAR", centroX - 50, 260);

        // 4. NÍVEL 1 (apenas "NIVEL 1", sem "MONTANHA")
        fontPequena.setColor(Color.CYAN);
        fontPequena.draw(batch, "NIVEL 1", centroX - 55, 150);

        // 5. Instrução
        fontPequena.setColor(Color.LIGHT_GRAY);
        fontPequena.draw(batch, "Toque para comecar", centroX - 100, 100);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        fontGrande.dispose();
        fontMedia.dispose();
        fontPequena.dispose();
    }
}
