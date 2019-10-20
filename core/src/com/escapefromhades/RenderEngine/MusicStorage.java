package com.escapefromhades.RenderEngine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.escapefromhades.Entities.GlobalValues;

public class MusicStorage {
    private Music menuMusic, mainMusic, winMusic, bossMusic;

    public MusicStorage() {
        loadMusic();
    }

    private void loadMusic() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("music/music_menu.ogg"));
        menuMusic.setLooping(true);
        mainMusic = Gdx.audio.newMusic(Gdx.files.internal("music/music_main.ogg"));
        mainMusic.setLooping(true);
        bossMusic = Gdx.audio.newMusic(Gdx.files.internal("music/music_boss.ogg"));
        bossMusic.setLooping(true);
        winMusic = Gdx.audio.newMusic(Gdx.files.internal("music/music_victory.ogg"));
        winMusic.setLooping(false);
        setVolume();
    }

    public void startMenuMusic() {
        mainMusic.stop();
        winMusic.stop();
        bossMusic.stop();
        menuMusic.play();
    }

    public void startMainMusic() {
        winMusic.stop();
        menuMusic.stop();
        bossMusic.stop();
        mainMusic.play();
    }

    public void startWinMusic() {
        menuMusic.stop();
        mainMusic.stop();
        bossMusic.stop();
        winMusic.play();
    }

    public void startBossMusic() {
        menuMusic.stop();
        mainMusic.stop();
        winMusic.stop();
        bossMusic.play();
    }

    public void setVolume() {
        menuMusic.setVolume(GlobalValues.musicVolume);
        mainMusic.setVolume(GlobalValues.musicVolume);
        winMusic.setVolume(GlobalValues.musicVolume);
        bossMusic.setVolume(GlobalValues.musicVolume);
    }
}
