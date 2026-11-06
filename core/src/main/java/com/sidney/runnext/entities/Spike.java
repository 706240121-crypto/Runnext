package com.sidney.runnext.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * Spike.java
 *
 * Obstáculo fixo no chão (espinhos). Quem lhe toca com os pés leva dano;
 * para passar é preciso saltar por cima. Desenhado com triângulos.
 */
public class Spike {

    public static final float HEIGHT = 28f;
    private static final float TOOTH_WIDTH = 20f;

    private final float x;
    private final float y;
    private final float width;
    private final int teeth;

    /**
     * @param x          x do lado esquerdo
     * @param groundTop  altura do chão onde assenta
     * @param width      largura aproximada (arredondada a um número inteiro de dentes)
     */
    public Spike(float x, float groundTop, float width) {
        this.x = x;
        this.y = groundTop;
        this.teeth = Math.max(1, Math.round(width / TOOTH_WIDTH));
        this.width = teeth * TOOTH_WIDTH;
    }

    public Rectangle getBounds() {
        // um pouco mais pequena que o desenho, para ser justa com o jogador
        return new Rectangle(x + 3f, y, width - 6f, HEIGHT - 6f);
    }

    /** Chamar com o ShapeRenderer em modo Filled e a câmara do mundo. */
    public void render(ShapeRenderer sr) {
        for (int i = 0; i < teeth; i++) {
            float tx = x + i * TOOTH_WIDTH;
            sr.setColor(0.78f, 0.80f, 0.85f, 1f);
            sr.triangle(tx, y, tx + TOOTH_WIDTH, y, tx + TOOTH_WIDTH / 2f, y + HEIGHT);
            sr.setColor(0.45f, 0.47f, 0.55f, 1f); // sombra do lado direito do dente
            sr.triangle(tx + TOOTH_WIDTH / 2f, y, tx + TOOTH_WIDTH, y, tx + TOOTH_WIDTH / 2f, y + HEIGHT);
        }
    }
}
