import java.util.List;
import java.util.Optional;

/**
 * A simple class representing a location in 2D space.
 */
final class Point
{
    public final int x;
    public final int y;
    public int h;
    public int f;
    public int g;
    public Point prev;

    public Point(int x, int y)
    {
        this.g = 0;
        this.h =0;
        this.x = x;
        this.y = y;
    }

    public int getF() {
        return g + h;
    }

    public void setG(int g) {
        this.g = g;
        this.f = g+h;
    }
    public int getG() {
        return g;
    }
    public void setH(int h) {
        this.h = h;
        this.f = g+h;
    }
    public int getH() {
        return h;
    }


    public Optional<Entity> nearestEntity(List<Entity> entities) {
        if (entities.isEmpty()) {
            return Optional.empty();
        } else {
            Entity nearest = entities.get(0);
            int nearestDistance = Functions.distanceSquared(nearest.getPosition(), this);

            for (Entity other : entities) {
                int otherDistance = Functions.distanceSquared(other.getPosition(), this);

                if (otherDistance < nearestDistance) {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }

    public boolean adjacent(Point p2) {
        return (this.x == p2.x && Math.abs(this.y - p2.y) == 1) || (this.y == p2.y && Math.abs(this.x - p2.x) == 1);
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public boolean equals(Object other) {
        return other instanceof Point && ((Point) other).x == this.x && ((Point) other).y == this.y;
    }

    public int hashCode() {
        int result = 17;
        result = result * 31 + x;
        result = result * 31 + y;
        return result;
    }
}
