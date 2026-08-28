package com.sidney.runnext.screens;

import static java.awt.Color.blue;
import static java.awt.Color.getColor;
import static java.awt.Color.red;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sidney.runnext.entities.player;

import java.awt.Color;

public class GameScreen extends ScreenAdapter {
    private SpriteBatch spritebatch;
    private player jogador;

    public GameScreen() {
        spritebatch = new SpriteBatch();
        jogador = new player();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f,  0.2f,   1 );
        jogador.andarParaEsquerda(delta);
        spritebatch.begin();
        jogador.render(spritebatch);
        spritebatch.end();
    }
}



