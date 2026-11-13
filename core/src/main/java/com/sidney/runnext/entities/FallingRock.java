package com.sidney.runnext.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * FallingRock.java
 *
 * Obstáculo novo do nível 2: uma pedra que fica pendurada no alto do ecrã e CAI quando
 * a personagem se aproxima (gravidade). Antes de cair, treme e aparece uma sombra no chão
 * a avisar onde vai cair. Quem for apanhado leva dano; a pedra parte-se e, passado
 * um tempo, volta ao alto.
 *
 *   WAITING -> SHAKING (0,5 s, avisa) -> FALLING (gravidade) -> BROKEN (parte-se) -> WAITING
 */
public class FallingRock {

    public enum State { WAITING, SHAKING, FALLING, BROKEN }

    private static final float RADIUS = 22f;
    private static final float HANG_Y = 430f;            // altura (centro) onde espera
    private static final float GRAVITY = -1100f;
    private static final float TRIGGER_BEFORE = 240f;    // cai quando a personagem está até 240 px antes...
    private static final float TRIGGER_AFTER = 30f;      // ...ou mesmo por baixo
    private static final float SHAKE_TIME = 0.5f;
    private static final float BROKEN_TIME = 2.5f;

    private final float x;            // centro horizontal
    private final float groundTop;

    private float y = HANG_Y;
    private float velocityY = 0f;
    private State state = State.WAITING;
    private float timer = 0f;
    private float time = 0f;

    public FallingRock(float centerX, float groundTop) {
        this.x = centerX;
        this.groundTop = groundTop;
    }

    public void update(float delta, float playerCenterX) {
        time += delta;

        switch (state) {
            case WAITING:
                if (playerCenterX >= x - TRIGGER_BEFORE && playerCenterX <= x + TRIGGER_AFTER) {
                    state = State.SHAKING;
                    timer = SHAKE_TIME;
                }
                break;

            case SHAKING:
                timer -= delta;
                if (timer <= 0f) {
                    state = State.FALLING;
                    velocityY = 0f;
                }
                break;

            case FALLING:
                velocityY += GRAVITY * delta;
                y += velocityY * delta;
                if (y - RADIUS <= groundTop) {       // bateu no chão
                    y = groundTop + RADIUS;
                    state = State.BROKEN;
                    timer = BROKEN_TIME;
                }
                break;

            case BROKEN:
                timer -= delta;
                if (timer <= 0f) {                   // volta ao alto, pronta para cair outra vez
                    y = HANG_Y;
                    state = State.WAITING;
                }
                break;
        }
    }

    /** A pedra acertou na personagem: parte-se. */
    public void hit() {
        state = State.BROKEN;
        timer = BROKEN_TIME;
        y = groundTop + RADIUS;
    }

    /** Só faz mal enquanto está a cair. */
    public boolean isDangerous() { return state == State.FALLING; }

    public Rectangle getBounds() {
        return new Rectangle(x - RADIUS + 3f, y - RADIUS + 3f, 2f * RADIUS - 6f, 2f * RADIUS - 6f);
    }

    /** Chamar com o ShapeRenderer em modo Filled e a câmara do mundo. */
    public void render(ShapeRenderer sr) {
        // sombra no chão: avisa onde vai cair
        if (state == State.SHAKING || state == State.FALLING) {
            sr.setColor(0.10f, 0.18f, 0.08f, 1f);
            sr.ellipse(x - 26f, groundTop + 1f, 52f, 9f);
        }

        switch (state) {
            case WAITING:
                drawRock(sr, x, HANG_Y);
                break;
            case SHAKING:
                drawRock(sr, x + MathUtils.sin(time * 70f) * 3f, HANG_Y + MathUtils.sin(time * 55f) * 2f);
                break;
            case FALLING:
                drawRock(sr, x, y);
                break;
            case BROKEN:
                if (timer > BROKEN_TIME - 0.6f) {    // estilhaços, só um instante
                    sr.setColor(0.42f, 0.42f, 0.46f, 1f);
                    sr.circle(x - 24f, groundTop + 5f, 6f);
                    sr.circle(x - 10f, groundTop + 8f, 5f);
                    sr.circle(x + 12f, groundTop + 6f, 7f);
                    sr.circle(x + 26f, groundTop + 4f, 5f);
                }
                break;
        }
    }

    private void drawRock(ShapeRenderer sr, float cx, float cy) {
        sr.setColor(0.33f, 0.33f, 0.37f, 1f);
        sr.circle(cx, cy, RADIUS);
        sr.setColor(0.50f, 0.50f, 0.55f, 1f);   // zona iluminada
        sr.circle(cx - 6f, cy + 6f, RADIUS * 0.55f);
        sr.setColor(0.24f, 0.24f, 0.28f, 1f);   // buraquinhos
        sr.circle(cx + 9f, cy - 7f, 4f);
        sr.circle(cx + 2f, cy - 13f, 3f);
    }
}
