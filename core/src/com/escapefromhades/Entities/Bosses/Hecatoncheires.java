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

public class Hecatoncheires extends Boss {
    private GameField parentField;
    private Player targetedPlayer;
    private static int defaultHealth, spinDamage;
    private static float spinStart, spinFinish;
    private static float radius, deflectRadius, deflectStartRadius, spinRadius;
    private float hecatoncheiresX, hecatoncheiresY, hecatoncheiresIdleTime = 0, hecatoncheiresAnimationStart = 0,
            hecatoncheiresWalkingAnimationStart = 0, hecatoncheiresInvulnTime = -1, stepSoundCooldown = 0;
    private int currentHealth, hecatoncheiresDirection = 0, hecatoncheiresAction;

    private final int[] hecatoncheiresWalkOffsetX = {16, 18, 16, 18}, hecatoncheiresWalkOffsetY = {18, 18, 18, 18};
    private final int hecatoncheiresOffsetX = 16, hecatoncheiresOffsetY = 16;
    private final int hecatoncheiresSpinOffsetX = 20, hecatoncheiresSpinOffsetY = 20;

    private long sound = -1;

    private boolean isAlive = true;

    public Hecatoncheires(GameField parentField, Player player) {
        this.parentField = parentField;
        this.targetedPlayer = player;
        currentHealth = defaultHealth;
    }

