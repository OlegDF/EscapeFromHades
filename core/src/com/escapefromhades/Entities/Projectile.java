package com.escapefromhades.Entities;

import com.badlogic.gdx.Gdx;
import com.escapefromhades.GameStates.GameField;
import com.escapefromhades.RenderEngine.Renderer;

import java.io.BufferedReader;
import java.io.IOException;

public class Projectile {
    private GameField parentField;

    private int projectileType, projectileDirection;
    private static int[] projectileFlyingX, projectileFlyingY,projectileExplosionX, projectileExplosionY;
    private static int[] projectileDamage;
    private static boolean[] projectileStoppable;
    private float projectileX, projectileY, projectileSpeedX, projectileSpeedY, projectileExplosionTime = 0,
            projectileExplosionStart = 0;
    private boolean projectileIsFlying = true;

    public Projectile(GameField parentField, int projectileType, int projectileDirection, float projectileX, float projectileY,
                      float projectileSpeedX, float projectileSpeedY) {
        this.parentField = parentField;
        this.projectileType = projectileType;
        this.projectileDirection = projectileDirection;
        this.projectileX = projectileX;
        this.projectileY = projectileY;
        this.projectileSpeedX = projectileSpeedX;
        this.projectileSpeedY = projectileSpeedY;
    }

    public static void prepareProjectileValueArrays(int projectileTypesNum) {
        projectileFlyingX = new int[projectileTypesNum];
        projectileFlyingY = new int[projectileTypesNum];
        projectileExplosionX = new int[projectileTypesNum];
        projectileExplosionY = new int[projectileTypesNum];
        projectileDamage = new int[projectileTypesNum];
        projectileStoppable = new boolean[projectileTypesNum];
    }

    public static void loadProjectileValues(final String projectileStatFile, BufferedReader reader, int i) {
        try {
            String line;
            String[] currentLine;
            line = reader.readLine();
            currentLine = line.split(" ");
            projectileFlyingX[i] = Integer.valueOf(currentLine[0]);
            projectileFlyingY[i] = Integer.valueOf(currentLine[1]);
            projectileExplosionX[i] = Integer.valueOf(currentLine[2]);
            projectileExplosionY[i] = Integer.valueOf(currentLine[3]);
            line = reader.readLine();
            projectileDamage[i] = Integer.valueOf(line);
            line = reader.readLine();
            projectileStoppable[i] = line.equals("stoppable");
        } catch (IOException e) {
            System.err.println("Error reading the file: " + projectileStatFile);
        }
    }

    public int getDamage() {
        return projectileDamage[projectileType];
    }

    public boolean exists() {
        return projectileIsFlying;
    }
    public boolean isDangerous() {
        return projectileIsFlying && projectileExplosionTime <= 0;
    }

    public void renderProjectile(Renderer renderer) {
        int tileX = parentField.tileX, tileY = parentField.tileY, pixelSize = GlobalValues.pixelSize;
        if(projectileIsFlying) {
            if(projectileExplosionTime == 0) {
                if(projectileDirection < 3) {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.projectileFlying[projectileType][projectileDirection],
                            (int)(projectileX * tileX - projectileFlyingX[projectileType] / 2) * pixelSize,
                            (int)(projectileY * tileY - projectileFlyingY[projectileType] / 2) * pixelSize,
                            pixelSize, false, 0, 0, false);
                } else {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.projectileFlying[projectileType][1],
                            (int)(projectileX * tileX - projectileFlyingX[projectileType] / 2) * pixelSize,
                            (int)(projectileY * tileY - projectileFlyingY[projectileType] / 2) * pixelSize,
                            pixelSize, true, 0, 0, false);
                }
            } else {
                if(projectileDirection < 3) {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.projectileExplosion[projectileType][projectileDirection],
                            (int)(projectileX * tileX - projectileExplosionX[projectileType] / 2) * pixelSize,
                            (int)(projectileY * tileY - projectileExplosionY[projectileType] / 2) * pixelSize,
                            pixelSize, false, 0, projectileExplosionStart, false);
                } else {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.projectileExplosion[projectileType][1],
                            (int)(projectileX * tileX - projectileExplosionX[projectileType] / 2) * pixelSize,
                            (int)(projectileY * tileY - projectileExplosionY[projectileType] / 2) * pixelSize,
                            pixelSize, true, 0, projectileExplosionStart, false);
                }
            }
        }
    }

    public void processProjectileMovement(float elapsedTime) {
        if(projectileIsFlying) {
            float delta = Gdx.graphics.getDeltaTime();
            if(projectileExplosionTime == 0) {
                projectileX += delta / parentField.spriteStorage.projectileTimePerTile[projectileType] * projectileSpeedX;
                projectileY += delta / parentField.spriteStorage.projectileTimePerTile[projectileType] * projectileSpeedY;
                constrainProjectile(elapsedTime);
            } else if(projectileExplosionTime > 0) {
                projectileExplosionTime -= delta;
                if(projectileExplosionTime <= 0) {
                    projectileIsFlying = false;
                }
            } else {
                projectileIsFlying = false;
            }
        }
    }

    private void constrainProjectile(float elapsedTime) {
        if(projectileX <= 0 || projectileX >= parentField.getWidth() || projectileY <= 0 || projectileY >= parentField.getHeight()) {
            explodeProjectile(elapsedTime);
            return;
        }
        projectileX = Math.max(projectileX, 0);
        projectileX = Math.min(projectileX, parentField.getWidth());
        projectileY = Math.max(projectileY, 0);
        projectileY = Math.min(projectileY, parentField.getHeight());
        if(parentField.getWall((int)projectileX, (int)projectileY) && projectileStoppable[projectileType]) {
            explodeProjectile(elapsedTime);
        }
    }

    public boolean checkCollision(float x, float y, float radius) {
        float collisionDistanceX, collisionDistanceY;
        if(projectileDirection == 1 || projectileDirection == 3) {
            collisionDistanceX = (float) projectileFlyingX[projectileType] / parentField.tileX / 2;
            collisionDistanceY = (float) projectileFlyingY[projectileType] / parentField.tileY / 2;
        } else {
            collisionDistanceX = (float) projectileFlyingY[projectileType] / parentField.tileY / 2;
            collisionDistanceY = (float) projectileFlyingX[projectileType] / parentField.tileX / 2;
        }
        return (projectileX - 0.5f) - x <= radius + collisionDistanceX &&
                x - (projectileX - 0.5f) <= radius + collisionDistanceX &&
                (projectileY - 0.5f) - y <= radius + collisionDistanceY &&
                y - (projectileY - 0.5f) <= radius + collisionDistanceY;
    }

    public boolean checkRadialCollision(float x, float y, float radius) {
        return Math.sqrt(Math.pow(projectileX - 0.5f - x, 2) + Math.pow(projectileY - 0.5f - y, 2)) <= radius;
    }

    public void explodeProjectile(float elapsedTime) {
        if(!parentField.spriteStorage.projectileSoundIsPlaying[projectileType]) {
            parentField.spriteStorage.projectileExplosionSounds[projectileType].play(parentField.getSoundVolume());
            parentField.spriteStorage.projectileSoundIsPlaying[projectileType] = true;
        }
        projectileExplosionTime = parentField.spriteStorage.projectileExplosionDuration[projectileType];
        projectileExplosionStart = elapsedTime;
    }
}
