package com.escapefromhades.Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.escapefromhades.GameStates.GameField;
import com.escapefromhades.RenderEngine.Renderer;

import java.io.BufferedReader;
import java.io.IOException;

public class Player {
    private GameField parentField;
    private final float invulnPeriod = 2f;
    private static boolean[] canAttack, attackRanged;
    private static int[] playerHealth, playerProjectile;
    private static float[] hitDistance, projectileLaunchFrame;
    private float playerX, playerY, oldPlayerX, oldPlayerY, playerIdleTime, remainingInvuln;
    private int playerType, currentHealth, playerDirection = 0, playerTileX, playerTileY;
    private boolean playerIsAlive, projectileLaunched = false;

    private int playerAction = -1;
    private float playerWalkingAnimationStart = 0, playerAnimationStart;

    final float margin = 0.05f;
    private final int[] playerWalkOffsetX = {0, 0, 0, 0}, playerWalkOffsetY = {2, 0, 2, 0};
    private final int[] playerAttackOffsetX = {0, 0, 0, 16}, playerAttackOffsetY = {16, 0, 16, 0};

    private final int[] keyWalkDefault = {Input.Keys.DOWN, Input.Keys.RIGHT, Input.Keys.UP, Input.Keys.LEFT};
    private int[] keyWalk;
    private final int keyAttackDefault = Input.Keys.X;
    private int keyAttack;

    private Sound hurtSound, deathSound, strikeSound, teleInSound, teleOutSound;

    public Player(GameField parentField, int playerType) {
        this.parentField = parentField;
        this.playerType = playerType;
        this.currentHealth = playerHealth[playerType];
        keyWalk = keyWalkDefault.clone();
        keyAttack = keyAttackDefault;
        loadSounds();
    }

    public static void preparePlayerValueArrays(int playerTypesNum) {
        playerHealth = new int[playerTypesNum];
        playerProjectile = new int[playerTypesNum];
        hitDistance = new float[playerTypesNum];
        projectileLaunchFrame = new float[playerTypesNum];
        canAttack = new boolean[playerTypesNum];
        attackRanged = new boolean[playerTypesNum];
    }

