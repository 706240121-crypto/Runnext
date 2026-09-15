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

// GameScreen representa o ecrã principal do jogo (onde a jogabilidade acontece).
// Implementa "Screen" (ciclo de vida do LibGDX: show/render/resize/pause/resume/hide/dispose)
// e "InputProcessor" (para tratar toques/teclas diretamente, em vez de fazer apenas polling).
public class GameScreen implements Screen, InputProcessor {

    private final Game game; // Referência à aplicação principal, usada para trocar de ecrã (ex: voltar ao menu)

    // Dimensões fixas do "mundo" do jogo (unidades lógicas, não pixels reais do ecrã).
    // O Viewport depois escala isto para caber em qualquer resolução real do dispositivo.
    private static final float WORLD_WIDTH = 800;
    private static final float WORLD_HEIGHT = 480;

    private final OrthographicCamera camera; // Câmara 2D usada para projetar o mundo no ecrã
    private final Viewport viewport;         // Garante que o jogo mantém a proporção em ecrãs diferentes
    private final ShapeRenderer shapeRenderer; // Desenha formas geométricas simples (retângulos, etc.)
    private final SpriteBatch batch;           // Desenha texto/imagens (usado aqui para as fontes)
    private final BitmapFont font;             // Fonte pequena (textos normais, ícones de botões)
    private final BitmapFont fontGrande;       // Fonte grande (título "PAUSADO")

    private final Player player; // Entidade jogável (definida na classe Player)
    private final Rectangle ground; // Retângulo que representa o "chão" (para colisão)

    // Detalhes do Fundo (Estrelas e Nuvens)
    private final float[] starX = new float[40];
    private final float[] starY = new float[40];
    private final float[] starSize = new float[40];

    private final float[] cloudX = new float[5];
    private final float[] cloudY = new float[5];
    private final float[] cloudWidth = new float[5];
    private final float[] cloudHeight = new float[5];
    private final float[] cloudSpeed = new float[5];

    // Botões de controlo (ESQUERDA, DIREITA e SALTAR)
    private final Rectangle btnLeft;
    private final Rectangle btnRight;
    private final Rectangle btnJump;
    private boolean touchLeft = false;
    private boolean touchRight = false;
    private boolean touchJump = false;

    // Botão de Pausa (centralizado no topo)
    private final Rectangle btnPause;

    // Estado do Jogo: true = jogo pausado (congela a lógica, mostra o menu de pausa)
    private boolean isPaused = false;

    // Botões do Menu de Pausa
    private final Rectangle btnResume;
    private final Rectangle btnRestart;
    private final Rectangle btnMainMenu;

    public GameScreen(Game game) {
        this.game = game;

        // Câmara centrada no meio do "mundo" lógico (400, 240)
        camera = new OrthographicCamera();
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        // FitViewport: mantém a proporção (aspect ratio) do jogo, adicionando barras
        // pretas nas laterais/topo se o ecrã real tiver proporção diferente.
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        // Fonte normal: aumentada 2x e pintada de preto (usada nos botões esquerda/direita)
        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.BLACK);

        // Fonte grande: aumentada 3x e branca (usada no título "PAUSADO")
        fontGrande = new BitmapFont();
        fontGrande.getData().setScale(3f);
        fontGrande.setColor(Color.WHITE);

        // Inicializar estrelas aleatórias
        for (int i = 0; i < 40; i++) {
            starX[i] = (float) Math.random() * WORLD_WIDTH;
            starY[i] = (float) Math.random() * (WORLD_HEIGHT - 100) + 100;
            starSize[i] = (float) Math.random() * 2 + 1;
        }

        // Inicializar nuvens aleatórias
        for (int i = 0; i < 5; i++) {
            cloudX[i] = (float) Math.random() * WORLD_WIDTH;
            cloudY[i] = (float) Math.random() * 150 + 250;
            cloudWidth[i] = (float) Math.random() * 60 + 60;
            cloudHeight[i] = (float) Math.random() * 20 + 20;
            cloudSpeed[i] = (float) Math.random() * 15 + 5;
        }

