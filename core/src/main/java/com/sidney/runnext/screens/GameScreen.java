package com.sidney.runnext.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
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
import com.sidney.runnext.entities.Player;

public class GameScreen implements Screen, InputProcessor {

    private final Game game;
    private static final float WORLD_WIDTH = 800;
    private static final float WORLD_HEIGHT = 480;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont fontGrande;

    private final Player player;
    private final Rectangle ground;

    // Botões de controle (ESQUERDA e DIREITA - juntos no canto inferior esquerdo)
    private final Rectangle btnLeft;
    private final Rectangle btnRight;
    private boolean touchLeft = false;
    private boolean touchRight = false;

    // Botão de Pausa (CENTRALIZADO NO TOPO)
    private final Rectangle btnPause;

    // Estado do Jogo
    private boolean isPaused = false;

    // Botões do Menu de Pausa
    private final Rectangle btnResume;
    private final Rectangle btnRestart;
    private final Rectangle btnMainMenu;

    public GameScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        // Fontes
        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.BLACK);

        fontGrande = new BitmapFont();
        fontGrande.getData().setScale(3f);
        fontGrande.setColor(Color.WHITE);

        // Entidades do jogo
        ground = new Rectangle(0, 0, WORLD_WIDTH, 40);
        player = new Player(100, ground.y + ground.height + 10);

        // Botões ESQUERDA e DIREITA (juntos no canto inferior esquerdo)
        btnLeft = new Rectangle(30, 15, 60, 50);
        btnRight = new Rectangle(100, 15, 60, 50);

        // ✅ Botão de Pausa CENTRALIZADO NO TOPO
        // Cálculo: (WORLD_WIDTH / 2) - (largura do botão / 2) = 400 - 25 = 375
        btnPause = new Rectangle(375, WORLD_HEIGHT - 60, 50, 40);

        // Botões do Menu de Pausa (Centralizados)
        float centerX = WORLD_WIDTH / 2f - 100;
        btnResume = new Rectangle(centerX, 300, 200, 50);
        btnRestart = new Rectangle(centerX, 230, 200, 50);
        btnMainMenu = new Rectangle(centerX, 160, 200, 50);
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "=== TELA INICIADA ===");
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        // Verificar input de toque
        updateTouchInput();

        // Lógica do Jogo (Só executa se NÃO estiver em pausa)
        if (!isPaused) {
            player.update(delta);

            // Limites do ecrã
            if (player.getX() < 0) {
                player.setX(0);
            } else if (player.getX() + Player.WIDTH > WORLD_WIDTH) {
                player.setX(WORLD_WIDTH - Player.WIDTH);
            }

            // Colisão com o chão
            Rectangle bounds = player.getBounds();
            if (bounds.y <= ground.y + ground.height && player.getVelocityY() <= 0) {
                player.landOn(ground.y + ground.height);
            }
        }

        // Desenhar
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // DESENHO DO CENÁRIO E PERSONAGEM
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(ground.x, ground.y, ground.width, ground.height);
        player.render(shapeRenderer);
        shapeRenderer.end();

        // DESENHO DA UI (Botões sempre visíveis)
        drawUI();

        // DESENHO DO MENU DE PAUSA (Se estiver pausado)
        if (isPaused) {
            drawPauseMenu();
        }
    }

    private void updateTouchInput() {
        // Resetar inputs de movimento
        touchLeft = false;
        touchRight = false;

        if (Gdx.input.isTouched()) {
            // Converter coordenadas do toque
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            float touchX = touchPos.x;
            float touchY = touchPos.y;

            if (isPaused) {
                // Lógica dos botões do Menu de Pausa
                if (btnResume.contains(touchX, touchY)) {
                    isPaused = false;
                    Gdx.app.log("GameScreen", "Continuar");
                } else if (btnRestart.contains(touchX, touchY)) {
                    player.setX(100);
                    player.setY(ground.y + ground.height + 10);
                    isPaused = false;
                    Gdx.app.log("GameScreen", "Reiniciar");
                } else if (btnMainMenu.contains(touchX, touchY)) {
                    game.setScreen(new MenuScreen(game));
                    dispose();
                    Gdx.app.log("GameScreen", "Menu Principal");
                }
            } else {
                // Lógica normal do jogo
                if (btnLeft.contains(touchX, touchY)) {
                    touchLeft = true;
                }
                if (btnRight.contains(touchX, touchY)) {
                    touchRight = true;
                }

                // Botão Pausa (agora no topo centralizado)
                if (btnPause.contains(touchX, touchY)) {
                    isPaused = true;
                    Gdx.app.log("GameScreen", "Pausa ativada");
                }
            }
        }

        // Movimento (apenas se não estiver pausado)
        if (!isPaused) {
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
    }

    private void drawUI() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Botões Esquerda/Direita (Brancos)
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(btnLeft.x, btnLeft.y, btnLeft.width, btnLeft.height);
        shapeRenderer.rect(btnRight.x, btnRight.y, btnRight.width, btnRight.height);

        // Botão de Pausa (Cinza Escuro) - agora no topo centralizado
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(btnPause.x, btnPause.y, btnPause.width, btnPause.height);

        shapeRenderer.end();

        // Texto/Ícones dos botões
        batch.begin();
        font.setColor(Color.BLACK);
        font.draw(batch, "<", btnLeft.x + 25, btnLeft.y + 35);
        font.draw(batch, ">", btnRight.x + 25, btnRight.y + 35);

        // Ícone Pausa (||) - cor branca
        font.setColor(Color.WHITE);
        font.draw(batch, "||", btnPause.x + 10, btnPause.y + 28);
        batch.end();
    }

    private void drawPauseMenu() {
        // Fundo escuro semi-transparente
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // Botões do Menu de Pausa
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(btnResume.x, btnResume.y, btnResume.width, btnResume.height);
        shapeRenderer.rect(btnRestart.x, btnRestart.y, btnRestart.width, btnRestart.height);
        shapeRenderer.rect(btnMainMenu.x, btnMainMenu.y, btnMainMenu.width, btnMainMenu.height);
        shapeRenderer.end();

        // Texto do Menu de Pausa
        batch.begin();
        fontGrande.setColor(Color.WHITE);
        fontGrande.draw(batch, "PAUSADO", WORLD_WIDTH / 2f - 80, 400);

        font.setColor(Color.WHITE);
        font.draw(batch, "CONTINUAR", btnResume.x + 40, btnResume.y + 35);
        font.draw(batch, "REINICIAR", btnRestart.x + 45, btnRestart.y + 35);
        font.draw(batch, "MENU", btnMainMenu.x + 65, btnMainMenu.y + 35);
        batch.end();
    }

    // =========================================================
    // MÉTODOS OBRIGATÓRIOS DO InputProcessor
    // =========================================================

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPos);
        Gdx.app.log("GameScreen", "Touch em: " + touchPos.x + ", " + touchPos.y);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    // =========================================================

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        isPaused = true;
    }

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        fontGrande.dispose();
    }
}
