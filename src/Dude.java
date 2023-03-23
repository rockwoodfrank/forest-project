import java.nio.file.Path;
import java.util.List;

public interface Dude extends Creature {
    double getActionPeriod();
    default Point nextPosition(WorldModel world, Point destPos) {
        PathingStrategy strategy = new AStarPathingStrategy();
        if (destPos == null || this.getPosition() == null)
            return this.getPosition();
        List<Point> path = strategy.computePath(
                this.getPosition(),
                destPos,
                p-> world.withinBounds(p) && (!world.isOccupied(p) || world.getOccupant(p).get() instanceof Stump),
                (p1, p2) -> p1 != null && p1.adjacent(p2),
                PathingStrategy.CARDINAL_NEIGHBORS
                );
        if (path.size() == 0) return this.getPosition();
        return path.get(0);
    }
    default boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler) {
        if (this.getPosition().adjacent(target.getPosition())) {
            return true;
        } else {
            Point nextPos = this.nextPosition(world, target.getPosition());

            if (!this.getPosition().equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
            }
            return false;
        }
    }
    default void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, createActivityAction(world, imageStore), this.getActionPeriod());
        scheduler.scheduleEvent(this, createAnimationAction(0), this.getAnimationPeriod());
    }
    default Action createActivityAction(WorldModel world, ImageStore imageStore) {
        return new Activity(this, world, imageStore);
    }
}
