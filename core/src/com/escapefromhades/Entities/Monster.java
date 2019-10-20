package com.escapefromhades.Entities;

import com.badlogic.gdx.Gdx;
import com.escapefromhades.GameStates.GameField;
import com.escapefromhades.RenderEngine.Renderer;

import java.io.BufferedReader;
import java.io.IOException;

public class Monster {
    private GameField parentField;
    private static int[] monsterHealth, monsterDamage, monsterAlgorithm;
    private static float[] monsterCollisionDistance;
    private float monsterX, monsterY, startX, startY, cycleTimeRemaining, cycleLength, monsterIdleTime = 0;
    private int monsterType, currentHealth, monsterDirection = 0, monsterPathStep = -1;
    private int[] monsterPath;
    private boolean monsterIsAlive = true;

    private int monsterAction = -1;
    private float monsterWalkingAnimationStart = 0, monsterAnimationStart = 0, respawnCooldown = 0;

    private final int[] monsterWalkOffsetX = {0, 0, 0, 0}, monsterWalkOffsetY = {2, 0, 2, 0};
    private final int[] monsterAttackOffsetX = {0, 0, 0, 16}, monsterAttackOffsetY = {16, 0, 16, 0};

    public Monster(GameField parentField, int monsterType) {
        this.parentField = parentField;
        this.monsterType = monsterType;
        this.currentHealth = monsterHealth[monsterType];
    }

    public static void prepareMonsterValueArrays(int monsterTypesNum) {
        monsterAlgorithm = new int[monsterTypesNum];
        monsterHealth = new int[monsterTypesNum];
        monsterDamage = new int[monsterTypesNum];
        monsterCollisionDistance = new float[monsterTypesNum];
    }

