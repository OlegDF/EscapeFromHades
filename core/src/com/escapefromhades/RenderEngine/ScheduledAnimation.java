package com.escapefromhades.RenderEngine;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ScheduledAnimation extends ScheduledEntity {

    private Animation<TextureRegion> sprite;
    private float x, y, scale, rotation, startTime;
    private boolean flipX, reverse;

    ScheduledAnimation(Animation<TextureRegion> sprite, float x, float y, float scale, boolean flipX, float rotation, float startTime,
                       boolean reverse) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.flipX = flipX;
        this.rotation = rotation;
        this.startTime = startTime;
        this.reverse = reverse;
    }

    public void draw(SpriteBatch batch, float elapsedTime, float screenX, float screenY) {
        float time = elapsedTime - startTime;
        while(time < 0) {
            time += sprite.getAnimationDuration();
        }
        if(reverse) {
            time = sprite.getAnimationDuration() - time;
        }
        while(time < 0) {
            time += sprite.getAnimationDuration();
        }
        TextureAtlas.AtlasRegion currentFrame = (TextureAtlas.AtlasRegion)sprite.getKeyFrame(time, sprite.getPlayMode() == Animation.PlayMode.LOOP);
        float width = currentFrame.getRegionWidth();
        float height = currentFrame.getRegionHeight();
        batch.draw(currentFrame,  currentFrame.offsetX * scale + x - screenX + (flipX?(currentFrame.originalWidth - currentFrame.offsetX * 2) * scale:0),
                currentFrame.offsetY * scale + y - screenY,
                0, 0, width, height, flipX?-scale:scale, scale, rotation);
    }

}
