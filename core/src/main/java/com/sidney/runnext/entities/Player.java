package com.sidney.runnext.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Player {

    public static final float WIDTH = 100f;
    public static final float HEIGHT = 150f;
    private static final float FOOT_OFFSET = 12f;

    private static final float MOVE_SPEED = 200f;
    private static final float JUMP_SPEED = 500f;
    private static final float GRAVITY = -1000f;

    private float x, y;
    private float velocityX, velocityY;
    private boolean onGround;
    private boolean facingRight = true;

    private boolean movingLeft = false;
    private boolean movingRight = false;

    public enum State { IDLE, RUN, JUMP, FALL }
    private State state = State.IDLE;

    private Texture spriteSheet;
    private Animation<TextureRegion> runAnimation;
    private TextureRegion idleFrame;
    private TextureRegion jumpFrame;
    private float stateTime = 0f;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;

        spriteSheet = new Texture(Gdx.files.internal("odete.png"));

        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 4, spriteSheet.getHeight() / 2);

        // 1. Ciclo de corrida com os 3 quadros limpos
        TextureRegion[] runFrames = new TextureRegion[3];
        runFrames[0] = tmp[0][0];
        runFrames[1] = tmp[0][1];
        runFrames[2] = tmp[0][2];
        runAnimation = new Animation<TextureRegion>(0.12f, runFrames);

        // 2. Estados estáticos
        idleFrame = tmp[1][0]; // Parada de frente

        // CORREÇÃO DO SALTO: Usamos o quadro [0][3] (o 4º da linha de cima) que tem exatamente
        // o mesmo alinhamento de grelha da corrida, evitando que a imagem fique cortada ao meio.
        jumpFrame = tmp[0][3];
    }

    public void update(float delta) {
        handleInput();
        applyPhysics(delta);
        updateState();
        stateTime += delta;
    }

    private void handleInput() {
        velocityX = 0;
        if (movingLeft) {
            velocityX = -MOVE_SPEED;
            facingRight = false;
        }
        if (movingRight) {
            velocityX = MOVE_SPEED;
            facingRight = true;
        }
    }

    public void jump() {
        if (onGround) {
            velocityY = JUMP_SPEED;
            onGround = false;
        }
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
        y = groundY - FOOT_OFFSET;
        velocityY = 0;
        onGround = true;
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame;

        switch (state) {
            case RUN:
                currentFrame = runAnimation.getKeyFrame(stateTime, true);
                break;
            case JUMP:
            case FALL:
                currentFrame = jumpFrame;
                break;
            case IDLE:
            default:
                currentFrame = idleFrame;
                break;
        }

        // Limpeza de segurança para evitar qualquer flip acumulado
        if (currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        float drawWidth = WIDTH;
        float drawX = x;

        // Inversão correta e segura da direção
        if (facingRight) {
            drawX += WIDTH;
            drawWidth = -WIDTH;
        }

        batch.draw(currentFrame, drawX, y, drawWidth, HEIGHT);
    }

    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public float getVelocityY() { return velocityY; }
    public State getState() { return state; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }
}
