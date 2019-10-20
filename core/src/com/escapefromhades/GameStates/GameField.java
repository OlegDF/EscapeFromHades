package com.escapefromhades.GameStates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.escapefromhades.Entities.*;
import com.escapefromhades.Entities.Bosses.Hecatoncheires;
import com.escapefromhades.Entities.Bosses.Odysseus;
import com.escapefromhades.RenderEngine.MusicStorage;
import com.escapefromhades.RenderEngine.Renderer;
import com.escapefromhades.RenderEngine.SpriteStorage;

import java.io.*;
import java.util.ArrayList;

public class GameField {
    private Renderer renderer;
    private MusicStorage musicStorage;
    public SpriteStorage spriteStorage;

    private float elapsedTime = 0;

    public final int tileX = 16, tileY = 16;
    private final int wallOffset = 3;
    private float timeTaken = 0;
    private boolean transitionInProgress = false, characterHasBeenChosen = false, playerHasWon = false, continueChosen = true;
    private int stageNum, mapNum, width = 12, height = 12, startX = -1, startY = -1, finishX = -1, finishY = -1;
    public int bossX = -1, bossY = -1;

    private Player player;
    private Monster[] monsters;
    private Trap[] traps;
    private ArrayList<Projectile> friendlyProjectiles, enemyProjectiles;
    private Boss boss;

    private TextureAtlas masterAtlas, bossAtlas;
    private Sprite floorSprite, darknessSprite;
    private Sprite[] wallSprites, wallShadowSprites;

    private Animation<TextureRegion> startPortal, finishPortal, portalBarrier;

    private boolean bossLoaded = false, bossKilled = false;
    private String[] bossDescriptionLines;
    private float portalBarrierRemainingTime = 0.6f, portalBarrierStart = -1;
    private float descriptionFadeInFinish = 0.4f, descriptionFadeOutStart = 2.6f, descriptionDuration = 3f,
            descriptionRemainingTime = 3f, descriptionStart = -1;

    private boolean[][] isWall;

    private int winscreenFrames = 8, winscreenLoopFrames = 8, winscreenWidth = 128, winscreenHeight = 96;
    private float winscreenTime = 0.8f, winscreenLoopTime = 0.8f, winscreenTimeRemaining, winscreenStartTime;
    private Animation<TextureRegion> winscreen, winscreenLoop;
    private TextureAtlas winAtlas;

    private int selectionCircleFrames = 6, selectionWindowWidth = 84, selectionWindowHeight = 120,
            selectedCharacter = 0, selectionCircleWidth = 32;
    private Animation<TextureRegion> selectionCircle;
    private Sprite selectionWindow;
    private String[] characterDescriptions, menuLines;

    public boolean gameShouldBeClosed = false;

    public GameField(SpriteBatch batch, MusicStorage musicStorage, int stageNum) {
        this.stageNum = stageNum;
        this.musicStorage = musicStorage;
        GlobalValues.pixelSize = Gdx.graphics.getHeight() / height / tileY;
        renderer = new Renderer(batch);
        masterAtlas = renderer.addAtlas(new TextureAtlas("atlases/masteratlas.txt"));
        spriteStorage = new SpriteStorage();
        readCharacterStats();
        readMonsterStats();
        readTrapStats();
        readProjectileStats();
        readBossStats();
        spriteStorage.doLoading(renderer, masterAtlas);
        loadWalls();
        loadPortals();
        loadSelectionWindow();
        readMenuText();
        musicStorage.startMenuMusic();
    }

    private void startGame(int playerType) {
        player = new Player(this, playerType);
        mapNum = 0;
        musicStorage.startMainMusic();
        readMap(mapNum);
    }

