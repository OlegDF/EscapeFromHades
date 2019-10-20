package com.escapefromhades.Entities;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class GlobalValues {
    public static BitmapFont fontSmall, fontMid, fontLarge;
    public static int pixelSize;
    public static float soundVolume = 1, musicVolume = 1;
    public static boolean titlescreenDone = false;

    public static final int languagesNum = 2;
    public static int language = 1;
    public static final String[] languageFolder = {"en", "ru"};

    public static int[] lastMap;
    public static int highScoresNum = 5, stagesNum;
    public static float[][] highScores;
    public static boolean highScoresUpdated = false;
}
