package com.sidney.runnext.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

// SoundManager carrega e toca os efeitos sonoros do jogo (ficheiros em assets/audio/sfx).
// Cada ecrã que precise de sons cria uma instância e chama dispose() ao terminar.
public class SoundManager implements Disposable {

    private final Sound jump;
    private final Sound coin;
    private final Sound hurt;
    private final Sound death;

    // Volume dos efeitos (0 a 1). O menu de Opções poderá alterar este valor.
    private float volume = 1f;

    public SoundManager() {
        jump = load("audio/sfx/jump.wav");
        coin = load("audio/sfx/coin.wav");
        hurt = load("audio/sfx/hurt.wav");
        death = load("audio/sfx/death.wav");
    }

    private static Sound load(String path) {
        return Gdx.audio.newSound(Gdx.files.internal(path));
    }

    public void setVolume(float volume) {
        this.volume = MathUtils.clamp(volume, 0f, 1f);
    }

    public float getVolume() {
        return volume;
    }

    public void playJump()  { jump.play(volume); }   // quando a personagem salta
    public void playCoin()  { coin.play(volume); }   // quando apanha uma moeda
    public void playHurt()  { hurt.play(volume); }   // quando sofre dano (ainda por ligar)
    public void playDeath() { death.play(volume); }  // quando morre / Game Over (ainda por ligar)

    @Override
    public void dispose() {
        jump.dispose();
        coin.dispose();
        hurt.dispose();
        death.dispose();
    }
}
