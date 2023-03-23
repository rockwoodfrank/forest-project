import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public final class Stump extends Tree{
    private int healthLimit;

    public Stump(String id, Point position, List<PImage> images, int health, int healthLimit) {
        super(id, position, images, 0, 0, health);
        this.healthLimit = healthLimit;
    }
    public int getHealth() {
        return health;
    }
    public void setHealth(int value) {
        this.health = value;
    }
    public Action createAnimationAction(int repeatCount) {
        return new Animation(this, repeatCount);
    }
}
