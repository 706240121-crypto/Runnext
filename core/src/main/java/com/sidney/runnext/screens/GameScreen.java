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
import com.sidney.runnext.Progress;
import com.sidney.runnext.entities.Coin;
import com.sidney.runnext.entities.Enemy;
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
    private final BitmapFont font;      // texto do HUD (moedas, botões)
    private final BitmapFont menuFont;  // texto dos menus (escala definida pelo MenuStyle)

    private final Player player;

    private static final float GROUND_TOP = 45f; // altura do topo do chão

    // Layout das águas de cada nível: {x, largura, x, largura, ...}
    // ATENÇÃO: a personagem salta no máximo ~200 px. Não ponha larguras acima de ~190.
    private static final float[] WATER_LAYOUT_PADRAO = {800, 120, 2000, 160, 3300, 180};
    private static final float[] WATER_LAYOUT_NIVEL_2 = {650, 150, 1100, 170, 1650, 160, 2250, 180, 2900, 170};

    // Inimigos do nível 2: {x esquerdo da zona, x direito da zona, velocidade em px/s}
    // Cada zona tem de ficar DENTRO de um bloco de chão (senão o inimigo anda por cima da água).
    private static final float[][] ENEMIES_NIVEL_2 = {
        {1340, 1560,  70},
        {2530, 2820,  90},
        {3300, 3750, 110}
    };
    // Todos os inimigos do nível 2 usam o mesmo sprite (se pôr mais nomes aqui, são usados por ordem)
    private static final String[] ENEMY_SPRITES_NIVEL_2 = {
        "enemies/snake.png"
    };
    private static final float INVULNERABLE_TIME = 1.5f; // segundos sem levar dano após ser atingida

    private final Rectangle[] grounds;  // blocos de chão (o que sobra entre as águas)
    private final Rectangle[] waters;   // zonas de água (sem chão por baixo)

    private final Rectangle finishPortal;

    private final int level;
    private final Texture backgroundTexture;
    private final TextureRegion background;

    private final Texture coinTexture;
    private final Animation<TextureRegion> coinAnimation;
    private final Array<Coin> coins = new Array<>();
    private int coinsCollected = 0;
    private float stateTime = 0f;

    private final Array<Enemy> enemies = new Array<>();
    private final Array<Texture> enemyTextures = new Array<>();
    private float invulnTimer = 0f;

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

    // Menu de pausa
    private final Rectangle btnResume;
    private final Rectangle btnRestart;
    private final Rectangle btnMainMenu;
    private final Rectangle[] pauseButtons;
    private final String[] pauseLabels = {"CONTINUAR", "REINICIAR", "MENU PRINCIPAL"};

    // Estados dos menus
    private boolean isLevelComplete = false;
    private boolean showingScore = false;
    private boolean isGameOver = false;

    // Menu de Nível Completo
    private final Rectangle btnLC_Next;
    private final Rectangle btnLC_Restart;
    private final Rectangle btnLC_Score;
    private final Rectangle btnScore_Back;
    private final Rectangle[] levelCompleteButtons;
    private final String[] levelCompleteLabels;

    // Menu de Game Over
    private final Rectangle btnGO_Restart;
    private final Rectangle btnGO_Menu;
    private final Rectangle[] gameOverButtons;
    private final String[] gameOverLabels = {"REINICIAR", "MENU PRINCIPAL"};

    // Ecrã para onde vamos trocar no FIM do frame.
    // (Trocar de ecrã a meio do render e destruir este ecrã fazia o jogo fechar.)
    private Screen pendingScreen = null;

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

        menuFont = new BitmapFont();

        backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_FILES[this.level - 1]));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        background = new TextureRegion(backgroundTexture, 40, 0, 1200, 720);

        coinTexture = new Texture(Gdx.files.internal("sprites/coin.png"));
        coinTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] coinFrames = TextureRegion.split(coinTexture, 16, 16);
        coinAnimation = new Animation<>(0.12f, coinFrames[0]);

        // Só o nível 2 tem um layout próprio (mais águas, mais juntas). Níveis 1 e 3 ficam como estavam.
        float[] layout = (this.level == 2) ? WATER_LAYOUT_NIVEL_2 : WATER_LAYOUT_PADRAO;
        int nAgua = layout.length / 2;
        waters = new Rectangle[nAgua];
        grounds = new Rectangle[nAgua + 1];

        // O chão é o que sobra entre as águas
        float inicioChao = 0f;
        for (int i = 0; i < nAgua; i++) {
            float wx = layout[i * 2];
            float ww = layout[i * 2 + 1];
            waters[i] = new Rectangle(wx, 0, ww, 35);
            grounds[i] = new Rectangle(inicioChao, 0, wx - inicioChao, GROUND_TOP);
            inicioChao = wx + ww;
        }
        grounds[nAgua] = new Rectangle(inicioChao, 0, WORLD_WIDTH - inicioChao, GROUND_TOP);

        finishPortal = new Rectangle(4050, 45, 60, 150);

        player = new Player(100, GROUND_TOP);

        createCoins();

        if (this.level == 2) {
            for (String path : ENEMY_SPRITES_NIVEL_2) {
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                enemyTextures.add(t);
            }
        }
        createEnemies();

        btnLeft = new Rectangle(25, 15, 80, 80);
        btnRight = new Rectangle(115, 15, 80, 80);
        btnJumpCenter = new Vector2(VIEWPORT_WIDTH - 85, 55);

        btnPause = new Rectangle(375, WORLD_HEIGHT - 60, 50, 40);

        // Todos os menus usam as mesmas posições/estilo do menu inicial (MenuStyle)
        btnResume = MenuStyle.button(0);
        btnRestart = MenuStyle.button(1);
        btnMainMenu = MenuStyle.button(2);
        pauseButtons = new Rectangle[] {btnResume, btnRestart, btnMainMenu};

        btnLC_Next = MenuStyle.button(0);
        btnLC_Restart = MenuStyle.button(1);
        btnLC_Score = MenuStyle.button(2);
        btnScore_Back = MenuStyle.button(2);
        levelCompleteButtons = new Rectangle[] {btnLC_Next, btnLC_Restart, btnLC_Score};
        levelCompleteLabels = new String[] {
            (this.level < Progress.TOTAL_LEVELS) ? "PRÓXIMO NÍVEL" : "MENU PRINCIPAL",
            "REINICIAR",
            "VER PONTUAÇÃO"
        };

        btnGO_Restart = MenuStyle.button(0);
        btnGO_Menu = MenuStyle.button(1);
        gameOverButtons = new Rectangle[] {btnGO_Restart, btnGO_Menu};
    }

    private void createCoins() {
        coins.clear();
        coinsCollected = 0;

        if (level == 2) {
            createCoinsNivel2();
            return;
        }

        float groundTop = GROUND_TOP;

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

    private void createEnemies() {
        enemies.clear();
        invulnTimer = 0f;

        if (level == 2) {
            for (int i = 0; i < ENEMIES_NIVEL_2.length; i++) {
                float[] cfg = ENEMIES_NIVEL_2[i];
                TextureRegion sprite = new TextureRegion(enemyTextures.get(i % enemyTextures.size));
                enemies.add(new Enemy(sprite, cfg[0], cfg[1], GROUND_TOP, cfg[2]));
            }
        }
    }

    // Moedas do nível 2: uma fila em cada bloco de chão + um arco por cima de cada água
    private void createCoinsNivel2() {
        for (int g = 0; g < grounds.length; g++) {
            Rectangle gr = grounds[g];
            float inicio = (g == 0) ? 400f : gr.x + 70f;
            float fim = gr.x + gr.width - 70f;
            int n = 0;
            for (float cx = inicio; cx <= fim && n < 8; cx += 55f, n++) {
                coins.add(new Coin(cx, GROUND_TOP + 30));
            }
        }

        float[] arcY = {175f, 205f, 220f, 205f, 175f};
        for (Rectangle w : waters) {
            float centro = w.x + w.width / 2f;
            for (int i = 0; i < arcY.length; i++) {
                coins.add(new Coin(centro - 80f + i * 40f, arcY[i]));
            }
        }
    }

    private Rectangle getPlayerBody() {
        Rectangle b = player.getBounds();
        return new Rectangle(b.x + 25, b.y + 12, b.width - 50, b.height - 25);
    }

    // true se o ponto x (centro dos pés) está sobre algum bloco de chão
    private boolean isOverGround(float x) {
        for (Rectangle g : grounds) {
            if (x >= g.x && x <= g.x + g.width) return true;
        }
        return false;
    }

    // Converte um toque no ecrã para as coordenadas do HUD/menus (800x480),
    // tendo em conta as barras pretas do FitViewport.
    private Vector3 toHud(int screenX, int screenY) {
        Vector3 v = new Vector3(screenX, screenY, 0);
        hudCamera.unproject(v, viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight());
        return v;
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "=== TELA INICIADA (nível " + level + ") ===");
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        handleMenuTouch();   // toques nos menus (pausa, nível completo, game over)
        updateTouchInput();  // controlos do jogo (botões no ecrã e teclado)

        // Troca de ecrã SÓ aqui, antes de usar o batch/fontes deste ecrã neste frame
        if (pendingScreen != null) {
            trocarEcra();
            return;
        }

        // Só atualiza a física se NÃO estiver pausado, NÃO tiver completado o nível e NÃO for Game Over
        if (!isPaused && !isLevelComplete && !isGameOver) {
            float prevFootY = player.getFootY(); // posição dos pés antes do update
            player.update(delta);
            stateTime += delta;

            if (player.getX() < 0) {
                player.setX(0);
            } else if (player.getX() + Player.WIDTH > WORLD_WIDTH) {
                player.setX(WORLD_WIDTH - Player.WIDTH);
            }

            // Aterragem pelo centro dos pés, só se vinha de cima do chão
            Rectangle body = getPlayerBody();
            float feetX = body.x + body.width / 2f;
            float groundLevel = GROUND_TOP;

            if (player.getVelocityY() <= 0 && prevFootY >= groundLevel - 1f && player.getFootY() <= groundLevel) {
                if (isOverGround(feetX)) {
                    player.landOn(groundLevel);
                }
            }

            if (player.getY() < 25) {
                perderVidaEApojar();
            }

            Rectangle playerBody = getPlayerBody();
            if (playerBody.overlaps(finishPortal)) {
                Gdx.app.log("GameScreen", "Nível " + level + " concluído!");
                isLevelComplete = true;
                isPaused = true;
                Progress.unlock(level + 1); // desbloqueia o próximo nível (fica guardado)
            }

            for (Coin coin : coins) {
                if (!coin.isCollected() && playerBody.overlaps(coin.getBounds())) {
                    coin.collect();
                    coinsCollected++;
                    Gdx.app.log("GameScreen", "Moeda apanhada! Total: " + coinsCollected);
                }
            }

            // Inimigos: pisar por cima derrota-os; tocar de lado tira uma vida
            if (invulnTimer > 0f) invulnTimer -= delta;

            for (Enemy enemy : enemies) {
                if (isGameOver) break;
                if (!enemy.isAlive()) continue;
                enemy.update(delta);

                Rectangle eb = enemy.getBounds();
                if (playerBody.overlaps(eb)) {
                    boolean pisou = player.getVelocityY() < 0 && prevFootY >= eb.y + eb.height - 15f;
                    if (pisou) {
                        enemy.kill();
                        player.bounce(0.7f);
                    } else if (invulnTimer <= 0f) {
                        tomarDanoDeInimigo();
                    }
                }
            }
        } else if (!isLevelComplete && !isGameOver) {
            stateTime += delta;
        }

        // Menus: ocupam o ecrã todo, com o mesmo visual do menu inicial
        if (isGameOver) {
            drawMenu("YOU LOSE", gameOverButtons, gameOverLabels);
        } else if (isLevelComplete) {
            if (showingScore) {
                drawScoreScreen();
            } else {
                drawMenu("NÍVEL COMPLETO!", levelCompleteButtons, levelCompleteLabels);
            }
        } else if (isPaused) {
            drawMenu("PAUSADO", pauseButtons, pauseLabels);
        } else {
            drawWorld();
            drawUI();
        }
    }

    private void trocarEcra() {
        Screen destino = pendingScreen;
        pendingScreen = null;
        dispose();
        game.setScreen(destino);
    }

    private void drawWorld() {
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

        for (Rectangle g : grounds) {
            desenharBlocoChao(g.x, g.width);
        }
        for (Rectangle w : waters) {
            desenharAgua(w);
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
        for (Enemy e : enemies) {
            e.render(batch);
        }
        // pisca enquanto está invulnerável depois de levar dano
        boolean piscar = invulnTimer > 0f && ((int) (invulnTimer * 10f) % 2 == 0);
        if (!piscar) player.render(batch);
        batch.end();
    }

    private void tomarDanoDeInimigo() {
        lives--;
        Gdx.app.log("GameScreen", "Tocou num inimigo! Vidas restantes: " + lives);
        invulnTimer = INVULNERABLE_TIME;
        player.bounce(0.5f); // pequeno salto de susto

        if (lives <= 0) {
            isGameOver = true;
            isPaused = true;
        }
    }

    private void irParaProximoNivelOuMenu() {
        int nextLevel = level + 1;
        if (nextLevel <= Progress.TOTAL_LEVELS) {
            pendingScreen = new GameScreen(game, nextLevel);
        } else {
            pendingScreen = new MenuScreen(game, 0);
        }
    }

    private void perderVidaEApojar() {
        lives--;
        Gdx.app.log("GameScreen", "Caiu na água! Vidas restantes: " + lives);

        if (lives <= 0) {
            Gdx.app.log("GameScreen", "Game Over! Exibindo tela de YOU LOSE...");
            isGameOver = true;
            isPaused = true; // Pausa o jogo para mostrar o menu
        } else {
            // Respawn baseado no centro do corpo e nas posições da água
            float cx = getPlayerBody().x + getPlayerBody().width / 2f;
            int idx = 0;
            for (int i = 0; i < waters.length; i++) {
                if (cx > waters[i].x - 100f) idx = i;
            }
            player.setX(idx == 0 ? 100f : waters[idx].x - 100f);
            player.setY(GROUND_TOP);
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

    // Desenha um bloco de água a partir do seu retângulo
    private void desenharAgua(Rectangle w) {
        shapeRenderer.setColor(0.1f, 0.35f, 0.75f, 1f);
        shapeRenderer.rect(w.x, w.y, w.width, w.height);
        shapeRenderer.setColor(0.2f, 0.55f, 0.95f, 1f);
        for (float xw = w.x + 5; xw + 12 <= w.x + w.width; xw += 25) {
            shapeRenderer.rect(xw, 24, 12, 4);
        }
    }

    private void reiniciarJogo() {
        lives = 3;
        coinsCollected = 0;
        player.setX(100f);
        player.setY(GROUND_TOP);
        isLevelComplete = false;
        showingScore = false;
        isGameOver = false;
        isPaused = false;
        createCoins();
        createEnemies();
    }

    // Toques nos botões dos menus (só um toque novo por vez, não repete enquanto o dedo está pousado)
    private void handleMenuTouch() {
        boolean menuAtivo = isGameOver || isLevelComplete || isPaused;
        if (!menuAtivo || !Gdx.input.justTouched()) return;

        Vector3 t = toHud(Gdx.input.getX(), Gdx.input.getY());

        if (isGameOver) {
            if (btnGO_Restart.contains(t.x, t.y)) {
                reiniciarJogo();
            } else if (btnGO_Menu.contains(t.x, t.y)) {
                pendingScreen = new MenuScreen(game, 0);
            }
        } else if (isLevelComplete) {
            if (!showingScore) {
                if (btnLC_Next.contains(t.x, t.y)) {
                    irParaProximoNivelOuMenu();
                } else if (btnLC_Restart.contains(t.x, t.y)) {
                    reiniciarJogo();
                } else if (btnLC_Score.contains(t.x, t.y)) {
                    showingScore = true;
                }
            } else if (btnScore_Back.contains(t.x, t.y)) {
                showingScore = false;
            }
        } else { // pausa
            if (btnResume.contains(t.x, t.y)) {
                isPaused = false;
            } else if (btnRestart.contains(t.x, t.y)) {
                reiniciarJogo();
            } else if (btnMainMenu.contains(t.x, t.y)) {
                pendingScreen = new MenuScreen(game, 0);
            }
        }
    }

    // Controlos do jogo (botões no ecrã + teclado)
    private void updateTouchInput() {
        touchLeft = false;
        touchRight = false;
        touchJump = false;

        if (isPaused || isLevelComplete || isGameOver) return;

        for (int i = 0; i < 3; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector3 touchPos = toHud(Gdx.input.getX(i), Gdx.input.getY(i));
                float touchX = touchPos.x;
                float touchY = touchPos.y;

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

    // Menu genérico (pausa, nível completo, game over) com o estilo do menu inicial
    private void drawMenu(String titulo, Rectangle[] botoes, String[] textos) {
        MenuStyle.clear();

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Rectangle b : botoes) {
            MenuStyle.drawButton(shapeRenderer, b, true);
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        MenuStyle.drawTitle(batch, menuFont, titulo, MenuStyle.TITLE_Y);
        for (int i = 0; i < botoes.length; i++) {
            MenuStyle.drawButtonLabel(batch, menuFont, textos[i], botoes[i], true);
        }
        batch.end();
    }

    private void drawScoreScreen() {
        MenuStyle.clear();

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        MenuStyle.drawButton(shapeRenderer, btnScore_Back, true);
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        MenuStyle.drawTitle(batch, menuFont, "PONTUAÇÃO", MenuStyle.TITLE_Y);
        MenuStyle.drawLine(batch, menuFont, "Vidas Restantes: " + lives, 300f);
        MenuStyle.drawLine(batch, menuFont, "Moedas Apanhadas: " + coinsCollected, 255f);
        MenuStyle.drawButtonLabel(batch, menuFont, "VOLTAR", btnScore_Back, true);
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
        menuFont.dispose();
        player.dispose();
        backgroundTexture.dispose();
        coinTexture.dispose();
        for (Texture t : enemyTextures) {
            t.dispose();
        }
    }
}
