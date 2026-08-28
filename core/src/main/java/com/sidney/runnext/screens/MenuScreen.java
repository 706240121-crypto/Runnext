package com.sidney.runnext.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuScreen implements Screen {

    private final Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont hintFont;
    private final GlyphLayout layout;

    public MenuScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        batch = new SpriteBatch();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);

        hintFont = new BitmapFont();
        hintFont.getData().setScale(1.5f);
        hintFont.setColor(Color.LIGHT_GRAY);

        layout = new GlyphLayout();
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.justTouched()) {
            game.setScreen(new GameScreen());
            return;
        }

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        layout.setText(titleFont, "RUNNEXT");
        titleFont.draw(batch, layout, (viewport.getWorldWidth() - layout.width) / 2f, 320);

        layout.setText(hintFont, "Toque na tela ou pressione ENTER para jogar");
        hintFont.draw(batch, layout, (viewport.getWorldWidth() - layout.width) / 2f, 200);
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
        titleFont.dispose();
        hintFont.dispose();
    }
}
