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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sidney.runnext.entities.Coin;
import com.sidney.runnext.entities.Player;

// GameScreen representa o ecrã principal do jogo (onde a jogabilidade acontece).
public class GameScreen implements Screen, InputProcessor {

    private final Game game; // Referência à aplicação principal

    // Dimensões fixas do "mundo" do jogo
    private static final float WORLD_WIDTH = 800;
    private static final float WORLD_HEIGHT = 480;

    // Imagens de fundo, uma por nível (1 = Floresta, 2 = Montanha, 3 = Castelo).
    // Os ficheiros estão na pasta assets/backgrounds.
    private static final String[] BACKGROUND_FILES = {
        "backgrounds/bg_nivel1_floresta.png",
        "backgrounds/bg_nivel2_montanha.png",
        "backgrounds/bg_nivel3_castelo.png"
    };

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont fontGrande;

    private final Player player; // Entidade jogável
    private final Rectangle ground; // Retângulo do "chão"

    // Fundo do nível atual
    private final int level;                  // Número do nível (1 a 3)
    private final Texture backgroundTexture;  // Imagem carregada do ficheiro
    private final TextureRegion background;   // Parte da imagem que é desenhada (sem deformar)

    // Moedas
    private final Texture coinTexture;                    // Folha com os 4 frames da moeda a rodar
    private final Animation<TextureRegion> coinAnimation; // Animação da moeda
    private final Array<Coin> coins = new Array<>();      // Moedas do nível
    private int coinsCollected = 0;                       // Quantas moedas o jogador já apanhou
    private float stateTime = 0f;                         // Tempo acumulado, para animar as moedas

    // Botões de controlo
    private final Rectangle btnLeft;
    private final Rectangle btnRight;
    private final Rectangle btnJump;
    private boolean touchLeft = false;
    private boolean touchRight = false;
    private boolean touchJump = false;

    // Botão de Pausa
    private final Rectangle btnPause;
    private boolean isPaused = false;

    // Botões do Menu de Pausa
    private final Rectangle btnResume;
    private final Rectangle btnRestart;
    private final Rectangle btnMainMenu;

    // Construtor usado pelo MenuScreen: começa sempre no nível 1.
    public GameScreen(Game game) {
        this(game, 1);
    }

    // Construtor com o nível escolhido (1 = Floresta, 2 = Montanha, 3 = Castelo).
    public GameScreen(Game game, int level) {
        this.game = game;
        this.level = Math.max(1, Math.min(level, BACKGROUND_FILES.length));

        camera = new OrthographicCamera();
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.BLACK);

        fontGrande = new BitmapFont();
        fontGrande.getData().setScale(3f);
        fontGrande.setColor(Color.WHITE);