    public static void loadMonsterValues(final String monsterStatFile, BufferedReader reader, int i) {
        try {
            String line;
            line = reader.readLine();
            if(line.equals("patrol")) {
                monsterAlgorithm[i] = 0;
            } else if(line.equals("cyclic")) {
                monsterAlgorithm[i] = 1;
            }
            line = reader.readLine();
            monsterHealth[i] = Integer.valueOf(line);
            line = reader.readLine();
            monsterDamage[i] = Integer.valueOf(line);
            line = reader.readLine();
            monsterCollisionDistance[i] = Float.valueOf(line);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + monsterStatFile);
        }
    }

    public void setX(float x) {
        monsterX = x;
        startX = x;
    }

    public void setY(float y) {
        monsterY = y;
        startY = y;
    }

    public float getX() {
        return monsterX;
    }

    public float getY() {
        return monsterY;
    }

    public float getHealth() {
        return currentHealth;
    }

    public float getCollisionDistance() {
        return monsterCollisionDistance[monsterType];
    }

    public boolean isDangerous() {
        return monsterIsAlive && !(monsterAction == 1) && !(monsterAction == 0) && !(monsterAction == 5) && cycleTimeRemaining >= 0;
    }

    public boolean isAlive() {
        return monsterIsAlive;
    }

    public int getDamage() {
        return monsterDamage[monsterType];
    }

    public void readPath(String path) {
        monsterPath = new int[path.length()];
        for(int i = 0; i < monsterPath.length; i++) {
            monsterPath[i] = Character.getNumericValue(path.charAt(i));
        }
        cycleLength = monsterPath.length * parentField.spriteStorage.monsterTimePerTile[monsterType] +
                parentField.spriteStorage.monsterAttackAnimationDuration[monsterType] +
                parentField.spriteStorage.monsterDeathAnimationDuration[monsterType] +
                parentField.spriteStorage.monsterSpawnAnimationDuration[monsterType];
        cycleTimeRemaining = cycleLength - parentField.spriteStorage.monsterSpawnAnimationDuration[monsterType];
        respawnCooldown = cycleLength * 2;
    }

    public void renderMonster(Renderer renderer) {
        int tileX = parentField.tileX, tileY = parentField.tileY, pixelSize = GlobalValues.pixelSize;
        if(cycleTimeRemaining < 0) {
            return;
        }
        switch(monsterAction) {
            case 0:
                renderer.addAnimationToSchedule(parentField.spriteStorage.monsterSpawn[monsterType],
                        (int)(monsterX * tileX) * pixelSize, (int)(monsterY * tileY) * pixelSize, pixelSize, false, 0, monsterAnimationStart, false);
                break;
            case 1:
                renderer.addAnimationToSchedule(parentField.spriteStorage.monsterDeath[monsterType],
                        (int)(monsterX * tileX) * pixelSize, (int)(monsterY * tileY) * pixelSize, pixelSize, false, 0, monsterAnimationStart, false);
                break;
            case 3:
                if(monsterDirection < 3) {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.monsterAttack[monsterType][monsterDirection], (int)(monsterX * tileX - monsterAttackOffsetX[monsterDirection]) * pixelSize,
                            (int)(monsterY * tileY - monsterAttackOffsetY[monsterDirection]) * pixelSize, pixelSize, false, 0, monsterAnimationStart, false);
                } else {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.monsterAttack[monsterType][1], (int)(monsterX * tileX - monsterAttackOffsetX[monsterDirection]) * pixelSize,
                            (int)(monsterY * tileY - monsterAttackOffsetY[monsterDirection]) * pixelSize, pixelSize, true, 0, monsterAnimationStart, false);
                }
                break;
            case 5:
                break;
            default:
                if(monsterDirection < 3) {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.monsterWalk[monsterType][monsterDirection], (int)(monsterX * tileX - monsterWalkOffsetX[monsterDirection]) * pixelSize,
                            (int)(monsterY * tileY - monsterWalkOffsetY[monsterDirection]) * pixelSize, pixelSize, false, 0, monsterWalkingAnimationStart, false);
                } else {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.monsterWalk[monsterType][1], (int)(monsterX * tileX - monsterWalkOffsetX[monsterDirection]) * pixelSize,
                            (int)(monsterY * tileY - monsterWalkOffsetY[monsterDirection]) * pixelSize, pixelSize, true, 0, monsterWalkingAnimationStart, false);
                }
        }
    }

    public void processMonsterMovement(float elapsedTime) {
        cycleTimeRemaining -= Gdx.graphics.getDeltaTime();
        if(monsterIdleTime <= 0) {
            monsterWalkingAnimationStart = elapsedTime;
            if(cycleTimeRemaining >= 0) {
                switch(monsterAlgorithm[monsterType]) {
                    case 0:
                        nextPatrolAction();
                        break;
                    case 1:
                        nextCyclicAction(elapsedTime);
                        break;
                }
            }
            if(currentHealth <= 0 && monsterIsAlive) {
                killMonster(elapsedTime);
            }
        }
        if(monsterAction < 0 && cycleTimeRemaining >= 0) {
            if(monsterIsAlive) {
                float delta = Math.min(Gdx.graphics.getDeltaTime(), monsterIdleTime);
                switch(monsterDirection) {
                    case 0:
                        monsterY -= delta / parentField.spriteStorage.monsterTimePerTile[monsterType];
                        break;
                    case 1:
                        monsterX += delta / parentField.spriteStorage.monsterTimePerTile[monsterType];
                        break;
                    case 2:
                        monsterY += delta / parentField.spriteStorage.monsterTimePerTile[monsterType];
                        break;
                    case 3:
                        monsterX -= delta / parentField.spriteStorage.monsterTimePerTile[monsterType];
                        break;
                }
            }
        }
        if(monsterIdleTime > 0) {
            monsterIdleTime -= Gdx.graphics.getDeltaTime();
        }
        switch(monsterAlgorithm[monsterType]) {
            case 0:
                if(cycleTimeRemaining <= 0) {
                    cycleTimeRemaining += cycleLength;
                }
                break;
            case 1:
                if(respawnCooldown <= 0) {
                    cycleTimeRemaining = cycleLength;
                    respawnCooldown += cycleLength * 1.5f;
                } else {
                    respawnCooldown -= Gdx.graphics.getDeltaTime();
                }
                break;
        }
    }

    private void nextPatrolAction() {
        switch(monsterAction) {
            case 1:
                monsterIsAlive = false;
                break;
            default:
                monsterPathStep = (monsterPathStep + 1) % monsterPath.length;
                monsterDirection = monsterPath[monsterPathStep];
                monsterIdleTime = parentField.spriteStorage.monsterTimePerTile[monsterType];
        }
        if(monsterAction >= 0) {
            monsterAction = -1;
        }
    }

    private void nextCyclicAction(float elapsedTime) {
        switch(monsterAction) {
            case 0:
                monsterAction = -1;
                break;
            case 1:
                monsterAction = 5;
                monsterX = startX;
                monsterY = startY;
                monsterPathStep = -1;
                currentHealth = monsterHealth[monsterType];
                break;
            case 3:
                killMonster(elapsedTime);
                break;
            case 5:
                if(cycleLength - cycleTimeRemaining <= parentField.spriteStorage.monsterDeathAnimationDuration[monsterType]) {
                    spawnMonster(elapsedTime);
                }
                break;
            default:
                if(monsterPathStep == monsterPath.length - 1) {
                    monsterPathStep = -1;
                    makeMonsterAttack(elapsedTime);
                } else {
                    monsterPathStep = (monsterPathStep + 1) % monsterPath.length;
                    monsterDirection = monsterPath[monsterPathStep];
                    monsterIdleTime = parentField.spriteStorage.monsterTimePerTile[monsterType];
                }
        }
    }

    public void killMonster(float elapsedTime) {
        if(!parentField.spriteStorage.monsterDeathSoundIsPlaying[monsterType]) {
            parentField.spriteStorage.monsterDeathSounds[monsterType].play(parentField.getSoundVolume());
            parentField.spriteStorage.monsterDeathSoundIsPlaying[monsterType] = true;
        }
        monsterIdleTime = parentField.spriteStorage.monsterDeathAnimationDuration[monsterType];
        monsterAction = 1;
        monsterAnimationStart = elapsedTime;
        monsterWalkingAnimationStart = elapsedTime + parentField.spriteStorage.monsterDeathAnimationDuration[monsterType];
    }

    public void spawnMonster(float elapsedTime) {
        if(!parentField.spriteStorage.monsterSpawnSoundIsPlaying[monsterType]) {
            parentField.spriteStorage.monsterSpawnSounds[monsterType].play(parentField.getSoundVolume());
            parentField.spriteStorage.monsterSpawnSoundIsPlaying[monsterType] = true;
        }
        monsterIdleTime = parentField.spriteStorage.monsterSpawnAnimationDuration[monsterType];
        monsterAction = 0;
        monsterAnimationStart = elapsedTime;
        monsterWalkingAnimationStart = elapsedTime + parentField.spriteStorage.monsterSpawnAnimationDuration[monsterType];
    }

    public void makeMonsterAttack(float elapsedTime) {
        if(!parentField.spriteStorage.monsterAttackSoundIsPlaying[monsterType]) {
            parentField.spriteStorage.monsterAttackSounds[monsterType].play(parentField.getSoundVolume());
            parentField.spriteStorage.monsterAttackSoundIsPlaying[monsterType] = true;
        }
        monsterIdleTime = parentField.spriteStorage.monsterAttackAnimationDuration[monsterType];
        monsterAction = 3;
        monsterAnimationStart = elapsedTime;
        monsterWalkingAnimationStart = elapsedTime + parentField.spriteStorage.monsterAttackAnimationDuration[monsterType];
    }

    public void hurtMonster(int damage) {
        currentHealth -= damage;
    }

    public void checkCollisionWithProjectile(Projectile projectile, float elapsedTime) {
        if(projectile.checkCollision(monsterX, monsterY, monsterCollisionDistance[monsterType])) {
            hurtMonster(projectile.getDamage());
            if(currentHealth <= 0 && this.isDangerous()) {
                killMonster(elapsedTime);
            }
            projectile.explodeProjectile(elapsedTime);
        }
    }
}
