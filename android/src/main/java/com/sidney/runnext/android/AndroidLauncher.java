package com.sidney.runnext.android;

import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.sidney.runnext.Runnext;
import com.sidney.runnext.ShareHandler;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication implements ShareHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.

        // ✅ ALTERAÇÃO: Passamos "this" (o AndroidLauncher) para o jogo,
        // para que ele possa aceder ao método de partilha.
        initialize(new Runnext(this), configuration);
    }

    /**
     * ✅ NOVO: Implementação do ShareHandler.
     * Abre o chooser nativo do Android para partilhar via WhatsApp, Facebook, Email, etc.
     */
    @Override
    public void shareScore(String titulo, String mensagem) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, titulo);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mensagem);

        // Abre o menu de partilha do Android
        startActivity(Intent.createChooser(shareIntent, "Partilhar via..."));
    }
}
