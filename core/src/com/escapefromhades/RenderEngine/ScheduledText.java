package com.escapefromhades.RenderEngine;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ScheduledText extends ScheduledEntity {
    private BitmapFont font;
    private float x, y;
    private float r, g, b, a;
    private String text;

    ScheduledText(BitmapFont font, float x, float y, String text, float r, float g, float b, float a) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.text = text;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void draw(SpriteBatch batch, float elapsedTime, float screenX, float screenY) {
        font.setColor(r, g, b, a);
        font.draw(batch, text, x - screenX, y - screenY);
    }
}
