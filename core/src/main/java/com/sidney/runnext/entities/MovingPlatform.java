package com.sidney.runnext.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * MovingPlatform.java
 *
 * Plataforma que anda de um lado para o outro, à altura do chão, por cima da água.
 * A personagem pode subir para ela e é LEVADA por ela. Pára um instante em cada ponta
 * para dar tempo de subir ou descer.
 *
 * O GameScreen chama update() a cada frame e, se a personagem estiver em cima,
 * soma-lhe getDeltaX() (quanto a plataforma andou) à posição.
 */
public class MovingPlatform {

    public static final float THICKNESS = 20f;
    private static final float PAUSE_TIME = 0.8f;   // segundos parada em cada ponta

    private final float minX;      // posição mais à esquerda (x do lado esquerdo)
    private final float maxX;      // posição mais à direita
    private final float width;
    private final float top;       // altura do topo (igual à do chão)
    private final float speed;

    private float x;
    private boolean movingRight = true;
    private float pauseTimer = 0f;
    private float deltaX = 0f;     // quanto andou no último update

    public MovingPlatform(float leftX, float rightX, float width, float top, float speed) {
        this.minX = leftX;
        this.maxX = Math.max(leftX, rightX);
        this.width = width;
        this.top = top;
        this.speed = speed;
        this.x = leftX;
    }

    public void update(float delta) {
        deltaX = 0f;

        if (pauseTimer > 0f) {
            pauseTimer -= delta;
            return;
        }

        float old = x;
        x += (movingRight ? speed : -speed) * delta;

        if (x >= maxX) {
            x = maxX;
            movingRight = false;
            pauseTimer = PAUSE_TIME;
        } else if (x <= minX) {
            x = minX;
            movingRight = true;
            pauseTimer = PAUSE_TIME;
        }
        deltaX = x - old;
    }

    public float getDeltaX() { return deltaX; }

    public float getTop() { return top; }

    /** A posição horizontal px está por cima da plataforma? */
    public boolean contains(float px) {
        return px >= x && px <= x + width;
    }

    /** Chamar com o ShapeRenderer em modo Filled e a câmara do mundo. */
    public void render(ShapeRenderer sr) {
        float y = top - THICKNESS;

        sr.setColor(0.30f, 0.18f, 0.08f, 1f);                  // contorno / sombra
        sr.rect(x, y, width, THICKNESS);
        sr.setColor(0.60f, 0.40f, 0.18f, 1f);                  // madeira
        sr.rect(x + 2f, y + 3f, width - 4f, THICKNESS - 6f);
        sr.setColor(0.72f, 0.52f, 0.26f, 1f);                  // tábuas (claras)
        for (float px = x + 14f; px < x + width - 6f; px += 28f) {
            sr.rect(px, y + 3f, 3f, THICKNESS - 6f);
        }
        sr.setColor(0.80f, 0.80f, 0.85f, 1f);                  // pregos nas pontas
        sr.circle(x + 6f, y + THICKNESS / 2f, 2.5f);
        sr.circle(x + width - 6f, y + THICKNESS / 2f, 2.5f);
    }
}
