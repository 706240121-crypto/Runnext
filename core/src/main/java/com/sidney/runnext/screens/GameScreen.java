package com.sidney.runnext.screens;

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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sidney.runnext.entities.Player;

public class GameScreen implements Screen {

    private static final float WORLD_WIDTH = 800;
    private static final float WORLD_HEIGHT = 480;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;

    private final Player player;
    private final Rectangle ground;

    // Botões de controle
    private final Rectangle btnLeft;
    private final Rectangle btnRight;
    private boolean touchLeft = false;
    private boolean touchRight = false;

    public GameScreen() {
        camera = new OrthographicCamera();
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        ground = new Rectangle(0, 0, WORLD_WIDTH, 40);
        player = new Player(100, ground.y + ground.height + 10);

        // Criar botões de controle (posição na tela)
        btnLeft = new Rectangle(50, 20, 80, 60);
        btnRight = new Rectangle(150, 20, 80, 60);
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "=== TELA INICIADA ===");
    }

    @Override
    public void render(float delta) {
        handleTouchInput();
        player.update(delta);

        // Colisão com o chão
        Rectangle bounds = player.getBounds();
        if (bounds.y <= ground.y + ground.height && player.getVelocityY() <= 0) {
            player.landOn(ground.y + ground.height);
        }

        // Limpar tela
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Desenhar elementos do jogo
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Chão
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(ground.x, ground.y, ground.width, ground.height);

        // Player
        player.render(shapeRenderer);

        // Botões de controle
        shapeRenderer.setColor(touchLeft ? Color.GRAY : Color.BLUE);
        shapeRenderer.rect(btnLeft.x, btnLeft.y, btnLeft.width, btnLeft.height);

        shapeRenderer.setColor(touchRight ? Color.GRAY : Color.BLUE);
        shapeRenderer.rect(btnRight.x, btnRight.y, btnRight.width, btnRight.height);

        shapeRenderer.end();

        // Desenhar texto nos botões
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "◀", btnLeft.x + 25, btnLeft.y + 40);
        font.draw(batch, "▶", btnRight.x + 25, btnRight.y + 40);
        batch.end();
    }

    private void handleTouchInput() {
        touchLeft = false;
        touchRight = false;

        // Verificar toques na tela
        if (Gdx.input.isTouched()) {
            // Converter coordenadas do toque para coordenadas do mundo
            float touchX = Gdx.input.getX();
            float touchY = Gdx.input.getY();

            // Inverter Y (o Android tem Y invertido)
            touchY = WORLD_HEIGHT - touchY;

            // Verificar se tocou no botão esquerdo
            if (btnLeft.contains(touchX, touchY)) {
                touchLeft = true;
            }

            // Verificar se tocou no botão direito
            if (btnRight.contains(touchX, touchY)) {
                touchRight = true;
            }
        }

        // Atualizar input do player com toque ou teclado
        if (touchLeft || Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.setMovingLeft(true);
        } else {
            player.setMovingLeft(false);
        }

        if (touchRight || Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.setMovingRight(true);
        } else {
            player.setMovingRight(false);
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
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
