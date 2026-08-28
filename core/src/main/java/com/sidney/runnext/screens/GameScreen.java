package com.sidney.runnext.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

    private final Player player;
    private final Rectangle ground;

    public GameScreen() {
        camera = new OrthographicCamera();

        // CORREÇÃO 1: Centralizar a câmera no meio do mundo.
        // Sem isso, a câmera fica no canto (0,0) e o jogo parece quebrado.
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();

        ground = new Rectangle(0, 0, WORLD_WIDTH, 40);

        // CORREÇÃO 2: Iniciar o jogador um pouco acima do chão para evitar
        // que a gravidade puxe ele para baixo no primeiro milissegundo.
        player = new Player(100, ground.y + ground.height + 10);
    }

    @Override
    public void show() {
        // Log para sabermos se a tela carregou
        Gdx.app.log("GameScreen", "=== TELA INICIADA COM SUCESSO ===");
    }

    @Override
    public void render(float delta) {
        // 1. Atualiza a lógica do jogador
        player.update(delta);

        // 2. Colisão com o chão (usando ground.y + ground.height para ser preciso)
        Rectangle bounds = player.getBounds();
        if (bounds.y <= ground.y + ground.height && player.getVelocityY() <= 0) {
            player.landOn(ground.y + ground.height);
        }

        // 3. Limpa a tela
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 4. Atualiza a câmera e aplica ao renderizador
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 5. Desenha os elementos
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // CORREÇÃO 3: "Color.FOREST" NÃO EXISTE no libGDX padrão!
        // Isso causava erro ou tela preta. Mudei para Color.GREEN.
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(ground.x, ground.y, ground.width, ground.height);

        player.render(shapeRenderer);

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        // CORREÇÃO 4: O 'true' no final recentra a câmera automaticamente
        // em celulares com tamanhos de tela diferentes.
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
