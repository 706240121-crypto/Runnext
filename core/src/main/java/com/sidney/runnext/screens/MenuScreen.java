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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuScreen implements Screen {

    private final Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont fontGrande;
    private final BitmapFont fontMedia;

    // Estado do menu: 0 = Menu principal, 1 = Seleção de nível
    private int menuState = 0;

    public MenuScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Fonte grande para o título
        fontGrande = new BitmapFont();
        fontGrande.getData().setScale(4f);
        fontGrande.setColor(Color.WHITE);

        // Fonte média para botões
        fontMedia = new BitmapFont();
        fontMedia.getData().setScale(2.5f);
        fontMedia.setColor(Color.WHITE);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // Fundo escuro
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Coordenadas centrais
        float centroX = 400; // metade de 800

        if (menuState == 0) {
            // TELA 1: Menu principal
            renderMenuPrincipal(centroX);
        } else if (menuState == 1) {
            // TELA 2: Seleção de nível
            renderSelecaoNivel(centroX);
        }

        // Verificar toques ou tecla ENTER
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.justTouched()) {
            handleInput();
        }
    }

    private void renderMenuPrincipal(float centroX) {
        // Retângulo azul atrás do JOGAR (para parecer botão)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(centroX - 100, 170, 200, 50);
        shapeRenderer.end();

        batch.begin();
        // Título: RUNNEXT
        fontGrande.setColor(Color.WHITE);
        fontGrande.draw(batch, "RUNNEXT", centroX - 140, 350);

        // Texto: JOGAR (por cima do retângulo)
        fontMedia.setColor(Color.WHITE);
        fontMedia.draw(batch, "JOGAR", centroX - 60, 200);
        batch.end();
    }

    private void renderSelecaoNivel(float centroX) {
        // Retângulo azul atrás do NIVEL 1
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(centroX - 120, 170, 240, 50);
        shapeRenderer.end();

        batch.begin();
        // Título: RUNNEXT
        fontGrande.setColor(Color.WHITE);
        fontGrande.draw(batch, "RUNNEXT", centroX - 140, 350);

        // Texto: NIVEL 1 (por cima do retângulo)
        fontMedia.setColor(Color.WHITE);
        fontMedia.draw(batch, "NIVEL 1", centroX - 70, 200);
        batch.end();
    }

    private void handleInput() {
        if (menuState == 0) {
            // Está no menu principal → vai para seleção de nível
            menuState = 1;
        } else if (menuState == 1) {
            // Está na seleção de nível → inicia o jogo (passando 'game' para o GameScreen)
            game.setScreen(new GameScreen(game));
        }
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
        shapeRenderer.dispose();
        fontGrande.dispose();
        fontMedia.dispose();
    }
}
