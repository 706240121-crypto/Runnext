package com.sidney.runnext.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {

    public static final float WIDTH = 150f;
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

    public enum State { IDLE, RUN, JUMP, FALL, HURT, DEAD }
    private State state = State.IDLE;

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnimation;

    private Array<Texture> loadedTextures;
    private float stateTime = 0f;

    private static final float COYOTE_TIME = 0.1f;
    private float coyoteTimer = 0f;

    private int jumpCount = 0;
    private static final int MAX_JUMPS = 2;   // 2 = duplo salto; ponha 1 para desligar

    private float hurtTimer = 0f;
    private static final float HURT_DURATION = 1.5f;
    private boolean isHurt = false;

    private float deathTimer = 0f;
    private static final float DEATH_DURATION = 1.0f;
    private float deathRotation = 0f;
    private float deathAlpha = 1f;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;

        loadedTextures = new Array<Texture>();

        Array<TextureRegion> idleFrames = new Array<TextureRegion>();
        for (int i = 1; i <= 6; i++) {
            Texture tex = new Texture(Gdx.files.internal("idle/Warrior_Idle_" + i + ".png"));
            loadedTextures.add(tex);
            idleFrames.add(new TextureRegion(tex));
        }
        idleAnimation = new Animation<TextureRegion>(0.15f, idleFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> runFrames = new Array<TextureRegion>();
        for (int i = 1; i <= 8; i++) {
            Texture tex = new Texture(Gdx.files.internal("Run/Warrior_Run_" + i + ".png"));
            loadedTextures.add(tex);
            runFrames.add(new TextureRegion(tex));
        }
        runAnimation = new Animation<TextureRegion>(0.10f, runFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> jumpFrames = new Array<TextureRegion>();
        for (int i = 1; i <= 3; i++) {
            Texture tex = new Texture(Gdx.files.internal("Jump/Warrior_Jump_" + i + ".png"));
            loadedTextures.add(tex);
            jumpFrames.add(new TextureRegion(tex));
        }
        jumpAnimation = new Animation<TextureRegion>(0.12f, jumpFrames, Animation.PlayMode.NORMAL);
    }

    public void update(float delta) {
        if (isHurt) {
            hurtTimer -= delta;
            if (hurtTimer <= 0f) {
                isHurt = false;
                state = State.IDLE;
            }
        }

        if (state == State.DEAD) {
            deathTimer -= delta;
            deathRotation += 180f * delta;
            deathAlpha = Math.max(0f, deathTimer / DEATH_DURATION);
        }

        handleInput();
        applyPhysics(delta);
        updateState();
        stateTime += delta;
        if (coyoteTimer > 0f) coyoteTimer -= delta;
    }

    private void handleInput() {
        if (isHurt || state == State.DEAD) {
            velocityX = 0;
            return;
        }

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

    /**
     * Tenta saltar. Devolve true se saltou (o GameScreen usa isso para tocar o som).
     *
     * @param justPressed true só no instante em que o botão é carregado.
     *                    O 1.º salto (no chão) funciona também com o botão mantido;
     *                    o 2.º salto (no ar) exige um toque novo. Sem isto, o 2.º salto
     *                    gastava-se sozinho, 1 frame depois do 1.º, enquanto o botão estava carregado.
     */
    public boolean jump(boolean justPressed) {
        if (isHurt || state == State.DEAD) return false;

        if (onGround || coyoteTimer > 0f) {
            velocityY = JUMP_SPEED;
            onGround = false;
            coyoteTimer = 0f;
            jumpCount = 1;
            stateTime = 0f;
            return true;
        }

        if (justPressed && jumpCount < MAX_JUMPS) {
            velocityY = JUMP_SPEED;
            jumpCount++;
            stateTime = 0f;
            return true;
        }

        return false;
    }

    private void applyPhysics(float delta) {
        if (state != State.DEAD) {
            velocityY += GRAVITY * delta;
        } else {
            velocityY += (GRAVITY * 0.3f) * delta;
        }

        x += velocityX * delta;
        y += velocityY * delta;
    }

    private void updateState() {
        // CORRIGIDO: a morte tem prioridade sobre o dano. Antes, se levasse o último golpe
        // ainda "ferida", o estado voltava de DEAD para HURT e a animação de morte não aparecia.
        if (state == State.DEAD) {
            return;
        }

        if (isHurt) {
            state = State.HURT;
            return;
        }

        State oldState = state;

        if (!onGround) {
            state = velocityY > 0 ? State.JUMP : State.FALL;
        } else if (velocityX != 0) {
            state = State.RUN;
        } else {
            state = State.IDLE;
        }

        if (oldState != state) {
            stateTime = 0f;
        }
    }

    public void setHurt() {
        if (state == State.DEAD) return;
        isHurt = true;
        hurtTimer = HURT_DURATION;
        state = State.HURT;
        stateTime = 0f;
    }

    public void setDead() {
        isHurt = false;      // CORRIGIDO: morrer cancela o dano pendente
        hurtTimer = 0f;
        state = State.DEAD;
        deathTimer = DEATH_DURATION;
        deathRotation = 0f;
        deathAlpha = 1f;
        velocityX = 0;
        velocityY = JUMP_SPEED * 0.3f;
        stateTime = 0f;
    }

    // Verifica se a animação de morte terminou (usado pelo GameScreen)
    public boolean isDeathAnimationFinished() {
        return state == State.DEAD && deathTimer <= 0f;
    }

    public boolean isHurt() {
        return isHurt;
    }

    public void bounce(float factor) {
        if (state == State.DEAD) return;
        velocityY = JUMP_SPEED * factor;
        onGround = false;
        coyoteTimer = 0f;
        jumpCount = 0;
        stateTime = 0f;
    }

    public void landOn(float groundY) {
        if (state == State.DEAD) return;
        y = groundY - FOOT_OFFSET;
        velocityY = 0;
        onGround = true;
        coyoteTimer = COYOTE_TIME;
        jumpCount = 0;

        if (isHurt) {
            isHurt = false;
            state = State.IDLE;
        }
    }

    // Chamado pelo GameScreen quando já não há nada por baixo dos pés (ex: saiu da borda)
    public void leaveGround() {
        onGround = false;
        // Depois da tolerância (coyote), se ainda não saltou, o "salto do chão" já se perdeu:
        // sobra só o salto no ar (assim cair de uma borda não dá dois saltos no ar)
        if (jumpCount == 0 && coyoteTimer <= 0f) {
            jumpCount = 1;
        }
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame;

        switch (state) {
            case RUN:
                currentFrame = runAnimation.getKeyFrame(stateTime, true);
                break;
            case JUMP:
            case FALL:
                currentFrame = jumpAnimation.getKeyFrame(stateTime, false);
                break;
            default:
                currentFrame = idleAnimation.getKeyFrame(stateTime, true);
                break;
        }

        if (currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        float drawWidth = WIDTH;
        float drawX = x;

        if (!facingRight && state != State.DEAD) {
            drawX += WIDTH;
            drawWidth = -WIDTH;
        }

        if (state == State.DEAD) {
            batch.setColor(1f, 1f, 1f, deathAlpha);
            // Parâmetros: region, x, y, originX, originY, width, height, scaleX, scaleY, rotation
            batch.draw(currentFrame, drawX, y, WIDTH / 2f, HEIGHT / 2f, WIDTH, HEIGHT, 1f, 1f, deathRotation);
            batch.setColor(1f, 1f, 1f, 1f);
        } else if (isHurt) {
            float flash = (hurtTimer * 10f) % 1f;
            if (flash < 0.5f) {
                batch.setColor(1f, 0.3f, 0.3f, 1f);
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }
            batch.draw(currentFrame, drawX, y, drawWidth, HEIGHT);
            batch.setColor(1f, 1f, 1f, 1f);
        } else {
            batch.draw(currentFrame, drawX, y, drawWidth, HEIGHT);
        }
    }

    public void dispose() {
        if (loadedTextures != null) {
            for (Texture tex : loadedTextures) {
                tex.dispose();
            }
            loadedTextures.clear();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public float getVelocityY() { return velocityY; }
    public void setVelocityY(float velocityY) { this.velocityY = velocityY; }
    public State getState() { return state; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getFootY() { return y + FOOT_OFFSET; }
    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }

    public void reset() {
        state = State.IDLE;
        isHurt = false;
        hurtTimer = 0f;
        deathTimer = 0f;
        deathRotation = 0f;
        deathAlpha = 1f;
        velocityX = 0;
        velocityY = 0;
        onGround = false;
        coyoteTimer = 0f;
        jumpCount = 0;
    }
}
