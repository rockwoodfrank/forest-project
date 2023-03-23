public interface Animated extends Creature{
    public double getAnimationPeriod();
    default Action createAnimationAction(int repeatCount) {
        return new Animation(this, repeatCount);
    }
}
