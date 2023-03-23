import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public final class Dude_Full implements Dude, Animated {
    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int resourceLimit;
    private double actionPeriod;
    private double animationPeriod;
    public int health;

    public Dude_Full(String id, Point position, List<PImage> images, int resourceLimit, int resourceCount, double actionPeriod, double animationPeriod, int health, int healthLimit) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.resourceLimit = resourceLimit;
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
    public double getActionPeriod() {
        return actionPeriod;
    }
    public List<PImage> getImages()
    {return this.images;}
    public int getHealth() {
        return health;
    }
    public void nextImage() {
        this.imageIndex = this.imageIndex + 1;
    }

    public double getAnimationPeriod() {
        return this.animationPeriod;
    }

    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> fullTarget = world.findNearest(this.position, new ArrayList<>(List.of(House.class)));

        if (fullTarget.isPresent() && Dude.super.moveTo(world, fullTarget.get(), scheduler)) {
            this.transform(world, scheduler, imageStore);
        } else {
            scheduler.scheduleEvent(this, createActivityAction(world, imageStore), this.actionPeriod);
        }
    }

    public void transform(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        Dude_Not_Full dude = Functions.createDudeNotFull(this.id, this.position, this.actionPeriod, this.animationPeriod, this.resourceLimit, this.images);

        world.removeEntity(scheduler, this);

        world.addEntity(dude);
        dude.scheduleActions(scheduler, world, imageStore);
    }
}