    public static void loadHecatoncheiresValues(final String monsterStatFile, BufferedReader reader) {
        try {
            String line;
            String[] currentLine;
            line = reader.readLine();
            defaultHealth = Integer.valueOf(line);
            line = reader.readLine();
            spinDamage = Integer.valueOf(line);
            line = reader.readLine();
            currentLine = line.split(" ");
            spinStart = Float.valueOf(currentLine[0]);
            spinFinish = Float.valueOf(currentLine[1]);
            line = reader.readLine();
            currentLine = line.split(" ");
            radius = Float.valueOf(currentLine[0]);
            deflectRadius = Float.valueOf(currentLine[1]);
            deflectStartRadius = Float.valueOf(currentLine[2]);
            spinRadius = Float.valueOf(currentLine[3]);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + monsterStatFile);
        }
    }

    public void renderBoss(Renderer renderer) {
        renderHearts(renderer);
        int tileX = parentField.tileX, tileY = parentField.tileY, pixelSize = GlobalValues.pixelSize;
        if(isAlive) {
            switch(hecatoncheiresAction) {
                case 0:
                    renderer.addAnimationToSchedule(parentField.spriteStorage.hecatoncheiresDeath,
                            (int)(hecatoncheiresX * tileX - hecatoncheiresOffsetX) * pixelSize,
                            (int)(hecatoncheiresY * tileY - hecatoncheiresOffsetY) * pixelSize, pixelSize,
                            false, 0, hecatoncheiresAnimationStart, true);
                    break;
                case 1:
                    renderer.addAnimationToSchedule(parentField.spriteStorage.hecatoncheiresDeath,
                            (int)(hecatoncheiresX * tileX - hecatoncheiresOffsetX) * pixelSize,
                            (int)(hecatoncheiresY * tileY - hecatoncheiresOffsetY) * pixelSize, pixelSize,
                            false, 0, hecatoncheiresAnimationStart, false);
                    break;
                case 2:
                    renderer.addAnimationToSchedule(parentField.spriteStorage.hecatoncheiresDeflect,
                            (int)(hecatoncheiresX * tileX - hecatoncheiresOffsetX) * pixelSize,
                            (int)(hecatoncheiresY * tileY - hecatoncheiresOffsetY) * pixelSize, pixelSize,
                            false, 0, hecatoncheiresAnimationStart, true);
                    break;
                case 3:
                    renderer.addAnimationToSchedule(parentField.spriteStorage.hecatoncheiresSpin,
                            (int)(hecatoncheiresX * tileX - hecatoncheiresSpinOffsetX) * pixelSize,
                            (int)(hecatoncheiresY * tileY - hecatoncheiresSpinOffsetY) * pixelSize, pixelSize,
                            false, 0, hecatoncheiresAnimationStart, true);
                    break;
                default:
                    if(hecatoncheiresDirection < 3) {
                        renderer.addAnimationToSchedule(parentField.spriteStorage.hecatoncheiresWalk[hecatoncheiresDirection],
                                (int)(hecatoncheiresX * tileX - hecatoncheiresWalkOffsetX[hecatoncheiresDirection]) * pixelSize,
                                (int)(hecatoncheiresY * tileY - hecatoncheiresWalkOffsetY[hecatoncheiresDirection]) * pixelSize,
                                pixelSize, false, 0, hecatoncheiresWalkingAnimationStart, false);
                    } else {
                        renderer.addAnimationToSchedule(parentField.spriteStorage.hecatoncheiresWalk[1],
                                (int)(hecatoncheiresX * tileX - hecatoncheiresWalkOffsetX[hecatoncheiresDirection]) * pixelSize,
                                (int)(hecatoncheiresY * tileY - hecatoncheiresWalkOffsetY[hecatoncheiresDirection]) * pixelSize,
                                pixelSize, true, 0, hecatoncheiresWalkingAnimationStart, false);
                    }
            }
        } else {
            renderer.addAnimationToSchedule(parentField.spriteStorage.hecatoncheiresDeath,
                    (int)(hecatoncheiresX * tileX - hecatoncheiresOffsetX) * pixelSize,
                    (int)(hecatoncheiresY * tileY - hecatoncheiresOffsetY) * pixelSize, pixelSize,
                    false, 0, hecatoncheiresAnimationStart, false);
        }
    }

    private void renderHearts(Renderer renderer) {
        for(int i = 0; i < currentHealth; i++) {
            renderer.addSpriteToSchedule(parentField.spriteStorage.hecatoncheiresHeart, -parentField.spriteStorage.heartX * GlobalValues.pixelSize,
                    (parentField.spriteStorage.heartY * i) * GlobalValues.pixelSize,
                    GlobalValues.pixelSize, 0, false);
        }
        for(int i = Math.max(currentHealth, 0); i < defaultHealth; i++) {
            renderer.addSpriteToSchedule(parentField.spriteStorage.hecatoncheiresEmptyHeart, -parentField.spriteStorage.heartX * GlobalValues.pixelSize,
                    (parentField.spriteStorage.heartY * i) * GlobalValues.pixelSize,
                    GlobalValues.pixelSize, 0, false);
        }
    }

    public void processBossMovement(float elapsedTime) {
        float playerX = targetedPlayer.getX(), playerY = targetedPlayer.getY();
        if(targetedPlayer.isDying()) {
            hecatoncheiresAction= -1;
            hecatoncheiresIdleTime = 0;
            stopSound();
            playerX = parentField.bossX;
            playerY = parentField.bossY;
            currentHealth = defaultHealth;
        }
        float distanceToPlayer = (float)Math.sqrt(Math.pow(playerX - hecatoncheiresX, 2) + Math.pow(playerY - hecatoncheiresY, 2));
        float speedX = 0, speedY = 0, margin = 0.1f;
        if(distanceToPlayer > margin) {
            speedX = (playerX - hecatoncheiresX) / distanceToPlayer;
            speedY = (playerY - hecatoncheiresY) / distanceToPlayer;
        }
        if(targetedPlayer.isDying()) {
            speedX *= 2;
            speedY *= 2;
        }
        directBoss(speedX, speedY);
        if(distanceToPlayer <= margin && targetedPlayer.isDying()) {
            hecatoncheiresDirection = 0;
        }
        if(isAlive) {
            if(hecatoncheiresAction < 0) {
                if(distanceToPlayer <= spinRadius + 0.35f && !targetedPlayer.isDying()) {
                    makeHecatoncheiresSpin(elapsedTime);
                } else {
                    hecatoncheiresX += speedX * Gdx.graphics.getDeltaTime() / parentField.spriteStorage.hecatoncheiresTimePerTile;
                    hecatoncheiresY += speedY * Gdx.graphics.getDeltaTime() / parentField.spriteStorage.hecatoncheiresTimePerTile;
                    stepSoundCooldown -= Gdx.graphics.getDeltaTime();
                    if(stepSoundCooldown < 0) {
                        stepSoundCooldown = parentField.spriteStorage.hecatoncheiresTimePerTile;
                        if(!parentField.spriteStorage.hecatoncheiresStepSoundIsPlaying) {
                            stopSound();
                            sound = parentField.spriteStorage.hecatoncheiresStepSound.play(parentField.getSoundVolume());
                            parentField.spriteStorage.hecatoncheiresStepSoundIsPlaying = true;
                        }
                    }
                }
            } else if(hecatoncheiresIdleTime > 0) {
                hecatoncheiresIdleTime -= Gdx.graphics.getDeltaTime();
            } else {
                hecatoncheiresWalkingAnimationStart = elapsedTime;
                switch(hecatoncheiresAction) {
                    case 1:
                        isAlive = false;
                }
                hecatoncheiresAction = -1;
            }
            if(hecatoncheiresInvulnTime > 0) {
                hecatoncheiresInvulnTime -= Gdx.graphics.getDeltaTime();
            }
        }
    }

    private void directBoss(float speedX, float speedY) {
        if(speedX >= 0) {
            if(speedY >= 0) {
                if(Math.abs(speedY) > Math.abs(speedX)) {
                    hecatoncheiresDirection = 2;
                } else {
                    hecatoncheiresDirection = 1;
                }
            } else {
                if(Math.abs(speedY) > Math.abs(speedX)) {
                    hecatoncheiresDirection = 0;
                } else {
                    hecatoncheiresDirection = 1;
                }
            }
        } else {
            if(speedY >= 0) {
                if(Math.abs(speedY) > Math.abs(speedX)) {
                    hecatoncheiresDirection = 2;
                } else {
                    hecatoncheiresDirection = 3;
                }
            } else {
                if(Math.abs(speedY) > Math.abs(speedX)) {
                    hecatoncheiresDirection = 0;
                } else {
                    hecatoncheiresDirection = 3;
                }
            }
        }
    }

    public int checkHitWithPlayer() {
        if(!isAlive || hecatoncheiresAction == 0 || hecatoncheiresAction == 1) {
            return 0;
        }
        float playerX = targetedPlayer.getX(), playerY = targetedPlayer.getY();
        float dist = (float)Math.sqrt(Math.pow(playerX - hecatoncheiresX, 2) + Math.pow(playerY - hecatoncheiresY, 2));
        if(dist <= deflectRadius + 0.35f && hecatoncheiresAction == 2) {
            return 1;
        } else if(dist <= spinRadius + 0.35f && hecatoncheiresAction == 3 &&
                hecatoncheiresIdleTime >= spinStart && hecatoncheiresIdleTime <= spinFinish) {
            return spinDamage;
        } else if(dist <= radius + 0.35f && hecatoncheiresAction < 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public void killHecatoncheires(float elapsedTime) {
        if(!parentField.spriteStorage.hecatoncheiresDeathSoundIsPlaying) {
            stopSound();
            sound = parentField.spriteStorage.hecatoncheiresDeathSound.play(parentField.getSoundVolume());
            parentField.spriteStorage.hecatoncheiresDeathSoundIsPlaying = true;
        }
        hecatoncheiresIdleTime = parentField.spriteStorage.hecatoncheiresDeathAnimationDuration;
        hecatoncheiresAction = 1;
        hecatoncheiresAnimationStart = elapsedTime;
        hecatoncheiresWalkingAnimationStart = elapsedTime + parentField.spriteStorage.hecatoncheiresDeathAnimationDuration;
    }

    public void makeHecatoncheiresSpin(float elapsedTime) {
        if(!parentField.spriteStorage.hecatoncheiresSpinSoundIsPlaying) {
            stopSound();
            sound = parentField.spriteStorage.hecatoncheiresSpinSound.play(parentField.getSoundVolume());
            parentField.spriteStorage.hecatoncheiresSpinSoundIsPlaying = true;
        }
        hecatoncheiresIdleTime = parentField.spriteStorage.hecatoncheiresSpinAnimationDuration;
        hecatoncheiresAction = 3;
        hecatoncheiresAnimationStart = elapsedTime;
        hecatoncheiresWalkingAnimationStart = elapsedTime + parentField.spriteStorage.hecatoncheiresSpinAnimationDuration;
    }

    public void makeHecatoncheiresDeflect(float elapsedTime) {
        if(!parentField.spriteStorage.hecatoncheiresDeflectSoundIsPlaying) {
            stopSound();
            sound = parentField.spriteStorage.hecatoncheiresDeflectSound.play(parentField.getSoundVolume());
            parentField.spriteStorage.hecatoncheiresDeflectSoundIsPlaying = true;
        }
        hecatoncheiresIdleTime = parentField.spriteStorage.hecatoncheiresDeflectAnimationDuration;
        hecatoncheiresAction = 2;
        hecatoncheiresAnimationStart = elapsedTime;
        hecatoncheiresWalkingAnimationStart = elapsedTime + parentField.spriteStorage.hecatoncheiresDeflectAnimationDuration;
    }

    public void spawnHecatoncheires(float elapsedTime, float x, float y) {
        hecatoncheiresX = x;
        hecatoncheiresY = y;
        hecatoncheiresIdleTime = parentField.spriteStorage.hecatoncheiresDeathAnimationDuration - 0.1f;
        hecatoncheiresAction = 0;
        hecatoncheiresAnimationStart = elapsedTime;
        hecatoncheiresWalkingAnimationStart = elapsedTime + parentField.spriteStorage.hecatoncheiresDeathAnimationDuration - 0.1f;
    }

    public void checkIfBossIsHit(float elapsedTime) {
        if(!targetedPlayer.isAttacking() || hecatoncheiresInvulnTime > 0 || !isAlive) {
            return;
        }
        float playerX = targetedPlayer.getX(), playerY = targetedPlayer.getY();
        int direction = targetedPlayer.getDirection(), damage = targetedPlayer.getDamage();
        float range = targetedPlayer.getRange();
        final float hitWidth = 0.15f + radius;
        boolean isHit = false;
        switch(direction) {
            case 1:
                if(hecatoncheiresX - playerX <= range && hecatoncheiresX - playerX > 0 && Math.abs(hecatoncheiresY - playerY) <= hitWidth) {
                    isHit = true;
                }
                break;
            case 3:
                if(playerX - hecatoncheiresX <= range && playerX - hecatoncheiresX > 0 && Math.abs(hecatoncheiresY - playerY) <= hitWidth) {
                    isHit = true;
                }
                break;
            case 2:
                if(hecatoncheiresY - playerY <= range && hecatoncheiresY - playerY > 0 && Math.abs(hecatoncheiresX - playerX) <= hitWidth) {
                    isHit = true;
                }
                break;
            case 0:
                if(playerY - hecatoncheiresY <= range && playerY - hecatoncheiresY > 0 && Math.abs(hecatoncheiresX - playerX) <= hitWidth) {
                    isHit = true;
                }
                break;
        }
        if(isHit && hecatoncheiresAction != 0 && hecatoncheiresAction != 1) {
            hurtHecatoncheires(damage, elapsedTime);
            hecatoncheiresInvulnTime = parentField.spriteStorage.playerAttackDuration[targetedPlayer.getPlayerType()] * 2;
        }
    }

    private void hurtHecatoncheires(float damage, float elapsedTime) {
        currentHealth -= damage;
        if(currentHealth <= 0 && isAlive && hecatoncheiresAction != 1) {
            killHecatoncheires(elapsedTime);
        }
    }

    public void checkCollisionWithProjectile(Projectile projectile, float elapsedTime) {
        if(!isAlive) {
            return;
        }
        if(projectile.checkRadialCollision(hecatoncheiresX, hecatoncheiresY, spinRadius) && hecatoncheiresAction == 3 &&
                hecatoncheiresIdleTime >= spinStart && hecatoncheiresIdleTime <= spinFinish) {
            projectile.explodeProjectile(elapsedTime);
        } else if(projectile.checkRadialCollision(hecatoncheiresX, hecatoncheiresY, deflectRadius) && hecatoncheiresAction == 2) {
            projectile.explodeProjectile(elapsedTime);
        } else if(projectile.checkRadialCollision(hecatoncheiresX, hecatoncheiresY, radius)) {
            hurtHecatoncheires(projectile.getDamage(), elapsedTime);
            projectile.explodeProjectile(elapsedTime);
        } else if(projectile.checkRadialCollision(hecatoncheiresX, hecatoncheiresY, deflectStartRadius) && hecatoncheiresAction < 0) {
            makeHecatoncheiresDeflect(elapsedTime);
        }
    }

    private void stopSound() {
        if(sound >= 0) {
            parentField.spriteStorage.hecatoncheiresSpinSound.stop(sound);
            parentField.spriteStorage.hecatoncheiresStepSound.stop(sound);
            parentField.spriteStorage.hecatoncheiresDeathSound.stop(sound);
            parentField.spriteStorage.hecatoncheiresDeflectSound.stop(sound);
        }
    }

    public boolean isAlive() {
        return isAlive;
    }
}
