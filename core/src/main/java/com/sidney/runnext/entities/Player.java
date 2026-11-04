package com.sidney.runnext.entities;

import com.badlogic.gdx.Gdx;
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

    public enum State { IDLE, RUN, JUMP, FALL }
    private State state = State.IDLE;

    // Animações para os novos sprites da Guerreira
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnimation;

    // Lista para guardar as texturas e fazer o dispose no final
    private Array<Texture> loadedTextures;

    private float stateTime = 0f;

    // Tolerância: ainda pode saltar este tempo (s) depois de sair da borda
    private static final float COYOTE_TIME = 0.1f;
    private float coyoteTimer = 0f;

    // ✅ NOVO: Variáveis para controlar o Salto Duplo
    private int jumpCount = 0;
    private static final int MAX_JUMPS = 2;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;

        loadedTextures = new Array<Texture>();

        // 1. Carregar Animação IDLE (6 frames da pasta assets/idle)
        Array<TextureRegion> idleFrames = new Array<TextureRegion>();
        for (int i = 1; i <= 6; i++) {
            Texture tex = new Texture(Gdx.files.internal("idle/Warrior_Idle_" + i + ".png"));
            loadedTextures.add(tex);
            idleFrames.add(new TextureRegion(tex));
        }
        idleAnimation = new Animation<TextureRegion>(0.15f, idleFrames, Animation.PlayMode.LOOP);

        // 2. Carregar Animação RUN (8 frames da pasta assets/Run)
        Array<TextureRegion> runFrames = new Array<TextureRegion>();
        for (int i = 1; i <= 8; i++) {
            Texture tex = new Texture(Gdx.files.internal("Run/Warrior_Run_" + i + ".png"));
            loadedTextures.add(tex);
            runFrames.add(new TextureRegion(tex));
        }
        runAnimation = new Animation<TextureRegion>(0.10f, runFrames, Animation.PlayMode.LOOP);

        // 3. Carregar Animação JUMP (3 frames da pasta assets/Jump)
        Array<TextureRegion> jumpFrames = new Array<TextureRegion>();
        for (int i = 1; i <= 3; i++) {
            Texture tex = new Texture(Gdx.files.internal("Jump/Warrior_Jump_" + i + ".png"));
            loadedTextures.add(tex);
            jumpFrames.add(new TextureRegion(tex));
        }
        jumpAnimation = new Animation<TextureRegion>(0.12f, jumpFrames, Animation.PlayMode.NORMAL);
    }

    public void update(float delta) {
        handleInput();
        applyPhysics(delta);
        updateState();
        stateTime += delta;
        if (coyoteTimer > 0f) coyoteTimer -= delta;
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

    // ✅ MODIFICADO: Lógica do Salto Duplo
    public void jump() {
        if (onGround || coyoteTimer > 0f) {
            velocityY = JUMP_SPEED;
            onGround = false;
            coyoteTimer = 0f;
            jumpCount = 1; // Regista o primeiro salto
            stateTime = 0f; // Reinicia a animação
        } else if (jumpCount < MAX_JUMPS) {
            velocityY = JUMP_SPEED; // Aplica o segundo impulso
            jumpCount++; // Regista o segundo salto
            stateTime = 0f; // Reinicia a animação para dar feedback visual do duplo salto
        }
    }

    private void applyPhysics(float delta) {
        velocityY += GRAVITY * delta;
        x += velocityX * delta;
        y += velocityY * delta;
    }

    private void updateState() {
        State oldState = state;

        if (!onGround) {
            state = velocityY > 0 ? State.JUMP : State.FALL;
        } else if (velocityX != 0) {
            state = State.RUN;
        } else {
            state = State.IDLE;
        }

        // Se mudou de estado, reinicia o contador do frame da animação
        if (oldState != state) {
            stateTime = 0f;
        }
    }

    // NOVO: salto automático (ao pisar um inimigo ou ao levar dano). factor = força do salto (0 a 1)
    public void bounce(float factor) {
        velocityY = JUMP_SPEED * factor;
        onGround = false;
        coyoteTimer = 0f;
        jumpCount = 0; // Reseta a contagem para permitir saltos normais após quicar no inimigo
        stateTime = 0f;
    }

    // ✅ MODIFICADO: Reseta a contagem de saltos ao tocar no chão
    public void landOn(float groundY) {
        y = groundY - FOOT_OFFSET;
        velocityY = 0;
        onGround = true;
        coyoteTimer = COYOTE_TIME;
        jumpCount = 0; // Permite novos 2 saltos
    }

    // NOVO: chamado pelo GameScreen quando já não há nada por baixo dos pés (ex: saiu da borda)
    public void leaveGround() {
        onGround = false;
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
            case IDLE:
            default:
                currentFrame = idleAnimation.getKeyFrame(stateTime, true);
                break;
        }

        // Garante que a orientação base do frame não vem invertida
        if (currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        float drawWidth = WIDTH;
        float drawX = x;

        // CORREÇÃO DA DIREÇÃO:
        // Inverte a imagem quando estiver a olhar para a ESQUERDA (!facingRight)
        if (!facingRight) {
            drawX += WIDTH;
            drawWidth = -WIDTH;
        }

        batch.draw(currentFrame, drawX, y, drawWidth, HEIGHT);
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

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    public State getState() { return state; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    // NOVO: altura dos pés (usada pelo GameScreen para detectar aterragem no chão)
    public float getFootY() { return y + FOOT_OFFSET; }

    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }
}
