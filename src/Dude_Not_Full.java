import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public class Dude_Not_Full implements Dude, Animated {
    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int resourceLimit;
    private int resourceCount;
    private double actionPeriod;
    private double animationPeriod;
    public int health;

    public Dude_Not_Full(String id, Point position, List<PImage> images, int resourceLimit, int resourceCount, double actionPeriod, double animationPeriod, int health, int healthLimit) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.health = health;
    }
    public String getId()
    {return id;}
    public Point getPosition()
    {return position;}
    public void setPosition(Point position)
    {this.position = position;}
    public int getImageIndex()
    {return this.imageIndex;}
    public List<PImage> getImages()
    {return this.images;}
    public int getHealth() {
        return health;
    }
    public void setHealth(int value) {
        this.health = value;
    }
    public double getActionPeriod() {
        return actionPeriod;
    }

    public boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler) {
        if (Dude.super.moveTo(world, target, scheduler))
        {
            this.resourceCount += 1;
            target.setHealth(target.getHealth()-1);
            return true;
        }
        return false;
    }
    public void nextImage() {
        this.imageIndex = this.imageIndex + 1;
    }

    public double getAnimationPeriod() {
        return this.animationPeriod;
    }
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> target = world.findNearest(this.position, new ArrayList<>(Arrays.asList(Tree.class, Sapling.class)));

        if (target.isEmpty() || !this.moveTo(world, target.get(), scheduler) || !this.transform(world, scheduler, imageStore)) {
            scheduler.scheduleEvent(this, createActivityAction(world, imageStore), this.actionPeriod);
        }
    }
    public boolean transform(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        if (this.resourceCount >= this.resourceLimit) {
            Dude_Full dude = Functions.createDudeFull(this.id, this.position, this.actionPeriod, this.animationPeriod, this.resourceLimit, this.images);

            world.removeEntity(scheduler, this);
            scheduler.unscheduleAllEvents(this);

            world.addEntity(dude);
            dude.scheduleActions(scheduler, world, imageStore);

            return true;
        }

        return false;
    }
}
