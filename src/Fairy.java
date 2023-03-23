import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public class Fairy implements Creature, Animated{
    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private double actionPeriod;
    private double animationPeriod;
    public int health;

    public Fairy(String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod, int health) {
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
    public List<PImage> getImages()
    {return this.images;}

    public void setImages(List<PImage> images) {
        this.images = images;
    }

    public int getHealth() {
        return health;
    }
    public double getActionPeriod() {
        return actionPeriod;
    }

    public Action createActivityAction(WorldModel world, ImageStore imageStore) {
        return new Activity(this, world, imageStore);
    }

    public boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler) {
        if (this.position.adjacent(target.getPosition())) {
            world.removeEntity(scheduler, target);
            return true;
        } else {
            Point nextPos = this.nextPosition(world, target.getPosition());

            if (!this.position.equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
            }
            return false;
        }
    }
    public void nextImage() {
        this.imageIndex = this.imageIndex + 1;
    }

    public double getAnimationPeriod() {
        return this.animationPeriod;
    }

    public void setAnimationPeriod(double animationPeriod) {
        this.animationPeriod = animationPeriod;
    }

    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, createActivityAction(world, imageStore), this.actionPeriod);
        scheduler.scheduleEvent(this, createAnimationAction(0), this.getAnimationPeriod());
    }

    public Point nextPosition(WorldModel world, Point destPos) {
        /*int horiz = Integer.signum(destPos.x - this.position.x);
        Point newPos = new Point(this.position.x + horiz, this.position.y);

        if (horiz == 0 || world.isOccupied(newPos)) {
            int vert = Integer.signum(destPos.y - this.position.y);
            newPos = new Point(this.position.x, this.position.y + vert);

            if (vert == 0 || world.isOccupied(newPos)) {
                newPos = this.position;
            }
        }

        return newPos;*/

        PathingStrategy strategy = new AStarPathingStrategy();
        if (destPos == null || this.getPosition() == null)
            return this.getPosition();
        List<Point> path = strategy.computePath(
                this.getPosition(),
                destPos,
                p-> world.withinBounds(p) && !world.isOccupied(p),
                (p1, p2) -> p1 != null && p1.adjacent(p2),
                PathingStrategy.CARDINAL_NEIGHBORS
        );
        if (path.size() == 0) return this.getPosition();
        return path.get(0);
    }
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> fairyTarget = world.findNearest(this.position, new ArrayList<>(List.of(Stump.class)));

        if (fairyTarget.isPresent()) {
            Point tgtPos = fairyTarget.get().getPosition();

            if (this.moveTo(world, fairyTarget.get(), scheduler)) {

                Sapling sapling = Functions.createSapling(Functions.SAPLING_KEY + "_" + fairyTarget.get().getId(), tgtPos, imageStore.getImageList(Functions.SAPLING_KEY), 0);

                world.addEntity(sapling);
                sapling.scheduleActions(scheduler, world, imageStore);
            }
        }

        scheduler.scheduleEvent(this, createActivityAction(world, imageStore), this.actionPeriod);
    }
}
