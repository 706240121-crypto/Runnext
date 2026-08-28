package com.sidney.runnext.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class player {
    private Texture texture;
    private float x;
    private float y;
    private float width;

     private float height;
    private float velocidade;

    public player() {
        texture = new Texture("libgdx.png");
            x = 30;
            y = 100;
            width = 100;
            height = 100;
            velocidade = 100;
    }
    public void andarParaEsquerda(float delta) {
        x-= velocidade * delta;
    }
    public void render(SpriteBatch spriteBatch){
        spriteBatch.draw(texture, x, y, width, height);
    }
    public void dispose() {
        texture.dispose();
    }
}