        // Chão: uma faixa retangular no fundo do ecrã, com 40 de altura
        ground = new Rectangle(0, 0, WORLD_WIDTH, 40);

        // Jogador criado logo acima do chão, começando em X=100
        player = new Player(100, ground.y + ground.height + 10);

        // Botões ESQUERDA e DIREITA (lado a lado, canto inferior esquerdo)
        btnLeft = new Rectangle(30, 15, 60, 50);
        btnRight = new Rectangle(100, 15, 60, 50);

        // Botão SALTAR (canto inferior direito)
        btnJump = new Rectangle(WORLD_WIDTH - 140, 15, 110, 50);

        // Botão de Pausa centralizado no topo.
        // Cálculo: (WORLD_WIDTH / 2) - (largura do botão / 2) = 400 - 25 = 375
        btnPause = new Rectangle(375, WORLD_HEIGHT - 60, 50, 40);

        // Botões do Menu de Pausa, todos centralizados horizontalmente (mesma centerX),
        // empilhados verticalmente com espaçamento de 70 entre eles.
        float centerX = WORLD_WIDTH / 2f - 100;
        btnResume = new Rectangle(centerX, 300, 200, 50);
        btnRestart = new Rectangle(centerX, 230, 200, 50);
        btnMainMenu = new Rectangle(centerX, 160, 200, 50);
    }

    @Override
    public void show() {
        // Chamado quando este ecrã se torna o ecrã ativo.
        Gdx.app.log("GameScreen", "=== TELA INICIADA ===");
        // Regista esta classe como o processador de input (ativa touchDown, keyDown, etc.)
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        // delta = tempo (em segundos) desde o frame anterior — usado para animações
        // consistentes independentemente da taxa de frames do dispositivo.

        // 1) Ler o estado atual do toque/teclado
        updateTouchInput();

        // 2) Atualizar lógica do jogo (só corre se o jogo NÃO estiver pausado)
        if (!isPaused) {
            player.update(delta);

            // Atualizar movimento das nuvens
            for (int i = 0; i < 5; i++) {
                cloudX[i] -= cloudSpeed[i] * delta;
                if (cloudX[i] + cloudWidth[i] < 0) {
                    cloudX[i] = WORLD_WIDTH;
                    cloudY[i] = (float) Math.random() * 150 + 250;
                }
            }

            // Impede o jogador de sair pelos limites laterais do ecrã
            if (player.getX() < 0) {
                player.setX(0);
            } else if (player.getX() + Player.WIDTH > WORLD_WIDTH) {
                player.setX(WORLD_WIDTH - Player.WIDTH);
            }

            // Colisão simples com o chão: se o jogador está a cair (velocidade Y <= 0)
            // e chegou à altura do chão, "aterra" nele.
            Rectangle bounds = player.getBounds();
            if (bounds.y <= ground.y + ground.height && player.getVelocityY() <= 0) {
                player.landOn(ground.y + ground.height);
            }
        }

        // 3) Limpar o ecrã com uma cor de fundo azul-acinzentado escuro
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Atualiza a câmara e aplica a matriz de projeção aos renderizadores,
        // para que tudo seja desenhado na escala/posição corretas do "mundo" lógico.
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // 4) Desenhar o cenário (estrelas, nuvens, chão) e o jogador
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Estrelas
        shapeRenderer.setColor(Color.WHITE);
        for (int i = 0; i < 40; i++) {
            shapeRenderer.rect(starX[i], starY[i], starSize[i], starSize[i]);
        }

        // Nuvens (cinza azulado suave)
        shapeRenderer.setColor(0.5f, 0.5f, 0.6f, 1f);
        for (int i = 0; i < 5; i++) {
            shapeRenderer.rect(cloudX[i], cloudY[i], cloudWidth[i], cloudHeight[i]);
            shapeRenderer.rect(cloudX[i] + cloudWidth[i] * 0.2f, cloudY[i] + cloudHeight[i] * 0.8f, cloudWidth[i] * 0.6f, cloudHeight[i] * 0.4f);
        }

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(ground.x, ground.y, ground.width, ground.height);
        player.render(shapeRenderer); // O próprio Player sabe desenhar-se a si mesmo
        shapeRenderer.end();

        // 5) Desenhar a interface (botões esquerda/direita/pausa) — sempre visível
        drawUI();

        // 6) Se estiver pausado, desenhar o menu de pausa por cima de tudo
        if (isPaused) {
            drawPauseMenu();
        }
    }

    private void updateTouchInput() {
        // Reinicia os estados de movimento a cada frame
        touchLeft = false;
        touchRight = false;
        touchJump = false;

        // Verifica multi-touch (até 3 dedos)
        for (int i = 0; i < 3; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector3 touchPos = new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0);
                camera.unproject(touchPos);

                float touchX = touchPos.x;
                float touchY = touchPos.y;

                if (isPaused) {
                    if (btnResume.contains(touchX, touchY) && i == 0) {
                        isPaused = false;
                        Gdx.app.log("GameScreen", "Continuar");
                    } else if (btnRestart.contains(touchX, touchY) && i == 0) {
                        player.setX(100);
                        player.setY(ground.y + ground.height + 10);
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

        // Aplica o movimento ao jogador
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

        // Botões Esquerda, Direita e Saltar (Brancos)
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(btnLeft.x, btnLeft.y, btnLeft.width, btnLeft.height);
        shapeRenderer.rect(btnRight.x, btnRight.y, btnRight.width, btnRight.height);
        shapeRenderer.rect(btnJump.x, btnJump.y, btnJump.width, btnJump.height);

        // Botão de Pausa (Cinza Escuro)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(btnPause.x, btnPause.y, btnPause.width, btnPause.height);

        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.BLACK);
        font.draw(batch, "<", btnLeft.x + 25, btnLeft.y + 35);
        font.draw(batch, ">", btnRight.x + 25, btnRight.y + 35);
        font.draw(batch, "SALTAR", btnJump.x + 10, btnJump.y + 35);

        font.setColor(Color.WHITE);
        font.draw(batch, "||", btnPause.x + 10, btnPause.y + 28);
        batch.end();
    }

    private void drawPauseMenu() {
        // Fundo escuro semi-transparente cobrindo
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f); // RGBA: preto com 70% de opacidade
        shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // Botões do Menu de Pausa (Continuar / Reiniciar / Menu Principal)
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
    // (a maioria não é usada aqui, pois o input é lido via polling
    // em updateTouchInput(); mas a interface exige implementá-los todos)


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Apenas regista no log a posição do toque (em coordenadas do mundo), para depuração.
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPos);
        Gdx.app.log("GameScreen", "Touch em: " + touchPos.x + ", " + touchPos.y);
        return true; // indica que o evento foi tratado
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false; // não usado
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false; // não usado
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false; // não usado
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false; // não usado
    }

    @Override
    public boolean keyDown(int keycode) {
        return false; // não usado (teclado é lido via polling em updateTouchInput)
    }

    @Override
    public boolean keyUp(int keycode) {
        return false; // não usado
    }

    @Override
    public boolean keyTyped(char character) {
        return false; // não usado
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false; // não usado
    }


    @Override
    public void resize(int width, int height) {
        // Chamado quando a janela/ecrã muda de tamanho (ex: rotação do dispositivo).
        // "true" recentra a câmara no viewport atualizado.
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        // Chamado pelo sistema operativo quando a app vai para segundo plano
        // (ex: utilizador recebe uma chamada). Pausa o jogo automaticamente.
        isPaused = true;
    }

    @Override
    public void resume() {
        // Chamado quando a app volta ao primeiro plano. Vazio de propósito:
        // o jogo fica pausado até o jogador clicar em "Continuar" manualmente.
    }

    @Override
    public void hide() {
        // Chamado quando este deixa de ser o ecrã ativo (trocou para outro Screen).
        // Vazio: a limpeza de recursos é feita explicitamente em dispose().
    }

    @Override
    public void dispose() {
        // Liberta a memória nativa (fora do heap Java) usada pelos recursos gráficos.
        // Essencial em LibGDX: estes objetos não são recolhidos pelo Garbage Collector normal.
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        fontGrande.dispose();
    }
}
