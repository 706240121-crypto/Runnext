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

        // Fonte para as setas
        font = new BitmapFont();
        font.getData().setScale(2.5f);
        font.setColor(Color.BLACK); // Setas pretas para contrastar com o botão branco

        ground = new Rectangle(0, 0, WORLD_WIDTH, 40);
        player = new Player(100, ground.y + ground.height + 10);

        // Botões menores e melhor posicionados
        btnLeft = new Rectangle(30, 15, 60, 50);
        btnRight = new Rectangle(100, 15, 60, 50);
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "=== TELA INICIADA ===");
    }

    @Override
    public void render(float delta) {
        handleTouchInput();
        player.update(delta);

        // ✅ IMPEDIR A PERSONAGEM DE SAIR DO ECRÃ (Limites do Mundo)
        if (player.getX() < 0) {
            player.setX(0); // Bateu na parede esquerda
        } else if (player.getX() + Player.WIDTH > WORLD_WIDTH) {
            player.setX(WORLD_WIDTH - Player.WIDTH); // Bateu na parede direita
        }

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

        // ✅ Botão ESQUERDA (SEMPRE BRANCO, mesmo ao clicar)
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(btnLeft.x, btnLeft.y, btnLeft.width, btnLeft.height);

        // Borda do botão esquerdo (cinza claro para se ver o contorno)
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rectLine(btnLeft.x, btnLeft.y, btnLeft.x + btnLeft.width, btnLeft.y, 2);
        shapeRenderer.rectLine(btnLeft.x, btnLeft.y, btnLeft.x, btnLeft.y + btnLeft.height, 2);
        shapeRenderer.rectLine(btnLeft.x + btnLeft.width, btnLeft.y, btnLeft.x + btnLeft.width, btnLeft.y + btnLeft.height, 2);
        shapeRenderer.rectLine(btnLeft.x, btnLeft.y + btnLeft.height, btnLeft.x + btnLeft.width, btnLeft.y + btnLeft.height, 2);

        // ✅ Botão DIREITA (SEMPRE BRANCO, mesmo ao clicar)
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(btnRight.x, btnRight.y, btnRight.width, btnRight.height);

        // Borda do botão direito
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rectLine(btnRight.x, btnRight.y, btnRight.x + btnRight.width, btnRight.y, 2);
        shapeRenderer.rectLine(btnRight.x, btnRight.y, btnRight.x, btnRight.y + btnRight.height, 2);
        shapeRenderer.rectLine(btnRight.x + btnRight.width, btnRight.y, btnRight.x + btnRight.width, btnRight.y + btnRight.height, 2);
        shapeRenderer.rectLine(btnRight.x, btnRight.y + btnRight.height, btnRight.x + btnRight.width, btnRight.y + btnRight.height, 2);

        shapeRenderer.end();

        // Desenhar SETAS (< e >) nos botões
        batch.begin();
        font.setColor(Color.BLACK); // Garantir que a cor é preta

        // Seta ESQUERDA (<)
        float leftTextX = btnLeft.x + 18;
        float leftTextY = btnLeft.y + 35;
        font.draw(batch, "<", leftTextX, leftTextY);

        // Seta DIREITA (>)
        float rightTextX = btnRight.x + 20;
        float rightTextY = btnRight.y + 35;
        font.draw(batch, ">", rightTextX, rightTextY);

        batch.end();
    }

    private void handleTouchInput() {
        touchLeft = false;
        touchRight = false;

        // Verificar toques na tela
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.input.getY();

            // Inverter Y (Android tem Y invertido)
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
