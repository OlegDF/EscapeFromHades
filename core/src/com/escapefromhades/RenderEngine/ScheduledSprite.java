package com.escapefromhades.RenderEngine;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ScheduledSprite extends ScheduledEntity {

    private Sprite sprite;
    private float x, y, scale, rotation;
    private boolean flipX;

    ScheduledSprite(Sprite sprite, float x, float y, float scale, float rotation, boolean flipX) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.rotation = rotation;
        this.flipX = flipX;
    }

    public void draw(SpriteBatch batch, float elapsedTime, float screenX, float screenY) {
        sprite.setPosition(x - screenX, y - screenY);
        sprite.setRotation(rotation);
        sprite.setOrigin(0, 0);
        sprite.setScale(scale);
        sprite.setFlip(flipX, false);
        sprite.draw(batch);
    }

}
