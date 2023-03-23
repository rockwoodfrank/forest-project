import processing.core.PImage;

import java.util.*;

/**
 * Represents the 2D World in which this simulation is running.
 * Keeps track of the size of the world, the background image for each
 * location in the world, and the entities that populate the world.
 */
public final class WorldModel {
    private int numRows;
    private int numCols;
    private Background[][] background;
    private Entity[][] occupancy;
    private Set<Entity> entities;

    public WorldModel() {

    }

    public int getNumRows() {
        return numRows;
    }
    public int getNumCols() {
        return numCols;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public Optional<Entity> getOccupant(Point pos) {
        if (this.isOccupied(pos)) {
            return Optional.of(this.getOccupancyCell(pos));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Entity> findNearest(Point pos, List<Class> kinds) {
        List<Entity> ofType = new LinkedList<>();
        for (Class kind : kinds) {
            for (Entity entity : this.entities) {
                if (entity.getClass() == kind) {
                    ofType.add(entity);
                }
            }
        }

        return pos.nearestEntity(ofType);
    }

    public void parseSaveFile(Scanner saveFile, ImageStore imageStore, Background defaultBackground){
        String lastHeader = "";
        int headerLine = 0;
        int lineCounter = 0;
        while(saveFile.hasNextLine()){
            lineCounter++;
            String line = saveFile.nextLine().strip();
            if(line.endsWith(":")){
                headerLine = lineCounter;
                lastHeader = line;
                switch (line){
                    case "Backgrounds:" -> this.background = new Background[this.numRows][this.numCols];
                    case "Entities:" -> {
                        this.occupancy = new Entity[this.numRows][this.numCols];
                        this.entities = new HashSet<>();
                    }
                }
            }else{
                switch (lastHeader){
                    case "Rows:" -> this.numRows = Integer.parseInt(line);
                    case "Cols:" -> this.numCols = Integer.parseInt(line);
                    case "Backgrounds:" -> this.parseBackgroundRow(line, lineCounter-headerLine-1, imageStore);
                    case "Entities:" -> this.parseEntity(line, imageStore);
                }
            }
        }
    }

    public void removeEntityAt(Point pos) {
        if (this.withinBounds(pos) && this.getOccupancyCell(pos) != null) {
            Entity entity = this.getOccupancyCell(pos);

            /* This moves the entity just outside of the grid for
             * debugging purposes. */
            entity.setPosition(new Point(-1, -1));
            this.entities.remove(entity);
            this.setOccupancyCell(pos, null);
        }
    }

    public void load(Scanner saveFile, ImageStore imageStore, Background defaultBackground){
        this.parseSaveFile(saveFile, imageStore, defaultBackground);
        if(this.background == null){
            this.background = new Background[this.numRows][this.numCols];
            for (Background[] row : this.background)
                Arrays.fill(row, defaultBackground);
        }
        if(this.occupancy == null){
            this.occupancy = new Entity[this.numRows][this.numCols];
            this.entities = new HashSet<>();
        }
    }

    public void removeEntity(EventScheduler scheduler, Entity entity) {
        scheduler.unscheduleAllEvents(entity);
        removeEntityAt(entity.getPosition());
    }

    public void moveEntity(EventScheduler scheduler, Entity entity, Point pos) {
        Point oldPos = entity.getPosition();
        if (this.withinBounds(pos) && !pos.equals(oldPos)) {
            this.setOccupancyCell(oldPos, null);
            Optional<Entity> occupant = this.getOccupant(pos);
            occupant.ifPresent(target -> removeEntity(scheduler, target));
            this.setOccupancyCell(pos, entity);
            entity.setPosition(pos);
        }
    }

    /*
           Assumes that there is no entity currently occupying the
           intended destination cell.
        */
    public void addEntity(Entity entity) {
        if (this.withinBounds(entity.getPosition())) {
            this.setOccupancyCell(entity.getPosition(), entity);
            this.entities.add(entity);
        }
    }

    public boolean withinBounds(Point pos) {
        return pos.y >= 0 && pos.y < this.numRows && pos.x >= 0 && pos.x < this.numCols;
    }

    public boolean isOccupied(Point pos) {
        return this.withinBounds(pos) && this.getOccupancyCell(pos) != null;
    }

    public void tryAddEntity(Entity entity) {
        if (isOccupied(entity.getPosition())) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

        addEntity(entity);
    }

    public Entity getOccupancyCell(Point pos) {
        return occupancy[pos.y][pos.x];
    }

    public void setOccupancyCell(Point pos, Entity entity) {
        occupancy[pos.y][pos.x] = entity;
    }

    public Background getBackgroundCell(Point pos) {
        return this.background[pos.y][pos.x];
    }

    public void setBackgroundCell(Point pos, Background background) {
        this.background[pos.y][pos.x] = background;
    }

    public Optional<PImage> getBackgroundImage(Point pos) {
        if (withinBounds(pos)) {
            return Optional.of(getBackgroundCell(pos).getCurrentImage());
        } else {
            return Optional.empty();
        }
    }

    public void parseBackgroundRow(String line, int row, ImageStore imageStore) {
        String[] cells = line.split(" ");
        if(row < numRows){
            int rows = Math.min(cells.length, numCols);
            for (int col = 0; col < rows; col++){
                background[row][col] = new Background(cells[col], imageStore.getImageList(cells[col]));
            }
        }
    }

    public void parseEntity(String line, ImageStore imageStore) {
        String[] properties = line.split(" ", Functions.ENTITY_NUM_PROPERTIES + 1);
        if (properties.length >= Functions.ENTITY_NUM_PROPERTIES) {
            String key = properties[Functions.PROPERTY_KEY];
            String id = properties[Functions.PROPERTY_ID];
            Point pt = new Point(Integer.parseInt(properties[Functions.PROPERTY_COL]), Integer.parseInt(properties[Functions.PROPERTY_ROW]));

            properties = properties.length == Functions.ENTITY_NUM_PROPERTIES ?
                    new String[0] : properties[Functions.ENTITY_NUM_PROPERTIES].split(" ");

            switch (key) {
                case Functions.OBSTACLE_KEY -> parseObstacle(properties, pt, id, imageStore);
                case Functions.DUDE_KEY -> parseDude(properties, pt, id, imageStore);
                case Functions.FAIRY_KEY -> parseFairy(properties, pt, id, imageStore);
                case Functions.HOUSE_KEY -> parseHouse(properties, pt, id, imageStore);
                case Functions.TREE_KEY -> parseTree(properties, pt, id, imageStore);
                case Functions.SAPLING_KEY -> parseSapling(properties, pt, id, imageStore);
                case Functions.STUMP_KEY -> parseStump(properties, pt, id, imageStore);
                default -> throw new IllegalArgumentException("Entity key is unknown");
            }
        }else{
            throw new IllegalArgumentException("Entity must be formatted as [key] [id] [x] [y] ...");
        }
    }

    public void parseSapling(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == Functions.SAPLING_NUM_PROPERTIES) {
            int health = Integer.parseInt(properties[Functions.SAPLING_HEALTH]);
            Entity entity = Functions.createSapling(id, pt, imageStore.getImageList(Functions.SAPLING_KEY), health);
            tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", Functions.SAPLING_KEY, Functions.SAPLING_NUM_PROPERTIES));
        }
    }

    public void parseDude(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == Functions.DUDE_NUM_PROPERTIES) {
            Entity entity = Functions.createDudeNotFull(id, pt, Double.parseDouble(properties[Functions.DUDE_ACTION_PERIOD]), Double.parseDouble(properties[Functions.DUDE_ANIMATION_PERIOD]), Integer.parseInt(properties[Functions.DUDE_LIMIT]), imageStore.getImageList(Functions.DUDE_KEY));
            tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", Functions.DUDE_KEY, Functions.DUDE_NUM_PROPERTIES));
        }
    }

    public void parseFairy(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == Functions.FAIRY_NUM_PROPERTIES) {
            Entity entity = Functions.createFairy(id, pt, Double.parseDouble(properties[Functions.FAIRY_ACTION_PERIOD]), Double.parseDouble(properties[Functions.FAIRY_ANIMATION_PERIOD]), imageStore.getImageList(Functions.FAIRY_KEY));
            tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", Functions.FAIRY_KEY, Functions.FAIRY_NUM_PROPERTIES));
        }
    }

