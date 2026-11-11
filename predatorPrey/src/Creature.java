import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
 
public abstract class Creature extends Entity {
 
    private int speed;
    private int starvation;
    private int lastX;
    private int lastY;
    private int dx;
    private int dy;
 
    private static final int STARVATION_THRESHOLD = 100;
 
    // Sets world size, shares with rendering
    public static final int WORLD_WIDTH = 1500;
    public static final int WORLD_HEIGHT = 800;
    protected static final int SIZE = 20;
 
    public Creature(int speed, int starvation, boolean isFood, int x, int y) {
        super(x, y, isFood);
        this.speed = speed;
        this.starvation = starvation;
        this.lastX = x;
        this.lastY = y;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        this.dx = rng.nextBoolean() ? 1 : -1;
        this.dy = rng.nextBoolean() ? 1 : -1;
    }
 
    protected abstract int getStarvationRate();
 
    public boolean starve(){
        starvation += getStarvationRate();
        return starvation >= STARVATION_THRESHOLD;
    }
 
    protected void recordPosition(){
        lastX = getX();
        lastY = getY();
    }
 
    public int getVelocityX(){
        return getX() - lastX;
    }
 
    public int getVelocityY(){
        return getY() - lastY;
    }
 
    protected void moveWithBounce(){
        int newX = getX() + dx * getSpeed();
        int newY = getY() + dy * getSpeed();
 
        if (newX < 0 || newX > WORLD_WIDTH - SIZE) {
            dx = -dx;
            newX = getX() + dx * getSpeed();
        }
        if (newY < 0 || newY > WORLD_HEIGHT - SIZE) {
            dy = -dy;
            newY = getY() + dy * getSpeed();
        }
 
        setX(Math.max(0, Math.min(newX, WORLD_WIDTH - SIZE)));
        setY(Math.max(0, Math.min(newY, WORLD_HEIGHT - SIZE)));
    }
 
    // Keeps a creature inside the visible world after any direct setX/setY movement
    protected void clampToWorld(){
        setX(Math.max(0, Math.min(getX(), WORLD_WIDTH - SIZE)));
        setY(Math.max(0, Math.min(getY(), WORLD_HEIGHT - SIZE)));
    }
 
    public void resetStarvation(){
        starvation = 0;
    }
 
    protected void addStarvation(int amount){
        starvation += amount;
    }
 
    public abstract void movement();
    public abstract void eat();
    public abstract void reproduce();
 
    // ---- Stream helpers -------------------------------------------------
 
    protected double distanceTo(Entity other){
        return Math.hypot(getX() - other.getX(), getY() - other.getY());
    }
 
    /** All creatures of one subtype, e.g. ofType(sim, Prey.class). Replaces the instanceof loops. */
    protected static <T extends Creature> Stream<T> ofType(List<Creature> creatures, Class<T> type){
        return creatures.stream()
                .filter(type::isInstance)
                .map(type::cast);
    }
 
    public <E extends Entity> Optional<E> findClosest(Stream<E> targets){
        return targets.min(Comparator.comparingDouble(this::distanceTo));
    }
 
    protected <E extends Creature> Optional<E> findNearestInRange(Stream<E> candidates, int range){
        return findClosest(candidates.filter(c -> distanceTo(c) <= range));
    }
 
    public int getSpeed(){
        return speed;
    }
 
    public void setSpeed(int speed){
        this.speed = speed;
    }
 
    public int getStarvation(){
        return starvation;
    }
}