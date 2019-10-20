package com.escapefromhades.RenderEngine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.io.BufferedReader;
import java.io.IOException;

public class SpriteStorage {

    public int playerTypesNum, monsterTypesNum, trapTypesNum, projectileTypesNum;
    public String[] playerNames, monsterNames, trapNames, projectileNames;
    public String hecatoncheiresName, odysseusName;

    private int[] playerWalkAnimationFrames, playerAttackAnimationFrames, playerSpawnAnimationFrames,
            playerDeathAnimationFrames, playerHurtAnimationFrames;
    public float[] playerTimePerTile, playerAttackDuration, playerSpawnAnimationDuration, playerDeathAnimationDuration,
            playerHurtAnimationDuration;
    public Animation<TextureRegion>[][] playerWalk, playerAttack;
    public Animation<TextureRegion>[] playerSpawn, playerDeath, playerHurt;

    public int[] monsterWalkAnimationFrames, monsterAttackAnimationFrames, monsterSpawnAnimationFrames, monsterDeathAnimationFrames;
    public float[] monsterTimePerTile, monsterAttackAnimationDuration, monsterSpawnAnimationDuration, monsterDeathAnimationDuration;
    public Animation<TextureRegion>[][] monsterWalk, monsterAttack;
    public Animation<TextureRegion>[] monsterSpawn, monsterDeath;
    public boolean[] monsterAnimationsExtended;

    private int[] trapWarningFrames, trapActiveFrames;
    private float[] trapWarningDuration, trapActiveDuration;
    public Animation<TextureRegion>[][] trapWarning, trapActive;
    public Sprite[][] trapIdle;
    public boolean[] trapDirected;

    private int[] projectileFlyingFrames, projectileExplosionFrames;
    private float[] projectileFlyingDuration;
    public float[] projectileTimePerTile, projectileExplosionDuration;
    public Animation<TextureRegion>[][] projectileFlying, projectileExplosion;
    public boolean[] projectileDirected;

    private int hecatoncheiresWalkAnimationFrames, hecatoncheiresSpinAnimationFrames, hecatoncheiresDeflectAnimationFrames,
            hecatoncheiresDeathAnimationFrames;
    public float hecatoncheiresTimePerTile, hecatoncheiresSpinAnimationDuration, hecatoncheiresDeflectAnimationDuration,
            hecatoncheiresDeathAnimationDuration;
    public Animation<TextureRegion>[] hecatoncheiresWalk;
    public Animation<TextureRegion> hecatoncheiresSpin, hecatoncheiresDeflect, hecatoncheiresDeath;

    private int odysseusTeleportAnimationFrames, odysseusShotAnimationFrames, odysseusTripleShotAnimationFrames,
            odysseusDeathAnimationFrames;
    public float odysseusTeleportAnimationDuration, odysseusShotAnimationDuration, odysseusTripleShotAnimationDuration,
            odysseusDeathAnimationDuration;
    public Animation<TextureRegion>[] odysseusShot, odysseusTripleShot;
    public Animation<TextureRegion> odysseusSpawn, odysseusTeleport, odysseusDeath;
    public Sprite odysseusTeleporter;

    public Sound[] monsterDeathSounds, monsterAttackSounds, monsterSpawnSounds, trapSounds, projectileExplosionSounds;
    public boolean[] monsterDeathSoundIsPlaying, monsterAttackSoundIsPlaying, monsterSpawnSoundIsPlaying,
            trapSoundIsPlaying, projectileSoundIsPlaying;

    public Sound hecatoncheiresSpinSound, hecatoncheiresDeflectSound, hecatoncheiresDeathSound, hecatoncheiresStepSound;
    public boolean hecatoncheiresSpinSoundIsPlaying, hecatoncheiresDeflectSoundIsPlaying, hecatoncheiresDeathSoundIsPlaying,
            hecatoncheiresStepSoundIsPlaying;

