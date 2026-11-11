import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
 
public class Prey extends Creature {
    private boolean inDanger;
    private List<Creature> sim;
    private List<Grass> grassList;
    private static final int THREAT_RANGE = 120;
    private static final double FLEE_SPEED_MULTIPLIER = 1.8;
    private static final int EAT_RADIUS = 10;
    private static final double REPRODUCE_CHANCE = 0.05;
    private static final int REPRODUCE_THRESHOLD = 20;
    private static final int GRAZE_THRESHOLD = 60;
    private int starvationRate = 1;
 
    public Prey(int speed, int hunger, int x, int y, List<Creature> sim, List<Grass> grassList) {
        super(speed, hunger, false, x, y);
        this.inDanger = false;
        this.sim = sim;
        this.grassList = grassList;
    }
 
    public boolean isInDanger(){
        return inDanger;
    }
 
    public void setInDanger(boolean inDanger){
        this.inDanger = inDanger;
    }
 
    @Override
    public void reproduce() {
        if (sim == null){
            throw new IllegalStateException("Cannot reproduce: no simulation list assigned");
        }
        if (getStarvation() <= REPRODUCE_THRESHOLD
                && ThreadLocalRandom.current().nextDouble() < REPRODUCE_CHANCE) {
            sim.add(new Prey(getSpeed(), 0, getX(), getY(), sim, grassList));
            addStarvation(REPRODUCE_THRESHOLD);
        }
    }
 
    @Override
    public void movement() {
        Optional<Predator> threat = findNearestInRange(ofType(sim, Predator.class), THREAT_RANGE);
        setInDanger(threat.isPresent());
 
        // Threat nearby -> flee, otherwise graze
        threat.ifPresentOrElse(this::flee, this::graze);
        recordPosition();
    }
 
    private void graze() {
        if (getStarvation() < GRAZE_THRESHOLD) {
            moveWithBounce();
        } else {
            findClosest(edibleGrass()).ifPresent(this::moveToward);
        }
    }
 
    private Stream<Grass> edibleGrass() {
        return grassList.stream().filter(Grass::isEdible);
    }
 
    private void moveToward(Entity target) {
        double diffX = target.getX() - getX();
        double diffY = target.getY() - getY();
        double dist = Math.hypot(diffX, diffY);
        if (dist == 0) return;
 
        setX(getX() + (int) Math.round((diffX / dist) * getSpeed()));
        setY(getY() + (int) Math.round((diffY / dist) * getSpeed()));
        clampToWorld();
    }
 
    private void flee(Creature threat) {
        double diffX = getX() - threat.getX();
        double diffY = getY() - threat.getY();
        double distance = Math.hypot(diffX, diffY);
        if (distance == 0) {
            diffX = 1;
            distance = 1;
        }
 
        double boostedSpeed = getSpeed() * FLEE_SPEED_MULTIPLIER;
        int moveX = (int) Math.round((diffX / distance) * boostedSpeed);
        int moveY = (int) Math.round((diffY / distance) * boostedSpeed);
 
        setX(getX() + moveX);
        setY(getY() + moveY);
        clampToWorld();
    }
 
    @Override
    protected int getStarvationRate(){
        return isInDanger() ? starvationRate * 2 : starvationRate; // Prey starve faster when fleeing
    }
 
    @Override
    public void eat(){
        findClosest(edibleGrass())
                .filter(grass -> distanceTo(grass) <= EAT_RADIUS)
                .ifPresent(grass -> {
                    grass.setEaten(true);
                    resetStarvation();
                });
    }
}
 