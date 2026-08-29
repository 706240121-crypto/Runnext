package com.sidney.runnext;

import com.badlogic.gdx.Game;
import com.sidney.runnext.screens.MenuScreen;

public class Runnext extends Game {

    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
