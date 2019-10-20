package com.escapefromhades.Entities;

import com.badlogic.gdx.Gdx;
import com.escapefromhades.GameStates.GameField;
import com.escapefromhades.RenderEngine.Renderer;

import java.io.BufferedReader;
import java.io.IOException;

public class Trap {
    private GameField parentField;
    private static int[] trapDamage, trapProjectile;
    private static float[] trapCycleLength, trapWarningStart, trapActiveStart;
    private float trapX, trapY, trapCycleTime = 0, trapCycleStart = 0;
    private int trapType, trapDirection = 0;
    private boolean soundPlayed = false, projectileLaunched = false, cycleReset = true;

    private static int[] trapOffsetX, trapOffsetY;
    private static boolean[] trapLow, trapRanged;

    private static float[] trapWidthLeft, trapWidthRight, trapHeightDown, trapHeightUp;

    public Trap(GameField parentField, int trapType) {
        this.parentField = parentField;
        this.trapType = trapType;
    }

    public static void prepareTrapValueArrays(int trapTypesNum) {
        trapDamage = new int[trapTypesNum];
        trapProjectile = new int[trapTypesNum];
        trapCycleLength = new float[trapTypesNum];
        trapWarningStart = new float[trapTypesNum];
        trapActiveStart = new float[trapTypesNum];
        trapOffsetX = new int[trapTypesNum];
        trapOffsetY = new int[trapTypesNum];
        trapLow = new boolean[trapTypesNum];
        trapRanged = new boolean[trapTypesNum];
        trapWidthLeft = new float[trapTypesNum];
        trapWidthRight = new float[trapTypesNum];
        trapHeightDown = new float[trapTypesNum];
        trapHeightUp = new float[trapTypesNum];
    }