    public Sound odysseusShotSound, odysseusSpawnSound, odysseusTeleportSound, odysseusDeathSound;
    public boolean odysseusShotSoundIsPlaying, odysseusSpawnSoundIsPlaying, odysseusTeleportSoundIsPlaying,
            odysseusDeathSoundIsPlaying;

    public Sprite heart, emptyHeart, hecatoncheiresHeart, hecatoncheiresEmptyHeart, odysseusHeart, odysseusEmptyHeart;
    public final float heartX = 12, heartY = 12;

    public SpriteStorage() {}

    public void preparePlayerValueArrays(int playerTypesNum) {
        this.playerTypesNum = playerTypesNum;
        playerNames = new String[playerTypesNum];
        playerTimePerTile = new float[playerTypesNum];
        playerAttackDuration = new float[playerTypesNum];
        playerSpawnAnimationDuration = new float[playerTypesNum];
        playerDeathAnimationDuration = new float[playerTypesNum];
        playerHurtAnimationDuration = new float[playerTypesNum];
        playerWalkAnimationFrames = new int[playerTypesNum];
        playerAttackAnimationFrames = new int[playerTypesNum];
        playerSpawnAnimationFrames = new int[playerTypesNum];
        playerDeathAnimationFrames = new int[playerTypesNum];
        playerHurtAnimationFrames = new int[playerTypesNum];
    }

    public void prepareMonsterValueArrays(int monsterTypesNum) {
        this.monsterTypesNum = monsterTypesNum;
        monsterNames = new String[monsterTypesNum];
        monsterTimePerTile = new float[monsterTypesNum];
        monsterDeathAnimationDuration = new float[monsterTypesNum];
        monsterSpawnAnimationDuration = new float[monsterTypesNum];
        monsterAttackAnimationDuration = new float[monsterTypesNum];
        monsterWalkAnimationFrames = new int[monsterTypesNum];
        monsterDeathAnimationFrames = new int[monsterTypesNum];
        monsterSpawnAnimationFrames = new int[monsterTypesNum];
        monsterAttackAnimationFrames = new int[monsterTypesNum];
        monsterAnimationsExtended = new boolean[monsterTypesNum];
    }

    public void prepareTrapValueArrays(int trapTypesNum) {
        this.trapTypesNum = trapTypesNum;
        trapNames = new String[trapTypesNum];
        trapWarningDuration = new float[trapTypesNum];
        trapActiveDuration = new float[trapTypesNum];
        trapWarningFrames = new int[trapTypesNum];
        trapActiveFrames = new int[trapTypesNum];
        trapDirected = new boolean[trapTypesNum];
    }

    public void prepareProjectileValueArrays(int projectileTypesNum) {
        this.projectileTypesNum = projectileTypesNum;
        projectileNames = new String[projectileTypesNum];
        projectileTimePerTile = new float[projectileTypesNum];
        projectileFlyingDuration = new float[projectileTypesNum];
        projectileExplosionDuration = new float[projectileTypesNum];
        projectileFlyingFrames = new int[projectileTypesNum];
        projectileExplosionFrames = new int[projectileTypesNum];
        projectileDirected = new boolean[projectileTypesNum];
    }

