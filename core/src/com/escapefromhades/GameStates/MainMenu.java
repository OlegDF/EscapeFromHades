package com.escapefromhades.GameStates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.*;
import com.escapefromhades.Entities.GlobalValues;
import com.escapefromhades.RenderEngine.MusicStorage;
import com.escapefromhades.RenderEngine.Renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainMenu {
    private MusicStorage musicStorage;
    private Renderer renderer;

    private TextureAtlas menuAtlas;
    private Sprite soundScale, soundArrow, soundSlider;
    private Animation<TextureRegion> startPortal, finishPortal, settingsIcon, runner, selectionCircle;
    private int width, height, selectionCircleWidth = 32, selectionCircleHeight = 32, iconWidth = 16, iconHeight = 16;

    private Sprite[] stagescreen;
    private int stagescreenWidth = 128, stagescreenHeight = 96;

    private boolean settingsMenuIsOn = false, scoresMenuIsOn = false, stageChoiceMenuIsOn = false;
    private int chosenOption = 0, chosenStage = 0;

    public boolean gameShouldBeStarted = false;
    public int nextGameStage = 0;

    private int titlescreenFrames = 21, titlescreenWidth = 256, titlescreenHeight = 192;
    private float titlescreenTime = 2.1f, titlescreenTimeRemaining;
    private Animation<TextureRegion> titlescreen;
    private TextureAtlas titleAtlas;

    private String[] menuLines, stageNames;

    public MainMenu(SpriteBatch batch, MusicStorage musicStorage) {
        GlobalValues.pixelSize = Gdx.graphics.getHeight() / titlescreenHeight;
        readGameSettings();
        if(!GlobalValues.highScoresUpdated) {
            readHighScores();
        } else {
            writeHighScores();
        }
        renderer = new Renderer(batch);
        this.musicStorage = musicStorage;
        menuAtlas = renderer.addAtlas(new TextureAtlas("atlases/main_menu.txt"));
        width = Gdx.graphics.getWidth() / GlobalValues.pixelSize;
        height = Gdx.graphics.getHeight() / GlobalValues.pixelSize;
        readMenuText();
        if(GlobalValues.titlescreenDone) {
            titlescreenTimeRemaining = 0;
        } else {
            loadTitlescreen();
            titlescreenTimeRemaining = titlescreenTime;
        }
        loadSprites();
        musicStorage.startMenuMusic();
    }

    private void loadSprites() {
        soundScale = renderer.addSprite(menuAtlas, "sound_scale");
        soundArrow = renderer.addSprite(menuAtlas, "sound_arrow");
        soundSlider = renderer.addSprite(menuAtlas, "sound_slider");
        startPortal = renderer.addAnimation(menuAtlas, "startportal", 0.075f, 8, Animation.PlayMode.LOOP);
        finishPortal = renderer.addAnimation(menuAtlas, "finishportal", 0.075f, 8, Animation.PlayMode.LOOP);
        runner = renderer.addAnimation(menuAtlas, "messenger_walk_south", 0.0375f, 8, Animation.PlayMode.LOOP);
        settingsIcon = renderer.addAnimation(menuAtlas, "settings_icon", 0.075f, 3, Animation.PlayMode.LOOP);
        selectionCircle = renderer.addAnimation(menuAtlas, "selection_circle", 0.075f, 6, Animation.PlayMode.LOOP);
        stagescreen = new Sprite[GlobalValues.stagesNum];
        for(int i = 0; i < GlobalValues.stagesNum; i++) {
            stagescreen[i] = renderer.addSprite(menuAtlas, "stagescreen" + i);
        }
    }

    private void loadTitlescreen() {
        titleAtlas = renderer.addAtlas(new TextureAtlas("atlases/titleatlas.txt"));
        titlescreen = renderer.addAnimation(titleAtlas, "titlescreen", titlescreenTime / titlescreenFrames,
                titlescreenFrames, Animation.PlayMode.NORMAL);
        titlescreenTimeRemaining = titlescreenTime;
    }

    private void disposeOfTitlescreen() {
        titleAtlas.dispose();
        titleAtlas = null;
    }

    private void readMenuText() {
        final String menuFile = "texts/" + GlobalValues.languageFolder[GlobalValues.language] + "/main_menu.txt";
        try {
            Class cls = Class.forName("com.escapefromhades.GameStates.MainMenu");
            ClassLoader cLoader = cls.getClassLoader();
            InputStream in = cLoader.getResourceAsStream(menuFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            try {
                line = reader.readLine();
                menuLines = new String[Integer.valueOf(line)];
                for(int i = 0; i < menuLines.length; i++) {
                    line = reader.readLine();
                    menuLines[i] = line;
                }
                stageNames = new String[GlobalValues.stagesNum];
                for(int i = 0; i < GlobalValues.stagesNum; i++) {
                    line = reader.readLine();
                    stageNames[i] = line;
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + menuFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class MainMenu not found while reading this: " + menuFile);
        }
    }

    public void render(float elapsedTime) {
        if(!settingsMenuIsOn && !scoresMenuIsOn && !stageChoiceMenuIsOn) {
            renderInitialMenu();
        } else if(!scoresMenuIsOn && !stageChoiceMenuIsOn) {
            renderSettingsMenu();
        } else if(!stageChoiceMenuIsOn) {
            renderScoreMenu();
        } else {
            renderStageChoice();
        }
        if(titlescreenTimeRemaining > 0) {
            titlescreenTimeRemaining -= Gdx.graphics.getDeltaTime();
            renderTitlescreen();
        } else {
            GlobalValues.titlescreenDone = true;
            if (titleAtlas != null) {
                disposeOfTitlescreen();
            }
            if(!settingsMenuIsOn && !scoresMenuIsOn && !stageChoiceMenuIsOn) {
                processInitialMenuChange();
            } else if(!scoresMenuIsOn && !stageChoiceMenuIsOn) {
                processSettingsMenuChange();
            } else if(!stageChoiceMenuIsOn) {
                processScoreMenuChange();
            } else {
                processStageChoiceChange();
            }
        }
        renderer.render(elapsedTime, (titlescreenWidth - width) / 2 * GlobalValues.pixelSize,
                (titlescreenHeight - height) / 2 * GlobalValues.pixelSize);
    }

    private void renderTitlescreen() {
        renderer.addAnimationToSchedule(titlescreen, 0, 0, GlobalValues.pixelSize, false, 0, 0, false);
    }

    private void renderLineWithIcons(String line, Sprite icon, BitmapFont font, float x, float y) {
        renderer.addCenteredTextToSchedule(font, x, y, line, 1, 1, 1, 1);
        final GlyphLayout layout = new GlyphLayout(font, line);
        renderer.addSpriteToSchedule(icon,
                x - (iconWidth / 2 + selectionCircleWidth / 2) * GlobalValues.pixelSize - layout.width / 2,
                y - iconHeight / 2 * GlobalValues.pixelSize,
                GlobalValues.pixelSize, 0, false);
        renderer.addSpriteToSchedule(icon,
                x + (selectionCircleWidth / 2 - iconWidth / 2) * GlobalValues.pixelSize + layout.width / 2,
                y - iconHeight / 2 * GlobalValues.pixelSize,
                GlobalValues.pixelSize, 0, true);
    }

    private void renderSelectedLineWithIcons(String line, Sprite icon, BitmapFont font, float x, float y) {
        renderLineWithIcons(line, icon, font, x, y);
        final GlyphLayout layout = new GlyphLayout(font, line);
        renderer.addAnimationToSchedule(selectionCircle,
                x - selectionCircleWidth * GlobalValues.pixelSize - layout.width / 2,
                y - selectionCircleHeight / 2 * GlobalValues.pixelSize,
                GlobalValues.pixelSize, false, 0, 0, false);
        renderer.addAnimationToSchedule(selectionCircle,
                x + layout.width / 2,
                y - selectionCircleHeight / 2 * GlobalValues.pixelSize,
                GlobalValues.pixelSize, false, 0, 0, false);
    }

    private void renderLineWithAnimatedIcons(String line, Animation<TextureRegion> icon, BitmapFont font, float x, float y) {
        renderer.addCenteredTextToSchedule(font, x, y, line, 1, 1, 1, 1);
        final GlyphLayout layout = new GlyphLayout(font, line);
        renderer.addAnimationToSchedule(icon,
                x - (iconWidth / 2 + selectionCircleWidth / 2) * GlobalValues.pixelSize - layout.width / 2,
                y - iconHeight / 2 * GlobalValues.pixelSize,
                GlobalValues.pixelSize, false, 0, 0, false);
        renderer.addAnimationToSchedule(icon,
                x + (selectionCircleWidth / 2 - iconWidth / 2) * GlobalValues.pixelSize + layout.width / 2,
                y - iconHeight / 2 * GlobalValues.pixelSize,
                GlobalValues.pixelSize, false, 0, 0, true);
    }

    private void renderSelectedLineWithAnimatedIcons(String line, Animation<TextureRegion> icon, BitmapFont font, float x, float y) {
        renderLineWithAnimatedIcons(line, icon, font, x, y);
        final GlyphLayout layout = new GlyphLayout(font, line);
        renderer.addAnimationToSchedule(selectionCircle,
                x - selectionCircleWidth * GlobalValues.pixelSize - layout.width / 2,
                y - selectionCircleHeight / 2 * GlobalValues.pixelSize,
                GlobalValues.pixelSize, false, 0, 0, false);
        renderer.addAnimationToSchedule(selectionCircle,
                x + layout.width / 2,
                y - selectionCircleHeight / 2 * GlobalValues.pixelSize,
                GlobalValues.pixelSize, false, 0, 0, false);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void renderInitialMenu() {
        Animation[] icons = {finishPortal, settingsIcon, runner, startPortal};
        for(int i = 0; i <= 3; i++) {
            if(i == chosenOption) {
                renderSelectedLineWithAnimatedIcons(menuLines[i], icons[i], GlobalValues.fontLarge,
                        (titlescreenWidth / 2) * GlobalValues.pixelSize,
                        (titlescreenHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * (i - 1.5f));
            } else {
                renderLineWithAnimatedIcons(menuLines[i], icons[i], GlobalValues.fontLarge,
                        (titlescreenWidth / 2) * GlobalValues.pixelSize,
                        (titlescreenHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * (i - 1.5f));
            }
        }
    }

    private void processInitialMenuChange() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && chosenOption > 0) {
            chosenOption--;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && chosenOption < 3) {
            chosenOption++;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            switch (chosenOption) {
                case 0:
                    stageChoiceMenuIsOn = true;
                    chosenOption = 0;
                    chosenStage = 0;
                    break;
                case 1:
                    settingsMenuIsOn = true;
                    chosenOption = 0;
                    break;
                case 2:
                    scoresMenuIsOn = true;
                    chosenOption = 0;
                    chosenStage = 0;
                    break;
                case 3:
                    writeGameSettings();
                    Gdx.app.exit();
            }
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    //the menu used for changing sound volume and language
    private void renderSettingsMenu() {
        final int sliderWidth = 16, sliderHeight = 16, sliderSectionsNum = 12;
        for(int i = 0; i < 2; i++) {
            renderer.addCenteredTextToSchedule(GlobalValues.fontLarge,
                    (titlescreenWidth / 2) * GlobalValues.pixelSize,
                    (titlescreenHeight / 2 - sliderHeight * (i - 1.5f)) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * (i - 2f), menuLines[4 + i],
                    1, 1, 1, 1);
            for(int x = 0; x < sliderSectionsNum; x++) {
                renderer.addSpriteToSchedule(soundScale,
                        (titlescreenWidth / 2 + sliderWidth * (x - (float)sliderSectionsNum / 2)) * GlobalValues.pixelSize,
                        (titlescreenHeight / 2 - sliderHeight * (i - 1f)) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * (i - 1.5f), GlobalValues.pixelSize, 0, false);
            }
            renderer.addSpriteToSchedule(soundArrow,
                    (titlescreenWidth / 2 + sliderWidth * (-(float)sliderSectionsNum / 2 - 1)) * GlobalValues.pixelSize,
                    (titlescreenHeight / 2 - sliderHeight * (i - 1f)) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * (i - 1.5f), GlobalValues.pixelSize, 0, false);
            renderer.addSpriteToSchedule(soundArrow,
                    (titlescreenWidth / 2 + sliderWidth * ((float)sliderSectionsNum / 2)) * GlobalValues.pixelSize,
                    (titlescreenHeight / 2 - sliderHeight * (i - 1f)) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * (i - 1.5f), GlobalValues.pixelSize, 0, true);
        }
        renderer.addSpriteToSchedule(soundSlider,
                (titlescreenWidth / 2 + sliderWidth * ((sliderSectionsNum - 1) * (-0.5f + GlobalValues.musicVolume) - 0.5f)) * GlobalValues.pixelSize,
                (titlescreenHeight / 2 - sliderHeight * (-1f)) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * (-1.5f), GlobalValues.pixelSize, 0, false);
        renderer.addSpriteToSchedule(soundSlider,
                (titlescreenWidth / 2 + sliderWidth * ((sliderSectionsNum - 1) * (-0.5f + GlobalValues.soundVolume) - 0.5f)) * GlobalValues.pixelSize,
                (titlescreenHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * (-0.5f), GlobalValues.pixelSize, 0, false);
        renderer.addCenteredTextToSchedule(GlobalValues.fontLarge,
                (titlescreenWidth / 2) * GlobalValues.pixelSize,
                (titlescreenHeight / 2 - sliderHeight * (0.5f)) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 0.5f, menuLines[6],
                1, 1, 1, 1);
        GlyphLayout layout = new GlyphLayout(GlobalValues.fontLarge, menuLines[6]);
        renderer.addSpriteToSchedule(soundArrow,
                (titlescreenWidth / 2 - sliderWidth / 2 - selectionCircleWidth / 2) * GlobalValues.pixelSize - layout.width / 2,
                (titlescreenHeight / 2 - sliderHeight) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 0.5f, GlobalValues.pixelSize, 0, false);
        renderer.addSpriteToSchedule(soundArrow,
                (titlescreenWidth / 2 - sliderWidth / 2 + selectionCircleWidth / 2) * GlobalValues.pixelSize + layout.width / 2,
                (titlescreenHeight / 2 - sliderHeight) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 0.5f, GlobalValues.pixelSize, 0, true);
        renderer.addCenteredTextToSchedule(GlobalValues.fontLarge,
                (titlescreenWidth / 2) * GlobalValues.pixelSize,
                (titlescreenHeight / 2 - sliderHeight * (0.5f)) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 2f, menuLines[7],
                1, 1, 1, 1);
        layout = new GlyphLayout(GlobalValues.fontLarge, menuLines[7]);
        renderer.addAnimationToSchedule(startPortal,
                (titlescreenWidth / 2 - iconWidth / 2 - selectionCircleWidth / 2) * GlobalValues.pixelSize - layout.width / 2,
                (titlescreenHeight / 2 - sliderHeight * (0.5f) - iconHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 2f,
                GlobalValues.pixelSize, false, 0, 0, false);
        renderer.addAnimationToSchedule(startPortal,
                (titlescreenWidth / 2 - iconWidth / 2 + selectionCircleWidth / 2) * GlobalValues.pixelSize + layout.width / 2,
                (titlescreenHeight / 2 - sliderHeight * (0.5f) - iconHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 2f,
                GlobalValues.pixelSize, false, 0, 0, false);
        switch(chosenOption) {
            case 0:
            case 1:
                renderer.addAnimationToSchedule(selectionCircle,
                        (titlescreenWidth / 2 + sliderWidth * (-(float)sliderSectionsNum / 2 - 0.5f) - selectionCircleWidth / 2) * GlobalValues.pixelSize,
                        (titlescreenHeight / 2 - sliderHeight * (chosenOption - 1.5f) - selectionCircleHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * (chosenOption - 1.5f),
                        GlobalValues.pixelSize, false, 0, 0, false);
                renderer.addAnimationToSchedule(selectionCircle,
                        (titlescreenWidth / 2 + sliderWidth * ((float)sliderSectionsNum / 2 + 0.5f) - selectionCircleWidth / 2) * GlobalValues.pixelSize,
                        (titlescreenHeight / 2 - sliderHeight * (chosenOption - 1.5f) - selectionCircleHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * (chosenOption - 1.5f),
                        GlobalValues.pixelSize, false, 0, 0, false);
                break;
            case 2:
                layout = new GlyphLayout(GlobalValues.fontLarge, menuLines[6]);
                renderer.addAnimationToSchedule(selectionCircle,
                        (titlescreenWidth / 2 - selectionCircleWidth) * GlobalValues.pixelSize - layout.width / 2,
                        (titlescreenHeight / 2 - sliderHeight / 2 - selectionCircleHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 0.5f,
                        GlobalValues.pixelSize, false, 0, 0, false);
                renderer.addAnimationToSchedule(selectionCircle,
                        (titlescreenWidth / 2) * GlobalValues.pixelSize + layout.width / 2,
                        (titlescreenHeight / 2 - sliderHeight / 2 - selectionCircleHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 0.5f,
                        GlobalValues.pixelSize, false, 0, 0, false);
                break;
            case 3:
                layout = new GlyphLayout(GlobalValues.fontLarge, menuLines[7]);
                renderer.addAnimationToSchedule(selectionCircle,
                        (titlescreenWidth / 2 - selectionCircleWidth) * GlobalValues.pixelSize - layout.width / 2,
                        (titlescreenHeight / 2 - sliderHeight * (0.5f) - selectionCircleHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 2f,
                        GlobalValues.pixelSize, false, 0, 0, false);
                renderer.addAnimationToSchedule(selectionCircle,
                        (titlescreenWidth / 2) * GlobalValues.pixelSize + layout.width / 2,
                        (titlescreenHeight / 2 - sliderHeight * (0.5f) - selectionCircleHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 2f,
                        GlobalValues.pixelSize, false, 0, 0, false);
                break;
        }
    }

    private void processSettingsMenuChange() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && chosenOption > 0) {
            chosenOption--;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && chosenOption < 3) {
            chosenOption++;
        }
        final float volumeChangePerSecond = 0.5f;
        switch (chosenOption) {
            case 0:
                if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && GlobalValues.musicVolume > 0) {
                    GlobalValues.musicVolume = Math.max(GlobalValues.musicVolume - Gdx.graphics.getDeltaTime() * volumeChangePerSecond, 0);
                }
                if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && GlobalValues.musicVolume < 1) {
                    GlobalValues.musicVolume = Math.min(GlobalValues.musicVolume + Gdx.graphics.getDeltaTime() * volumeChangePerSecond, 1);
                }
                break;
            case 1:
                if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && GlobalValues.soundVolume > 0) {
                    GlobalValues.soundVolume = Math.max(GlobalValues.soundVolume - Gdx.graphics.getDeltaTime() * volumeChangePerSecond, 0);
                }
                if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && GlobalValues.soundVolume < 1) {
                    GlobalValues.soundVolume = Math.min(GlobalValues.soundVolume + Gdx.graphics.getDeltaTime() * volumeChangePerSecond, 1);
                }
                break;
            case 2:
                if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && GlobalValues.language > 0) {
                    GlobalValues.language = Math.max(GlobalValues.language - 1, 0);
                    readMenuText();
                }
                if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && GlobalValues.language < GlobalValues.languagesNum - 1) {
                    GlobalValues.language = Math.min(GlobalValues.language + 1, GlobalValues.languagesNum);
                    readMenuText();
                }
                break;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            switch (chosenOption) {
                case 3:
                    settingsMenuIsOn = false;
                    chosenOption = 0;
                    writeGameSettings();
                    break;
            }
        }
    }

    private void renderScoreMenu() {
        if(chosenOption == 0) {
            renderSelectedLineWithIcons(menuLines[8] + (chosenStage + 1), soundArrow, GlobalValues.fontLarge,
                    (titlescreenWidth / 2) * GlobalValues.pixelSize,
                    (titlescreenHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * (-(float)GlobalValues.highScoresNum / 2) + GlobalValues.fontLarge.getLineHeight() * 2);
        } else {
            renderLineWithIcons(menuLines[8] + (chosenStage + 1), soundArrow, GlobalValues.fontLarge,
                    (titlescreenWidth / 2) * GlobalValues.pixelSize,
                    (titlescreenHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * (-(float)GlobalValues.highScoresNum / 2) + GlobalValues.fontLarge.getLineHeight() * 2);
        }
        renderer.addCenteredTextToSchedule(GlobalValues.fontLarge, (titlescreenWidth / 2) * GlobalValues.pixelSize,
                (titlescreenHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * (-(float)GlobalValues.highScoresNum / 2) + GlobalValues.fontLarge.getLineHeight(),
                stageNames[chosenStage], 1, 1, 1, 1);
        for(int i = 0; i < GlobalValues.highScores[0].length; i++) {
            renderer.addCenteredTextToSchedule(GlobalValues.fontMid,
                    (titlescreenWidth / 2) * GlobalValues.pixelSize,
                    (titlescreenHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * (i - (float)GlobalValues.highScoresNum / 2),
                    (int)(GlobalValues.highScores[chosenStage][i] / 60) + ":" +
                            (GlobalValues.highScores[chosenStage][i] % 60 < 10?"0":"") +
                            (int)(GlobalValues.highScores[chosenStage][i] % 60) + "." +
                            (int)(GlobalValues.highScores[chosenStage][i] % 1 * 10),
                    1, 1, 1, 1);
        }
        if(chosenOption == 1) {
            renderSelectedLineWithAnimatedIcons(menuLines[7], startPortal, GlobalValues.fontLarge,
                    (titlescreenWidth / 2) * GlobalValues.pixelSize,
                    (titlescreenHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * ((float)GlobalValues.highScoresNum / 2));
        } else {
            renderLineWithAnimatedIcons(menuLines[7], startPortal, GlobalValues.fontLarge,
                    (titlescreenWidth / 2) * GlobalValues.pixelSize,
                    (titlescreenHeight / 2) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * ((float)GlobalValues.highScoresNum / 2));
        }
    }

    private void processScoreMenuChange() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && chosenOption > 0) {
            chosenOption--;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && chosenOption < 1) {
            chosenOption++;
        }
        switch (chosenOption) {
            case 0:
                if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && chosenStage > 0) {
                    chosenStage--;
                }
                if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && chosenStage < GlobalValues.stagesNum - 1) {
                    chosenStage++;
                }
                break;
            case 1:
                if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    scoresMenuIsOn = false;
                    chosenOption = 0;
                }
                break;
        }
    }

    private void renderStageChoice() {
        if(chosenOption == 0) {
            renderSelectedLineWithIcons(menuLines[8] + (chosenStage + 1), soundArrow, GlobalValues.fontLarge,
                    (titlescreenWidth / 2) * GlobalValues.pixelSize,
                    (titlescreenHeight) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight());
        } else {
            renderLineWithIcons(menuLines[8] + (chosenStage + 1), soundArrow, GlobalValues.fontLarge,
                    (titlescreenWidth / 2) * GlobalValues.pixelSize,
                    (titlescreenHeight) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight());
        }
        renderer.addCenteredTextToSchedule(GlobalValues.fontLarge, (titlescreenWidth / 2) * GlobalValues.pixelSize,
                (titlescreenHeight) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * 2,
                stageNames[chosenStage], 1, 1, 1, 1);
        renderer.addSpriteToSchedule(stagescreen[chosenStage],
                (titlescreenWidth / 2 - stagescreenWidth / 2) * GlobalValues.pixelSize,
                (titlescreenHeight - stagescreenHeight) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * 3,
                GlobalValues.pixelSize, 0, false);
        if(chosenOption == 1) {
            renderSelectedLineWithAnimatedIcons(menuLines[7], startPortal, GlobalValues.fontLarge,
                    (titlescreenWidth / 2) * GlobalValues.pixelSize,
                    (titlescreenHeight - stagescreenHeight) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * 4);
        } else {
            renderLineWithAnimatedIcons(menuLines[7], startPortal, GlobalValues.fontLarge,
                    (titlescreenWidth / 2) * GlobalValues.pixelSize,
                    (titlescreenHeight - stagescreenHeight) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * 4);
        }
    }

    private void processStageChoiceChange() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && chosenOption > 0) {
            chosenOption--;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && chosenOption < 1) {
            chosenOption++;
        }
        switch (chosenOption) {
            case 0:
                if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && chosenStage > 0) {
                    chosenStage--;
                }
                if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && chosenStage < GlobalValues.stagesNum - 1) {
                    chosenStage++;
                }
                if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    gameShouldBeStarted = true;
                    nextGameStage = chosenStage;
                }
                break;
            case 1:
                if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    stageChoiceMenuIsOn = false;
                    chosenOption = 0;
                }
                break;
        }
    }

    private void readGameSettings() {
        final String settingsFile = "settings.txt";
        FileHandle file = Gdx.files.local(settingsFile);
        if(file.exists()) {
            BufferedReader reader = new BufferedReader(file.reader());
            String line;
            try {
                line = reader.readLine();
                if(!line.equals("verified")) {
                    setDefaultSettings();
                    reader.close();
                    return;
                }
                line = reader.readLine();
                GlobalValues.musicVolume = Float.valueOf(line);
                line = reader.readLine();
                GlobalValues.soundVolume = Float.valueOf(line);
                line = reader.readLine();
                GlobalValues.language = Integer.valueOf(line);
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + settingsFile);
            }
        } else {
            setDefaultSettings();
        }
    }

    private void readHighScores() {
        final String scoreFile = "highscores.txt";
        FileHandle file = Gdx.files.local(scoreFile);
        if(file.exists()) {
            BufferedReader reader = new BufferedReader(file.reader());
            try {
                final float defaultHighScore = 59999.99f;
                int scoreStagesNum = Integer.valueOf(reader.readLine());
                GlobalValues.highScoresNum = Integer.valueOf(reader.readLine());
                GlobalValues.highScores = new float[GlobalValues.stagesNum][GlobalValues.highScoresNum];
                for(int i = 0; i < GlobalValues.highScores.length; i++) {
                    if(i < scoreStagesNum) {
                        for(int j = 0; j < GlobalValues.highScores[0].length; j++) {
                            GlobalValues.highScores[i][j] = Float.valueOf(reader.readLine());
                        }
                    } else {
                        for(int j = 0; j < GlobalValues.highScores[0].length; j++) {
                            GlobalValues.highScores[i][j] = defaultHighScore;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading the file: " + scoreFile);
            }
        } else {
            setDefaultHighScores();
        }
    }

    private void setDefaultSettings() {
        GlobalValues.musicVolume = 1;
        GlobalValues.soundVolume = 1;
        GlobalValues.language = 0;
    }

    private void setDefaultHighScores() {
        final float defaultHighScore = 59999.99f;
        GlobalValues.highScoresNum = 5;
        GlobalValues.highScores = new float[GlobalValues.stagesNum][GlobalValues.highScoresNum];
        for(int i = 0; i < GlobalValues.highScores.length; i++) {
            for(int j = 0; j < GlobalValues.highScores[0].length; j++) {
                GlobalValues.highScores[i][j] = defaultHighScore;
            }
        }
    }

    private void writeGameSettings() {
        final String settingsFile = "settings.txt";
        FileHandle file = Gdx.files.local(settingsFile);
        file.writeString("verified", false);
        file.writeString("\n" + GlobalValues.musicVolume, true);
        file.writeString("\n" + GlobalValues.soundVolume, true);
        file.writeString("\n" + GlobalValues.language, true);
    }

    private void writeHighScores() {
        final String settingsFile = "highscores.txt";
        FileHandle file = Gdx.files.local(settingsFile);
        file.writeString("" + GlobalValues.stagesNum, false);
        file.writeString("\n" + GlobalValues.highScoresNum, true);
        for(int i = 0; i < GlobalValues.highScores.length; i++) {
            for(int j = 0; j < GlobalValues.highScores[i].length; j++) {
                file.writeString("\n" + GlobalValues.highScores[i][j], true);
            }
        }
    }

    public void clear() {
        renderer.clear();
    }
}
