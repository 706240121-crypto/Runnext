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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
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

    private int menuState = 0; // 0 = Menu principal, 1 = Seleção de Níveis

    private final Rectangle btnJogar;
    private final Rectangle btnNivel1;
    private final Rectangle btnNivel2;

    public MenuScreen(Game game) {
        this(game, 0);
    }

    public MenuScreen(Game game, int menuState) {
        this.game = game;
        this.menuState = menuState;

        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        fontGrande = new BitmapFont();
        fontGrande.getData().setScale(4f);
        fontGrande.setColor(Color.WHITE);

        fontMedia = new BitmapFont();
        fontMedia.getData().setScale(2.2f);
        fontMedia.setColor(Color.WHITE);

        float centroX = 400f;
        btnJogar = new Rectangle(centroX - 100, 170, 200, 50);
        btnNivel1 = new Rectangle(centroX - 130, 230, 260, 50);
        btnNivel2 = new Rectangle(centroX - 130, 150, 260, 50);
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        float centroX = 400f;

        if (menuState == 0) {
            renderMenuPrincipal(centroX);
        } else {
            renderSelecaoNivel(centroX);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (menuState == 0) menuState = 1;
            else {
                game.setScreen(new GameScreen(game, 1));
                dispose();
            }
        }
        if (Gdx.input.justTouched()) {
            handleTouchInput();
        }
    }

    private void renderMenuPrincipal(float centroX) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(btnJogar.x, btnJogar.y, btnJogar.width, btnJogar.height);
        shapeRenderer.end();

        batch.begin();
        fontGrande.draw(batch, "RUNNEXT", centroX - 140, 350);
        fontMedia.draw(batch, "JOGAR", centroX - 45, btnJogar.y + 35);
        batch.end();
    }

    private void renderSelecaoNivel(float centroX) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(btnNivel1.x, btnNivel1.y, btnNivel1.width, btnNivel1.height);
        shapeRenderer.rect(btnNivel2.x, btnNivel2.y, btnNivel2.width, btnNivel2.height);
        shapeRenderer.end();

        batch.begin();
        fontGrande.draw(batch, "RUNNEXT", centroX - 140, 370);
        fontMedia.draw(batch, "NIVEL 1", centroX - 55, btnNivel1.y + 35);
        fontMedia.draw(batch, "NIVEL 2", centroX - 55, btnNivel2.y + 35);
        batch.end();
    }

    private void handleTouchInput() {
        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPos);
        float x = touchPos.x;
        float y = touchPos.y;

        if (menuState == 0) {
            if (btnJogar.contains(x, y)) {
                menuState = 1;
            }
        } else {
            if (btnNivel1.contains(x, y)) {
                game.setScreen(new GameScreen(game, 1));
                dispose();
            } else if (btnNivel2.contains(x, y)) {
                game.setScreen(new GameScreen(game, 2));
                dispose();
            }
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
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
