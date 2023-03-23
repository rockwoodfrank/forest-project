public class Activity implements Action{
    private Creature creature;
    private WorldModel world;
    private ImageStore imageStore;

    public Activity(Creature creature, WorldModel world, ImageStore imageStore) {
        this.creature = creature;
        this.world = world;
        this.imageStore = imageStore;
    }

    public void executeAction(EventScheduler scheduler) {
        this.creature.executeActivity(this.world, this.imageStore, scheduler);
    }
}
