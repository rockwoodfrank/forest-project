/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public interface Creature extends Entity{
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler);
}
