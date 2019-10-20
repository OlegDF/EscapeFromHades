package com.escapefromhades.RenderEngine;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class ScheduledEntity {

    abstract void draw(SpriteBatch batch, float elapsedTime, float screenX, float screenY);
}
