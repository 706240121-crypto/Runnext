package com.sidney.runnext.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * Boss.java
 *
 * Boss final do nível 3. Comportamento (máquina de estados):
 *
 *   WAITING   -> espera que a personagem chegue à arena
 *   PATROL    -> anda de um lado para o outro; de tempos a tempos prepara um ataque
 *   TELEGRAPH -> avisa o ataque (treme e fica vermelho) virado para a personagem
 *   CHARGE    -> investida rápida na direcção da personagem (o ataque)
 *   STUNNED   -> atordoado depois de ser pisado: fica quieto e inofensivo
 *   DYING     -> desaparece aos poucos; DEAD -> já foi
 *
 * Tem MAX_HP de vida: cada vez que a personagem lhe cai em cima, perde 1.
 * Com 2 ou menos de vida entra em "fúria": mais rápido e ataca com mais frequência.
 */
public class Boss {

    public enum State { WAITING, PATROL, TELEGRAPH, CHARGE, STUNNED, DYING, DEAD }

    public static final float WIDTH = 140f;
    public static final float HEIGHT = 113f;
    public static final int MAX_HP = 5;

    // valores normais / valores em fúria
    private static final float PATROL_SPEED = 80f;
    private static final float PATROL_SPEED_RAGE = 120f;
    private static final float CHARGE_SPEED = 260f;
    private static final float CHARGE_SPEED_RAGE = 330f;
    private static final float CHARGE_TIME = 0.6f;
    private static final float CHARGE_TIME_RAGE = 0.7f;
    private static final float TELEGRAPH_TIME = 0.8f;
    private static final float TELEGRAPH_TIME_RAGE = 0.6f;
    private static final float COOLDOWN = 3.5f;
    private static final float COOLDOWN_RAGE = 2.2f;

    private static final float STUN_TIME = 1.2f;
    private static final float DYING_TIME = 1.5f;

    private final TextureRegion region;
    private final float minX;   // limites da arena (x do lado esquerdo do Boss)
    private final float maxX;
    private final float y;

    private float x;
    private int hp = MAX_HP;
    private State state = State.WAITING;
    private float stateTimer = 0f;
    private float attackCooldown = 2f;
    private boolean movingRight = false;
    private float time = 0f;

    /**
     * @param region     imagem do Boss
     * @param areaLeft   x onde começa a arena
     * @param areaRight  x onde termina a arena (borda direita do Boss)
     * @param startX     x onde o Boss espera
     * @param groundTop  altura do chão
     */
    public Boss(TextureRegion region, float areaLeft, float areaRight, float startX, float groundTop) {
        this.region = region;
        this.minX = areaLeft;
        this.maxX = Math.max(areaLeft, areaRight - WIDTH);
        this.x = MathUtils.clamp(startX, minX, maxX);
        this.y = groundTop;
    }

    /** Acorda o Boss (chamado quando a personagem entra na arena). */
    public void activate() {
        if (state == State.WAITING) {
            state = State.PATROL;
            attackCooldown = 2f;
        }
    }

    public void update(float delta, float playerCenterX) {
        time += delta;

        switch (state) {
            case PATROL:
                moveHorizontally(patrolSpeed() * delta);
                attackCooldown -= delta;
                if (attackCooldown <= 0f) {
                    movingRight = playerCenterX > x + WIDTH / 2f; // vira-se para a personagem
                    state = State.TELEGRAPH;
                    stateTimer = isEnraged() ? TELEGRAPH_TIME_RAGE : TELEGRAPH_TIME;
                }
                break;

            case TELEGRAPH:
                stateTimer -= delta;
                if (stateTimer <= 0f) {
                    state = State.CHARGE;
                    stateTimer = isEnraged() ? CHARGE_TIME_RAGE : CHARGE_TIME;
                }
                break;

            case CHARGE:
                boolean bateuNaParede = moveHorizontally(chargeSpeed() * delta);
                stateTimer -= delta;
                if (stateTimer <= 0f || bateuNaParede) {
                    endAttack();
                }
                break;

            case STUNNED:
                stateTimer -= delta;
                if (stateTimer <= 0f) {
                    state = State.PATROL;
                    attackCooldown = isEnraged() ? COOLDOWN_RAGE : COOLDOWN;
                }
                break;

            case DYING:
                stateTimer -= delta;
                if (stateTimer <= 0f) {
                    state = State.DEAD;
                }
                break;

            default: // WAITING e DEAD não fazem nada
                break;
        }
    }