    public static void loadTrapValues(final String trapStatFile, BufferedReader reader, int i) {
        try {
            String line;
            String[] currentLine;
            line = reader.readLine();
            trapDamage[i] = Integer.valueOf(line);
            line = reader.readLine();
            currentLine = line.split(" ");
            trapCycleLength[i] = Float.valueOf(currentLine[0]);
            trapWarningStart[i] = Float.valueOf(currentLine[1]);
            trapActiveStart[i] = Float.valueOf(currentLine[2]);
            line = reader.readLine();
            currentLine = line.split(" ");
            trapOffsetX[i] = Integer.valueOf(currentLine[0]);
            trapOffsetY[i] = Integer.valueOf(currentLine[1]);
            line = reader.readLine();
            trapLow[i] = line.equals("low");
            line = reader.readLine();
            trapRanged[i] = line.equals("ranged");
            line = reader.readLine();
            trapProjectile[i] = Integer.valueOf(line);
            line = reader.readLine();
            currentLine = line.split(" ");
            trapWidthLeft[i] = Float.valueOf(currentLine[0]);
            trapWidthRight[i] = Float.valueOf(currentLine[1]);
            trapHeightUp[i] = Float.valueOf(currentLine[2]);
            trapHeightDown[i] = Float.valueOf(currentLine[3]);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + trapStatFile);
        }
    }

    public void setX(float x) {
        trapX = x;
    }

    public void setY(float y) {
        trapY = y;
    }

    public void setCycleStart(float start, float elapsedTime) {
        trapCycleTime = start;
        trapCycleStart = elapsedTime - start;
    }

    public void setDirection(int direction) {
        trapDirection = direction;
    }

    public int getDamage() {
        if(!trapRanged[trapType])
            return trapDamage[trapType];
        else
            return 0;
    }

    public boolean isTrapLow() {
        return trapLow[trapType];
    }

    public boolean checkCollision(float x, float y) {
        return trapX - x <= trapWidthLeft[trapType] && x - trapX <= trapWidthRight[trapType] &&
                trapY - y <= trapHeightDown[trapType] && y - trapY <= trapHeightUp[trapType];
    }

    public boolean isDangerous() {
        return trapCycleTime >= trapActiveStart[trapType];
    }

    public void renderTrap(Renderer renderer) {
        int tileX = parentField.tileX, tileY = parentField.tileY, pixelSize = GlobalValues.pixelSize;
        if(trapCycleTime >= trapActiveStart[trapType]) {
            if(!soundPlayed && !parentField.spriteStorage.trapSoundIsPlaying[trapType]) {
                soundPlayed = true;
                parentField.spriteStorage.trapSoundIsPlaying[trapType] = true;
                parentField.spriteStorage.trapSounds[trapType].play(parentField.getSoundVolume());
            }
            if(trapDirection < 3) {
                renderer.addAnimationToSchedule(parentField.spriteStorage.trapActive[trapType][trapDirection],
                        (int)(trapX * tileX - trapOffsetX[trapType]) * pixelSize, (int)(trapY * tileY - trapOffsetY[trapType]) * pixelSize, pixelSize,
                        false, 0, trapCycleStart + trapActiveStart[trapType], false);
            } else {
                renderer.addAnimationToSchedule(parentField.spriteStorage.trapActive[trapType][1],
                        (int)(trapX * tileX - trapOffsetX[trapType]) * pixelSize, (int)(trapY * tileY - trapOffsetY[trapType]) * pixelSize, pixelSize,
                        true, 0, trapCycleStart + trapActiveStart[trapType], false);
            }
        } else if(trapCycleTime >= trapWarningStart[trapType]) {
            soundPlayed = false;
            if(trapDirection < 3) {
                renderer.addAnimationToSchedule(parentField.spriteStorage.trapWarning[trapType][trapDirection],
                        (int) (trapX * tileX - trapOffsetX[trapType]) * pixelSize, (int) (trapY * tileY - trapOffsetY[trapType]) * pixelSize, pixelSize,
                        false, 0, trapCycleStart + trapWarningStart[trapType], false);
            } else {
                renderer.addAnimationToSchedule(parentField.spriteStorage.trapWarning[trapType][1],
                        (int) (trapX * tileX - trapOffsetX[trapType]) * pixelSize, (int) (trapY * tileY - trapOffsetY[trapType]) * pixelSize, pixelSize,
                        true, 0, trapCycleStart + trapWarningStart[trapType], false);
            }
        } else {
            if(trapDirection < 3) {
                renderer.addSpriteToSchedule(parentField.spriteStorage.trapIdle[trapType][trapDirection],
                        (int) (trapX * tileX - trapOffsetX[trapType]) * pixelSize,
                        (int) (trapY * tileY - trapOffsetY[trapType]) * pixelSize, pixelSize, 0, false);
            } else {
                renderer.addSpriteToSchedule(parentField.spriteStorage.trapIdle[trapType][1],
                        (int) (trapX * tileX - trapOffsetX[trapType]) * pixelSize,
                        (int) (trapY * tileY - trapOffsetY[trapType]) * pixelSize, pixelSize, 0, true);
            }
        }
    }

    public void processTrapMovement() {
        trapCycleTime = (trapCycleTime + Gdx.graphics.getDeltaTime()) % trapCycleLength[trapType];
        if(trapCycleTime >= trapActiveStart[trapType]) {
            if(trapRanged[trapType] && !projectileLaunched) {
                projectileLaunched = true;
                launchProjectile();
            }
        } else {
            projectileLaunched = false;
        }
        if(trapCycleTime < trapActiveStart[trapType]) {
            if(!cycleReset) {
                cycleReset = true;
                trapCycleStart += trapCycleLength[trapType];
            }
        } else {
            cycleReset = false;
        }
    }

    private void launchProjectile() {
        switch(trapDirection) {
            case 0:
                parentField.createEnemyProjectile(trapProjectile[trapType], trapDirection, trapX + 0.5f, trapY - trapHeightDown[trapType]);
                break;
            case 1:
                parentField.createEnemyProjectile(trapProjectile[trapType], trapDirection, trapX + 1f + trapWidthRight[trapType], trapY + 0.5f);
                break;
            case 2:
                parentField.createEnemyProjectile(trapProjectile[trapType], trapDirection, trapX + 0.5f, trapY + 1f + trapHeightUp[trapType]);
                break;
            case 3:
                parentField.createEnemyProjectile(trapProjectile[trapType], trapDirection, trapX - trapWidthLeft[trapType], trapY + 0.5f);
                break;
        }
        projectileLaunched = true;
    }
}
