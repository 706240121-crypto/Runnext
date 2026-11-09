package com.sidney.runnext;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Settings.java
 *
 * Opções do jogador (menu OPÇÕES): música ligada/desligada e efeitos sonoros ligados/desligados.
 * Ficam guardadas no telemóvel, por isso mantêm-se depois de fechar o jogo.
 */
public final class Settings {

    private static final String PREFS_NAME = "runnext_settings";
    private static final String KEY_MUSIC = "musicOn";
    private static final String KEY_SFX = "sfxOn";

    private Settings() {}

    private static Preferences prefs() {
        return Gdx.app.getPreferences(PREFS_NAME);
    }

    public static boolean isMusicOn() {
        return prefs().getBoolean(KEY_MUSIC, true);
    }

    public static void setMusicOn(boolean on) {
        Preferences p = prefs();
        p.putBoolean(KEY_MUSIC, on);
        p.flush();
    }

    public static boolean isSfxOn() {
        return prefs().getBoolean(KEY_SFX, true);
    }

    public static void setSfxOn(boolean on) {
        Preferences p = prefs();
        p.putBoolean(KEY_SFX, on);
        p.flush();
    }
}
