package com.sidney.runnext.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sidney.runnext.entities.Coin;
import com.sidney.runnext.entities.Player;

public class GameScreen implements Screen, InputProcessor {

    private final Game game;

    private static final float WORLD_WIDTH = 4200f;
    private static final float WORLD_HEIGHT = 480f;
    private static final float VIEWPORT_WIDTH = 800f;

    private static final String[] BACKGROUND_FILES = {
        "backgrounds/bg_nivel1_floresta.png",
        "backgrounds/bg_nivel2_montanha.png",
        "backgrounds/bg_nivel3_castelo.png"
    };

    private final OrthographicCamera camera;
    private final OrthographicCamera hudCamera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont fontGrande;

    private final Player player;

    private final Rectangle ground1;
    private final Rectangle ground2;
    private final Rectangle ground3;
    private final Rectangle ground4;
    private final Rectangle finishPortal;

    private final int level;
    private final Texture backgroundTexture;
    private final TextureRegion background;

    private final Texture coinTexture;
    private final Animation<TextureRegion> coinAnimation;
    private final Array<Coin> coins = new Array<>();
    private int coinsCollected = 0;
    private float stateTime = 0f;

    private int lives = 3;

    private final Rectangle btnLeft;
    private final Rectangle btnRight;
    private final Vector2 btnJumpCenter;
    private final float btnJumpRadius = 45f;

    private boolean touchLeft = false;
    private boolean touchRight = false;
    private boolean touchJump = false;

    private final Rectangle btnPause;
    private boolean isPaused = false;

    private final Rectangle btnResume;
    private final Rectangle btnRestart;
    private final Rectangle btnMainMenu;

    public GameScreen(Game game) {
        this(game, 1);
    }

    public GameScreen(Game game, int level) {
        this.game = game;
        this.level = Math.max(1, Math.min(level, BACKGROUND_FILES.length));

        camera = new OrthographicCamera();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, VIEWPORT_WIDTH, WORLD_HEIGHT);

        viewport = new FitViewport(VIEWPORT_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.BLACK);

        fontGrande = new BitmapFont();
        fontGrande.getData().setScale(3f);
        fontGrande.setColor(Color.WHITE);

        backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_FILES[this.level - 1]));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        background = new TextureRegion(backgroundTexture, 40, 0, 1200, 720);

        coinTexture = new Texture(Gdx.files.internal("sprites/coin.png"));
        coinTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] coinFrames = TextureRegion.split(coinTexture, 16, 16);
        coinAnimation = new Animation<>(0.12f, coinFrames[0]);

        ground1 = new Rectangle(0, 0, 800, 45);
        ground2 = new Rectangle(980, 0, 1020, 45);
        ground3 = new Rectangle(2250, 0, 1050, 45);
        ground4 = new Rectangle(3600, 0, 600, 45);

        finishPortal = new Rectangle(4050, 45, 60, 150);

        player = new Player(100, ground1.y + ground1.height);

        createCoins();

        btnLeft = new Rectangle(25, 15, 80, 80);
        btnRight = new Rectangle(115, 15, 80, 80);
        btnJumpCenter = new Vector2(VIEWPORT_WIDTH - 85, 55);

        btnPause = new Rectangle(375, WORLD_HEIGHT - 60, 50, 40);

        float centerX = VIEWPORT_WIDTH / 2f - 100;
        btnResume = new Rectangle(centerX, 300, 200, 50);
        btnRestart = new Rectangle(centerX, 230, 200, 50);
        btnMainMenu = new Rectangle(centerX, 160, 200, 50);
    }

    private void createCoins() {
        coins.clear();
        coinsCollected = 0;

        float groundTop = ground1.y + ground1.height;

        for (int i = 0; i < 6; i++) {
            coins.add(new Coin(450 + (i * 50), groundTop + 30));
        }
        for (int i = 0; i < 8; i++) {
            coins.add(new Coin(1200 + (i * 55), groundTop + 30));
        }
        for (int i = 0; i < 10; i++) {
            coins.add(new Coin(2600 + (i * 55), groundTop + 30));
        }

        float[] arcY = {groundTop + 150, groundTop + 185, groundTop + 205, groundTop + 185, groundTop + 150};
        for (int i = 0; i < 5; i++) {
            coins.add(new Coin(3800 + (i * 35), groundTop + arcY[i % arcY.length]));
        }
    }

    private Rectangle getPlayerBody() {
        Rectangle b = player.getBounds();
        return new Rectangle(b.x + 25, b.y + 12, b.width - 50, b.height - 25);
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "=== TELA INICIADA (nível " + level + ") ===");
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        updateTouchInput();

        if (!isPaused) {
            player.update(delta);
            stateTime += delta;

            if (player.getX() < 0) {
                player.setX(0);
            } else if (player.getX() + Player.WIDTH > WORLD_WIDTH) {
                player.setX(WORLD_WIDTH - Player.WIDTH);
            }

            Rectangle bounds = player.getBounds();
            float groundLevel = ground1.y + ground1.height;

            if (bounds.x + bounds.width > ground1.x && bounds.x < ground1.x + ground1.width) {
                if (bounds.y <= groundLevel && player.getVelocityY() <= 0) player.landOn(groundLevel);
            }
            if (bounds.x + bounds.width > ground2.x && bounds.x < ground2.x + ground2.width) {
                if (bounds.y <= groundLevel && player.getVelocityY() <= 0) player.landOn(groundLevel);
            }
            if (bounds.x + bounds.width > ground3.x && bounds.x < ground3.x + ground3.width) {
                if (bounds.y <= groundLevel && player.getVelocityY() <= 0) player.landOn(groundLevel);
            }
            if (bounds.x + bounds.width > ground4.x && bounds.x < ground4.x + ground4.width) {
                if (bounds.y <= groundLevel && player.getVelocityY() <= 0) player.landOn(groundLevel);
            }

            if (player.getY() < -50) {
                perderVidaEApojar();
            }

            Rectangle playerBody = getPlayerBody();
            if (playerBody.overlaps(finishPortal)) {
                Gdx.app.log("GameScreen", "Nível " + level + " concluído!");
                avancarProximoNivel();
            }

            for (Coin coin : coins) {
                if (!coin.isCollected() && playerBody.overlaps(coin.getBounds())) {
                    coin.collect();
                    coinsCollected++;
                    Gdx.app.log("GameScreen", "Moeda apanhada! Total: " + coinsCollected);
                }
            }
        }

        camera.position.x = Math.max(VIEWPORT_WIDTH / 2f, Math.min(player.getX(), WORLD_WIDTH - VIEWPORT_WIDTH / 2f));
        camera.position.y = WORLD_HEIGHT / 2f;
        camera.update();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (float bgX = 0; bgX < WORLD_WIDTH; bgX += VIEWPORT_WIDTH) {
            batch.draw(background, bgX, 0, VIEWPORT_WIDTH, WORLD_HEIGHT);
        }
        for (Coin coin : coins) {
            TextureRegion frame = coinAnimation.getKeyFrame(stateTime + coin.getAnimationOffset(), true);
            coin.render(batch, frame);
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        desenharBlocoChao(ground1.x, ground1.width);
        desenharBlocoChao(ground2.x, ground2.width);
        desenharBlocoChao(ground3.x, ground3.width);
        desenharBlocoChao(ground4.x, ground4.width);

        shapeRenderer.setColor(0.1f, 0.35f, 0.75f, 1f);
        shapeRenderer.rect(800, 0, 180, 35);
        shapeRenderer.setColor(0.2f, 0.55f, 0.95f, 1f);
        for (float xw = 805; xw < 980; xw += 25) {
            shapeRenderer.rect(xw, 24, 12, 4);
        }

        shapeRenderer.setColor(0.1f, 0.35f, 0.75f, 1f);
        shapeRenderer.rect(2000, 0, 250, 35);
        shapeRenderer.setColor(0.2f, 0.55f, 0.95f, 1f);
        for (float xw = 2005; xw < 2250; xw += 25) {
            shapeRenderer.rect(xw, 24, 12, 4);
        }

        shapeRenderer.setColor(0.1f, 0.35f, 0.75f, 1f);
        shapeRenderer.rect(3300, 0, 300, 35);
        shapeRenderer.setColor(0.2f, 0.55f, 0.95f, 1f);
        for (float xw = 3305; xw < 3600; xw += 25) {
            shapeRenderer.rect(xw, 24, 12, 4);
        }

        float pulse = (float) Math.sin(stateTime * 6f) * 4f;
        float rx = finishPortal.x;
        float ry = finishPortal.y;

        shapeRenderer.setColor(1f, 0.2f, 0.2f, 0.6f);
        shapeRenderer.rect(rx - 4, ry, 68, 150 + pulse);
        shapeRenderer.setColor(1f, 0.6f, 0.1f, 0.6f);
        shapeRenderer.rect(rx + 2, ry, 56, 138 + pulse);
        shapeRenderer.setColor(1f, 1f, 0.2f, 0.6f);
        shapeRenderer.rect(rx + 8, ry, 44, 126 + pulse);
        shapeRenderer.setColor(0.2f, 0.9f, 0.3f, 0.6f);
        shapeRenderer.rect(rx + 14, ry, 32, 114 + pulse);
        shapeRenderer.setColor(0.2f, 0.5f, 1f, 0.6f);
        shapeRenderer.rect(rx + 20, ry, 20, 102 + pulse);
        shapeRenderer.setColor(0.7f, 0.3f, 0.9f, 0.6f);
        shapeRenderer.rect(rx + 24, ry, 12, 90 + pulse);

        shapeRenderer.end();

        batch.begin();
        player.render(batch);
        batch.end();

        drawUI();

        if (isPaused) {
            drawPauseMenu();
        }
    }

    private void avancarProximoNivel() {
        int nextLevel = level + 1;
        if (nextLevel <= 3) {
            game.setScreen(new GameScreen(game, nextLevel));
        } else {
            // Quando conclui o jogo, volta para o menu principal inicial (estado 0)
            game.setScreen(new MenuScreen(game, 0));
        }
        dispose();
    }

    private void perderVidaEApojar() {
        lives--;
        Gdx.app.log("GameScreen", "Caiu na água! Vidas restantes: " + lives);

        if (lives <= 0) {
            Gdx.app.log("GameScreen", "Game Over! A retornar ao Menu Principal...");
            // Quando as vidas chegam a 0, volta para o Menu Principal (estado 0)
            game.setScreen(new MenuScreen(game, 0));
            dispose();
        } else {
            if (player.getX() > 3300) {
                player.setX(3200f);
            } else if (player.getX() > 2000) {
                player.setX(1900f);
            } else {
                player.setX(100f);
            }
            player.setY(ground1.y + ground1.height);
        }
    }

    private void desenharBlocoChao(float startX, float largura) {
        shapeRenderer.setColor(0.25f, 0.15f, 0.1f, 1f);
        shapeRenderer.rect(startX, 0, largura, 20);
        shapeRenderer.setColor(0.15f, 0.45f, 0.15f, 1f);
        shapeRenderer.rect(startX, 20, largura, 15);
        shapeRenderer.setColor(0.35f, 0.75f, 0.25f, 1f);
        shapeRenderer.rect(startX, 35, largura, 10);
        shapeRenderer.setColor(0.45f, 0.85f, 0.3f, 1f);
        for (float x = startX; x < startX + largura; x += 16) {
            shapeRenderer.rect(x, 43, 8, 5);
        }
    }

    private void reiniciarJogo() {
        lives = 3;
        coinsCollected = 0;
        player.setX(100f);
        player.setY(ground1.y + ground1.height);
        createCoins();
    }

    private void updateTouchInput() {
        touchLeft = false;
        touchRight = false;
        touchJump = false;

        for (int i = 0; i < 3; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector3 touchPos = new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0);
                hudCamera.unproject(touchPos);

                float touchX = touchPos.x;
                float touchY = touchPos.y;

                if (isPaused) {
                    if (btnResume.contains(touchX, touchY) && i == 0) {
                        isPaused = false;
                    } else if (btnRestart.contains(touchX, touchY) && i == 0) {
                        reiniciarJogo();
                        isPaused = false;
                    } else if (btnMainMenu.contains(touchX, touchY) && i == 0) {
                        // Botão MENU no menu de pausa retorna ao Menu Principal
                        game.setScreen(new MenuScreen(game, 0));
                        dispose();
                    }
                } else {
                    if (btnLeft.contains(touchX, touchY)) touchLeft = true;
                    if (btnRight.contains(touchX, touchY)) touchRight = true;

                    if (Vector2.dst(touchX, touchY, btnJumpCenter.x, btnJumpCenter.y) <= btnJumpRadius) {
                        touchJump = true;
                    }

                    if (btnPause.contains(touchX, touchY) && i == 0) {
                        isPaused = true;
                    }
                }
            }
        }

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

            if (touchJump || Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                player.jump();
            }
        }
    }

    private void drawUI() {
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.3f, 0.3f, 0.35f, 0.7f);
        shapeRenderer.rect(btnLeft.x, btnLeft.y, btnLeft.width, btnLeft.height);
        shapeRenderer.rect(btnRight.x, btnRight.y, btnRight.width, btnRight.height);

        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 0.8f);
        shapeRenderer.rect(btnPause.x, btnPause.y, btnPause.width, btnPause.height);

        shapeRenderer.setColor(0.85f, 0.65f, 0.35f, 0.9f);
        shapeRenderer.circle(btnJumpCenter.x, btnJumpCenter.y, btnJumpRadius);
        shapeRenderer.setColor(0.95f, 0.82f, 0.55f, 0.95f);
        shapeRenderer.circle(btnJumpCenter.x, btnJumpCenter.y, btnJumpRadius - 7f);

        for (int i = 0; i < 3; i++) {
            float hx = 220 + (i * 30);
            float hy = WORLD_HEIGHT - 35;
            if (i < lives) {
                shapeRenderer.setColor(0.95f, 0.15f, 0.15f, 1f);
            } else {
                shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 1f);
            }
            shapeRenderer.circle(hx - 5, hy + 3, 8);
            shapeRenderer.circle(hx + 5, hy + 3, 8);
            shapeRenderer.triangle(hx - 14, hy + 1, hx + 14, hy + 1, hx, hy - 14);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "<", btnLeft.x + 30, btnLeft.y + 52);
        font.draw(batch, ">", btnRight.x + 30, btnRight.y + 52);
        font.draw(batch, "||", btnPause.x + 14, btnPause.y + 28);

        font.getData().setScale(1.2f);
        font.setColor(Color.valueOf("5A3815"));
        font.draw(batch, "SALTAR", btnJumpCenter.x - 38, btnJumpCenter.y + 6);
        font.getData().setScale(2f);

        font.setColor(Color.BLACK);
        font.draw(batch, "NIVEL " + level, VIEWPORT_WIDTH / 2f - 48, WORLD_HEIGHT - 18);
        font.setColor(Color.YELLOW);
        font.draw(batch, "NIVEL " + level, VIEWPORT_WIDTH / 2f - 50, WORLD_HEIGHT - 16);

        TextureRegion coinIcon = coinAnimation.getKeyFrame(stateTime, true);
        batch.draw(coinIcon, 20, WORLD_HEIGHT - 50, 32, 32);

        String coinText = "x " + coinsCollected + " / " + coins.size;
        font.setColor(Color.BLACK);
        font.draw(batch, coinText, 62, WORLD_HEIGHT - 22);
        font.setColor(Color.valueOf("FFD700"));
        font.draw(batch, coinText, 60, WORLD_HEIGHT - 20);
        batch.end();
    }

    private void drawPauseMenu() {
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, VIEWPORT_WIDTH, WORLD_HEIGHT);

        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(btnResume.x, btnResume.y, btnResume.width, btnResume.height);
        shapeRenderer.rect(btnRestart.x, btnRestart.y, btnRestart.width, btnRestart.height);
        shapeRenderer.rect(btnMainMenu.x, btnMainMenu.y, btnMainMenu.width, btnMainMenu.height);
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        fontGrande.setColor(Color.WHITE);
        fontGrande.draw(batch, "PAUSADO", VIEWPORT_WIDTH / 2f - 80, 400);

        font.setColor(Color.WHITE);
        font.draw(batch, "CONTINUAR", btnResume.x + 40, btnResume.y + 35);
        font.draw(batch, "REINICIAR", btnRestart.x + 45, btnRestart.y + 35);
        font.draw(batch, "MENU", btnMainMenu.x + 65, btnMainMenu.y + 35);
        batch.end();
    }

    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return true; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() { isPaused = true; }
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        fontGrande.dispose();
        player.dispose();
        backgroundTexture.dispose();
        coinTexture.dispose();
    }
}