    public void parseTree(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == Functions.TREE_NUM_PROPERTIES) {
            Entity entity = Functions.createTree(id, pt, Double.parseDouble(properties[Functions.TREE_ACTION_PERIOD]), Double.parseDouble(properties[Functions.TREE_ANIMATION_PERIOD]), Integer.parseInt(properties[Functions.TREE_HEALTH]), imageStore.getImageList(Functions.TREE_KEY));
            tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", Functions.TREE_KEY, Functions.TREE_NUM_PROPERTIES));
        }
    }

    public void parseObstacle(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == Functions.OBSTACLE_NUM_PROPERTIES) {
            Entity entity = Functions.createObstacle(id, pt, Double.parseDouble(properties[Functions.OBSTACLE_ANIMATION_PERIOD]), imageStore.getImageList(Functions.OBSTACLE_KEY));
            tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", Functions.OBSTACLE_KEY, Functions.OBSTACLE_NUM_PROPERTIES));
        }
    }

    public void parseHouse(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == Functions.HOUSE_NUM_PROPERTIES) {
            Entity entity = Functions.createHouse(id, pt, imageStore.getImageList(Functions.HOUSE_KEY));
            tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", Functions.HOUSE_KEY, Functions.HOUSE_NUM_PROPERTIES));
        }
    }

    public void parseStump(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == Functions.STUMP_NUM_PROPERTIES) {
            Entity entity = Functions.createStump(id, pt, imageStore.getImageList(Functions.STUMP_KEY));
            tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", Functions.STUMP_KEY, Functions.STUMP_NUM_PROPERTIES));
        }
    }

    /**
     * Helper method for testing. Don't move or modify this method.
     */
    public List<String> log(){
        List<String> list = new ArrayList<>();
        for (Entity entity : entities) {
            String log = entity.log();
            if(log != null) list.add(log);
        }
        return list;
    }
}
