package com.sidney.runnext.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * PatrolEnemy.java
 *
 * TIPO 2 de inimigo: patrulha entre dois pontos, A e B.
 * Anda de A até B, PARA um instante, volta de B até A, PARA outra vez, e repete.
 * Herda de Enemy (guarda posição, sprite, "está vivo", caixa de colisão...)
 * e só muda o comportamento: update() (as paragens) e render() (balança parado).
 *
 * Como para nas pontas, dá tempo à personagem para o pisar ou passar.
 */
public class PatrolEnemy extends Enemy {

    private final float pauseTime;   // segundos parado em cada ponta
    private float pauseTimer = 0f;

    /**
     * @param pointA     x do ponto A (extremo esquerdo da patrulha)
     * @param pointB     x do ponto B (extremo direito, borda direita do inimigo)
     * @param pauseTime  tempo que fica parado em cada ponto
     */
    public PatrolEnemy(com.badlogic.gdx.graphics.g2d.TextureRegion region,
                       float pointA, float pointB, float groundTop, float speed, float pauseTime) {
        super(region, pointA, pointB, groundTop, speed);
        this.pauseTime = pauseTime;
    }

    @Override
    public void update(float delta) {
        if (!alive) return;
        stateTime += delta;

        // parado numa ponta: só espera
        if (pauseTimer > 0f) {
            pauseTimer -= delta;
            return;
        }

        x += (movingRight ? speed : -speed) * delta;

        if (x >= maxX) {            // chegou a B
            x = maxX;
            movingRight = false;
            pauseTimer = pauseTime;
        } else if (x <= minX) {     // chegou a A
            x = minX;
            movingRight = true;
            pauseTimer = pauseTime;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!alive) return;

        if (pauseTimer > 0f) {
            // parado: balança devagar, como se olhasse à volta
            float balanco = 5f * MathUtils.sin(stateTime * 3f);
            batch.draw(region, x, y, WIDTH / 2f, 0f, WIDTH, HEIGHT, 1f, 1f, balanco);
        } else {
            super.render(batch); // a andar: balanço e saltinhos, como o tipo 1
        }
    }
}