    public static void loadPlayerValues(final String charStatFile, BufferedReader reader, int i) {
        try {
            String line;
            line = reader.readLine();
            playerHealth[i] = Integer.valueOf(line);
            line = reader.readLine();
            canAttack[i] = !line.equals("noattack");
            attackRanged[i] = line.equals("ranged");
            line = reader.readLine();
            hitDistance[i] = Float.valueOf(line);
            line = reader.readLine();
            playerProjectile[i] = Integer.valueOf(line);
            line = reader.readLine();
            projectileLaunchFrame[i] = Float.valueOf(line);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + charStatFile);
        }
    }

    public void loadSounds() {
        hurtSound = Gdx.audio.newSound(Gdx.files.internal("sounds/player/" + parentField.spriteStorage.playerNames[playerType] + "_hurt.wav"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/player/" + parentField.spriteStorage.playerNames[playerType] + "_death.wav"));
        strikeSound = Gdx.audio.newSound(Gdx.files.internal("sounds/player/strike.wav"));
        teleInSound = Gdx.audio.newSound(Gdx.files.internal("sounds/player/tele_in.wav"));
        teleOutSound = Gdx.audio.newSound(Gdx.files.internal("sounds/player/tele_out.wav"));
    }

    public void setPosition(float x, float y) {
        playerX = x;
        playerY = y;
    }

    public void setAliveState(boolean playerIsAlive) {
        this.playerIsAlive = playerIsAlive;
    }

    public boolean isVulnerable() {
        return playerIsAlive && !(remainingInvuln > 0 || playerAction == 1 || playerAction == 0 || playerAction == 2);
    }

    public boolean isAlive() {
        return playerIsAlive;
    }

    public boolean isDying() {
        return playerAction == 1;
    }

    public boolean isAttacking() {
        return playerAction == 3;
    }

    public int getPlayerType() {
        return playerType;
    }

    public float getX() {
        return playerX;
    }

    public float getY() {
        return playerY;
    }

    public int getDamage() {
        return 1;
    }

    public int getDirection() {
        return playerDirection;
    }

    public float getRange() {
        return hitDistance[playerType];
    }

    public float getIdleTime() {
        return playerIdleTime;
    }

    public void renderPlayer(Renderer renderer) {
        renderHearts(renderer);
        int tileX = parentField.tileX, tileY = parentField.tileY, pixelSize = GlobalValues.pixelSize;
        float blinkingInterval = 0.15f;
        if(remainingInvuln > 0 && playerAction != 4) {
            if(remainingInvuln % blinkingInterval <= blinkingInterval / 2) {
                return;
            }
        }
        switch(playerAction) {
            case 0:
                renderer.addAnimationToSchedule(parentField.spriteStorage.playerSpawn[playerType], (int)(playerX * tileX) * pixelSize,
                        (int)(playerY * tileY) * pixelSize, pixelSize, false, 0, playerAnimationStart, false);
                break;
            case 1:
                renderer.addAnimationToSchedule(parentField.spriteStorage.playerDeath[playerType], (int)(playerX * tileX) * pixelSize,
                        (int)(playerY * tileY) * pixelSize, pixelSize, false, 0, playerAnimationStart, false);
                break;
            case 2:
                renderer.addAnimationToSchedule(parentField.spriteStorage.playerSpawn[playerType], (int)(playerX * tileX) * pixelSize,
                        (int)(playerY * tileY) * pixelSize, pixelSize, false, 0, playerAnimationStart, true);
                break;
            case 3:
                if(playerDirection < 3) {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.playerAttack[playerType][playerDirection],
                            (int)(playerX * tileX - playerAttackOffsetX[playerDirection]) * pixelSize,
                            (int)(playerY * tileY - playerAttackOffsetY[playerDirection]) * pixelSize,
                            pixelSize, false, 0, playerAnimationStart, false);
                } else {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.playerAttack[playerType][1],
                            (int)(playerX * tileX - playerAttackOffsetX[playerDirection]) * pixelSize,
                            (int)(playerY * tileY - playerAttackOffsetY[playerDirection]) * pixelSize,
                            pixelSize, true, 0, playerAnimationStart, false);
                }
                break;
            case 4:
                renderer.addAnimationToSchedule(parentField.spriteStorage.playerHurt[playerType], (int)(playerX * tileX) * pixelSize,
                        (int)(playerY * tileY) * pixelSize, pixelSize, false, 0, playerAnimationStart, true);
                break;
            default:
                if(playerDirection < 3) {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.playerWalk[playerType][playerDirection],
                            (int)(playerX * tileX - playerWalkOffsetX[playerDirection]) * pixelSize,
                            (int)(playerY * tileY - playerWalkOffsetY[playerDirection]) * pixelSize,
                            pixelSize, false, 0, playerWalkingAnimationStart, false);
                } else {
                    renderer.addAnimationToSchedule(parentField.spriteStorage.playerWalk[playerType][1],
                            (int)(playerX * tileX - playerWalkOffsetX[playerDirection]) * pixelSize,
                            (int)(playerY * tileY - playerWalkOffsetY[playerDirection]) * pixelSize,
                            pixelSize, true, 0, playerWalkingAnimationStart, false);
                }
        }
    }

    private void renderHearts(Renderer renderer) {
        for(int i = 0; i < currentHealth; i++) {
            renderer.addSpriteToSchedule(parentField.spriteStorage.heart, -parentField.spriteStorage.heartX * GlobalValues.pixelSize,
                    (parentField.getHeight() * parentField.tileY - parentField.spriteStorage.heartY * (i + 1)) * GlobalValues.pixelSize,
                    GlobalValues.pixelSize, 0, false);
        }
        for(int i = Math.max(currentHealth, 0); i < playerHealth[playerType]; i++) {
            renderer.addSpriteToSchedule(parentField.spriteStorage.emptyHeart, -parentField.spriteStorage.heartX * GlobalValues.pixelSize,
                    (parentField.getHeight() * parentField.tileY - parentField.spriteStorage.heartY * (i + 1)) * GlobalValues.pixelSize,
                    GlobalValues.pixelSize, 0, false);
        }
    }

    public void processPlayerMovement(float elapsedTime) {
        playerTileX = Math.round(playerX);
        playerTileY = Math.round(playerY);
        if(playerIdleTime <= 0) {
            parentField.stopTransition();
            if(currentHealth <= 0) {
                killPlayer(elapsedTime);
            }
            if(playerAction >= 0) {
                switch(playerAction) {
                    case 0:
                        playerIsAlive = true;
                        break;
                    case 1:
                        playerIsAlive = false;
                        break;
                    case 2:
                        parentField.loadNextMap(elapsedTime);
                        break;
                }
                playerAction = -1;
                playerWalkingAnimationStart = elapsedTime;
            }
            if(playerIsAlive) {
                processMovementInputs(elapsedTime);
            } else {
                respawnPlayer(elapsedTime);
            }
        } else {
            playerIdleTime -= Gdx.graphics.getDeltaTime();
            launchProjectile();
        }
        if(remainingInvuln > 0) {
            remainingInvuln -= Gdx.graphics.getDeltaTime();
        }
    }

    private void processMovementInputs(float elapsedTime) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            //killPlayer(elapsedTime);
        }
        if(Gdx.input.isKeyPressed(keyAttack)) {
            makePlayerAttack(elapsedTime);
        }
        if(Gdx.input.isKeyPressed(keyWalk[1])) {
            playerDirection = 1;
        } else if(Gdx.input.isKeyPressed(keyWalk[3])) {
            playerDirection = 3;
        } else if(Gdx.input.isKeyPressed(keyWalk[0])) {
            playerDirection = 0;
        } else if(Gdx.input.isKeyPressed(keyWalk[2])) {
            playerDirection = 2;
        }
        boolean playerMovesEast = false, playerMovesWest = false, playerMovesSouth = false, playerMovesNorth = false;
        if(Gdx.input.isKeyPressed(keyWalk[1])) {
            playerMovesEast = true;
            if(playerX - playerTileX > 0 && playerTileX < parentField.getWidth() - 1) {
                if(!parentField.getWall(playerTileX + 1, playerTileY)) {
                    if (playerTileY - playerY > margin && playerTileY > 0) {
                        if (parentField.getWall(playerTileX + 1, playerTileY - 1)) {
                            if (!Gdx.input.isKeyPressed(keyWalk[2]))
                                playerMovesNorth = true;
                        }
                    } else if (playerTileY - playerY < -margin && playerTileY < parentField.getHeight() - 1) {
                        if (parentField.getWall(playerTileX + 1, playerTileY + 1)) {
                            if (!Gdx.input.isKeyPressed(keyWalk[0]))
                                playerMovesSouth = true;
                        }
                    }
                }
            }
        }
        if(Gdx.input.isKeyPressed(keyWalk[3])) {
            playerMovesWest = true;
            if(playerTileX - playerX > 0 && playerTileX > 0) {
                if(!parentField.getWall(playerTileX - 1, playerTileY)) {
                    if(playerTileY - playerY > margin && playerTileY > 0) {
                        if (parentField.getWall(playerTileX - 1, playerTileY - 1)) {
                            if (!Gdx.input.isKeyPressed(keyWalk[2]))
                                playerMovesNorth = true;
                        }
                    } else if(playerTileY - playerY < -margin && playerTileY < parentField.getHeight() - 1) {
                        if (parentField.getWall(playerTileX - 1, playerTileY + 1)) {
                            if(!Gdx.input.isKeyPressed(keyWalk[0]))
                                playerMovesSouth = true;
                        }
                    }
                }
            }
        }
        if(Gdx.input.isKeyPressed(keyWalk[0])) {
            playerMovesSouth = true;
            if(playerTileY - playerY > 0 && playerTileY > 0) {
                if(!parentField.getWall(playerTileX, playerTileY - 1)) {
                    if (playerTileX - playerX > margin && playerTileX > 0) {
                        if (parentField.getWall(playerTileX - 1, playerTileY - 1)) {
                            if (!Gdx.input.isKeyPressed(keyWalk[3]))
                                playerMovesEast = true;
                        }
                    } else if (playerTileX - playerX < -margin && playerTileX < parentField.getWidth() - 1) {
                        if (parentField.getWall(playerTileX + 1, playerTileY - 1)) {
                            if (!Gdx.input.isKeyPressed(keyWalk[1]))
                                playerMovesWest = true;
                        }
                    }
                }
            }
        }
        if(Gdx.input.isKeyPressed(keyWalk[2])) {
            playerMovesNorth = true;
            if(playerY - playerTileY > 0 && playerTileY < parentField.getHeight() - 1) {
                if(!parentField.getWall(playerTileX, playerTileY + 1)) {
                    if (playerTileX - playerX > margin && playerTileX > 0) {
                        if (parentField.getWall(playerTileX - 1, playerTileY + 1)) {
                            if (!Gdx.input.isKeyPressed(keyWalk[3]))
                                playerMovesEast = true;
                        }
                    } else if (playerTileX - playerX < -margin && playerTileX < parentField.getWidth() - 1) {
                        if (parentField.getWall(playerTileX + 1, playerTileY + 1)) {
                            if (!Gdx.input.isKeyPressed(keyWalk[1]))
                                playerMovesWest = true;
                        }
                    }
                }
            }
        }
        movePlayer(elapsedTime, playerMovesEast, playerMovesWest, playerMovesSouth, playerMovesNorth);
    }

    private void movePlayer(float elapsedTime, boolean playerMovesEast, boolean playerMovesWest, boolean playerMovesSouth, boolean playerMovesNorth) {
        oldPlayerX = playerX;
        oldPlayerY = playerY;
        if(playerMovesSouth) {
            playerY -= Gdx.graphics.getDeltaTime() / parentField.spriteStorage.playerTimePerTile[playerType];
        }
        if(playerMovesNorth) {
            playerY += Gdx.graphics.getDeltaTime() / parentField.spriteStorage.playerTimePerTile[playerType];
        }
        constrainPlayerVertically();
        playerTileX = Math.round(playerX);
        playerTileY = Math.round(playerY);
        oldPlayerX = playerX;
        oldPlayerY = playerY;
        if(playerMovesWest) {
            playerX -= Gdx.graphics.getDeltaTime() / parentField.spriteStorage.playerTimePerTile[playerType];
        }
        if(playerMovesEast) {
            playerX += Gdx.graphics.getDeltaTime() / parentField.spriteStorage.playerTimePerTile[playerType];
        }
        constrainPlayerHorizontally();
        playerTileX = Math.round(playerX);
        playerTileY = Math.round(playerY);
        if(playerTileX == parentField.getFinishX() && playerTileY == parentField.getFinishY() && parentField.finishPortalOpen()) {
            finishLevel(elapsedTime);
        }
    }

    private void launchProjectile() {
        if(playerAction == 3) {
            if(attackRanged[playerType] &&
                    playerIdleTime <= parentField.spriteStorage.playerAttackDuration[playerType] - projectileLaunchFrame[playerType] &&
                    !projectileLaunched) {
                switch(playerDirection) {
                    case 0:
                        parentField.createFriendlyProjectile(playerProjectile[playerType], playerDirection, playerX + 0.5f, playerY - 0.5f);
                        break;
                    case 1:
                        parentField.createFriendlyProjectile(playerProjectile[playerType], playerDirection, playerX + 1.5f, playerY + 0.5f);
                        break;
                    case 2:
                        parentField.createFriendlyProjectile(playerProjectile[playerType], playerDirection, playerX + 0.5f, playerY + 1.5f);
                        break;
                    case 3:
                        parentField.createFriendlyProjectile(playerProjectile[playerType], playerDirection, playerX - 0.5f, playerY + 0.5f);
                        break;
                }
                projectileLaunched = true;
            }
        }
    }

    private void respawnPlayer(float elapsedTime) {
        teleOutSound.play(parentField.getSoundVolume());
        remainingInvuln = 0;
        playerX = parentField.getStartX();
        playerY = parentField.getStartY();
        currentHealth = playerHealth[playerType];
        playerIdleTime = parentField.spriteStorage.playerSpawnAnimationDuration[playerType];
        playerAction = 0;
        playerAnimationStart = elapsedTime;
        playerWalkingAnimationStart = elapsedTime + parentField.spriteStorage.playerSpawnAnimationDuration[playerType];
    }

    private void killPlayer(float elapsedTime) {
        remainingInvuln = 0;
        playerIdleTime = parentField.spriteStorage.playerDeathAnimationDuration[playerType];
        playerAction = 1;
        playerAnimationStart = elapsedTime;
        playerWalkingAnimationStart = elapsedTime + parentField.spriteStorage.playerDeathAnimationDuration[playerType];
    }

    private void finishLevel(float elapsedTime) {
        teleInSound.play(parentField.getSoundVolume());
        parentField.startTransition();
        playerIdleTime = parentField.spriteStorage.playerSpawnAnimationDuration[playerType];
        playerAction = 2;
        playerAnimationStart = elapsedTime;
        playerWalkingAnimationStart = elapsedTime + parentField.spriteStorage.playerSpawnAnimationDuration[playerType];
    }

    private void makePlayerAttack(float elapsedTime) {
        if(!canAttack[playerType]) {
            return;
        }
        strikeSound.play(parentField.getSoundVolume());
        playerIdleTime = parentField.spriteStorage.playerAttackDuration[playerType];
        playerAction = 3;
        playerAnimationStart = elapsedTime;
        playerWalkingAnimationStart = elapsedTime + parentField.spriteStorage.playerAttackDuration[playerType];
        projectileLaunched = false;
    }

    public void hurtPlayer(float elapsedTime, int damage) {
        if(damage > 0) {
            currentHealth -= damage;
            if(currentHealth > 0) {
                hurtSound.play(parentField.getSoundVolume());
            } else {
                deathSound.play(parentField.getSoundVolume());
            }
            remainingInvuln = invulnPeriod;
            playerIdleTime = parentField.spriteStorage.playerHurtAnimationDuration[playerType];
            playerAction = 4;
            playerAnimationStart = elapsedTime;
            playerWalkingAnimationStart = elapsedTime + parentField.spriteStorage.playerHurtAnimationDuration[playerType];
            if(currentHealth <= 0) {
                killPlayer(elapsedTime);
            }
        }
    }

    private void constrainPlayerHorizontally() {
        playerX = Math.max(playerX, 0);
        playerX = Math.min(playerX, parentField.getHeight() - 1);
        if(playerTileY - playerY > margin && playerTileY > 0) {
            if(playerTileX - playerX > margin && playerTileX > 0) {
                if (parentField.getWall(playerTileX - 1, playerTileY - 1)) {
                    playerX = oldPlayerX;
                }
            }
            if(playerTileX - playerX < -margin && playerTileX < parentField.getWidth() - 1) {
                if (parentField.getWall(playerTileX + 1, playerTileY - 1)) {
                    playerX = oldPlayerX;
                }
            }
        }
        if(playerTileY - playerY < -margin && playerTileY < parentField.getHeight() - 1) {
            if(playerTileX - playerX > margin && playerTileX > 0) {
                if (parentField.getWall(playerTileX - 1, playerTileY + 1)) {
                    playerX = oldPlayerX;
                }
            }
            if(playerTileX - playerX < -margin && playerTileX < parentField.getWidth() - 1) {
                if (parentField.getWall(playerTileX + 1, playerTileY + 1)) {
                    playerX = oldPlayerX;
                }
            }
        }
        if(playerTileX - playerX > margin && playerTileX > 0) {
            if (parentField.getWall(playerTileX - 1, playerTileY)) {
                playerX = oldPlayerX;
            }
        }
        if(playerTileX - playerX < -margin && playerTileX < parentField.getWidth() - 1) {
            if (parentField.getWall(playerTileX + 1, playerTileY)) {
                playerX = oldPlayerX;
            }
        }
    }

    private void constrainPlayerVertically() {
        playerY = Math.max(playerY, 0);
        playerY = Math.min(playerY, parentField.getHeight() - 1);
        if(playerTileX - playerX > margin && playerTileX > 0) {
            if(playerTileY - playerY > margin && playerTileY > 0) {
                if (parentField.getWall(playerTileX - 1, playerTileY - 1)) {
                    playerY = oldPlayerY;
                }
            }
            if(playerTileY - playerY < -margin && playerTileY < parentField.getHeight() - 1) {
                if (parentField.getWall(playerTileX - 1, playerTileY + 1)) {
                    playerY = oldPlayerY;
                }
            }
        }
        if(playerTileX - playerX < -margin && playerTileX < parentField.getWidth() - 1) {
            if(playerTileY - playerY > margin && playerTileY > 0) {
                if (parentField.getWall(playerTileX + 1, playerTileY - 1)) {
                    playerY = oldPlayerY;
                }
            }
            if(playerTileY - playerY < -margin && playerTileY < parentField.getHeight() - 1) {
                if (parentField.getWall(playerTileX + 1, playerTileY + 1)) {
                    playerY = oldPlayerY;
                }
            }
        }
        if(playerTileY - playerY > margin && playerTileY > 0) {
            if (parentField.getWall(playerTileX, playerTileY - 1)) {
                playerY = oldPlayerY;
            }
        }
        if(playerTileY - playerY < -margin && playerTileY < parentField.getHeight() - 1) {
            if (parentField.getWall(playerTileX, playerTileY + 1)) {
                playerY = oldPlayerY;
            }
        }
    }

    public void checkCollisionWithMonster(Monster monster, float elapsedTime) {
        float mX = monster.getX(), mY = monster.getY();
        if(Math.sqrt(Math.pow(playerX - mX, 2) + Math.pow(playerY - mY, 2)) <= monster.getCollisionDistance() + 0.35f && remainingInvuln <= 0) {
            hurtPlayer(elapsedTime, monster.getDamage());
        }
    }

    public void checkHitWithMonster(Monster monster, float elapsedTime) {
        if(playerAction != 3) {
            return;
        }
        float mX = monster.getX(), mY = monster.getY();
        final float hitWidth = 0.25f;
        boolean isHit = false;
        switch(playerDirection) {
            case 1:
                if(mX - playerX <= hitDistance[playerType] && mX - playerX > 0 && Math.abs(mY - playerY) <= hitWidth) {
                    isHit = true;
                }
                break;
            case 3:
                if(playerX - mX <= hitDistance[playerType] && playerX - mX > 0 && Math.abs(mY - playerY) <= hitWidth) {
                    isHit = true;
                }
                break;
            case 2:
                if(mY - playerY <= hitDistance[playerType] && mY - playerY > 0 && Math.abs(mX - playerX) <= hitWidth) {
                    isHit = true;
                }
                break;
            case 0:
                if(playerY - mY <= hitDistance[playerType] && playerY - mY > 0 && Math.abs(mX - playerX) <= hitWidth) {
                    isHit = true;
                }
                break;
        }
        if(isHit && monster.isDangerous()) {
            monster.hurtMonster(1);
            if(monster.getHealth() <= 0) {
                monster.killMonster(elapsedTime);
            }
        }
    }

    public void checkCollisionWithTrap(Trap trap, float elapsedTime) {
        if(trap.checkCollision(playerX, playerY)) {
            hurtPlayer(elapsedTime, trap.getDamage());
        }
    }

    public void checkCollisionWithProjectile(Projectile projectile, float elapsedTime) {
        if(projectile.checkCollision(playerX, playerY, 0.35f)) {
            hurtPlayer(elapsedTime, projectile.getDamage());
            projectile.explodeProjectile(elapsedTime);
        }
    }
}
