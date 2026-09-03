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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

// Ecrã de menu do jogo. Funciona como um "assistente" de 2 passos:
// 1) Menu principal (botão JOGAR) -> 2) Seleção de nível (botão NIVEL 1) -> inicia o GameScreen.
public class MenuScreen implements Screen {

    private final Game game; // Referência à aplicação, usada para trocar para o GameScreen

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;         // Desenha texto (títulos e botões)
    private final ShapeRenderer shapeRenderer; // Desenha os retângulos de fundo dos botões
    private final BitmapFont fontGrande; // Fonte do título "RUNNEXT"
    private final BitmapFont fontMedia;  // Fonte dos textos dos botões

    // Estado do menu: 0 = Menu principal, 1 = Seleção de nível
    // (controla qual ecrã "virtual" é desenhado e o que acontece ao tocar/premir ENTER)
    private int menuState = 0;

    public MenuScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        // Mundo lógico de 800x480, igual ao do GameScreen, para manter consistência visual
        viewport = new FitViewport(800, 480, camera);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Fonte grande para o título (aumentada 4x)
        fontGrande = new BitmapFont();
        fontGrande.getData().setScale(4f);
        fontGrande.setColor(Color.WHITE);

        // Fonte média para os textos dos botões (aumentada 2.5x)
        fontMedia = new BitmapFont();
        fontMedia.getData().setScale(2.5f);
        fontMedia.setColor(Color.WHITE);
    }

    @Override
    public void show() {
        // Nada a inicializar ao mostrar este ecrã (ao contrário do GameScreen,
        // este menu não regista um InputProcessor — lê o input diretamente em render()).
    }

    @Override
    public void render(float delta) {
        // Limpa o ecrã com um fundo escuro
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Coordenada X central do ecrã (mundo lógico de largura 800)
        float centroX = 400; // metade de 800

        // Desenha o ecrã correspondente ao estado atual do menu
        if (menuState == 0) {
            // TELA 1: Menu principal
            renderMenuPrincipal(centroX);
        } else if (menuState == 1) {
            // TELA 2: Seleção de nível
            renderSelecaoNivel(centroX);
        }

        // Avança o menu se o utilizador premir ENTER ou tocar em qualquer parte do ecrã
        // (nota: não há verificação de qual botão foi tocado — ver observação no fim)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.justTouched()) {
            handleInput();
        }
    }

    private void renderMenuPrincipal(float centroX) {
        // Retângulo azul atrás do texto "JOGAR" (para parecer um botão clicável)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(centroX - 100, 170, 200, 50);
        shapeRenderer.end();

        batch.begin();
        // Título: RUNNEXT
        fontGrande.setColor(Color.WHITE);
        fontGrande.draw(batch, "RUNNEXT", centroX - 140, 350);

        // Texto: JOGAR (desenhado por cima do retângulo azul)
        fontMedia.setColor(Color.WHITE);
        fontMedia.draw(batch, "JOGAR", centroX - 60, 200);
        batch.end();
    }

    private void renderSelecaoNivel(float centroX) {
        // Retângulo azul atrás do texto "NIVEL 1" (ligeiramente mais largo, pois o texto é maior)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(centroX - 120, 170, 240, 50);
        shapeRenderer.end();

        batch.begin();
        // Título: RUNNEXT (repetido, mantém consistência visual entre os dois ecrãs do menu)
        fontGrande.setColor(Color.WHITE);
        fontGrande.draw(batch, "RUNNEXT", centroX - 140, 350);

        // Texto: NIVEL 1 (desenhado por cima do retângulo azul)
        fontMedia.setColor(Color.WHITE);
        fontMedia.draw(batch, "NIVEL 1", centroX - 70, 200);
        batch.end();
    }

    // Avança o "fluxo" do menu em resposta a um toque/ENTER.
    // Não verifica coordenadas — qualquer toque no ecrã ou tecla ENTER conta como confirmação.
    private void handleInput() {
        if (menuState == 0) {
            // Está no menu principal → avança para a seleção de nível
            menuState = 1;
        } else if (menuState == 1) {
            // Está na seleção de nível → inicia o jogo, passando 'game' para o GameScreen
            // (assim o GameScreen também consegue trocar de ecrã mais tarde, ex: voltar ao menu)
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        // Ajusta o viewport quando o ecrã/janela muda de tamanho
        viewport.update(width, height, true);
    }

    @Override public void pause() {}   // Não há nada para pausar num ecrã de menu estático
    @Override public void resume() {}  // Nada a retomar
    @Override public void hide() {}    // Nada a limpar ao trocar de ecrã (feito em dispose())

    @Override
    public void dispose() {
        // Liberta a memória nativa usada pelos recursos gráficos deste ecrã
        batch.dispose();
        shapeRenderer.dispose();
        fontGrande.dispose();
        fontMedia.dispose();
    }
}
