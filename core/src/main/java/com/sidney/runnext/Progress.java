package com.sidney.runnext;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Progress.java
 *
 * Guarda no telemóvel qual é o nível mais alto desbloqueado.
 * Nível 1 está sempre aberto; o 2 abre ao completar o 1; o 3 abre ao completar o 2.
 * Fica guardado mesmo depois de fechar o jogo.
 */
public final class Progress {

    public static final int TOTAL_LEVELS = 3;

    private static final String PREFS_NAME = "runnext_progress";
    private static final String KEY_MAX_LEVEL = "maxLevelUnlocked";

    private Progress() {}

    private static Preferences prefs() {
        return Gdx.app.getPreferences(PREFS_NAME);
    }

    /** Maior nível desbloqueado (1 a TOTAL_LEVELS). */
    public static int getMaxUnlocked() {
        int saved = prefs().getInteger(KEY_MAX_LEVEL, 1);
        return Math.max(1, Math.min(saved, TOTAL_LEVELS));
    }

    public static boolean isUnlocked(int level) {
        return level <= getMaxUnlocked();
    }

    /** Desbloqueia o nível indicado (só se ainda não estiver desbloqueado). */
    public static void unlock(int level) {
        int target = Math.min(level, TOTAL_LEVELS);
        if (target > getMaxUnlocked()) {
            Preferences p = prefs();
            p.putInteger(KEY_MAX_LEVEL, target);
            p.flush();
        }
    }

    private static String bestKey(int level) {
        return "bestScoreLevel" + level;
    }

    /** Melhor pontuação guardada para esse nível (0 se ainda não houver). */
    public static int getBestScore(int level) {
        return prefs().getInteger(bestKey(level), 0);
    }

    /** Guarda a pontuação se for a melhor desse nível. Devolve true se for um novo recorde. */
    public static boolean submitScore(int level, int score) {
        if (score > getBestScore(level)) {
            Preferences p = prefs();
            p.putInteger(bestKey(level), score);
            p.flush();
            return true;
        }
        return false;
    }

    /** Volta a bloquear tudo menos o nível 1 (útil para testes). */
    public static void reset() {
        Preferences p = prefs();
        p.putInteger(KEY_MAX_LEVEL, 1);
        p.flush();
    }
}
