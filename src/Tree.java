import processing.core.PImage;

import java.util.List;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public class Tree implements Creature, Animated {
    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private double actionPeriod;
    private double animationPeriod;
    public int health;

    public Tree(String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod, int health) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
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
    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
    }
    public List<PImage> getImages()
    {return this.images;}
    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }
    public double getActionPeriod() {
        return actionPeriod;
    }

    public Action createActivityAction(WorldModel world, ImageStore imageStore) {
        return new Activity(this, world, imageStore);
    }
    public void nextImage() {
        this.imageIndex = this.imageIndex + 1;
    }
    public double getAnimationPeriod() {
        return this.animationPeriod;
    }

    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, createActivityAction(world, imageStore), this.actionPeriod);
        scheduler.scheduleEvent(this, createAnimationAction(0), this.getAnimationPeriod());
    }

    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {

        if (!this.transform(world, scheduler, imageStore)) {

            scheduler.scheduleEvent(this, createActivityAction(world, imageStore), this.actionPeriod);
        }
    }
    public boolean transform(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
            return transformTree(world, scheduler, imageStore);
    }

    public boolean transformTree(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        if (this.health <= 0) {
            Stump stump = Functions.createStump(Functions.STUMP_KEY + "_" + this.id, this.position, imageStore.getImageList(Functions.STUMP_KEY));

            world.removeEntity(scheduler, this);

            world.addEntity(stump);
            return true;
        }
        return false;
    }
}