    private void readMap(int mapNum) {
        Texture mapFile = new Texture("maps/stage" + stageNum + "/map" + mapNum + "/map.png");
        width = mapFile.getWidth();
        height = mapFile.getHeight();
        isWall = new boolean[width][height];
        mapFile.getTextureData().prepare();
        Pixmap pixMap = mapFile.getTextureData().consumePixmap();
        final int wallColor = 0x000000ff;
        final int startColor = 0xff8000ff;
        final int finishColor = 0x00ff00ff;
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < width; y++) {
                int pixColor = pixMap.getPixel(x, y);
                if(pixColor == wallColor) {
                    isWall[x][height - 1 - y] = true;
                } else if(pixColor == startColor) {
                    startX = x;
                    startY = height - 1 - y;
                } else if(pixColor == finishColor) {
                    finishX = x;
                    finishY = height - 1 - y;
                }
            }
        }
        player.setPosition(startX, startY);
        player.setAliveState(false);
        transitionInProgress = true;
        if(startX < 0) {
            System.err.println("No start point on map " + mapNum + "!");
        }
        if(finishX < 0) {
            System.err.println("No finish point on map " + mapNum + "!");
        }
        readMonsters(mapNum);
        readTraps(mapNum);
        friendlyProjectiles = new ArrayList<Projectile>();
        enemyProjectiles = new ArrayList<Projectile>();
    }

    private void readBossMap(float elapsedTime) {
        monsters = new Monster[0];
        traps = new Trap[0];
        Texture mapFile = new Texture("maps/stage" + stageNum + "/map_boss/map.png");
        width = mapFile.getWidth();
        height = mapFile.getHeight();
        isWall = new boolean[width][height];
        mapFile.getTextureData().prepare();
        Pixmap pixMap = mapFile.getTextureData().consumePixmap();
        final int wallColor = 0x000000ff;
        final int startColor = 0xff8000ff;
        final int finishColor = 0x00ff00ff;
        final int bossColor = 0xff0000ff;
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < width; y++) {
                int pixColor = pixMap.getPixel(x, y);
                if(pixColor == wallColor) {
                    isWall[x][height - 1 - y] = true;
                } else if(pixColor == startColor) {
                    startX = x;
                    startY = height - 1 - y;
                } else if(pixColor == finishColor) {
                    finishX = x;
                    finishY = height - 1 - y;
                } else if(pixColor == bossColor) {
                    bossX = x;
                    bossY = height - 1 - y;
                }
            }
        }
        player.setPosition(startX, startY);
        player.setAliveState(false);
        transitionInProgress = true;
        if(startX < 0) {
            System.err.println("No start point on map!");
        }
        if(finishX < 0) {
            System.err.println("No finish point on boss map!");
        }
        if(bossX < 0) {
            System.err.println("No boss on boss map!");
        }
        bossKilled = false;
        portalBarrierRemainingTime = 0.6f;
        portalBarrierStart = -1;
        descriptionRemainingTime = descriptionDuration;
        descriptionStart = -1;
        musicStorage.startBossMusic();
        readBoss(elapsedTime);
        friendlyProjectiles = new ArrayList<Projectile>();
        enemyProjectiles = new ArrayList<Projectile>();
        pixMap.dispose();
    }

    private void readMonsters(int mapNum) {
        final String monsterFile = "maps/stage" + stageNum + "/map" + mapNum + "/monsters.txt";
        try {
            BufferedReader reader = getBufferedReader(monsterFile);
            String line;
            try {
                line = reader.readLine();
                String[] currentLine;
                monsters = new Monster[Integer.valueOf(line)];
                if(monsters.length <= 0) {
                    reader.close();
                    return;
                }
                line = reader.readLine();
                currentLine = line.split(" ");
                for(int i = 0; i < monsters.length; i++) {
                    monsters[i] = new Monster(this, Integer.valueOf(currentLine[i]));
                }
                line = reader.readLine();
                currentLine = line.split(" ");
                for(int i = 0; i < monsters.length; i++) {
                    monsters[i].setX(Integer.valueOf(currentLine[i]));
                }
                line = reader.readLine();
                currentLine = line.split(" ");
                for(int i = 0; i < monsters.length; i++) {
                    monsters[i].setY(Integer.valueOf(currentLine[i]));
                }
                line = reader.readLine();
                currentLine = line.split(" ");
                for(int i = 0; i < monsters.length; i++) {
                    monsters[i].readPath(currentLine[i]);
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + monsterFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + monsterFile);
        }
    }

    private BufferedReader getBufferedReader(String file) throws ClassNotFoundException {
        Class cls = Class.forName("com.escapefromhades.GameStates.GameField");
        ClassLoader cLoader = cls.getClassLoader();
        InputStream in = cLoader.getResourceAsStream(file);
        return new BufferedReader(new InputStreamReader(in));
    }

    private void readTraps(int mapNum) {
        final String trapFile = "maps/stage" + stageNum + "/map" + mapNum + "/traps.txt";
        try {
            BufferedReader reader = getBufferedReader(trapFile);
            String line;
            try {
                line = reader.readLine();
                String[] currentLine;
                traps = new Trap[Integer.valueOf(line)];
                if(traps.length <= 0) {
                    reader.close();
                    return;
                }
                line = reader.readLine();
                currentLine = line.split(" ");
                for(int i = 0; i < traps.length; i++) {
                    traps[i] = new Trap(this, Integer.valueOf(currentLine[i]));
                }
                line = reader.readLine();
                currentLine = line.split(" ");
                for(int i = 0; i < traps.length; i++) {
                    traps[i].setX(Integer.valueOf(currentLine[i]));
                }
                line = reader.readLine();
                currentLine = line.split(" ");
                for(int i = 0; i < traps.length; i++) {
                    traps[i].setY(Integer.valueOf(currentLine[i]));
                }
                line = reader.readLine();
                currentLine = line.split(" ");
                for(int i = 0; i < traps.length; i++) {
                    traps[i].setCycleStart(Float.valueOf(currentLine[i]), elapsedTime);
                }
                line = reader.readLine();
                currentLine = line.split(" ");
                for(int i = 0; i < traps.length; i++) {
                    traps[i].setDirection(Integer.valueOf(currentLine[i]));
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + trapFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + trapFile);
        }
    }

    private void readBoss(float elapsedTime) {
        final String bossFile = "maps/stage" + stageNum + "/map_boss/boss.txt";
        try {
            BufferedReader reader = getBufferedReader(bossFile);
            String line;
            try {
                line = reader.readLine();
                if(line.equals("hecatoncheires")) {
                    readBossDescription("hecatoncheires");
                    bossAtlas = renderer.addAtlas(new TextureAtlas("atlases/hecatoncheires.txt"));
                    spriteStorage.loadHecatoncheires(renderer, bossAtlas);
                    boss = new Hecatoncheires(this, player);
                    ((Hecatoncheires) boss).spawnHecatoncheires(elapsedTime, bossX, bossY);
                } else if(line.equals("odysseus")) {
                    readBossDescription("odysseus");
                    bossAtlas = renderer.addAtlas(new TextureAtlas("atlases/odysseus.txt"));
                    spriteStorage.loadOdysseus(renderer, bossAtlas);
                    boss = new Odysseus(this, player);
                    ((Odysseus) boss).spawnOdysseus(elapsedTime, bossX, bossY);
                } else {
                    System.err.println("Unrecognized boss: " + line);
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + bossFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + bossFile);
        }
    }

    private void clearBossAtlas() {
        bossAtlas.dispose();
        bossAtlas = null;
    }

    private void readBossDescription(String bossName) {
        final String bossFile = "texts/" + GlobalValues.languageFolder[GlobalValues.language] + "/" + bossName + "_description.txt";
        try {
            BufferedReader reader = getBufferedReader(bossFile);
            String line;
            try {
                line = reader.readLine();
                int lineCount = Integer.valueOf(line);
                bossDescriptionLines = new String[lineCount];
                for(int i = 0; i < lineCount; i++) {
                    bossDescriptionLines[i] = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + bossFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + bossFile);
        }
    }

    private void readMenuText() {
        final String charFile = "texts/" + GlobalValues.languageFolder[GlobalValues.language] + "/character_descriptions.txt";
        try {
            Class cls = Class.forName("com.escapefromhades.GameStates.GameField");
            ClassLoader cLoader = cls.getClassLoader();
            InputStream in = cLoader.getResourceAsStream(charFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            try {
                line = reader.readLine();
                String[] currentLine = line.split(" ");
                characterDescriptions = new String[spriteStorage.playerTypesNum];
                int[] lineCount = new int[spriteStorage.playerTypesNum];
                for(int i = 0; i < spriteStorage.playerTypesNum; i++) {
                    lineCount[i] = Integer.valueOf(currentLine[i]);
                }
                for(int i = 0; i < spriteStorage.playerTypesNum; i++) {
                    characterDescriptions[i] = "";
                    for(int j = 0; j < lineCount[i]; j++) {
                        line = reader.readLine();
                        characterDescriptions[i] += "\n" + line;
                    }
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + charFile);
            }
            final String menuFile = "texts/" + GlobalValues.languageFolder[GlobalValues.language] + "/menu_lines.txt";
            in = cLoader.getResourceAsStream(menuFile);
            reader = new BufferedReader(new InputStreamReader(in));
            try {
                line = reader.readLine();
                menuLines = new String[Integer.valueOf(line)];
                for(int i = 0; i < menuLines.length; i++) {
                    line = reader.readLine();
                    menuLines[i] = line;
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + menuFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + charFile);
        }
    }

    private void readCharacterStats() {
        final String charStatsFile = "texts/character_stats.txt";
        try {
            BufferedReader reader = getBufferedReader(charStatsFile);
            String line;
            try {
                line = reader.readLine();
                int playerTypesNum = Integer.valueOf(line);
                Player.preparePlayerValueArrays(playerTypesNum);
                spriteStorage.preparePlayerValueArrays(playerTypesNum);
                for(int i = 0; i < playerTypesNum; i++) {
                    spriteStorage.loadPlayerValues(charStatsFile, reader, i);
                    Player.loadPlayerValues(charStatsFile, reader, i);
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + charStatsFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + charStatsFile);
        }
    }

    private void readMonsterStats() {
        final String monsterStatsFile = "texts/monster_stats.txt";
        try {
            BufferedReader reader = getBufferedReader(monsterStatsFile);
            String line;
            try {
                line = reader.readLine();
                int monsterTypesNum = Integer.valueOf(line);
                Monster.prepareMonsterValueArrays(monsterTypesNum);
                spriteStorage.prepareMonsterValueArrays(monsterTypesNum);
                for(int i = 0; i < monsterTypesNum; i++) {
                    spriteStorage.loadMonsterValues(monsterStatsFile, reader, i);
                    Monster.loadMonsterValues(monsterStatsFile, reader, i);
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + monsterStatsFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + monsterStatsFile);
        }
    }

    private void readTrapStats() {
        final String trapStatsFile = "texts/trap_stats.txt";
        try {
            BufferedReader reader = getBufferedReader(trapStatsFile);
            String line;
            try {
                line = reader.readLine();
                int trapTypesNum = Integer.valueOf(line);
                Trap.prepareTrapValueArrays(trapTypesNum);
                spriteStorage.prepareTrapValueArrays(trapTypesNum);
                for(int i = 0; i < trapTypesNum; i++) {
                    spriteStorage.loadTrapValues(trapStatsFile, reader, i);
                    Trap.loadTrapValues(trapStatsFile, reader, i);
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + trapStatsFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + trapStatsFile);
        }
    }

    private void readProjectileStats() {
        final String projectileStatsFile = "texts/projectile_stats.txt";
        try {
            BufferedReader reader = getBufferedReader(projectileStatsFile);
            String line;
            try {
                line = reader.readLine();
                int projectileTypesNum = Integer.valueOf(line);
                Projectile.prepareProjectileValueArrays(projectileTypesNum);
                spriteStorage.prepareProjectileValueArrays(projectileTypesNum);
                for(int i = 0; i < projectileTypesNum; i++) {
                    spriteStorage.loadProjectileValues(projectileStatsFile, reader, i);
                    Projectile.loadProjectileValues(projectileStatsFile, reader, i);
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + projectileStatsFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + projectileStatsFile);
        }
    }

    private void readBossStats() {
        loadHecatoncheiresStats();
        loadOdysseusStats();
    }

    private void loadHecatoncheiresStats() {
        final String hecatoncheiresStatsFile = "texts/hecatoncheires_stats.txt";
        try {
            BufferedReader reader = getBufferedReader(hecatoncheiresStatsFile);
            try {
                spriteStorage.loadHecatoncheiresValues(hecatoncheiresStatsFile, reader);
                Hecatoncheires.loadHecatoncheiresValues(hecatoncheiresStatsFile, reader);
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + hecatoncheiresStatsFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + hecatoncheiresStatsFile);
        }
    }

    private void loadOdysseusStats() {
        final String odysseusStatsFile = "texts/odysseus_stats.txt";
        try {
            BufferedReader reader = getBufferedReader(odysseusStatsFile);
            try {
                spriteStorage.loadOdysseusValues(odysseusStatsFile, reader);
                Odysseus.loadOdysseusValues(odysseusStatsFile, reader);
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading the file: " + odysseusStatsFile);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class GameField not found while reading this: " + odysseusStatsFile);
        }
    }

    private void loadWalls() {
        floorSprite = renderer.addSprite(masterAtlas, "floor");
        darknessSprite = renderer.addSprite(masterAtlas, "darkness");
        wallSprites = new Sprite[4];
        wallShadowSprites = new Sprite[4];
        String[] directionSuffix = {"_west", "_east", "_south", "_north"};
        for(int i = 0; i < 4; i++) {
            wallSprites[i] = renderer.addSprite(masterAtlas, "wall" + directionSuffix[i]);
            wallShadowSprites[i] = renderer.addSprite(masterAtlas, "shadow" + directionSuffix[i]);
        }
    }

    private void loadPortals() {
        startPortal = renderer.addAnimation(masterAtlas, "startportal", 0.075f, 8, Animation.PlayMode.LOOP);
        finishPortal = renderer.addAnimation(masterAtlas, "finishportal", 0.075f, 8, Animation.PlayMode.LOOP);
        portalBarrier = renderer.addAnimation(masterAtlas, "portal_barrier", 0.075f, 8, Animation.PlayMode.NORMAL);
    }

    private void loadWinscreen(float elapsedTime) {
        clearBossAtlas();
        bossLoaded = false;
        updateHighScores();
        winAtlas = renderer.addAtlas(new TextureAtlas("atlases/winscreen_" + spriteStorage.playerNames[player.getPlayerType()] + ".txt"));
        winscreen = renderer.addAnimation(winAtlas, "winscreen_" + spriteStorage.playerNames[player.getPlayerType()] + "_start",
                winscreenTime / winscreenFrames, winscreenFrames, Animation.PlayMode.NORMAL);
        winscreenLoop = renderer.addAnimation(winAtlas, "winscreen_" + spriteStorage.playerNames[player.getPlayerType()] + "_loop",
                winscreenLoopTime / winscreenLoopFrames, winscreenLoopFrames, Animation.PlayMode.LOOP);
        playerHasWon = true;
        winscreenTimeRemaining = winscreenTime;
        winscreenStartTime = elapsedTime;
    }

    private void loadSelectionWindow() {
        selectionWindow = renderer.addSprite(masterAtlas, "selection_window");
        selectionCircle = renderer.addAnimation(masterAtlas, "selection_circle", 0.075f, selectionCircleFrames, Animation.PlayMode.LOOP);
    }

    private void disposeOfWinscreen() {
        winAtlas.dispose();
        winAtlas = null;
    }

    public void render(float elapsedTime) {
        this.elapsedTime = elapsedTime;
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gameShouldBeClosed = true;
        }
        if(characterHasBeenChosen) {
            if(!playerHasWon) {
                if (winAtlas != null) {
                    disposeOfWinscreen();
                }
                resetSounds();
                renderScore();
                renderFloor();
                renderWalls();
                renderPortals();
                processGameLogic(elapsedTime);
                for (Trap trap : traps) {
                    if (trap.isTrapLow())
                        trap.renderTrap(renderer);
                }
                for (Monster monster : monsters) {
                    if (monster.isAlive())
                        monster.renderMonster(renderer);
                }
                if(bossLoaded) {
                    boss.renderBoss(renderer);
                }
                player.renderPlayer(renderer);
                for (Trap trap : traps) {
                    if (!trap.isTrapLow())
                        trap.renderTrap(renderer);
                }
                for(Projectile projectile: friendlyProjectiles) {
                    if(projectile.exists())
                        projectile.renderProjectile(renderer);
                }
                for(Projectile projectile: enemyProjectiles) {
                    if(projectile.exists())
                        projectile.renderProjectile(renderer);
                }
                if(bossLoaded) {
                    renderBossDescription();
                }
            } else {
                renderWinscreen();
                if(winscreenTimeRemaining > 0) {
                    winscreenTimeRemaining -= Gdx.graphics.getDeltaTime();
                } else if (Math.abs(winscreenTimeRemaining) <= 1){
                    winscreenStartTime = elapsedTime;
                    winscreenTimeRemaining = -100;
                }
            }
        } else {
            renderCharacterSelection();
        }
        renderer.render(elapsedTime, tileX * width / 2 * GlobalValues.pixelSize - Gdx.graphics.getWidth() / 2, tileY * GlobalValues.pixelSize * height / 2 - Gdx.graphics.getHeight() / 2);
    }

    private void resetSounds() {
        for(int i = 0; i < spriteStorage.monsterDeathSoundIsPlaying.length; i++) {
            spriteStorage.monsterDeathSoundIsPlaying[i] = false;
            spriteStorage.monsterAttackSoundIsPlaying[i] = false;
            spriteStorage.monsterSpawnSoundIsPlaying[i] = false;
        }
        for(int i = 0; i < spriteStorage.trapSoundIsPlaying.length; i++) {
            spriteStorage.trapSoundIsPlaying[i] = false;
        }
        for(int i = 0; i < spriteStorage.projectileSoundIsPlaying.length; i++) {
            spriteStorage.projectileSoundIsPlaying[i] = false;
        }
        spriteStorage.hecatoncheiresSpinSoundIsPlaying = false;
        spriteStorage.hecatoncheiresDeflectSoundIsPlaying = false;
        spriteStorage.hecatoncheiresDeathSoundIsPlaying = false;
        spriteStorage.hecatoncheiresStepSoundIsPlaying = false;
        spriteStorage.odysseusShotSoundIsPlaying = false;
        spriteStorage.odysseusSpawnSoundIsPlaying = false;
        spriteStorage.odysseusTeleportSoundIsPlaying = false;
        spriteStorage.odysseusDeathSoundIsPlaying = false;
    }

    private void processGameLogic(float elapsedTime) {
        for (Monster monster : monsters) {
            if (monster.isAlive())
                monster.processMonsterMovement(elapsedTime);
        }
        for (Trap trap : traps) {
            trap.processTrapMovement();
        }
        for (Projectile projectile: friendlyProjectiles) {
            projectile.processProjectileMovement(elapsedTime);
        }
        for (Projectile projectile: enemyProjectiles) {
            projectile.processProjectileMovement(elapsedTime);
        }
        player.processPlayerMovement(elapsedTime);
        for (Monster monster : monsters) {
            if (monster.isAlive()) {
                player.checkHitWithMonster(monster, elapsedTime);
                for (Projectile projectile: friendlyProjectiles) {
                    if (projectile.isDangerous())
                        monster.checkCollisionWithProjectile(projectile, elapsedTime);
                }
            }
        }
        if(bossLoaded) {
            boss.processBossMovement(elapsedTime);
            for (Projectile projectile: friendlyProjectiles) {
                if (projectile.isDangerous())
                    boss.checkCollisionWithProjectile(projectile, elapsedTime);
            }
            boss.checkIfBossIsHit(elapsedTime);
            if(!boss.isAlive()) {
                bossKilled = true;
            }
        }
        if(player.isVulnerable()) {
            if(!bossLoaded) {
                for (Monster monster : monsters) {
                    if (monster.isDangerous() && player.isVulnerable())
                        player.checkCollisionWithMonster(monster, elapsedTime);
                }
                for (Trap trap : traps) {
                    if (trap.isDangerous() && player.isVulnerable())
                        player.checkCollisionWithTrap(trap, elapsedTime);
                }
            } else {
                player.hurtPlayer(elapsedTime, boss.checkHitWithPlayer());
            }
            for (Projectile projectile: enemyProjectiles) {
                if (projectile.isDangerous() && player.isVulnerable())
                    player.checkCollisionWithProjectile(projectile, elapsedTime);
            }
        }
    }

    private void renderScore() {
        if(!transitionInProgress)
            timeTaken += Gdx.graphics.getDeltaTime();
        renderer.addTextToSchedule(GlobalValues.fontSmall,
                tileX * (width + 0.25f) * GlobalValues.pixelSize,
                tileY * height * GlobalValues.pixelSize - GlobalValues.fontSmall.getLineHeight() - tileY,
                (int)(timeTaken / 60) + ":" + (timeTaken % 60 < 10?"0":"") + (int)(timeTaken % 60) + "." + (int)(timeTaken % 1 * 10),
                1, 1, 1, 1);
    }

    private void renderFloor() {
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if(isWall[x][y]) {
                    renderer.addSpriteToSchedule(darknessSprite,x * tileX * GlobalValues.pixelSize, y * tileY * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                } else {
                    renderer.addSpriteToSchedule(floorSprite,x * tileX * GlobalValues.pixelSize, y * tileY * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                }
            }
        }
    }

    private void renderWalls() {
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if(isWall[x][y]) {
                    if(x > 0) {
                        if(!isWall[x - 1][y]) {
                            renderer.addSpriteToSchedule(wallShadowSprites[0],(x * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                        }
                    }
                    if(x < width - 1) {
                        if(!isWall[x + 1][y]) {
                            renderer.addSpriteToSchedule(wallShadowSprites[1],(x * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                        }
                    }
                    if(y > 0) {
                        if(!isWall[x][y - 1]) {
                            renderer.addSpriteToSchedule(wallShadowSprites[2],(x * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                        }
                    }
                    if(y < height - 1) {
                        if(!isWall[x][y + 1]) {
                            renderer.addSpriteToSchedule(wallShadowSprites[3],(x * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                        }
                    }
                } else {
                    if(x == 0) {
                        renderer.addSpriteToSchedule(wallShadowSprites[1],((x - 1) * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                    }
                    if(x == width - 1) {
                        renderer.addSpriteToSchedule(wallShadowSprites[0],((x + 1) * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                    }
                    if(y == 0) {
                        renderer.addSpriteToSchedule(wallShadowSprites[3],(x * tileX - wallOffset) * GlobalValues.pixelSize, ((y - 1) * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                    }
                    if(y == height - 1) {
                        renderer.addSpriteToSchedule(wallShadowSprites[2],(x * tileX - wallOffset) * GlobalValues.pixelSize, ((y + 1) * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                    }
                }
            }
        }
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if(isWall[x][y]) {
                    if(x > 0) {
                        if(!isWall[x - 1][y]) {
                            renderer.addSpriteToSchedule(wallSprites[0],(x * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                        }
                    }
                    if(x < width - 1) {
                        if(!isWall[x + 1][y]) {
                            renderer.addSpriteToSchedule(wallSprites[1],(x * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                        }
                    }
                    if(y > 0) {
                        if(!isWall[x][y - 1]) {
                            renderer.addSpriteToSchedule(wallSprites[2],(x * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                        }
                    }
                    if(y < height - 1) {
                        if(!isWall[x][y + 1]) {
                            renderer.addSpriteToSchedule(wallSprites[3],(x * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                        }
                    }
                } else {
                    if(x == 0) {
                        renderer.addSpriteToSchedule(wallSprites[1],((x - 1) * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                    }
                    if(x == width - 1) {
                        renderer.addSpriteToSchedule(wallSprites[0],((x + 1) * tileX - wallOffset) * GlobalValues.pixelSize, (y * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                    }
                    if(y == 0) {
                        renderer.addSpriteToSchedule(wallSprites[3],(x * tileX - wallOffset) * GlobalValues.pixelSize, ((y - 1) * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                    }
                    if(y == height - 1) {
                        renderer.addSpriteToSchedule(wallSprites[2],(x * tileX - wallOffset) * GlobalValues.pixelSize, ((y + 1) * tileY - wallOffset) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
                    }
                }
            }
        }
    }

    private void renderPortals() {
        renderer.addAnimationToSchedule(startPortal, startX * tileX * GlobalValues.pixelSize, startY * tileX * GlobalValues.pixelSize, GlobalValues.pixelSize, false, 0, 0, false);
        renderer.addAnimationToSchedule(finishPortal, finishX * tileX * GlobalValues.pixelSize, finishY * tileX * GlobalValues.pixelSize, GlobalValues.pixelSize, false, 0, 0, false);
        if(bossLoaded) {
            if(!bossKilled) {
                renderer.addAnimationToSchedule(portalBarrier, finishX * tileX * GlobalValues.pixelSize, finishY * tileX * GlobalValues.pixelSize, GlobalValues.pixelSize, false, 0, elapsedTime, false);
            } else if(portalBarrierRemainingTime > 0) {
                if(portalBarrierStart < 0) {
                    portalBarrierStart = elapsedTime;
                }
                portalBarrierRemainingTime -= Gdx.graphics.getDeltaTime();
                renderer.addAnimationToSchedule(portalBarrier, finishX * tileX * GlobalValues.pixelSize, finishY * tileX * GlobalValues.pixelSize, GlobalValues.pixelSize, false, 0, portalBarrierStart, false);
            }
        }
    }

    private void renderBossDescription() {
        if(descriptionRemainingTime > 0) {
            if(descriptionStart < 0) {
                descriptionStart = elapsedTime;
            }
            descriptionRemainingTime -= Gdx.graphics.getDeltaTime();
            float alpha;
            if((descriptionDuration - descriptionRemainingTime) <= descriptionFadeOutStart) {
                alpha = (descriptionDuration - descriptionRemainingTime) / descriptionFadeOutStart;
            } else {
                alpha = descriptionRemainingTime / descriptionFadeInFinish;
            }
            renderer.addCenteredTextToSchedule(GlobalValues.fontLarge,
                    (tileX * width / 2) * GlobalValues.pixelSize,
                    (tileY * height / 2) * GlobalValues.pixelSize + GlobalValues.fontLarge.getLineHeight() / 2, bossDescriptionLines[0],
                    1, 1, 1, alpha);
            renderer.addCenteredTextToSchedule(GlobalValues.fontMid,
                    (tileX * width / 2) * GlobalValues.pixelSize,
                    (tileY * height / 2) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() / 2, bossDescriptionLines[1],
                    1, 1, 1, alpha);
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void renderWinscreen() {
        if(winscreenTimeRemaining > 0) {
            renderer.addAnimationToSchedule(winscreen, (tileX * width / 2 - winscreenWidth / 2) * GlobalValues.pixelSize,
                    (tileY * height / 2) * GlobalValues.pixelSize, GlobalValues.pixelSize, false, 0, winscreenStartTime, false);
        }  else {
            renderer.addAnimationToSchedule(winscreenLoop, (tileX * width / 2 - winscreenWidth / 2) * GlobalValues.pixelSize,
                    (tileY * height / 2) * GlobalValues.pixelSize, GlobalValues.pixelSize, false, 0, winscreenStartTime, false);
        }
        for(int i = 4; i <= 5; i++) {
            renderer.addCenteredTextToSchedule(GlobalValues.fontMid,
                    (tileX * width / 2) * GlobalValues.pixelSize,
                    (tileY * height / 2) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * (i - 3), menuLines[i],
                    1, 1, 1, 1);
        }
        renderer.addCenteredTextToSchedule(GlobalValues.fontMid,
                (tileX * width / 2) * GlobalValues.pixelSize,
                (tileY * height / 2) * GlobalValues.pixelSize - GlobalValues.fontMid.getLineHeight() * 3, menuLines[6] + " " +
                        (int)(timeTaken / 60) + ":" + (timeTaken % 60 < 10?"0":"") + (int)(timeTaken % 60) + "." + (int)(timeTaken % 1 * 10) + ".",
                1, 1, 1, 1);
        renderer.addCenteredTextToSchedule(GlobalValues.fontLarge,
                (tileX * width / 2) * GlobalValues.pixelSize,
                selectionWindowHeight / 2 + GlobalValues.fontLarge.getLineHeight() / 2, menuLines[7],
                1, 1, 1, 1);
        renderer.addCenteredTextToSchedule(GlobalValues.fontMid,
                (tileX * width / 2 - tileX * 5) * GlobalValues.pixelSize,
                selectionWindowHeight / 2, menuLines[8],
                1, 1, 1, 1);
        renderer.addCenteredTextToSchedule(GlobalValues.fontMid,
                (tileX * width / 2 + tileX * 5) * GlobalValues.pixelSize,
                selectionWindowHeight / 2, menuLines[9],
                1, 1, 1, 1);
        if(continueChosen) {
            renderer.addAnimationToSchedule(selectionCircle,
                    (tileX * width / 2 - tileX * 5 - selectionCircleWidth / 2) * GlobalValues.pixelSize,
                    0, GlobalValues.pixelSize, false, 0, 0, false);
            if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                continueChosen = false;
            }
            if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                characterHasBeenChosen = false;
                playerHasWon = false;
                musicStorage.startMenuMusic();
            }
        } else {
            renderer.addAnimationToSchedule(selectionCircle,
                    (tileX * width / 2 + tileX * 5 - selectionCircleWidth / 2) * GlobalValues.pixelSize,
                    0, GlobalValues.pixelSize, false, 0, 0, false);
            if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                continueChosen = true;
            }
            if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                gameShouldBeClosed = true;
            }
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void renderCharacterSelection() {
        renderer.addCenteredTextToSchedule(GlobalValues.fontLarge,
                (tileX * width / 2) * GlobalValues.pixelSize,
                (tileY * height) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() * 0.5f, menuLines[0],
                1, 1, 1, 1);
        for(int i = 0; i < spriteStorage.playerTypesNum; i++) {
            renderer.addSpriteToSchedule(selectionWindow,
                    (tileX * width / 2 + selectionWindowWidth * (i * 2 - spriteStorage.playerTypesNum) / 2) * GlobalValues.pixelSize,
                    (tileY * height / 2 - selectionWindowHeight / 2) * GlobalValues.pixelSize, GlobalValues.pixelSize, 0, false);
            renderer.addTextToSchedule(GlobalValues.fontSmall,
                    (tileX * width / 2 + selectionWindowWidth * (i * 2 - spriteStorage.playerTypesNum) / 2 + 4) * GlobalValues.pixelSize,
                    (tileY * height / 2 - selectionWindowHeight / 2 + 91) * GlobalValues.pixelSize, characterDescriptions[i],
                    1, 1, 1, 1);
            renderer.addAnimationToSchedule(spriteStorage.playerWalk[i][0],
                    (tileX * width / 2 + selectionWindowWidth * (i * 2 - spriteStorage.playerTypesNum) / 2 + 34) * GlobalValues.pixelSize,
                    (tileY * height / 2 - selectionWindowHeight / 2 + 98) * GlobalValues.pixelSize, GlobalValues.pixelSize, false, 0, 0, false);
            if(i == selectedCharacter)
                renderer.addAnimationToSchedule(selectionCircle,
                        (tileX * width / 2 + selectionWindowWidth * (i * 2 - spriteStorage.playerTypesNum) / 2 + 26) * GlobalValues.pixelSize,
                        (tileY * height / 2 - selectionWindowHeight / 2 + 92) * GlobalValues.pixelSize, GlobalValues.pixelSize, false, 0, 0, false);
        }
        for(int i = 1; i <= 2; i++) {
            renderer.addCenteredTextToSchedule(GlobalValues.fontSmall,
                    (tileX * width / 2) * GlobalValues.pixelSize,
                    (tileY * height) * GlobalValues.pixelSize - GlobalValues.fontLarge.getLineHeight() - GlobalValues.fontSmall.getLineHeight() * (i - 1), menuLines[i],
                    1, 1, 1, 1);
        }
        renderer.addCenteredTextToSchedule(GlobalValues.fontSmall,
                (tileX * width / 2) * GlobalValues.pixelSize,
                GlobalValues.fontSmall.getLineHeight(), menuLines[3],
                1, 1, 1, 1);
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && selectedCharacter > 0) {
            selectedCharacter--;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && selectedCharacter < spriteStorage.playerTypesNum - 1) {
            selectedCharacter++;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startGame(selectedCharacter);
            characterHasBeenChosen = true;
            timeTaken = 0;
        }
    }

    public void loadNextMap(float elapsedTime) {
        if(mapNum < GlobalValues.lastMap[stageNum]) {
            mapNum++;
            readMap(mapNum);
        } else if(!bossLoaded) {
            bossLoaded = true;
            readBossMap(elapsedTime);
        } else {
            musicStorage.startWinMusic();
            loadWinscreen(elapsedTime);
        }
    }

    private void updateHighScores() {
        float currentReplacement = timeTaken;
        for(int i = 0; i < GlobalValues.highScores[stageNum].length; i++) {
            if(GlobalValues.highScores[stageNum][i] > currentReplacement) {
                float c = GlobalValues.highScores[stageNum][i];
                GlobalValues.highScores[stageNum][i] = currentReplacement;
                currentReplacement = c;
            }
        }
        GlobalValues.highScoresUpdated = true;
    }

    public void createFriendlyProjectile(int id, int dir, float x, float y) {
        float speedX = 0, speedY = 0;
        switch(dir) {
            case 0:
                speedY = -1;
                break;
            case 1:
                speedX = 1;
                break;
            case 2:
                speedY = 1;
                break;
            case 3:
                speedX = -1;
                break;
        }
        friendlyProjectiles.add(new Projectile(this, id, dir, x, y, speedX, speedY));
    }

    public void createEnemyProjectile(int id, int dir, float x, float y) {
        float speedX = 0, speedY = 0;
        switch(dir) {
            case 0:
                speedY = -1;
                break;
            case 1:
                speedX = 1;
                break;
            case 2:
                speedY = 1;
                break;
            case 3:
                speedX = -1;
                break;
        }
        enemyProjectiles.add(new Projectile(this, id, dir, x, y, speedX, speedY));
    }

    public void createDirectedEnemyProjectile(int id, float x, float y, float targetX, float targetY) {
        int dir;
        float dist = (float) Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
        float distX = (targetX - x) / dist, distY = (targetY - y) / dist;
        if(distX >= 0) {
            if(distY >= 0) {
                if(Math.abs(distY) > Math.abs(distX)) {
                    dir = 2;
                } else {
                    dir = 1;
                }
            } else {
                if(Math.abs(distY) > Math.abs(distX)) {
                    dir = 0;
                } else {
                    dir = 1;
                }
            }
        } else {
            if(distY >= 0) {
                if(Math.abs(distY) > Math.abs(distX)) {
                    dir = 2;
                } else {
                    dir = 3;
                }
            } else {
                if(Math.abs(distY) > Math.abs(distX)) {
                    dir = 0;
                } else {
                    dir = 3;
                }
            }
        }
        enemyProjectiles.add(new Projectile(this, id, dir, x, y, distX, distY));
    }

    public void clear() {
        renderer.clear();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getFinishX() {
        return finishX;
    }

    public int getFinishY() {
        return finishY;
    }

    public boolean getWall(int x, int y) {
        return isWall[x][y];
    }

    public void startTransition() {
        transitionInProgress = true;
    }

    public void stopTransition() {
        transitionInProgress = false;
    }

    public float getSoundVolume() {
        return GlobalValues.soundVolume;
    }

    public boolean finishPortalOpen() {
        return !bossLoaded || bossKilled;
    }
}
