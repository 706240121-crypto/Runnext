package com.sidney.runnext.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

// Coin representa uma moeda coleccionável no nível.
// Guarda a posição, a área de colisão e se já foi apanhada.
public class Coin {

    // Tamanho da moeda no mundo do jogo (unidades lógicas, não pixels reais)
    public static final float SIZE = 24f;

    // Quantos pontos vale cada moeda (será usado quando fizermos o placar)
    public static final int VALUE = 10;

    private final Rectangle bounds;     // Área usada para detetar o toque do jogador
    private final float animationOffset; // Desfasamento para as moedas não girarem todas ao mesmo tempo
    private boolean collected = false;   // true = já foi apanhada (deixa de ser desenhada)

    public Coin(float x, float y) {
        bounds = new Rectangle(x, y, SIZE, SIZE);
        animationOffset = (x * 0.003f) % 0.5f;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getAnimationOffset() {
        return animationOffset;
    }

    public boolean isCollected() {
        return collected;
    }

    // Marca a moeda como apanhada: desaparece do ecrã.
    public void collect() {
        collected = true;
    }

    // Desenha a moeda com o frame de animação recebido (só se ainda não foi apanhada).
    public void render(SpriteBatch batch, TextureRegion frame) {
        if (!collected) {
            batch.draw(frame, bounds.x, bounds.y, SIZE, SIZE);
        }
    }
}
