package com.sidney.runnext.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * Enemy.java
 *
 * Inimigo que patrulha para a esquerda e para a direita dentro de uma zona
 * (areaLeft..areaRight), virando quando chega a cada extremo.
 * É desenhado com um sprite (TextureRegion) que lhe é dado, com um pequeno
 * balanço e saltinhos para parecer que anda.
 * A textura é criada e destruída pelo GameScreen (o inimigo só a usa).
 */
public class Enemy {

    public static final float WIDTH = 64f;
    public static final float HEIGHT = 64f;

    private final TextureRegion region;
    private final float minX;   // limite esquerdo (x do lado esquerdo do corpo)
    private final float maxX;   // limite direito  (x do lado esquerdo do corpo)
    private final float y;
    private final float speed;

    private float x;
    private boolean movingRight = true;
    private boolean alive = true;
    private float stateTime = 0f;

    /**
     * @param region     imagem do inimigo
     * @param areaLeft   x onde começa a zona de patrulha
     * @param areaRight  x onde termina a zona de patrulha (borda direita do inimigo)
     * @param groundTop  altura do topo do chão onde o inimigo pisa
     * @param speed      velocidade em px/s (a personagem anda a 200)
     */
    public Enemy(TextureRegion region, float areaLeft, float areaRight, float groundTop, float speed) {
        this.region = region;
        this.minX = areaLeft;
        this.maxX = Math.max(areaLeft, areaRight - WIDTH);
        this.x = areaLeft;
        this.y = groundTop;
        this.speed = speed;
    }

    public void update(float delta) {
        if (!alive) return;
        stateTime += delta;

        x += (movingRight ? speed : -speed) * delta;

        if (x >= maxX) {
            x = maxX;
            movingRight = false;
        } else if (x <= minX) {
            x = minX;
            movingRight = true;
        }
    }

    public boolean isAlive() { return alive; }

    public void kill() { alive = false; }

    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    /** Chamar entre batch.begin() e batch.end(), com a câmara do mundo. */
    public void render(SpriteBatch batch) {
        if (!alive) return;

        float balanco = 8f * (float) Math.sin(stateTime * 10f);            // inclina de um lado para o outro
        float salto = 5f * Math.abs((float) Math.sin(stateTime * 10f));    // pequeno salto a cada passo

        batch.draw(region,
            x, y + salto,
            WIDTH / 2f, 0f,        // roda a partir da base, para os "pés" não saírem do chão
            WIDTH, HEIGHT,
            1f, 1f,
            balanco);
    }
}
