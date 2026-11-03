package com.sidney.runnext.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * MenuStyle.java
 *
 * Define o visual do menu inicial (fundo escuro, botões azuis, texto branco)
 * num só sítio. Menu inicial, seleção de níveis, pausa, nível completo e
 * game over usam todos esta classe, por isso ficam sempre iguais.
 * Para mudar o aspecto de TODOS os menus, mude só os valores aqui.
 */
public final class MenuStyle {

    public static final float CENTER_X = 400f;

    public static final float TITLE_SCALE = 4f;
    public static final float TEXT_SCALE = 2.2f;

    public static final float BUTTON_W = 300f;
    public static final float BUTTON_H = 50f;
    public static final float BUTTON_STEP = 70f;     // distância entre botões
    public static final float TITLE_Y = 370f;
    public static final float FIRST_BUTTON_Y = 250f;

    private static final Color BACKGROUND = new Color(0.1f, 0.1f, 0.15f, 1f);
    private static final Color BUTTON_ON = new Color(0f, 0f, 1f, 1f);
    private static final Color BUTTON_OFF = new Color(0.25f, 0.25f, 0.3f, 1f);
    private static final Color TEXT_ON = new Color(1f, 1f, 1f, 1f);
    private static final Color TEXT_OFF = new Color(0.55f, 0.55f, 0.6f, 1f);

    private static final GlyphLayout layout = new GlyphLayout();

    private MenuStyle() {}

    /** Rectângulo do botão número 'index' (0 = o de cima). */
    public static Rectangle button(int index) {
        return new Rectangle(CENTER_X - BUTTON_W / 2f,
            FIRST_BUTTON_Y - index * BUTTON_STEP,
            BUTTON_W, BUTTON_H);
    }

    /** Limpa o ecrã com a cor de fundo dos menus. */
    public static void clear() {
        Gdx.gl.glClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /** Chamar com o ShapeRenderer em modo Filled. */
    public static void drawButton(ShapeRenderer sr, Rectangle r, boolean enabled) {
        sr.setColor(enabled ? BUTTON_ON : BUTTON_OFF);
        sr.rect(r.x, r.y, r.width, r.height);
    }

    /** Pequeno cadeado à esquerda do botão (níveis bloqueados). ShapeRenderer em modo Filled. */
    public static void drawLock(ShapeRenderer sr, Rectangle r) {
        float cx = r.x + 35f;
        float cy = r.y + 14f;
        sr.setColor(TEXT_OFF);
        sr.rect(cx - 9, cy, 18, 14);        // corpo
        sr.rect(cx - 6, cy + 14, 3, 8);     // haste esquerda
        sr.rect(cx + 3, cy + 14, 3, 8);     // haste direita
        sr.rect(cx - 6, cy + 21, 12, 3);    // topo da haste
    }

    /** Título grande centrado (como "RUNNEXT"). Chamar entre batch.begin() e end(). */
    public static void drawTitle(SpriteBatch batch, BitmapFont font, String text, float y) {
        drawCentered(batch, font, text, TITLE_SCALE, TEXT_ON, y);
    }

    /** Linha de texto normal centrada (ex: pontuação). */
    public static void drawLine(SpriteBatch batch, BitmapFont font, String text, float y) {
        drawCentered(batch, font, text, TEXT_SCALE, TEXT_ON, y);
    }

    /** Texto centrado dentro de um botão. */
    public static void drawButtonLabel(SpriteBatch batch, BitmapFont font, String text,
                                       Rectangle r, boolean enabled) {
        font.getData().setScale(TEXT_SCALE);
        font.setColor(enabled ? TEXT_ON : TEXT_OFF);
        layout.setText(font, text);
        float x = r.x + (r.width - layout.width) / 2f;
        float y = r.y + (r.height + layout.height) / 2f;
        font.draw(batch, layout, x, y);
    }

    private static void drawCentered(SpriteBatch batch, BitmapFont font, String text,
                                     float scale, Color color, float y) {
        font.getData().setScale(scale);
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, CENTER_X - layout.width / 2f, y);
    }
}