        // Carregar o fundo do nível. A imagem é 16:9 (1280x720) e o mundo é 800x480 (5:3),
        // por isso cortamos 40 pixels de cada lado da imagem para ela encaixar sem esticar.
        backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_FILES[this.level - 1]));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        background = new TextureRegion(backgroundTexture, 40, 0, 1200, 720);

        // Carregar a animação da moeda: a folha tem 4 frames de 16x16 pixels lado a lado.
        // Nearest mantém o pixel art nítido ao ampliar.
        coinTexture = new Texture(Gdx.files.internal("sprites/coin.png"));
        coinTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] coinFrames = TextureRegion.split(coinTexture, 16, 16);
        coinAnimation = new Animation<>(0.12f, coinFrames[0]);

        // Chão: uma faixa no fundo do ecrã com altura de 45px
        ground = new Rectangle(0, 0, WORLD_WIDTH, 45);

        // Jogador criado exatamente no topo do chão
        player = new Player(100, ground.y + ground.height);

        // Colocar as moedas no nível
        createCoins();

        // Botões ESQUERDA, DIREITA e SALTAR
        btnLeft = new Rectangle(20, 15, 90, 80);
        btnRight = new Rectangle(125, 15, 90, 80);
        btnJump = new Rectangle(WORLD_WIDTH - 170, 15, 150, 80);

        // Botão de Pausa
        btnPause = new Rectangle(375, WORLD_HEIGHT - 60, 50, 40);

        // Botões do Menu de Pausa
        float centerX = WORLD_WIDTH / 2f - 100;
        btnResume = new Rectangle(centerX, 300, 200, 50);
        btnRestart = new Rectangle(centerX, 230, 200, 50);
        btnMainMenu = new Rectangle(centerX, 160, 200, 50);
    }

    // Cria as moedas do nível. Para mudar a posição, altere os números aqui.
    // (x = posição horizontal, y = altura; o topo do chão está em y = 45)
    private void createCoins() {
        coins.clear();
        coinsCollected = 0;

        float groundTop = ground.y + ground.height;

        // A personagem tem 150 de altura e o salto sobe cerca de 125 (JUMP_SPEED 500, GRAVITY -1000),
        // por isso as moedas "no ar" têm de ficar acima da cabeça dela (a partir de ~150 acima do chão).

        // Grupo 1: fila de 5 moedas ao nível do corpo (basta andar para as apanhar)
        for (int i = 0; i < 5; i++) {
            coins.add(new Coin(250 + i * 55, groundTop + 30));
        }

        // Grupo 2: arco de 5 moedas no ar (é preciso saltar para as apanhar)
        float[] arcY = {groundTop + 150, groundTop + 185, groundTop + 205, groundTop + 185, groundTop + 150};
        for (int i = 0; i < 5; i++) {
            coins.add(new Coin(530 + i * 55, arcY[i]));
        }
    }

    // Área do corpo do jogador usada para apanhar moedas. A imagem da personagem (100x150)
    // tem margens transparentes, por isso usamos um retângulo mais justo ao corpo.
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
        // 1) Ler o estado atual do toque/teclado
        updateTouchInput();

        // 2) Atualizar lógica do jogo (se não estiver pausado)
        if (!isPaused) {
            player.update(delta);
            stateTime += delta; // faz avançar a animação das moedas

            // Limites laterais do ecrã
            if (player.getX() < 0) {
                player.setX(0);
            } else if (player.getX() + Player.WIDTH > WORLD_WIDTH) {
                player.setX(WORLD_WIDTH - Player.WIDTH);
            }

            // Colisão com o chão: ajusta a posição para assentar exatamente na relva
            Rectangle bounds = player.getBounds();
            if (bounds.y <= ground.y + ground.height && player.getVelocityY() <= 0) {
                player.landOn(ground.y + ground.height);
            }

            // Colisão jogador x moedas: se tocar numa moeda, ela desaparece e conta +1.
            Rectangle playerBody = getPlayerBody();
            for (Coin coin : coins) {
                if (!coin.isCollected() && playerBody.overlaps(coin.getBounds())) {
                    coin.collect();
                    coinsCollected++;
                    Gdx.app.log("GameScreen", "Moeda apanhada! Total: " + coinsCollected);
                }
            }
        }

        // 3) Limpar o ecrã (as barras pretas do FitViewport aparecem com esta cor)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // 4) Desenhar o fundo do nível (800x480) e, por cima dele, as moedas
        batch.begin();
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        for (Coin coin : coins) {
            TextureRegion frame = coinAnimation.getKeyFrame(stateTime + coin.getAnimationOffset(), true);
            coin.render(batch, frame);
        }
        batch.end();

        // 5) Desenhar o chão de relva com ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // --- CHÃO DE RELVA EM CAMADAS (Efeito Relevado Pixel Art) ---
        // Camada 1: Base de Terra (Castanho)
        shapeRenderer.setColor(0.25f, 0.15f, 0.1f, 1f);
        shapeRenderer.rect(0, 0, WORLD_WIDTH, 20);

        // Camada 2: Sub-relva (Verde Escuro)
        shapeRenderer.setColor(0.15f, 0.45f, 0.15f, 1f);
        shapeRenderer.rect(0, 20, WORLD_WIDTH, 15);

        // Camada 3: Topo da Relva (Verde Claro Vivo)
        shapeRenderer.setColor(0.35f, 0.75f, 0.25f, 1f);
        shapeRenderer.rect(0, 35, WORLD_WIDTH, 10);

        // Detalhes de Tufos de Relva no topo
        shapeRenderer.setColor(0.45f, 0.85f, 0.3f, 1f);
        for (float x = 0; x < WORLD_WIDTH; x += 16) {
            shapeRenderer.rect(x, 43, 8, 5);
        }

        shapeRenderer.end();

        // 6) Desenhar o Jogador usando SpriteBatch
        batch.begin();
        player.render(batch);
        batch.end();

        // 7) Desenhar a interface de botões (UI) e o contador de moedas
        drawUI();

        // 8) Desenhar o menu de pausa se estiver ativo
        if (isPaused) {
            drawPauseMenu();
        }
    }

    private void updateTouchInput() {
        touchLeft = false;
        touchRight = false;
        touchJump = false;

        for (int i = 0; i < 3; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector3 touchPos = new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0);
                viewport.unproject(touchPos);

                float touchX = touchPos.x;
                float touchY = touchPos.y;

                if (isPaused) {
                    if (btnResume.contains(touchX, touchY) && i == 0) {
                        isPaused = false;
                        Gdx.app.log("GameScreen", "Continuar");
                    } else if (btnRestart.contains(touchX, touchY) && i == 0) {
                        player.setX(100);
                        player.setY(ground.y + ground.height);
                        createCoins(); // as moedas voltam a aparecer e o contador volta a zero
                        isPaused = false;
                        Gdx.app.log("GameScreen", "Reiniciar");
                    } else if (btnMainMenu.contains(touchX, touchY) && i == 0) {
                        game.setScreen(new MenuScreen(game));
                        dispose();
                        Gdx.app.log("GameScreen", "Menu Principal");
                    }
                } else {
                    if (btnLeft.contains(touchX, touchY)) touchLeft = true;
                    if (btnRight.contains(touchX, touchY)) touchRight = true;
                    if (btnJump.contains(touchX, touchY)) touchJump = true;
                    if (btnPause.contains(touchX, touchY) && i == 0) {
                        isPaused = true;
                        Gdx.app.log("GameScreen", "Pausa ativada");
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
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(btnLeft.x, btnLeft.y, btnLeft.width, btnLeft.height);
        shapeRenderer.rect(btnRight.x, btnRight.y, btnRight.width, btnRight.height);
        shapeRenderer.rect(btnJump.x, btnJump.y, btnJump.width, btnJump.height);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(btnPause.x, btnPause.y, btnPause.width, btnPause.height);

        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.BLACK);
        font.draw(batch, "<", btnLeft.x + 35, btnLeft.y + 50);
        font.draw(batch, ">", btnRight.x + 35, btnRight.y + 50);
        font.draw(batch, "SALTAR", btnJump.x + 25, btnJump.y + 50);

        font.setColor(Color.WHITE);
        font.draw(batch, "||", btnPause.x + 10, btnPause.y + 28);

        // Contador de moedas (canto superior esquerdo): ícone da moeda + "apanhadas / total".
        // O ícone usa a mesma animação das moedas do nível.
        TextureRegion coinIcon = coinAnimation.getKeyFrame(stateTime, true);
        batch.draw(coinIcon, 20, WORLD_HEIGHT - 50, 32, 32);

        String coinText = "x " + coinsCollected + " / " + coins.size;
        font.setColor(Color.BLACK); // sombra preta, para o texto ler-se bem em qualquer fundo
        font.draw(batch, coinText, 62, WORLD_HEIGHT - 22);
        font.setColor(Color.WHITE);
        font.draw(batch, coinText, 60, WORLD_HEIGHT - 20);
        batch.end();
    }

    private void drawPauseMenu() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(btnResume.x, btnResume.y, btnResume.width, btnResume.height);
        shapeRenderer.rect(btnRestart.x, btnRestart.y, btnRestart.width, btnRestart.height);
        shapeRenderer.rect(btnMainMenu.x, btnMainMenu.y, btnMainMenu.width, btnMainMenu.height);
        shapeRenderer.end();

        batch.begin();
        fontGrande.setColor(Color.WHITE);
        fontGrande.draw(batch, "PAUSADO", WORLD_WIDTH / 2f - 80, 400);

        font.setColor(Color.WHITE);
        font.draw(batch, "CONTINUAR", btnResume.x + 40, btnResume.y + 35);
        font.draw(batch, "REINICIAR", btnRestart.x + 45, btnRestart.y + 35);
        font.draw(batch, "MENU", btnMainMenu.x + 65, btnMainMenu.y + 35);
        batch.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        viewport.unproject(touchPos);
        Gdx.app.log("GameScreen", "Touch em: " + touchPos.x + ", " + touchPos.y);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }

    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }

    @Override
    public boolean keyDown(int keycode) { return false; }

    @Override
    public boolean keyUp(int keycode) { return false; }

    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }

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
        player.dispose();
        backgroundTexture.dispose();
        coinTexture.dispose();
    }
}
