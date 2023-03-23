import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public final class Obstacle implements Entity {
    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private double animationPeriod;
    public Obstacle(String id, Point position, List<PImage> images, double animationPeriod) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.animationPeriod = animationPeriod;
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

    public void nextImage() {
        this.imageIndex = this.imageIndex + 1;
    }
    public double getAnimationPeriod() {
        return this.animationPeriod;
    }
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, createAnimationAction(0), this.getAnimationPeriod());
    }
}
