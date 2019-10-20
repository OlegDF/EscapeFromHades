package com.escapefromhades.RenderEngine;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.LinkedList;

public class Renderer {

    private LinkedList<TextureAtlas> atlases;

    private LinkedList<Sprite> sprites;
    private LinkedList<Animation<TextureRegion>> animations;

    private LinkedList<ScheduledEntity> schedule;

    private SpriteBatch batch;

    public void render(float elapsedTime, float screenX, float screenY) {
        while(!schedule.isEmpty()) {
            schedule.remove().draw(batch, elapsedTime, screenX, screenY);
        }
    }

    public TextureAtlas addAtlas(TextureAtlas atlas) {
        atlases.add(atlas);
        return atlases.pollLast();
    }

    public Sprite addSprite(TextureAtlas atlas, String name) {
        sprites.add(atlas.createSprite(name));
        if(sprites.peek() == null) {
            System.err.println("Sprite not found: " + name);
        }
        return sprites.pollLast();
    }

    public Animation<TextureRegion> addAnimation(TextureAtlas atlas, String name, float frameDuration, int frameNum,
                                                 Animation.PlayMode playMode) {
        Array<TextureAtlas.AtlasRegion> regions = new Array<TextureAtlas.AtlasRegion>(frameNum);
        for(int i = 1; i <= frameNum; i++) {
            regions.add(atlas.findRegion(name + i));
            if(regions.peek() == null) {
                System.err.println("Animation frame not found: " + name + i);
            }
        }
        animations.add(new Animation<TextureRegion>(frameDuration, regions, playMode));
        return animations.pollLast();
    }

    public void addSpriteToSchedule(Sprite sprite, float x, float y, float scale, float rotation, boolean flipX) {
        schedule.add(new ScheduledSprite(sprite, x, y, scale, rotation, flipX));
    }

    public void addAnimationToSchedule(Animation<TextureRegion> animation, float x, float y, float scale, boolean flipX, float rotation,
                                       float startTime, boolean reverse) {
        schedule.add(new ScheduledAnimation(animation, x, y, scale, flipX, rotation, startTime, reverse));
    }

    public void addTextToSchedule(BitmapFont font, float x, float y, String text, float r, float g, float b, float a) {
        schedule.add(new ScheduledText(font, x, y, text, r, g, b, a));
    }

    public void addCenteredTextToSchedule(BitmapFont font, float x, float y, String text, float r, float g, float b, float a) {
        final GlyphLayout layout = new GlyphLayout(font, text);
        schedule.add(new ScheduledText(font, x - layout.width / 2, y + layout.height / 2, text, r, g, b, a));
    }

    public Renderer(SpriteBatch batch) {
        this.batch = batch;
        this.atlases = new LinkedList<TextureAtlas>();
        this.sprites = new LinkedList<Sprite>();
        this.animations = new LinkedList<Animation<TextureRegion>>();
        this.schedule = new LinkedList<ScheduledEntity>();
    }

    public void clear() {
        schedule.clear();
        for(TextureAtlas atlas: atlases) {
            atlas.dispose();
        }
        sprites.clear();
        animations.clear();
        atlases.clear();
    }
}