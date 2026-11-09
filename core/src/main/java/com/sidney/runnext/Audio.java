package com.sidney.runnext;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

/**
 * Audio.java
 *
 * Gere a MÚSICA de fundo (só toca uma de cada vez). É estática, por isso a música
 * continua a tocar quando se muda de ecrã, e só muda se o ecrã novo pedir outra.
 * Respeita a opção "Música" do menu OPÇÕES (Settings).
 *
 * Se o ficheiro de música não existir, o jogo NÃO fecha: avisa no Logcat e fica sem música.
 */
public final class Audio {

    private static final float MUSIC_VOLUME = 0.5f;

    private static Music current = null;
    private static String currentPath = null;

    private Audio() {}

    /**
     * Toca a música do ficheiro 'path'.
     * Se já for essa a que está a tocar (e em ciclo), deixa-a continuar sem reiniciar.
     */
    public static void playMusic(String path, boolean loop) {
        if (loop && current != null && path.equals(currentPath)) {
            if (Settings.isMusicOn() && !current.isPlaying()) current.play();
            return;
        }

        stopMusic();

        FileHandle file = findMusicFile(path);
        if (file == null) {
            Gdx.app.error("Audio", "MÚSICA EM FALTA: " + path
                + " (confirme que a pasta e o ficheiro estão em minúsculas: assets/audio/music/)");
            return;
        }

        try {
            Music music = Gdx.audio.newMusic(file);
            music.setLooping(loop);
            music.setVolume(MUSIC_VOLUME);
            current = music;
            currentPath = path;
            if (Settings.isMusicOn()) music.play();
            Gdx.app.log("Audio", "A tocar: " + file.path()
                + (Settings.isMusicOn() ? "" : " (música DESLIGADA nas Opções)"));
        } catch (Exception e) {
            // um ficheiro estragado não pode fechar o jogo
            Gdx.app.error("Audio", "Não consegui tocar " + path + ": " + e);
            current = null;
            currentPath = null;
        }
    }

    /**
     * Procura o ficheiro de música. No Android as maiúsculas/minúsculas das pastas contam
     * ("Music" não é "music"), por isso, se não estiver onde se espera, tenta algumas variações.
     */
    private static FileHandle findMusicFile(String path) {
        String[] candidatos = {
            path,
            path.replace("audio/music/", "audio/Music/"),
            path.replace("audio/music/", "Music/"),
            path.replace("audio/music/", "music/"),
            path.replace("audio/music/", "Audio/Music/"),
            path.replace("audio/music/", "Audio/music/")
        };
        for (String c : candidatos) {
            FileHandle f = Gdx.files.internal(c);
            if (f.exists()) {
                if (!c.equals(path)) {
                    Gdx.app.log("Audio", "Música encontrada em '" + c + "' (o esperado era '" + path
                        + "'). Convém renomear a pasta para minúsculas.");
                }
                return f;
            }
        }
        return null;
    }

    public static void stopMusic() {
        if (current != null) {
            current.stop();
            current.dispose();
            current = null;
            currentPath = null;
        }
    }

    /** Chamar depois de mudar a opção "Música": liga ou desliga a que está a tocar. */
    public static void applyMusicSetting() {
        if (current == null) return;
        if (Settings.isMusicOn()) {
            if (!current.isPlaying()) current.play();
        } else {
            current.pause();
        }
    }
}
