import processing.core.PImage;
import processing.core.util.image.load.FallbackImageLoadStrategy;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Imp extends Fairy{
    private int killCount;
    private boolean dying;
    private long deathTime;
    private Class<?> target;
    public Imp (String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod, int health, Class<?> target)
    {
        super(id, position, images, actionPeriod, animationPeriod, health);
        this.target = target;
        this.killCount = 0;
        this.dying = false;
    }
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        if (killCount < 3) {
            Optional<Entity> impTarget = world.findNearest(this.getPosition(), new ArrayList<>(List.of(Dude_Not_Full.class)));
            if (impTarget.isPresent()) {
                Point tgtPos = impTarget.get().getPosition();

                if (this.moveTo(world, impTarget.get(), scheduler)) {
                    killCount += 1;
                    Entity targeted = impTarget.get();
                    world.removeEntity(scheduler, targeted);
                    scheduler.unscheduleAllEvents(targeted);
                    Victim victim = Functions.createVictim("0002", tgtPos, 0.5, 1, 0, imageStore.getImageList("victim"));
                    world.addEntity(victim);
                    victim.scheduleActions(scheduler, world, imageStore);
                }
            }
        }
        else if (!dying){
            this.setImages(imageStore.getImageList("imp-death"));
            this.setAnimationPeriod(0.25);
            this.deathTime = calcDeathTime();
            this.dying = true;
        }
        else if (System.currentTimeMillis() >= deathTime) {
                world.removeEntity(scheduler, this);
        }
        scheduler.scheduleEvent(this, createActivityAction(world, imageStore), this.getActionPeriod());
    }
    public boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler) {
        if (this.getPosition().adjacent(target.getPosition())) {
            System.out.println("Get got!");
            return true;
        } else {
            Point nextPos = this.nextPosition(world, target.getPosition());

            if (!this.getPosition().equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
            }
            return false;
        }
    }

    private long calcDeathTime()
    {
        long startTime = System.currentTimeMillis();
        long deathTime = startTime + (long)(this.getAnimationPeriod() * 1000 * (this.getImages().size()-2));
        return deathTime;
    }
}
