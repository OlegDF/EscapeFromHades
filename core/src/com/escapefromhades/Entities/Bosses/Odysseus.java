package com.escapefromhades.Entities.Bosses;

import com.badlogic.gdx.Gdx;
import com.escapefromhades.Entities.Boss;
import com.escapefromhades.Entities.GlobalValues;
import com.escapefromhades.Entities.Player;
import com.escapefromhades.Entities.Projectile;
import com.escapefromhades.GameStates.GameField;
import com.escapefromhades.RenderEngine.Renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class Odysseus extends Boss {
    private GameField parentField;
    private Player targetedPlayer;
    private static int defaultHealth, arrowDamage;
    private static float projectileLaunchTime;
    private float odysseusX, odysseusY, odysseusIdleTime = 0, odysseusAnimationStart = 0,
            odysseusInvulnTime = -1;
    private int currentHealth, odysseusDirection = 0, odysseusAction;
    private float shotTargetX = 0, shotTargetY = 0, teleportTargetX = 0, teleportTargetY = 0;
    private boolean projectileLaunched = false;

    private static int[] teleportPositionsX, teleportPositionsY;

    private final int odysseusOffsetX = 4, odysseusOffsetY = 4, projectileID = 3;

    private long sound = -1;

    private boolean isAlive = true;

    public Odysseus(GameField parentField, Player player) {
        this.parentField = parentField;
        this.targetedPlayer = player;
        currentHealth = defaultHealth;
    }

    public static void loadOdysseusValues(final String monsterStatFile, BufferedReader reader) {
        try {
            String line;
            String[] currentLine;
            line = reader.readLine();
            defaultHealth = Integer.valueOf(line);
            line = reader.readLine();
            arrowDamage = Integer.valueOf(line);
            line = reader.readLine();
            projectileLaunchTime = Float.valueOf(line);
            line = reader.readLine();
            teleportPositionsX = new int[Integer.valueOf(line)];
            teleportPositionsY = new int[teleportPositionsX.length];
            line = reader.readLine();
            currentLine = line.split(" ");
            for(int i = 0; i < teleportPositionsX.length; i++) {
                teleportPositionsX[i] = Integer.valueOf(currentLine[i]);
            }
            line = reader.readLine();
            currentLine = line.split(" ");
            for(int i = 0; i < teleportPositionsY.length; i++) {
                teleportPositionsY[i] = Integer.valueOf(currentLine[i]);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + monsterStatFile);
        }
    }

    public void renderBoss(Renderer renderer) {
        renderHearts(renderer);
        renderTeleporters(renderer);
        int tileX = parentField.tileX, tileY = parentField.tileY, pixelSize = GlobalValues.pixelSize;
        if(isAlive) {
            switch (odysseusAction) {
                case 0:
                    renderer.addAnimationToSchedule(parentField.spriteStorage.odysseusSpawn,
                            (int)(odysseusX * tileX - odysseusOffsetX) * pixelSize,
                            (int)(odysseusY * tileY - odysseusOffsetY) * pixelSize, pixelSize,
                            false, 0, odysseusAnimationStart, false);
                    break;
                case 1:
                    renderer.addAnimationToSchedule(parentField.spriteStorage.odysseusDeath,
                            (int)(odysseusX * tileX - odysseusOffsetX) * pixelSize,
                            (int)(odysseusY * tileY - odysseusOffsetY) * pixelSize, pixelSize,
                            false, 0, odysseusAnimationStart, false);
                    break;
                case 2:
                    renderer.addAnimationToSchedule(parentField.spriteStorage.odysseusTeleport,
                            (int)(odysseusX * tileX - odysseusOffsetX) * pixelSize,
                            (int)(odysseusY * tileY - odysseusOffsetY) * pixelSize, pixelSize,
                            false, 0, odysseusAnimationStart, false);
                    break;
                case 3:
                    if(odysseusDirection < 3) {
                        renderer.addAnimationToSchedule(parentField.spriteStorage.odysseusShot[odysseusDirection],
                                (int)(odysseusX * tileX - odysseusOffsetX) * pixelSize,
                                (int)(odysseusY * tileY - odysseusOffsetY) * pixelSize,
                                pixelSize, false, 0, odysseusAnimationStart, false);
                    } else {
                        renderer.addAnimationToSchedule(parentField.spriteStorage.odysseusShot[1],
                                (int)(odysseusX * tileX - odysseusOffsetX) * pixelSize,
                                (int)(odysseusY * tileY - odysseusOffsetY) * pixelSize,
                                pixelSize, true, 0, odysseusAnimationStart, false);
                    }
                    break;
                case 4:
                    if(odysseusDirection < 3) {
                        renderer.addAnimationToSchedule(parentField.spriteStorage.odysseusTripleShot[odysseusDirection],
                                (int)(odysseusX * tileX - odysseusOffsetX) * pixelSize,
                                (int)(odysseusY * tileY - odysseusOffsetY) * pixelSize,
                                pixelSize, false, 0, odysseusAnimationStart, false);
                    } else {
                        renderer.addAnimationToSchedule(parentField.spriteStorage.odysseusTripleShot[1],
                                (int)(odysseusX * tileX - odysseusOffsetX) * pixelSize,
                                (int)(odysseusY * tileY - odysseusOffsetY) * pixelSize,
                                pixelSize, true, 0, odysseusAnimationStart, false);
                    }
                    break;
            }
        } else {
            renderer.addAnimationToSchedule(parentField.spriteStorage.odysseusDeath,
                    (int)(odysseusX * tileX - odysseusOffsetX) * pixelSize,
                    (int)(odysseusY * tileY - odysseusOffsetY) * pixelSize, pixelSize,
                    false, 0, odysseusAnimationStart, false);
        }
    }

    private void renderHearts(Renderer renderer) {
        for(int i = 0; i < currentHealth; i++) {
            renderer.addSpriteToSchedule(parentField.spriteStorage.odysseusHeart, -parentField.spriteStorage.heartX * GlobalValues.pixelSize,
                    (parentField.spriteStorage.heartY * i) * GlobalValues.pixelSize,
                    GlobalValues.pixelSize, 0, false);
        }
        for(int i = Math.max(currentHealth, 0); i < defaultHealth; i++) {
            renderer.addSpriteToSchedule(parentField.spriteStorage.odysseusEmptyHeart, -parentField.spriteStorage.heartX * GlobalValues.pixelSize,
                    (parentField.spriteStorage.heartY * i) * GlobalValues.pixelSize,
                    GlobalValues.pixelSize, 0, false);
        }
    }

    private void renderTeleporters(Renderer renderer) {
        for(int i = 0; i < teleportPositionsX.length; i++) {
            renderer.addSpriteToSchedule(parentField.spriteStorage.odysseusTeleporter,
                    teleportPositionsX[i] * parentField.tileX * GlobalValues.pixelSize,
                    teleportPositionsY[i] * parentField.tileY * GlobalValues.pixelSize,
                    GlobalValues.pixelSize, 0, false);
        }
    }

    public void processBossMovement(float elapsedTime) {
        float playerX = targetedPlayer.getX(), playerY = targetedPlayer.getY();
        if(targetedPlayer.isDying() && odysseusAction != 0 && odysseusAction != 2) {
            teleportOdysseus(elapsedTime);
            teleportTargetX = parentField.bossX;
            teleportTargetY = parentField.bossY;
            currentHealth = defaultHealth;
        }
        float distanceToPlayer = (float)Math.sqrt(Math.pow(playerX - odysseusX, 2) + Math.pow(playerY - odysseusY, 2));
        directBoss(shotTargetX - odysseusX, shotTargetY - odysseusY);
        if(isAlive) {
            if(odysseusIdleTime > 0) {
                odysseusIdleTime -= Gdx.graphics.getDeltaTime();
                if(odysseusIdleTime <= parentField.spriteStorage.odysseusShotAnimationDuration - projectileLaunchTime &&
                        !projectileLaunched) {
                    projectileLaunched = true;
                    switch(odysseusAction) {
                        case 3:
                            launchOneArrow(shotTargetX + 0.5f, shotTargetY + 0.5f);
                            break;
                        case 4:
                            launchThreeArrows(shotTargetX + 0.5f, shotTargetY + 0.5f);
                            break;
                    }
                }
            } else {
                projectileLaunched = false;
                switch(odysseusAction) {
                    case 0:
                        chooseShot(distanceToPlayer, elapsedTime);
                        break;
                    case 1:
                        isAlive = false;
                        break;
                    case 2:
                        spawnOdysseus(elapsedTime, teleportTargetX, teleportTargetY);
                        break;
                    case 3:
                    case 4:
                        chooseShot(distanceToPlayer, elapsedTime);
                }
            }
            if(odysseusInvulnTime > 0) {
                odysseusInvulnTime -= Gdx.graphics.getDeltaTime();
            }
        }

    }

    private void directBoss(float speedX, float speedY) {
        if(speedX >= 0) {
            if(speedY >= 0) {
                if(Math.abs(speedY) > Math.abs(speedX)) {
                    odysseusDirection = 2;
                } else {
                    odysseusDirection = 1;
                }
            } else {
                if(Math.abs(speedY) > Math.abs(speedX)) {
                    odysseusDirection = 0;
                } else {
                    odysseusDirection = 1;
                }
            }
        } else {
            if(speedY >= 0) {
                if(Math.abs(speedY) > Math.abs(speedX)) {
                    odysseusDirection = 2;
                } else {
                    odysseusDirection = 3;
                }
            } else {
                if(Math.abs(speedY) > Math.abs(speedX)) {
                    odysseusDirection = 0;
                } else {
                    odysseusDirection = 3;
                }
            }
        }
    }

    private void chooseShot(float distanceToPlayer, float elapsedTime) {
        if(distanceToPlayer >= parentField.getWidth() / 2) {
            makeOdysseusShootTriple(elapsedTime);
        } else {
            makeOdysseusShoot(elapsedTime);
        }
    }

    private void launchOneArrow(float targetX, float targetY) {
        parentField.createDirectedEnemyProjectile(projectileID, odysseusX + 0.5f, odysseusY + 0.5f, targetX, targetY);
    }

    private void launchThreeArrows(float targetX, float targetY) {
        final float angle = (float)Math.PI / 6;
        float target1X = (float)(odysseusX + (targetX - odysseusX) * Math.cos(-angle) - (targetY - odysseusY) * Math.sin(-angle));
        float target1Y = (float)(odysseusY + (targetX - odysseusX) * Math.sin(-angle) + (targetY - odysseusY) * Math.cos(-angle));
        float target2X = (float)(odysseusX + (targetX - odysseusX) * Math.cos(angle) - (targetY - odysseusY) * Math.sin(angle));
        float target2Y = (float)(odysseusY + (targetX - odysseusX) * Math.sin(angle) + (targetY - odysseusY) * Math.cos(angle));
        parentField.createDirectedEnemyProjectile(projectileID, odysseusX + 0.5f, odysseusY + 0.5f, targetX, targetY);
        parentField.createDirectedEnemyProjectile(projectileID, odysseusX + 0.5f, odysseusY + 0.5f, target1X, target1Y);
        parentField.createDirectedEnemyProjectile(projectileID, odysseusX + 0.5f, odysseusY + 0.5f, target2X, target2Y);
    }

    public void killOdysseus(float elapsedTime) {
        if(!parentField.spriteStorage.odysseusDeathSoundIsPlaying) {
            stopSound();
            sound = parentField.spriteStorage.odysseusDeathSound.play(parentField.getSoundVolume());
            parentField.spriteStorage.odysseusDeathSoundIsPlaying = true;
        }
        odysseusIdleTime = parentField.spriteStorage.odysseusDeathAnimationDuration;
        odysseusAction = 1;
        odysseusAnimationStart = elapsedTime;
    }

    public void spawnOdysseus(float elapsedTime, float x, float y) {
        if(!parentField.spriteStorage.odysseusSpawnSoundIsPlaying) {
            stopSound();
            sound = parentField.spriteStorage.odysseusSpawnSound.play(parentField.getSoundVolume());
            parentField.spriteStorage.odysseusSpawnSoundIsPlaying = true;
        }
        odysseusIdleTime = parentField.spriteStorage.odysseusTeleportAnimationDuration;
        odysseusAction = 0;
        odysseusX = x;
        odysseusY = y;
        odysseusAnimationStart = elapsedTime;
    }

    public void teleportOdysseus(float elapsedTime) {
        if(!parentField.spriteStorage.odysseusTeleportSoundIsPlaying) {
            stopSound();
            sound = parentField.spriteStorage.odysseusTeleportSound.play(parentField.getSoundVolume());
            parentField.spriteStorage.odysseusTeleportSoundIsPlaying = true;
        }
        chooseTeleporter(targetedPlayer.getX(), targetedPlayer.getY());
        odysseusIdleTime = parentField.spriteStorage.odysseusTeleportAnimationDuration;
        odysseusAction = 2;
        odysseusAnimationStart = elapsedTime;
    }

    public void makeOdysseusShoot(float elapsedTime) {
        if(!parentField.spriteStorage.odysseusShotSoundIsPlaying) {
            stopSound();
            sound = parentField.spriteStorage.odysseusShotSound.play(parentField.getSoundVolume());
            parentField.spriteStorage.odysseusShotSoundIsPlaying = true;
        }
        shotTargetX = targetedPlayer.getX();
        shotTargetY = targetedPlayer.getY();
        odysseusIdleTime = parentField.spriteStorage.odysseusShotAnimationDuration;
        odysseusAction = 3;
        odysseusAnimationStart = elapsedTime;
    }

    public void makeOdysseusShootTriple(float elapsedTime) {
        if(!parentField.spriteStorage.odysseusShotSoundIsPlaying) {
            stopSound();
            sound = parentField.spriteStorage.odysseusShotSound.play(parentField.getSoundVolume());
            parentField.spriteStorage.odysseusShotSoundIsPlaying = true;
        }
        shotTargetX = targetedPlayer.getX();
        shotTargetY = targetedPlayer.getY();
        odysseusIdleTime = parentField.spriteStorage.odysseusTripleShotAnimationDuration;
        odysseusAction = 4;
        odysseusAnimationStart = elapsedTime;
    }

    private void chooseTeleporter(float playerX, float playerY) {
        ArrayList<Integer> chosenTeleporters = new ArrayList<Integer>();
        boolean[] teleporterAdded = new boolean[teleportPositionsX.length];
        float[] teleporterDistances = new float[teleportPositionsX.length];
        for(int i = 0; i < teleportPositionsX.length; i++) {
            teleporterDistances[i] = (float) Math.sqrt(Math.pow(playerX - teleportPositionsX[i], 2) + Math.pow(playerY - teleportPositionsY[i], 2));
        }
        for(int i = 0; i < teleportPositionsX.length / 2; i++) {
            int chosenTeleporter = 0;
            for(int j = 0; j < teleportPositionsX.length; j++) {
                if(!teleporterAdded[j] && teleporterDistances[j] > teleporterDistances[chosenTeleporter]) {
                    chosenTeleporter = j;
                }
            }
            chosenTeleporters.add(chosenTeleporter);
            teleporterAdded[chosenTeleporter] = true;
        }
        int chosenTeleporter = chosenTeleporters.get((int)(Math.random() * chosenTeleporters.size()));
        teleportTargetX = teleportPositionsX[chosenTeleporter];
        teleportTargetY = teleportPositionsY[chosenTeleporter];
    }

    public int checkHitWithPlayer() {
        return 0;
    }

    public void checkIfBossIsHit(float elapsedTime) {
        if(!targetedPlayer.isAttacking() || odysseusInvulnTime > 0 || !isAlive) {
            return;
        }
        float playerX = targetedPlayer.getX(), playerY = targetedPlayer.getY();
        int direction = targetedPlayer.getDirection(), damage = targetedPlayer.getDamage();
        float range = targetedPlayer.getRange();
        final float hitWidth = 0.25f;
        boolean isHit = false;
        switch(direction) {
            case 1:
                if(odysseusX - playerX <= range && odysseusX - playerX > 0 && Math.abs(odysseusY - playerY) <= hitWidth) {
                    isHit = true;
                }
                break;
            case 3:
                if(playerX - odysseusX <= range && playerX - odysseusX > 0 && Math.abs(odysseusY - playerY) <= hitWidth) {
                    isHit = true;
                }
                break;
            case 2:
                if(odysseusY - playerY <= range && odysseusY - playerY > 0 && Math.abs(odysseusX - playerX) <= hitWidth) {
                    isHit = true;
                }
                break;
            case 0:
                if(playerY - odysseusY <= range && playerY - odysseusY > 0 && Math.abs(odysseusX - playerX) <= hitWidth) {
                    isHit = true;
                }
                break;
        }
        if(isHit && odysseusAction != 0 && odysseusAction != 1 && odysseusAction != 2) {
            hurtOdysseus(damage, elapsedTime);
            odysseusInvulnTime = parentField.spriteStorage.playerAttackDuration[targetedPlayer.getPlayerType()];
        }
    }

    private void hurtOdysseus(float damage, float elapsedTime) {
        currentHealth -= damage;
        teleportOdysseus(elapsedTime);
        if(currentHealth <= 0 && isAlive && odysseusAction != 1) {
            killOdysseus(elapsedTime);
        }
    }

    public void checkCollisionWithProjectile(Projectile projectile, float elapsedTime) {
        if(!isAlive || odysseusAction == 0 || odysseusAction == 1 || odysseusAction == 2) {
            return;
        }
        if(projectile.checkCollision(odysseusX, odysseusY, 0.5f)) {
            hurtOdysseus(projectile.getDamage(), elapsedTime);
            projectile.explodeProjectile(elapsedTime);
        }
    }

    private void stopSound() {
        if(sound >= 0) {
            parentField.spriteStorage.odysseusSpawnSound.stop(sound);
            parentField.spriteStorage.odysseusTeleportSound.stop(sound);
            parentField.spriteStorage.odysseusShotSound.stop(sound);
            parentField.spriteStorage.odysseusDeathSound.stop(sound);
        }
    }

    public boolean isAlive() {
        return isAlive;
    }

}
