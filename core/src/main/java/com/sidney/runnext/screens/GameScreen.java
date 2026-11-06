package com.sidney.runnext.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import com.sidney.runnext.entities.Boss;
import com.sidney.runnext.entities.Coin;
import com.sidney.runnext.entities.Enemy;
import com.sidney.runnext.entities.Player;
import com.sidney.runnext.entities.Spike;

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

    // Efeitos sonoros (assets/audio/sfx)
    private final Sound jumpSound;
    private final Sound coinSound;
    private final Sound hurtSound;
    private final Sound deathSound;

    private final Player player;

    private static final float GROUND_TOP = 45f; // altura do topo do chão

    // Layout das águas de cada nível: {x, largura, x, largura, ...}
    // ATENÇÃO: a personagem salta no máximo ~200 px. Não ponha larguras acima de ~190.
    private static final float[] WATER_LAYOUT_PADRAO = {800, 120, 2000, 160, 3300, 180};
    private static final float[] WATER_LAYOUT_NIVEL_2 = {650, 150, 1100, 170, 1650, 160, 2250, 180, 2900, 170};
    // Nível 3 (castelo): 3 águas e depois um chão comprido (obstáculos e a arena do Boss)
    private static final float[] WATER_LAYOUT_NIVEL_3 = {650, 150, 1300, 170, 2000, 180};

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
    // Inimigos do nível 3 (mesmo formato): mais rápidos que os do nível 2
    private static final float[][] ENEMIES_NIVEL_3 = {
        {1050, 1260,  90},
        {1520, 1690, 100},
        {2520, 2780, 110},
        {2950, 3250, 120}
    };

    // Espinhos do nível 3: {x esquerdo, largura}. Tocar com os pés tira uma vida.
    private static final float[][] SPIKES_NIVEL_3 = {
        { 900, 60},
        {1750, 60},
        {2400, 80},
        {2860, 60},
        {3300, 60}
    };

    // Boss do nível 3: a arena começa em ARENA_TRIGGER_X; o Boss patrulha entre as duas margens
    private static final float ARENA_TRIGGER_X = 3380f;
    private static final float BOSS_ZONE_LEFT = 3450f;
    private static final float BOSS_ZONE_RIGHT = 4180f;
    private static final float BOSS_START_X = 3950f;

    // Plataformas flutuantes do nível 2 (por cima dos inimigos): {x esquerdo, largura}
    // A personagem salta no máximo ~125 px acima do chão, por isso o topo não deve passar de ~150.
    private static final float[][] PLATFORMS_NIVEL_2 = {
        {1340, 220},
        {2530, 290},
        {3300, 450}
    };
    // Plataformas do nível 3 (mesmo formato), por cima dos inimigos
    private static final float[][] PLATFORMS_NIVEL_3 = {
        {1040, 240},
        {1500, 220},
        {2500, 300},
        {2930, 340}
    };
    private static final float PLATFORM_TOP = 160f;       // altura do topo da plataforma (o salto chega a ~170)
    private static final float PLATFORM_THICKNESS = 20f;
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

    private final Array<Rectangle> platforms = new Array<>(); // plataformas flutuantes
    private final Array<Enemy> enemies = new Array<>();
    private final Array<Spike> spikes = new Array<>();   // obstáculos que tiram vida
    private Boss boss = null;                            // só existe no nível 3
    private Texture bossTexture = null;
    private boolean bossScored = false;
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
    private boolean showingVictory = false;   // ecrã final, depois de completar o último nível
    private boolean isGameOver = false;
    private boolean resultsSaved = false;     // resultados do nível já calculados e guardados

    // Pontuação (enunciado: moeda +10, inimigo +50, completar nível +200)
    private static final int POINTS_COIN = 10;
    private static final int POINTS_ENEMY = 50;
    private static final int POINTS_LEVEL = 200;
    private static final int POINTS_BOSS = 500;
    private final int scoreCarried;           // pontos acumulados dos níveis anteriores
    private int levelScore = 0;               // pontos feitos neste nível
    private int enemiesDefeated = 0;
    private int bestScore = 0;
    private boolean newRecord = false;
    private final GlyphLayout hudLayout = new GlyphLayout();

    // Resultados do nível: números à esquerda, botões na coluna da direita
    private final Rectangle btnLC_Next;
    private final Rectangle btnLC_Restart;
    private final Rectangle btnLC_Menu;
    private final Rectangle[] levelCompleteButtons;
    private final String[] levelCompleteLabels;

    // Game Over
    private final Rectangle btnGO_Restart;
    private final Rectangle btnGO_Menu;
    private final Rectangle[] gameOverButtons;
    private final String[] gameOverLabels = {"TENTAR NOVAMENTE", "MENU PRINCIPAL"};

    // Vitória (fim do jogo)
    private final Rectangle btnVic_Again;
    private final Rectangle btnVic_Menu;
    private final Rectangle[] victoryButtons;
    private final String[] victoryLabels = {"JOGAR NOVAMENTE", "MENU PRINCIPAL"};

    // Ecrã para onde vamos trocar no FIM do frame.
    // (Trocar de ecrã a meio do render e destruir este ecrã fazia o jogo fechar.)
    private Screen pendingScreen = null;

    public GameScreen(Game game) {
        this(game, 1);
    }

    public GameScreen(Game game, int level) {
        this(game, level, 0);
    }

    // startScore = pontos acumulados dos níveis anteriores
    public GameScreen(Game game, int level, int startScore) {
        this.game = game;
        this.scoreCarried = Math.max(0, startScore);
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

        jumpSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/jump.wav"));
        coinSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/coin.wav"));
        hurtSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/hurt.wav"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/death.wav"));

        backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_FILES[this.level - 1]));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        background = new TextureRegion(backgroundTexture, 40, 0, 1200, 720);

        coinTexture = new Texture(Gdx.files.internal("sprites/coin.png"));
        coinTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] coinFrames = TextureRegion.split(coinTexture, 16, 16);
        coinAnimation = new Animation<>(0.12f, coinFrames[0]);

        // Cada nível tem o seu layout de águas (o nível 1 usa o padrão)
        float[] layout = (this.level == 2) ? WATER_LAYOUT_NIVEL_2
            : (this.level == 3) ? WATER_LAYOUT_NIVEL_3 : WATER_LAYOUT_PADRAO;
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

        createPlatforms();
        createSpikes();
        createCoins();

        if (this.level == 2 || this.level == 3) {
            for (String path : ENEMY_SPRITES_NIVEL_2) {
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                enemyTextures.add(t);
            }
        }
        if (this.level == 3) {
            bossTexture = new Texture(Gdx.files.internal("enemies/boss.png"));
            bossTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        createEnemies();
        createBoss();

        btnLeft = new Rectangle(25, 15, 80, 80);
        btnRight = new Rectangle(115, 15, 80, 80);
        btnJumpCenter = new Vector2(VIEWPORT_WIDTH - 85, 55);

        btnPause = new Rectangle(375, WORLD_HEIGHT - 60, 50, 40);

        // Todos os menus usam as mesmas posições/estilo do menu inicial (MenuStyle)
        btnResume = MenuStyle.button(0);
        btnRestart = MenuStyle.button(1);
        btnMainMenu = MenuStyle.button(2);
        pauseButtons = new Rectangle[] {btnResume, btnRestart, btnMainMenu};

        // Resultados do nível: botões na coluna da direita
        btnLC_Next = MenuStyle.buttonAt(600f, 350f, 0);
        btnLC_Restart = MenuStyle.buttonAt(600f, 350f, 1);
        btnLC_Menu = MenuStyle.buttonAt(600f, 350f, 2);
        levelCompleteButtons = new Rectangle[] {btnLC_Next, btnLC_Restart, btnLC_Menu};
        levelCompleteLabels = new String[] {
            (this.level < Progress.TOTAL_LEVELS) ? "PRÓXIMO NÍVEL" : "CONTINUAR",
            "REINICIAR",
            "MENU PRINCIPAL"
        };

        btnGO_Restart = MenuStyle.buttonAt(MenuStyle.CENTER_X, 290f, 0);
        btnGO_Menu = MenuStyle.buttonAt(MenuStyle.CENTER_X, 290f, 1);
        gameOverButtons = new Rectangle[] {btnGO_Restart, btnGO_Menu};

        btnVic_Again = MenuStyle.buttonAt(MenuStyle.CENTER_X, 260f, 0);
        btnVic_Menu = MenuStyle.buttonAt(MenuStyle.CENTER_X, 260f, 1);
        victoryButtons = new Rectangle[] {btnVic_Again, btnVic_Menu};
    }

    private void createCoins() {
        coins.clear();
        coinsCollected = 0;

        if (level == 2 || level == 3) {
            createCoinsGeradas();
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

    private void createPlatforms() {
        platforms.clear();
        float[][] cfgs = (level == 2) ? PLATFORMS_NIVEL_2 : (level == 3) ? PLATFORMS_NIVEL_3 : null;
        if (cfgs != null) {
            for (float[] cfg : cfgs) {
                platforms.add(new Rectangle(cfg[0], PLATFORM_TOP - PLATFORM_THICKNESS, cfg[1], PLATFORM_THICKNESS));
            }
        }
    }

    private void createSpikes() {
        spikes.clear();
        if (level == 3) {
            for (float[] cfg : SPIKES_NIVEL_3) {
                spikes.add(new Spike(cfg[0], GROUND_TOP, cfg[1]));
            }
        }
    }

    private void createBoss() {
        boss = null;
        bossScored = false;
        if (level == 3) {
            boss = new Boss(new TextureRegion(bossTexture), BOSS_ZONE_LEFT, BOSS_ZONE_RIGHT, BOSS_START_X, GROUND_TOP);
        }
    }

    // O portal de saída só funciona depois de derrotar o Boss (nos níveis que o têm)
    private boolean portalActive() {
        return boss == null || boss.isDead();
    }

    private void createEnemies() {
        enemies.clear();
        invulnTimer = 0f;

        float[][] cfgs = (level == 2) ? ENEMIES_NIVEL_2 : (level == 3) ? ENEMIES_NIVEL_3 : null;
        if (cfgs != null) {
            for (int i = 0; i < cfgs.length; i++) {
                float[] cfg = cfgs[i];
                TextureRegion sprite = new TextureRegion(enemyTextures.get(i % enemyTextures.size));
                enemies.add(new Enemy(sprite, cfg[0], cfg[1], GROUND_TOP, cfg[2]));
            }
        }
    }

    // Moedas geradas (níveis 2 e 3): uma fila em cada bloco de chão, um arco por cima de cada água
    // e uma fila em cima de cada plataforma
    private void createCoinsGeradas() {
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

        // Moedas em cima de cada plataforma (recompensa por lá chegar)
        for (Rectangle p : platforms) {
            int n = 0;
            for (float cx = p.x + 40f; cx <= p.x + p.width - 40f && n < 6; cx += 55f, n++) {
                coins.add(new Coin(cx, p.y + p.height + 30f));
            }
        }
    }

    // Caixa mais estreita que o corpo: torna mais justas as colisões com o Boss
    private Rectangle getPlayerHurtBox() {
        Rectangle b = getPlayerBody();
        return new Rectangle(b.x + 20f, b.y, b.width - 40f, b.height);
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

            boolean landed = false;

            if (player.getVelocityY() <= 0 && prevFootY >= groundLevel - 1f && player.getFootY() <= groundLevel) {
                if (isOverGround(feetX)) {
                    player.landOn(groundLevel);
                    landed = true;
                }
            }

            // Plataformas flutuantes: pousa-se vindo de CIMA; por BAIXO não se passa (bate e perde vida)
            for (Rectangle pl : platforms) {
                if (feetX < pl.x || feetX > pl.x + pl.width) continue; // pés fora da plataforma

                float top = pl.y + pl.height;
                float bottom = pl.y;

                // 1. POUSAR em cima: a descer, com os pés a vir de cima (ou da espessura) da plataforma
                if (!landed && player.getVelocityY() <= 0 && prevFootY >= bottom && player.getFootY() <= top) {
                    player.landOn(top);
                    landed = true;
                    break;
                }

                // 2. BATER por baixo: os pés estavam abaixo da plataforma e iam a subir através dela
                if (player.getVelocityY() > 0 && prevFootY < bottom && player.getFootY() >= bottom) {
                    player.setY(player.getY() - (player.getFootY() - bottom) - 1f); // pés logo abaixo da plataforma
                    player.bounce(-0.3f);                                            // empurra para baixo
                    bateuNaPlataforma();
                    break;
                }
            }

            // Sem nada por baixo dos pés (saiu da borda): já não está "no chão"
            if (!landed) {
                player.leaveGround();
            }

            if (player.getY() < 25) {
                perderVidaEApojar();
            }

            Rectangle playerBody = getPlayerBody();
            if (portalActive() && playerBody.overlaps(finishPortal)) {
                Gdx.app.log("GameScreen", "Nível " + level + " concluído!");
                isLevelComplete = true;
                isPaused = true;
                Progress.unlock(level + 1); // desbloqueia o próximo nível (fica guardado)
            }

            for (Coin coin : coins) {
                if (!coin.isCollected() && playerBody.overlaps(coin.getBounds())) {
                    coin.collect();
                    coinsCollected++;
                    coinSound.play();
                    levelScore += POINTS_COIN;
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
                        enemiesDefeated++;
                        levelScore += POINTS_ENEMY;
                        player.bounce(0.7f);
                    } else if (invulnTimer <= 0f) {
                        tomarDanoDeInimigo();
                    }
                }
            }

            // Obstáculos (espinhos): tocar com os pés tira uma vida
            if (!isGameOver && invulnTimer <= 0f) {
                Rectangle pes = new Rectangle(playerBody.x + 15f, player.getFootY(), playerBody.width - 30f, 16f);
                for (Spike sp : spikes) {
                    if (pes.overlaps(sp.getBounds())) {
                        tomarDanoDeInimigo();
                        break;
                    }
                }
            }

            // Boss: acorda quando a personagem chega à arena. Pisar-lhe tira 1 de vida; tocar de lado fere a personagem.
            if (boss != null && !isGameOver) {
                float centroX = playerBody.x + playerBody.width / 2f;
                if (!boss.isActive() && centroX >= ARENA_TRIGGER_X) {
                    boss.activate();
                }
                boss.update(delta, centroX);

                if (boss.isCollidable() && getPlayerHurtBox().overlaps(boss.getBounds())) {
                    Rectangle bb = boss.getBounds();
                    boolean pisou = player.getVelocityY() < 0 && prevFootY >= bb.y + bb.height - 20f;
                    if (pisou) {
                        boss.takeHit();
                        player.bounce(0.8f);
                    } else if (boss.canHurtPlayer() && invulnTimer <= 0f) {
                        tomarDanoDeInimigo();
                    }
                }

                if (boss.isDead() && !bossScored) {
                    bossScored = true;
                    levelScore += POINTS_BOSS;
                }
            }

            // Fim do nível: calcula e guarda os resultados (uma só vez)
            if (isLevelComplete && !resultsSaved) {
                resultsSaved = true;
                levelScore += POINTS_LEVEL;
                newRecord = Progress.submitScore(level, levelScore);
                bestScore = Progress.getBestScore(level);
            }
        } else if (!isLevelComplete && !isGameOver) {
            stateTime += delta;
        }

        // Menus: ocupam o ecrã todo, com o mesmo visual do menu inicial
        if (isGameOver) {
            drawGameOver();
        } else if (isLevelComplete) {
            if (showingVictory) {
                drawVictory();
            } else {
                drawLevelResults();
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
        for (Rectangle pl : platforms) {
            desenharPlataforma(pl);
        }
        for (Spike sp : spikes) {
            sp.render(shapeRenderer);
        }

        // Portal de saída (nos níveis com Boss só aparece depois de o derrotar)
        if (portalActive()) {
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
        }

        shapeRenderer.end();

        batch.begin();
        for (Enemy e : enemies) {
            e.render(batch);
        }
        if (boss != null) boss.render(batch);
        // pisca enquanto está invulnerável depois de levar dano
        boolean piscar = invulnTimer > 0f && ((int) (invulnTimer * 10f) % 2 == 0);
        if (!piscar) player.render(batch);
        batch.end();
    }

    // Som ao perder uma vida: "hurt" se ainda tem vidas, "death" se foi a última
    private void tocarSomDeDano() {
        if (lives <= 0) {
            deathSound.play();
        } else {
            hurtSound.play();
        }
    }

    // Bateu na parte de baixo de uma plataforma: perde uma vida (se não estiver protegida)
    private void bateuNaPlataforma() {
        if (invulnTimer > 0f) return;

        lives--;
        tocarSomDeDano();
        Gdx.app.log("GameScreen", "Bateu na plataforma! Vidas restantes: " + lives);
        invulnTimer = INVULNERABLE_TIME;

        if (lives <= 0) {
            isGameOver = true;
            isPaused = true;
        }
    }

    private void tomarDanoDeInimigo() {
        lives--;
        tocarSomDeDano();
        Gdx.app.log("GameScreen", "Levou dano! Vidas restantes: " + lives);
        invulnTimer = INVULNERABLE_TIME;
        player.bounce(0.5f); // pequeno salto de susto

        if (lives <= 0) {
            isGameOver = true;
            isPaused = true;
        }
    }

    private int totalScore() {
        return scoreCarried + levelScore;
    }

    private void irParaProximoNivelOuMenu() {
        int nextLevel = level + 1;
        if (nextLevel <= Progress.TOTAL_LEVELS) {
            pendingScreen = new GameScreen(game, nextLevel, totalScore());
        } else {
            showingVictory = true; // acabou o último nível: ecrã de vitória
        }
    }

    private void perderVidaEApojar() {
        lives--;
        tocarSomDeDano();
        Gdx.app.log("GameScreen", "Caiu na água! Vidas restantes: " + lives);

        if (lives <= 0) {
            Gdx.app.log("GameScreen", "Game Over! Exibindo tela de GAME OVER...");
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

    // Plataforma flutuante: terra com relva em cima (mesmas cores do chão)
    private void desenharPlataforma(Rectangle pl) {
        shapeRenderer.setColor(0.25f, 0.15f, 0.1f, 1f);
        shapeRenderer.rect(pl.x, pl.y, pl.width, pl.height);
        shapeRenderer.setColor(0.35f, 0.75f, 0.25f, 1f);
        shapeRenderer.rect(pl.x, pl.y + pl.height - 8f, pl.width, 8f);
        shapeRenderer.setColor(0.45f, 0.85f, 0.3f, 1f);
        for (float x = pl.x; x < pl.x + pl.width; x += 16f) {
            shapeRenderer.rect(x, pl.y + pl.height - 2f, 8f, 5f);
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
        levelScore = 0;
        enemiesDefeated = 0;
        resultsSaved = false;
        newRecord = false;
        showingVictory = false;
        player.setX(100f);
        player.setY(GROUND_TOP);
        isLevelComplete = false;
        isGameOver = false;
        isPaused = false;
        createCoins();
        createEnemies();
        createBoss();
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
            if (showingVictory) {
                if (btnVic_Again.contains(t.x, t.y)) {
                    pendingScreen = new GameScreen(game, 1, 0); // joga tudo de novo, pontuação a zero
                } else if (btnVic_Menu.contains(t.x, t.y)) {
                    pendingScreen = new MenuScreen(game, 0);
                }
            } else {
                if (btnLC_Next.contains(t.x, t.y)) {
                    irParaProximoNivelOuMenu();
                } else if (btnLC_Restart.contains(t.x, t.y)) {
                    reiniciarJogo();
                } else if (btnLC_Menu.contains(t.x, t.y)) {
                    pendingScreen = new MenuScreen(game, 0);
                }
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
            float vyAntes = player.getVelocityY();
            player.jump();
            if (vyAntes <= 0f && player.getVelocityY() > 0f) {
                jumpSound.play();
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

        // Barra de vida do Boss (por baixo do botão de pausa)
        if (boss != null && boss.isActive() && !boss.isDead()) {
            float bw = 300f;
            float bh = 16f;
            float bx = VIEWPORT_WIDTH / 2f - bw / 2f;
            float by = WORLD_HEIGHT - 110f;
            shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
            shapeRenderer.rect(bx - 3f, by - 3f, bw + 6f, bh + 6f);
            shapeRenderer.setColor(0.85f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(bx, by, bw * boss.getHpFraction(), bh);
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

        // Nível à direita do botão de pausa (antes tapava-o), nome do jogo no canto, pontuação à esquerda
        hudLayout.setText(font, "RUNNEXT");
        drawHudText("NIVEL " + level, 445f, WORLD_HEIGHT - 16f, Color.YELLOW);
        drawHudText("RUNNEXT", VIEWPORT_WIDTH - 20f - hudLayout.width, WORLD_HEIGHT - 16f, Color.WHITE);
        drawHudText("SCORE: " + totalScore(), 20f, WORLD_HEIGHT - 62f, Color.WHITE);
        if (boss != null && boss.isActive() && !boss.isDead()) {
            hudLayout.setText(font, "BOSS");
            drawHudText("BOSS", VIEWPORT_WIDTH / 2f - hudLayout.width / 2f, WORLD_HEIGHT - 70f, Color.WHITE);
        }

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
        MenuStyle.drawHeader(batch, menuFont);
        MenuStyle.drawTitle(batch, menuFont, titulo, MenuStyle.INGAME_TITLE_Y);
        for (int i = 0; i < botoes.length; i++) {
            MenuStyle.drawButtonLabel(batch, menuFont, textos[i], botoes[i], true);
        }
        batch.end();
    }

    // Texto do HUD com sombra preta (legível sobre qualquer fundo)
    private void drawHudText(String text, float x, float y, Color color) {
        font.setColor(Color.BLACK);
        font.draw(batch, text, x + 2f, y - 2f);
        font.setColor(color);
        font.draw(batch, text, x, y);
    }

    // Resultados no fim de cada nível: números à esquerda, botões à direita
    private void drawLevelResults() {
        MenuStyle.clear();

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Rectangle b : levelCompleteButtons) {
            MenuStyle.drawButton(shapeRenderer, b, true);
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        MenuStyle.drawHeader(batch, menuFont);
        MenuStyle.drawTitle(batch, menuFont, "NÍVEL COMPLETO!", MenuStyle.INGAME_TITLE_Y);

        float x = 50f;
        float y = 335f;
        int linhas = 5 + (enemies.size > 0 ? 1 : 0) + (boss != null ? 1 : 0);
        float step = (linhas > 6) ? 36f : 42f;
        MenuStyle.drawLeft(batch, menuFont, "Moedas: " + coinsCollected + " / " + coins.size, x, y, Color.WHITE);
        y -= step;
        if (enemies.size > 0) {
            MenuStyle.drawLeft(batch, menuFont, "Inimigos: " + enemiesDefeated + " / " + enemies.size, x, y, Color.WHITE);
            y -= step;
        }
        if (boss != null) {
            MenuStyle.drawLeft(batch, menuFont, "Boss: derrotado", x, y, MenuStyle.GOLD);
            y -= step;
        }
        MenuStyle.drawLeft(batch, menuFont, "Vidas: " + lives, x, y, Color.WHITE);
        y -= step;
        MenuStyle.drawLeft(batch, menuFont, "Pontos: " + levelScore, x, y, Color.WHITE);
        y -= step;
        MenuStyle.drawLeft(batch, menuFont, "TOTAL: " + totalScore(), x, y, MenuStyle.GOLD);
        y -= step;
        if (newRecord) {
            MenuStyle.drawLeft(batch, menuFont, "NOVO RECORDE!", x, y, MenuStyle.GOLD);
        } else {
            MenuStyle.drawLeft(batch, menuFont, "Recorde: " + bestScore, x, y, Color.WHITE);
        }

        for (int i = 0; i < levelCompleteButtons.length; i++) {
            MenuStyle.drawButtonLabel(batch, menuFont, levelCompleteLabels[i], levelCompleteButtons[i], true);
        }
        batch.end();
    }

    private void drawGameOver() {
        MenuStyle.clear();

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Rectangle b : gameOverButtons) {
            MenuStyle.drawButton(shapeRenderer, b, true);
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        MenuStyle.drawHeader(batch, menuFont);
        MenuStyle.drawTitle(batch, menuFont, "GAME OVER", MenuStyle.INGAME_TITLE_Y);
        MenuStyle.drawLine(batch, menuFont, "SCORE: " + totalScore(), 340f, MenuStyle.GOLD);
        for (int i = 0; i < gameOverButtons.length; i++) {
            MenuStyle.drawButtonLabel(batch, menuFont, gameOverLabels[i], gameOverButtons[i], true);
        }
        batch.end();
    }

    private void drawVictory() {
        MenuStyle.clear();

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Rectangle b : victoryButtons) {
            MenuStyle.drawButton(shapeRenderer, b, true);
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        MenuStyle.drawHeader(batch, menuFont);
        MenuStyle.drawTitle(batch, menuFont, "PARABÉNS!", MenuStyle.INGAME_TITLE_Y);
        MenuStyle.drawLine(batch, menuFont, "JOGO CONCLUÍDO", 345f, Color.WHITE);
        MenuStyle.drawLine(batch, menuFont, "SCORE: " + totalScore(), 300f, MenuStyle.GOLD);
        for (int i = 0; i < victoryButtons.length; i++) {
            MenuStyle.drawButtonLabel(batch, menuFont, victoryLabels[i], victoryButtons[i], true);
        }
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
        jumpSound.dispose();
        coinSound.dispose();
        hurtSound.dispose();
        deathSound.dispose();
        player.dispose();
        backgroundTexture.dispose();
        coinTexture.dispose();
        for (Texture t : enemyTextures) {
            t.dispose();
        }
        if (bossTexture != null) bossTexture.dispose();
    }
}
