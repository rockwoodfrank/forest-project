import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AStarPathingStrategy
        implements PathingStrategy
{
    private PriorityQueue<Point> openList;
    private  HashMap<Point, Boolean> closedList;
    private HashMap<Point, Integer> openSet;
    private Point current;
    private Point dest;
    public List<Point> computePath(Point start, Point end,
                                   Predicate<Point> canPassThrough,
                                   BiPredicate<Point, Point> withinReach,
                                   Function<Point, Stream<Point>> potentialNeighbors)
    {
        openList = new PriorityQueue<>(Comparator.comparing(Point::getF));
        closedList = new HashMap<>();
        openSet = new HashMap<>();
        start.prev = null;
        current = start;
        openList.add(start);
        while (dest == null && (!withinReach.test(current, end) || openList.size() != 0)) {
            closedList.put(current, true);
            // Check in case the entity doesn't have a destination
            if (current == null)
                break;
            if (withinReach.test(current, end))
            {
                dest = current;
                break;
            }
            Stream<Point> neighbors = potentialNeighbors.apply(current).filter(canPassThrough).filter(p -> closedList.get(p) == null);
            for (Object object : neighbors.toArray()) {
                Point point = (Point)object;
                point.g = current.g + 1;
                point.h = Math.abs(end.x - start.x) + Math.abs(end.y - start.y);
                point.f = point.g + point.h;
                point.prev = current;
                if (!openSet.containsKey(point)) {
                    openList.add(point);
                    openSet.put(point, point.f);
                }
                else if (point.f < openSet.get(point))
                {
                    openList.add(point);
                    openSet.put(point, point.f);
                }
            }
            current = openList.peek();
            if (openList.size() != 0)
                openList.remove();
        }

        List<Point> path = new ArrayList<>();
        if (current != null)
            while (current.prev != null) {
                path.add(0, current);
                current = current.prev;
            }
        openList = null;
        closedList = null;
        current = null;
        dest = null;
        return path;
    }
}