package com.escapefromhades.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.escapefromhades.EscapeFromHades;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
		config.fullscreen = true;
		config.forceExit = false;
		new LwjglApplication(new EscapeFromHades(), config);
		Gdx.input.setCursorCatched(true);
	}
}
