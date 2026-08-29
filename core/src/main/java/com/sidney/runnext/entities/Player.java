package com.sidney.runnext.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Player {

    public static final float WIDTH = 32f;
    public static final float HEIGHT = 48f;

    private static final float MOVE_SPEED = 200f;
    private static final float JUMP_SPEED = 500f;
    private static final float GRAVITY = -1000f;

    private float x, y;
    private float velocityX, velocityY;
    private boolean onGround;

    private boolean movingLeft = false;
    private boolean movingRight = false;

    public enum State { IDLE, RUN, JUMP, FALL }
    private State state = State.IDLE;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(float delta) {
        handleInput();
        applyPhysics(delta);
        updateState();
    }

    private void handleInput() {
        velocityX = 0;
        if (movingLeft) velocityX = -MOVE_SPEED;
        if (movingRight) velocityX = MOVE_SPEED;
    }

    private void applyPhysics(float delta) {
        velocityY += GRAVITY * delta;
        x += velocityX * delta;
        y += velocityY * delta;
    }

    private void updateState() {
        if (!onGround) {
            state = velocityY > 0 ? State.JUMP : State.FALL;
        } else if (velocityX != 0) {
            state = State.RUN;
        } else {
            state = State.IDLE;
        }
    }

    public void landOn(float groundY) {
        y = groundY;
        velocityY = 0;
        onGround = true;
    }

    public void render(ShapeRenderer renderer) {
        // Define a cor baseada no estado
        Color baseColor = Color.WHITE;
        if (state == State.RUN) baseColor = Color.CYAN;
        else if (state == State.JUMP) baseColor = Color.YELLOW;
        else if (state == State.FALL) baseColor = Color.ORANGE;

        renderer.setColor(baseColor);

        // 1. Cabeça (quadrado de 16x16 no topo)
        renderer.rect(x + 8, y + 32, 16, 16);

        // 2. Corpo (retângulo de 8x16 abaixo da cabeça)
        renderer.rect(x + 12, y + 16, 8, 16);

        // 3. Braços (linhas a sair do meio do corpo)
        renderer.rectLine(x + 16, y + 24, x + 4, y + 32, 3);   // Braço esquerdo
        renderer.rectLine(x + 16, y + 24, x + 28, y + 32, 3);  // Braço direito

        // 4. Pernas (linhas a sair da base do corpo)
        renderer.rectLine(x + 16, y + 16, x + 8, y + 4, 3);    // Perna esquerda
        renderer.rectLine(x + 16, y + 16, x + 24, y + 4, 3);   // Perna direita
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public float getVelocityY() { return velocityY; }
    public State getState() { return state; }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }
}
