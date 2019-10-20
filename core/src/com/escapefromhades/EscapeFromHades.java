package com.escapefromhades;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.escapefromhades.Entities.GlobalValues;
import com.escapefromhades.GameStates.GameField;
import com.escapefromhades.GameStates.MainMenu;
import com.escapefromhades.RenderEngine.MusicStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class EscapeFromHades extends ApplicationAdapter {
	private SpriteBatch batch;
	private MusicStorage musicStorage;
	private float elapsedTime;
	MainMenu menu;
	GameField gameField;
	private boolean gameIsOn;

	@Override
	public void create () {
		batch = new SpriteBatch();
		musicStorage = new MusicStorage();
		readStages();
		gameIsOn = false;
		menu = new MainMenu(batch, musicStorage);
		loadFont();
	}

	@Override
	public void render () {
		elapsedTime += Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            makeScreenshot();
        }
        musicStorage.setVolume();
		Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		if(gameIsOn) {
			gameField.render(elapsedTime);
			if(gameField.gameShouldBeClosed) {
				switchGameState();
			}
		} else {
			menu.render(elapsedTime);
			if(menu.gameShouldBeStarted) {
				switchGameState();
			}
		}
		batch.end();
	}

    private void makeScreenshot() {
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
        for(int i = 4; i < pixels.length; i += 4) {
            pixels[i - 1] = (byte) 255;
        }
        Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
        int counter = 0;
        FileHandle fh = new FileHandle(Gdx.files.getLocalStoragePath() + "screenshot" + counter + ".png");
        while(fh.exists()) {
            counter++;
            fh = new FileHandle(Gdx.files.getLocalStoragePath() + "screenshot" + counter + ".png");
        }
        PixmapIO.writePNG(fh, pixmap);
        pixmap.dispose();
    }

    private void switchGameState() {
		gameIsOn = !gameIsOn;
		if(gameIsOn) {
			menu.clear();
			gameField = new GameField(batch, musicStorage, menu.nextGameStage);
		} else {
			gameField.clear();
			menu = new MainMenu(batch, musicStorage);
		}
	}

	private void loadFont() {
		GlobalValues.fontSmall = new BitmapFont(Gdx.files.internal("fonts/mainfont.fnt"));
		GlobalValues.fontSmall.getData().setScale((float)GlobalValues.pixelSize / 2);
		GlobalValues.fontMid = new BitmapFont(Gdx.files.internal("fonts/mainfont.fnt"));
		GlobalValues.fontMid.getData().setScale((float)GlobalValues.pixelSize * 3 / 4);
		GlobalValues.fontLarge = new BitmapFont(Gdx.files.internal("fonts/mainfont.fnt"));
		GlobalValues.fontLarge.getData().setScale((float)GlobalValues.pixelSize);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		if(gameIsOn) {
			gameField.clear();
		} else {
			menu.clear();
		}
	}

	private void readStages() {
		final String stagesFile = "texts/stages_desc.txt";
		try {
			Class cls = Class.forName("com.escapefromhades.EscapeFromHades");
			ClassLoader cLoader = cls.getClassLoader();
			InputStream in = cLoader.getResourceAsStream(stagesFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			String[] currentLine;
			try {
				line = reader.readLine();
				GlobalValues.stagesNum = Integer.valueOf(line);
				GlobalValues.lastMap = new int[GlobalValues.stagesNum];
				line = reader.readLine();
				currentLine = line.split(" ");
				for(int i = 0; i < GlobalValues.stagesNum; i++) {
					GlobalValues.lastMap[i] = Integer.valueOf(currentLine[i]);
				}
				reader.close();
			} catch (IOException e) {
				System.err.println("Error reading the file: " + stagesFile);
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Class EscapeFromHades not found while reading this: " + stagesFile);
		}
	}
}
