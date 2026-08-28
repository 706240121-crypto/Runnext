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

    public enum State { IDLE, RUN, JUMP, FALL }
    private State state = State.IDLE;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(float delta) {
        handleInput();
        velocityY += GRAVITY * delta;
        x += velocityX * delta;
        y += velocityY * delta;
        updateState();
    }

    private void handleInput() {
        velocityX = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocityX = -MOVE_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocityX = MOVE_SPEED;
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) && onGround) {
            velocityY = JUMP_SPEED;
            onGround = false;
        }
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
        Color color;
        switch (state) {
            case RUN: color = Color.GREEN; break;
            case JUMP: color = Color.YELLOW; break;
            case FALL: color = Color.ORANGE; break;
            default: color = Color.WHITE;
        }
        renderer.setColor(color);
        renderer.rect(x, y, WIDTH, HEIGHT);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public float getVelocityY() { return velocityY; }
    public State getState() { return state; }
}
