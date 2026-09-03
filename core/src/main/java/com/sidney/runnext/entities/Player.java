package com.sidney.runnext.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

// Representa o jogador: posição, física (gravidade/movimento) e desenho ("boneco" feito de formas).
public class Player {

    // Dimensões fixas do jogador, usadas para colisão e para desenhar as partes do corpo
    public static final float WIDTH = 32f;
    public static final float HEIGHT = 48f;

    // Constantes de física (unidades do "mundo" por segundo)
    private static final float MOVE_SPEED = 200f;  // velocidade horizontal ao mover-se
    private static final float JUMP_SPEED = 500f;  // velocidade vertical inicial ao saltar (não usada ainda — ver nota no fim)
    private static final float GRAVITY = -1000f;   // aceleração da gravidade (negativa = puxa para baixo)

    private float x, y;                 // posição atual do jogador (canto inferior esquerdo do "hitbox")
    private float velocityX, velocityY; // velocidade atual nos eixos X e Y
    private boolean onGround;           // true quando o jogador está apoiado no chão

    // Flags de input, controladas externamente (pelo GameScreen) via setMovingLeft/setMovingRight
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // Estado atual da animação/comportamento do jogador
    public enum State { IDLE, RUN, JUMP, FALL }
    private State state = State.IDLE;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    // Chamado a cada frame pelo GameScreen. Atualiza input, física e estado, por esta ordem.
    public void update(float delta) {
        handleInput();
        applyPhysics(delta);
        updateState();
    }

    // Define a velocidade horizontal com base nas flags de movimento.
    // Nota: se movingLeft e movingRight estiverem ambos true, o direito "ganha"
    // (velocityX fica positivo), pois a condição da direita é avaliada por último.
    private void handleInput() {
        velocityX = 0;
        if (movingLeft) velocityX = -MOVE_SPEED;
        if (movingRight) velocityX = MOVE_SPEED;
    }

    // Aplica gravidade e move o jogador de acordo com a velocidade e o tempo decorrido (delta).
    // Usar "delta" garante que o movimento é consistente independentemente do FPS do dispositivo.
    private void applyPhysics(float delta) {
        velocityY += GRAVITY * delta; // acelera a queda a cada frame
        x += velocityX * delta;
        y += velocityY * delta;
    }

    // Decide o estado visual/lógico do jogador consoante a física atual.
    private void updateState() {
        if (!onGround) {
            // No ar: a subir (velocityY > 0) = JUMP; a descer = FALL
            state = velocityY > 0 ? State.JUMP : State.FALL;
        } else if (velocityX != 0) {
            state = State.RUN;
        } else {
            state = State.IDLE;
        }
    }

    // Chamado pelo GameScreen quando deteta colisão com o chão.
    // "Cola" o jogador à superfície e zera a velocidade vertical.
    public void landOn(float groundY) {
        y = groundY;
        velocityY = 0;
        onGround = true;
    }

    // Desenha o jogador como um "boneco palito" feito só de retângulos e linhas grossas.
    public void render(ShapeRenderer renderer) {
        // Cor base do boneco (a lógica de mudar cor consoante o estado está desativada/comentada)
        Color baseColor = Color.WHITE;
        //if (state == State.RUN) baseColor = Color.CYAN;
        //else if (state == State.JUMP) baseColor = Color.YELLOW;
        //  else if (state == State.FALL) baseColor = Color.ORANGE;

        renderer.setColor(baseColor);

        // 1. Cabeça (quadrado de 16x16 no topo, do y+32 ao y+48 = HEIGHT)
        renderer.rect(x + 8, y + 32, 16, 16);

        // 2. Corpo (retângulo de 8x16, entre a cabeça e as pernas)
        renderer.rect(x + 12, y + 16, 8, 16);

        // 3. Braços (linhas grossas de 3px, a partir do meio do corpo até acima da altura da cabeça)
        renderer.rectLine(x + 16, y + 24, x + 4, y + 32, 3);   // Braço esquerdo
        renderer.rectLine(x + 16, y + 24, x + 28, y + 32, 3);  // Braço direito

        // 4. Pernas (linhas grossas de 3px, a partir da base do corpo até ao chão do boneco)
        renderer.rectLine(x + 16, y + 16, x + 8, y + 4, 3);    // Perna esquerda
        renderer.rectLine(x + 16, y + 16, x + 24, y + 4, 3);   // Perna direita
    }

    // Retângulo de colisão (hitbox), usado pelo GameScreen para detetar o chão e os limites do ecrã
    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public float getVelocityY() { return velocityY; }
    public State getState() { return state; }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    // Chamados pelo GameScreen a cada frame, consoante o input (toque/teclado) detetado
    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }

    // Métodos para controlar a posição Y (ex: usados no "Reiniciar" do menu de pausa)
    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
