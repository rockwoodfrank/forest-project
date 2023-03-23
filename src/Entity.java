import java.util.*;

import processing.core.PImage;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public interface Entity {
    public String getId();
    public Point getPosition();
    public void setPosition(Point position);
    public int getImageIndex();
    public List<PImage> getImages();
    default int getHealth()
    {return 0;}
    default void setHealth(int value)
    {}
    default Action createAnimationAction(int repeatCount) {
        return new Animation(this, repeatCount);
    }

    default PImage getCurrentImage() {
        return this.getImages().get(this.getImageIndex() % this.getImages().size());
    }

    default PImage getCurrentImage(Object object) {
        if (object instanceof Background background) {
            return background.images.get(background.getImageIndex());
        } else if (object instanceof Entity entity) {
            return entity.getImages().get(entity.getImageIndex() % entity.getImages().size());
        } else {
            throw new UnsupportedOperationException(String.format("getCurrentImage not supported for %s", object));
        }
    }

    default void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore)
    {
        return;
    }
    public void nextImage();

    default double getAnimationPeriod() {
        throw new UnsupportedOperationException(String.format("getAnimationPeriod not supported for %s", this.getClass()));
    }

    /**
     * Helper method for testing. Preserve this functionality while refactoring.
     */
    default String log(){
        return this.getId().isEmpty() ? null :
                String.format("%s %d %d %d", this.getId(), this.getPosition().x, this.getPosition().y, this.getImageIndex());
    }
}
