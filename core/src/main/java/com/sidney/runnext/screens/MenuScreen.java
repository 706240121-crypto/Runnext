package com.sidney.runnext.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sidney.runnext.Progress;

public class MenuScreen implements Screen {

    private final Game game;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;

    private int menuState = 0; // 0 = Menu principal, 1 = Seleção de Níveis

    private final int maxUnlocked;
    private final Rectangle btnJogar;
    private final Rectangle[] btnNivel = new Rectangle[Progress.TOTAL_LEVELS];

    public MenuScreen(Game game) {
        this(game, 0);
    }

    public MenuScreen(Game game, int menuState) {
        this.game = game;
        this.menuState = menuState;

        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        maxUnlocked = Progress.getMaxUnlocked();

        btnJogar = MenuStyle.button(1);
        for (int i = 0; i < btnNivel.length; i++) {
            btnNivel[i] = MenuStyle.button(i);
        }
    }

    @Override
    public void show() {
        // Garante que nenhum ecrã antigo (já destruído) continua a receber toques
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        MenuStyle.clear();

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        if (menuState == 0) {
            renderMenuPrincipal();
        } else {
            renderSelecaoNivel();
        }

        // Tem de ser a ÚLTIMA coisa do render: pode trocar de ecrã e destruir este.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (menuState == 0) menuState = 1;
            else iniciarNivel(1);
        } else if (Gdx.input.justTouched()) {
            handleTouchInput();
        }
    }

    private void renderMenuPrincipal() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        MenuStyle.drawButton(shapeRenderer, btnJogar, true);
        shapeRenderer.end();

        batch.begin();
        MenuStyle.drawTitle(batch, font, "RUNNEXT", MenuStyle.TITLE_Y);
        MenuStyle.drawButtonLabel(batch, font, "JOGAR", btnJogar, true);
        batch.end();
    }

    private void renderSelecaoNivel() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < btnNivel.length; i++) {
            boolean livre = (i + 1) <= maxUnlocked;
            MenuStyle.drawButton(shapeRenderer, btnNivel[i], livre);
            if (!livre) MenuStyle.drawLock(shapeRenderer, btnNivel[i]);
        }
        shapeRenderer.end();

        batch.begin();
        MenuStyle.drawTitle(batch, font, "RUNNEXT", MenuStyle.TITLE_Y);
        for (int i = 0; i < btnNivel.length; i++) {
            boolean livre = (i + 1) <= maxUnlocked;
            MenuStyle.drawButtonLabel(batch, font, "NIVEL " + (i + 1), btnNivel[i], livre);
        }
        batch.end();
    }

    private void handleTouchInput() {
        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPos);
        float x = touchPos.x;
        float y = touchPos.y;

        if (menuState == 0) {
            if (btnJogar.contains(x, y)) {
                menuState = 1;
            }
        } else {
            for (int i = 0; i < btnNivel.length; i++) {
                boolean livre = (i + 1) <= maxUnlocked;
                if (livre && btnNivel[i].contains(x, y)) {
                    iniciarNivel(i + 1);
                    return;
                }
            }
        }
    }

    private void iniciarNivel(int nivel) {
        game.setScreen(new GameScreen(game, nivel));
        dispose();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