    public void loadPlayerValues(final String charStatFile, BufferedReader reader, int i) {
        try {
            String line;
            String[] currentLine;
            line = reader.readLine();
            playerNames[i] = line;
            line = reader.readLine();
            currentLine = line.split(" ");
            playerTimePerTile[i] = Float.valueOf(currentLine[0]);
            playerAttackDuration[i] = Float.valueOf(currentLine[1]);
            playerSpawnAnimationDuration[i] = Float.valueOf(currentLine[2]);
            playerDeathAnimationDuration[i] = Float.valueOf(currentLine[3]);
            playerHurtAnimationDuration[i] = Float.valueOf(currentLine[4]);
            line = reader.readLine();
            currentLine = line.split(" ");
            playerWalkAnimationFrames[i] = Integer.valueOf(currentLine[0]);
            playerAttackAnimationFrames[i] = Integer.valueOf(currentLine[1]);
            playerSpawnAnimationFrames[i] = Integer.valueOf(currentLine[2]);
            playerDeathAnimationFrames[i] = Integer.valueOf(currentLine[3]);
            playerHurtAnimationFrames[i] = Integer.valueOf(currentLine[4]);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + charStatFile);
        }
    }

    public void loadMonsterValues(final String monsterStatsFile, BufferedReader reader, int i) {
        try {
            String line;
            String[] currentLine;
            line = reader.readLine();
            monsterNames[i] = line;
            line = reader.readLine();
            monsterAnimationsExtended[i] = line.equals("extended");
            line = reader.readLine();
            currentLine = line.split(" ");
            monsterTimePerTile[i] = Float.valueOf(currentLine[0]);
            monsterDeathAnimationDuration[i] = Float.valueOf(currentLine[1]);
            if(monsterAnimationsExtended[i]) {
                monsterSpawnAnimationDuration[i] = Float.valueOf(currentLine[2]);
                monsterAttackAnimationDuration[i] = Float.valueOf(currentLine[3]);
            }
            line = reader.readLine();
            currentLine = line.split(" ");
            monsterWalkAnimationFrames[i] = Integer.valueOf(currentLine[0]);
            monsterDeathAnimationFrames[i] = Integer.valueOf(currentLine[1]);
            if(monsterAnimationsExtended[i]) {
                monsterSpawnAnimationFrames[i] = Integer.valueOf(currentLine[2]);
                monsterAttackAnimationFrames[i] = Integer.valueOf(currentLine[3]);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + monsterStatsFile);
        }
    }

    public void loadTrapValues(final String trapStatsFile, BufferedReader reader, int i) {
        try {
            String line;
            String[] currentLine;
            line = reader.readLine();
            trapNames[i] = line;
            line = reader.readLine();
            trapDirected[i] = line.equals("directed");
            line = reader.readLine();
            currentLine = line.split(" ");
            trapWarningDuration[i] = Float.valueOf(currentLine[0]);
            trapActiveDuration[i] = Float.valueOf(currentLine[1]);
            line = reader.readLine();
            currentLine = line.split(" ");
            trapWarningFrames[i] = Integer.valueOf(currentLine[0]);
            trapActiveFrames[i] = Integer.valueOf(currentLine[1]);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + trapStatsFile);
        }
    }

    public void loadProjectileValues(final String projectileStatsFile, BufferedReader reader, int i) {
        try {
            String line;
            String[] currentLine;
            line = reader.readLine();
            projectileNames[i] = line;
            line = reader.readLine();
            projectileDirected[i] = line.equals("directed");
            line = reader.readLine();
            currentLine = line.split(" ");
            projectileFlyingDuration[i] = Float.valueOf(currentLine[0]);
            projectileExplosionDuration[i] = Float.valueOf(currentLine[1]);
            line = reader.readLine();
            currentLine = line.split(" ");
            projectileFlyingFrames[i] = Integer.valueOf(currentLine[0]);
            projectileExplosionFrames[i] = Integer.valueOf(currentLine[1]);
            line = reader.readLine();
            projectileTimePerTile[i] = Float.valueOf(line);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + projectileStatsFile);
        }
    }


    public void loadHecatoncheiresValues(final String monsterStatsFile, BufferedReader reader) {
        try {
            String line;
            String[] currentLine;
            line = reader.readLine();
            hecatoncheiresName = line;
            line = reader.readLine();
            currentLine = line.split(" ");
            hecatoncheiresTimePerTile = Float.valueOf(currentLine[0]);
            hecatoncheiresSpinAnimationDuration = Float.valueOf(currentLine[1]);
            hecatoncheiresDeflectAnimationDuration = Float.valueOf(currentLine[2]);
            hecatoncheiresDeathAnimationDuration = Float.valueOf(currentLine[3]);
            line = reader.readLine();
            currentLine = line.split(" ");
            hecatoncheiresWalkAnimationFrames = Integer.valueOf(currentLine[0]);
            hecatoncheiresSpinAnimationFrames = Integer.valueOf(currentLine[1]);
            hecatoncheiresDeflectAnimationFrames = Integer.valueOf(currentLine[2]);
            hecatoncheiresDeathAnimationFrames = Integer.valueOf(currentLine[3]);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + monsterStatsFile);
        }
    }


    public void loadOdysseusValues(final String monsterStatsFile, BufferedReader reader) {
        try {
            String line;
            String[] currentLine;
            line = reader.readLine();
            odysseusName = line;
            line = reader.readLine();
            currentLine = line.split(" ");
            odysseusShotAnimationDuration = Float.valueOf(currentLine[0]);
            odysseusTripleShotAnimationDuration = Float.valueOf(currentLine[1]);
            odysseusTeleportAnimationDuration = Float.valueOf(currentLine[2]);
            odysseusDeathAnimationDuration = Float.valueOf(currentLine[3]);
            line = reader.readLine();
            currentLine = line.split(" ");
            odysseusShotAnimationFrames = Integer.valueOf(currentLine[0]);
            odysseusTripleShotAnimationFrames = Integer.valueOf(currentLine[1]);
            odysseusTeleportAnimationFrames = Integer.valueOf(currentLine[2]);
            odysseusDeathAnimationFrames = Integer.valueOf(currentLine[3]);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + monsterStatsFile);
        }
    }

    public void doLoading(Renderer renderer, TextureAtlas masterAtlas) {
        loadPlayer(renderer, masterAtlas);
        loadMonsters(renderer, masterAtlas);
        loadTraps(renderer, masterAtlas);
        loadProjectiles(renderer, masterAtlas);
        loadHearts(renderer, masterAtlas);
        loadSounds();
    }

    private void loadPlayer(Renderer renderer, TextureAtlas masterAtlas) {
        String[] directionSuffix = {"_south", "_east", "_north"};
        playerWalk = new Animation[playerTypesNum][3];
        playerAttack = new Animation[playerTypesNum][3];
        playerSpawn = new Animation[playerTypesNum];
        playerDeath = new Animation[playerTypesNum];
        playerHurt = new Animation[playerTypesNum];
        for(int k = 0; k < playerTypesNum; k++) {
            for(int i = 0; i < 3; i++) {
                playerWalk[k][i] = renderer.addAnimation(masterAtlas, playerNames[k] + "_walk" + directionSuffix[i],
                        playerTimePerTile[k] / playerWalkAnimationFrames[k], playerWalkAnimationFrames[k], Animation.PlayMode.LOOP);
                playerAttack[k][i] = renderer.addAnimation(masterAtlas, playerNames[k] + "_hit" + directionSuffix[i],
                        playerAttackDuration[k] / playerAttackAnimationFrames[k], playerAttackAnimationFrames[k], Animation.PlayMode.NORMAL);
            }
            playerSpawn[k] = renderer.addAnimation(masterAtlas, playerNames[k] + "_spawn",
                    playerSpawnAnimationDuration[k] / playerSpawnAnimationFrames[k], playerSpawnAnimationFrames[k], Animation.PlayMode.NORMAL);
            playerDeath[k] = renderer.addAnimation(masterAtlas, playerNames[k] + "_die",
                    playerDeathAnimationDuration[k] / playerDeathAnimationFrames[k], playerDeathAnimationFrames[k], Animation.PlayMode.NORMAL);
            playerHurt[k] = renderer.addAnimation(masterAtlas, playerNames[k] + "_hurt",
                    playerHurtAnimationDuration[k] / playerHurtAnimationFrames[k], playerHurtAnimationFrames[k], Animation.PlayMode.NORMAL);
        }
    }

    private void loadMonsters(Renderer renderer, TextureAtlas masterAtlas) {
        String[] directionSuffix = {"_south", "_east", "_north"};
        monsterWalk = new Animation[monsterTypesNum][3];
        monsterAttack = new Animation[monsterTypesNum][3];
        monsterSpawn = new Animation[monsterTypesNum];
        monsterDeath = new Animation[monsterTypesNum];
        for(int k = 0; k < monsterTypesNum; k++) {
            for(int i = 0; i < 3; i++) {
                monsterWalk[k][i] = renderer.addAnimation(masterAtlas, monsterNames[k] + "_walk" + directionSuffix[i],
                        monsterTimePerTile[k] / monsterWalkAnimationFrames[k], monsterWalkAnimationFrames[k], Animation.PlayMode.LOOP);
                monsterAttack[k][i] = renderer.addAnimation(masterAtlas, monsterNames[k] + "_hit" + directionSuffix[i],
                        monsterAttackAnimationDuration[k] / monsterAttackAnimationFrames[k], monsterAttackAnimationFrames[k], Animation.PlayMode.LOOP);
            }
            monsterSpawn[k] = renderer.addAnimation(masterAtlas, monsterNames[k] + "_spawn",
                    monsterSpawnAnimationDuration[k] / monsterSpawnAnimationFrames[k], monsterSpawnAnimationFrames[k], Animation.PlayMode.NORMAL);
            monsterDeath[k] = renderer.addAnimation(masterAtlas, monsterNames[k] + "_die",
                    monsterDeathAnimationDuration[k] / monsterDeathAnimationFrames[k], monsterDeathAnimationFrames[k], Animation.PlayMode.NORMAL);
        }
    }

    private void loadTraps(Renderer renderer, TextureAtlas masterAtlas) {
        String[] directionSuffix = {"_south", "_east", "_north"};
        trapIdle = new Sprite[trapTypesNum][3];
        trapWarning = new Animation[trapTypesNum][3];
        trapActive = new Animation[trapTypesNum][3];
        for(int k = 0; k < trapTypesNum; k++) {
            if(trapDirected[k]) {
                for(int i = 0; i < 3; i++) {
                    trapIdle[k][i] = renderer.addSprite(masterAtlas, trapNames[k] + "_idle" + directionSuffix[i]);
                    trapWarning[k][i] = renderer.addAnimation(masterAtlas, trapNames[k] + "_warning" + directionSuffix[i],
                            trapWarningDuration[k] / trapWarningFrames[k],
                            trapWarningFrames[k], Animation.PlayMode.LOOP);
                    trapActive[k][i] = renderer.addAnimation(masterAtlas, trapNames[k] + "_active" + directionSuffix[i],
                            trapActiveDuration[k] / trapActiveFrames[k],
                            trapActiveFrames[k], Animation.PlayMode.LOOP);
                }
            } else {
                trapIdle[k][0] = renderer.addSprite(masterAtlas, trapNames[k] + "_idle");
                trapIdle[k][1] = trapIdle[k][0];
                trapIdle[k][2] = trapIdle[k][0];
                trapWarning[k][0] = renderer.addAnimation(masterAtlas, trapNames[k] + "_warning",
                        trapWarningDuration[k] / trapWarningFrames[k],
                        trapWarningFrames[k], Animation.PlayMode.LOOP);
                trapWarning[k][1] = trapWarning[k][0];
                trapWarning[k][2] = trapWarning[k][0];
                trapActive[k][0] = renderer.addAnimation(masterAtlas, trapNames[k] + "_active",
                        trapActiveDuration[k] / trapActiveFrames[k],
                        trapActiveFrames[k], Animation.PlayMode.LOOP);
                trapActive[k][1] = trapActive[k][0];
                trapActive[k][2] = trapActive[k][0];
            }
        }
    }

    private void loadProjectiles(Renderer renderer, TextureAtlas masterAtlas) {
        String[] directionSuffix = {"_south", "_east", "_north"};
        projectileFlying = new Animation[projectileTypesNum][3];
        projectileExplosion = new Animation[projectileTypesNum][3];
        for(int k = 0; k < projectileTypesNum; k++) {
            if(projectileDirected[k]) {
                for(int i = 0; i < 3; i++) {
                    projectileFlying[k][i] = renderer.addAnimation(masterAtlas, projectileNames[k] + directionSuffix[i],
                            projectileFlyingDuration[k] / projectileFlyingFrames[k], projectileFlyingFrames[k], Animation.PlayMode.LOOP);
                    projectileExplosion[k][i] = renderer.addAnimation(masterAtlas, projectileNames[k] + "_hit" + directionSuffix[i],
                            projectileExplosionDuration[k] / projectileExplosionFrames[k], projectileExplosionFrames[k], Animation.PlayMode.NORMAL);
                }
            } else {
                projectileFlying[k][0] = renderer.addAnimation(masterAtlas, projectileNames[k],
                        projectileFlyingDuration[k] / projectileFlyingFrames[k], projectileFlyingFrames[k], Animation.PlayMode.LOOP);
                projectileFlying[k][1] = projectileFlying[k][0];
                projectileFlying[k][2] = projectileFlying[k][0];
                projectileExplosion[k][0] = renderer.addAnimation(masterAtlas, projectileNames[k] + "_hit",
                        projectileExplosionDuration[k] / projectileExplosionFrames[k], projectileExplosionFrames[k], Animation.PlayMode.NORMAL);
                projectileExplosion[k][1] = projectileExplosion[k][0];
                projectileExplosion[k][2] = projectileExplosion[k][0];
            }
        }
    }

    public void loadHecatoncheires(Renderer renderer, TextureAtlas bossAtlas) {
        String[] directionSuffix = {"_south", "_east", "_north"};
        hecatoncheiresWalk = new Animation[3];
        for(int i = 0; i < 3; i++) {
            hecatoncheiresWalk[i] = renderer.addAnimation(bossAtlas, hecatoncheiresName + "_walk" + directionSuffix[i],
                    hecatoncheiresTimePerTile / hecatoncheiresWalkAnimationFrames, hecatoncheiresWalkAnimationFrames, Animation.PlayMode.LOOP);
        }
        hecatoncheiresSpin = renderer.addAnimation(bossAtlas, hecatoncheiresName + "_spin",
                hecatoncheiresSpinAnimationDuration / hecatoncheiresSpinAnimationFrames, hecatoncheiresSpinAnimationFrames, Animation.PlayMode.NORMAL);
        hecatoncheiresDeflect = renderer.addAnimation(bossAtlas, hecatoncheiresName + "_spin_quick",
                hecatoncheiresDeflectAnimationDuration / hecatoncheiresDeflectAnimationFrames, hecatoncheiresDeflectAnimationFrames, Animation.PlayMode.NORMAL);
        hecatoncheiresDeath = renderer.addAnimation(bossAtlas, hecatoncheiresName + "_die",
                hecatoncheiresDeathAnimationDuration / hecatoncheiresDeathAnimationFrames, hecatoncheiresDeathAnimationFrames, Animation.PlayMode.NORMAL);
        hecatoncheiresSpinSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bosses/" + hecatoncheiresName + "_spin.wav"));
        hecatoncheiresDeflectSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bosses/" + hecatoncheiresName + "_deflect.wav"));
        hecatoncheiresDeathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bosses/" + hecatoncheiresName + "_death.wav"));
        hecatoncheiresStepSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bosses/" + hecatoncheiresName + "_step.wav"));
    }

    public void loadOdysseus(Renderer renderer, TextureAtlas bossAtlas) {
        String[] directionSuffix = {"_south", "_east", "_north"};
        odysseusShot = new Animation[3];
        odysseusTripleShot = new Animation[3];
        for(int i = 0; i < 3; i++) {
            odysseusShot[i] = renderer.addAnimation(bossAtlas, odysseusName + "_attack" + directionSuffix[i],
                    odysseusShotAnimationDuration / odysseusShotAnimationFrames, odysseusShotAnimationFrames, Animation.PlayMode.LOOP);
            odysseusTripleShot[i] = renderer.addAnimation(bossAtlas, odysseusName + "_attack_triple" + directionSuffix[i],
                    odysseusTripleShotAnimationDuration / odysseusTripleShotAnimationFrames, odysseusTripleShotAnimationFrames, Animation.PlayMode.LOOP);
        }
        odysseusSpawn = renderer.addAnimation(bossAtlas, odysseusName + "_spawn",
                odysseusTeleportAnimationDuration / odysseusTeleportAnimationFrames, odysseusTeleportAnimationFrames, Animation.PlayMode.NORMAL);
        odysseusTeleport = renderer.addAnimation(bossAtlas, odysseusName + "_teleport",
                odysseusTeleportAnimationDuration / odysseusTeleportAnimationFrames, odysseusTeleportAnimationFrames, Animation.PlayMode.NORMAL);
        odysseusDeath = renderer.addAnimation(bossAtlas, odysseusName + "_die",
                odysseusDeathAnimationDuration / odysseusDeathAnimationFrames, odysseusDeathAnimationFrames, Animation.PlayMode.NORMAL);
        odysseusTeleporter = renderer.addSprite(bossAtlas, odysseusName + "_platform");
        odysseusShotSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bosses/" + odysseusName + "_shot.wav"));
        odysseusSpawnSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bosses/" + odysseusName + "_spawn.wav"));
        odysseusTeleportSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bosses/" + odysseusName + "_teleport.wav"));
        odysseusDeathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bosses/" + odysseusName + "_death.wav"));
    }

    private void loadSounds() {
        monsterDeathSounds = new Sound[monsterTypesNum];
        monsterSpawnSounds = new Sound[monsterTypesNum];
        monsterAttackSounds = new Sound[monsterTypesNum];
        monsterDeathSoundIsPlaying = new boolean[monsterTypesNum];
        monsterSpawnSoundIsPlaying = new boolean[monsterTypesNum];
        monsterAttackSoundIsPlaying = new boolean[monsterTypesNum];
        trapSounds = new Sound[trapTypesNum];
        trapSoundIsPlaying = new boolean[trapTypesNum];
        projectileExplosionSounds = new Sound[projectileTypesNum];
        projectileSoundIsPlaying = new boolean[projectileTypesNum];
        for(int k = 0; k < monsterTypesNum; k++) {
            monsterDeathSounds[k] = Gdx.audio.newSound(Gdx.files.internal("sounds/monsters/" + monsterNames[k] + "_death.wav"));
            if(monsterAnimationsExtended[k]) {
                monsterSpawnSounds[k] = Gdx.audio.newSound(Gdx.files.internal("sounds/monsters/" + monsterNames[k] + "_spawn.wav"));
                monsterAttackSounds[k] = Gdx.audio.newSound(Gdx.files.internal("sounds/monsters/" + monsterNames[k] + "_hit.wav"));
            }
        }
        for(int k = 0; k < trapTypesNum; k++) {
            trapSounds[k] = Gdx.audio.newSound(Gdx.files.internal("sounds/traps/" + trapNames[k] + ".wav"));
        }
        for(int k = 0; k < projectileTypesNum; k++) {
            projectileExplosionSounds[k] = Gdx.audio.newSound(Gdx.files.internal("sounds/projectiles/" + projectileNames[k] + ".wav"));
        }
    }

    private void loadHearts(Renderer renderer, TextureAtlas masterAtlas) {
        heart = renderer.addSprite(masterAtlas, "heart");
        emptyHeart = renderer.addSprite(masterAtlas, "emptyheart");
        hecatoncheiresHeart = renderer.addSprite(masterAtlas, hecatoncheiresName + "_heart");
        hecatoncheiresEmptyHeart = renderer.addSprite(masterAtlas, hecatoncheiresName + "_emptyheart");
        odysseusHeart = renderer.addSprite(masterAtlas, odysseusName + "_heart");
        odysseusEmptyHeart = renderer.addSprite(masterAtlas, odysseusName + "_emptyheart");
    }
}