    // Move o Boss; devolve true se bateu num extremo da arena
    private boolean moveHorizontally(float dx) {
        x += movingRight ? dx : -dx;
        if (x >= maxX) {
            x = maxX;
            movingRight = false;
            return true;
        }
        if (x <= minX) {
            x = minX;
            movingRight = true;
            return true;
        }
        return false;
    }

    private void endAttack() {
        state = State.PATROL;
        attackCooldown = isEnraged() ? COOLDOWN_RAGE : COOLDOWN;
    }

    private float patrolSpeed() { return isEnraged() ? PATROL_SPEED_RAGE : PATROL_SPEED; }

    private float chargeSpeed() { return isEnraged() ? CHARGE_SPEED_RAGE : CHARGE_SPEED; }

    /**
     * A personagem caiu em cima do Boss. Devolve true se o golpe contou
     * (não conta se já está atordoado ou a morrer).
     */
    public boolean takeHit() {
        if (state == State.WAITING || state == State.STUNNED
            || state == State.DYING || state == State.DEAD) {
            return false;
        }
        hp--;
        if (hp <= 0) {
            hp = 0;
            state = State.DYING;
            stateTimer = DYING_TIME;
        } else {
            state = State.STUNNED;
            stateTimer = STUN_TIME;
        }
        return true;
    }

    public boolean isEnraged() { return hp <= 2; }

    public boolean isActive() { return state != State.WAITING; }

    public boolean isDead() { return state == State.DEAD; }

    /** Pode ser pisado / tocado (deixa de poder quando está a morrer). */
    public boolean isCollidable() { return state != State.DYING && state != State.DEAD; }

    /** Faz mal à personagem ao tocar-lhe? (atordoado não faz) */
    public boolean canHurtPlayer() { return isCollidable() && state != State.STUNNED; }

    public int getHp() { return hp; }

    public float getHpFraction() { return hp / (float) MAX_HP; }

    public Rectangle getBounds() {
        // um pouco mais pequena que o desenho, para ser justa com o jogador
        return new Rectangle(x + 14f, y, WIDTH - 28f, HEIGHT - 18f);
    }

    /** Chamar entre batch.begin() e batch.end(), com a câmara do mundo. */
    public void render(SpriteBatch batch) {
        if (state == State.DEAD) return;

        float drawX = x;
        float bob = 0f;
        float rotation = 0f;
        float scale = 1f;
        float alpha = 1f;
        float green = 1f;
        float blue = 1f;

        switch (state) {
            case WAITING:
                bob = 3f * Math.abs(MathUtils.sin(time * 3f));
                break;
            case PATROL:
                bob = 4f * Math.abs(MathUtils.sin(time * 8f));
                rotation = 4f * MathUtils.sin(time * 8f);
                break;
            case TELEGRAPH: // treme e fica vermelho: aviso do ataque
                drawX += MathUtils.sin(time * 60f) * 4f;
                green = 0.45f;
                blue = 0.45f;
                break;
            case CHARGE:
                rotation = movingRight ? -8f : 8f;
                green = 0.6f;
                blue = 0.6f;
                break;
            case STUNNED: // pisca, atordoado
                alpha = (((int) (time * 12f)) % 2 == 0) ? 0.4f : 1f;
                rotation = 8f * MathUtils.sin(time * 10f);
                break;
            case DYING: // encolhe e desaparece
                float t = stateTimer / DYING_TIME;
                alpha = t;
                scale = 0.6f + 0.4f * t;
                rotation = (1f - t) * 30f;
                break;
            default:
                break;
        }

        // em fúria fica avermelhado
        if (isEnraged() && state != State.STUNNED && state != State.DYING) {
            green *= 0.75f;
            blue *= 0.75f;
        }

        batch.setColor(1f, green, blue, alpha);
        batch.draw(region,
            drawX, y + bob,
            WIDTH / 2f, 0f,      // roda e encolhe a partir da base
            WIDTH, HEIGHT,
            scale, scale,
            rotation);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
