package com.escapefromhades.Entities;

import com.escapefromhades.RenderEngine.Renderer;

public abstract class Boss {
    public abstract int checkHitWithPlayer();

    public abstract void checkIfBossIsHit(float elapsedTime);

    public abstract void renderBoss(Renderer renderer);

    public abstract void processBossMovement(float elapsedTime);

    public abstract void checkCollisionWithProjectile(Projectile projectile, float elapsedTime);

    public abstract boolean isAlive();
}
