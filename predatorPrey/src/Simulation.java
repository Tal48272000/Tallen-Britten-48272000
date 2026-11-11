import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
 
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.FlowLayout;
 
public class Simulation{
 
    private static final int RENDER_INTERVAL_MS = 33;
    private static final int TICKS_PER_LOGIC_UPDATE = 9;
    private static final int GRASS_SIZE = 20; // Also affects grass amount
    private static final int GRASS_GROWTH_TIME = 1000; // Logic ticks for eaten grass to regrow
    private int frameCounter = 0;
    private final Random random = new Random();
 
    List<Creature> sim;
    List<Grass> grassList;
 
    //Necessary JFrame stuff to make things visiable
    private JFrame simStart;
    private JPanel panel;
    private JButton begin;
    private JButton savePred;
    private JButton savePrey;
    private JFrame runningSim;
    private SimPanel simPanel;
    private Timer gameTimer;
    private JLabel pred;
    private JLabel prey;
    private JLabel populationLabel;
    private JTextField predAmount;
    private JTextField preyAmount;
    private JLabel warning;
    private JPanel row1;
    private JPanel row2;
    private JPanel row3;
    private JPanel row4;
    private Dimension rowSize;
 
 
    public static void main(String[] args) throws Exception {
        List<Creature> sim = new ArrayList<>();
 
        Simulation mainSim = new Simulation(sim);
        mainSim.frameInitialise();
    }
 
 
    public Simulation(List<Creature> sim){ //Our JFrame simulation constructor
        this.sim = sim;
        this.grassList = new ArrayList<>();
    }
 
    public void frameInitialise(){ // How we setup the JFramse and make it work
 
        simStart = new JFrame("Predator, Prey Simulation");
        simStart.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        simStart.setSize(800, 700);
        simStart.setLocationRelativeTo(null);
        simStart.setResizable(false);
 
        panel = new JPanel();
        warning = new JLabel("DO NOT SAVE NUMBERS MULTIPLE TIMES");
        begin = new JButton("Start");
        pred = new JLabel("Starting predator amount");
        prey = new JLabel("Starting prey amount");
        predAmount = new JTextField(10);
        preyAmount = new JTextField(10);
        savePred = new JButton("Save");
        savePrey = new JButton("Save");
 
        row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        rowSize = new Dimension(800, 80);
 
        begin.addActionListener(e -> {
            spawnGrass();
 
            simStart.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            runningSim = new JFrame("Predator, Prey Simulation. RUNNING");
            runningSim.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
            simStart.dispose();
            simStart.setVisible(false);
 
            simPanel = new SimPanel();
            simPanel.setPreferredSize(new Dimension(Creature.WORLD_WIDTH, Creature.WORLD_HEIGHT));
            simPanel.setBackground(new Color(18, 84, 13));
            runningSim.add(simPanel, BorderLayout.CENTER);
 
            populationLabel = new JLabel();
            populationLabel.setFont(populationLabel.getFont().deriveFont(Font.BOLD, 16f));
            populationLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            runningSim.add(populationLabel, BorderLayout.NORTH);
            updatePopulationLabel(); // show the starting numbers straight away
 
            runningSim.setResizable(false);
            runningSim.pack();
            runningSim.setLocationRelativeTo(null);
            runningSim.setVisible(true);
 
            startGameLoop();
        });
 
        
        savePred.addActionListener(e ->
                spawnCreatures(predAmount.getText(), "predator",
                        (x, y) -> new Predator(5, 0, x, y, sim)));
 
        savePrey.addActionListener(e ->
                spawnCreatures(preyAmount.getText(), "prey",
                        (x, y) -> new Prey(5, 10, x, y, sim, grassList)));
 
 
        //Building the first JFrame
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(80, 20, 20, 20));
        row1.add(begin);
        row1.setMaximumSize(rowSize);
 
        row2.add(warning);
        row2.setMaximumSize(rowSize);
 
        row3.add(pred);
        row3.add(predAmount);
        row3.add(savePred);
        row3.setMaximumSize(rowSize);
 
        row4.add(prey);
        row4.add(preyAmount);
        row4.add(savePrey);
        row4.setMaximumSize(rowSize);
 
        panel.add(row1);
        panel.add(row2);
        panel.add(row3);
        panel.add(row4);
 
        simStart.add(panel, BorderLayout.CENTER);
        simStart.setVisible(true);
    }
 
 
    
    private void spawnCreatures(String text, String label, BiFunction<Integer, Integer, Creature> factory){
        int count;
        try {
            count = Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            System.out.println("Invalid " + label + " amount: " + text);
            return;
        }
 
        sim.addAll(IntStream.range(0, count)
                .mapToObj(i -> factory.apply(
                        random.nextInt(Creature.WORLD_WIDTH - 20),
                        random.nextInt(Creature.WORLD_HEIGHT - 20)))
                .toList());
        System.out.println("Added " + count + " " + label + "(s)");
    }
 
 
    private void spawnGrass(){
        grassList.addAll(
                IntStream.iterate(0, x -> x < Creature.WORLD_WIDTH, x -> x + GRASS_SIZE)
                        .boxed()
                        .flatMap(x -> IntStream.iterate(0, y -> y < Creature.WORLD_HEIGHT, y -> y + GRASS_SIZE)
                                .mapToObj(y -> new Grass(
                                        x, y,
                                        random.nextInt(GRASS_GROWTH_TIME),
                                        random.nextInt(20) != 0))) // 1 in 20 tiles starts edible
                        .toList());
    }
 
 
    private void startGameLoop(){
        gameTimer = new Timer(RENDER_INTERVAL_MS, e -> stepFrame());
        gameTimer.start();
    }
 
   
    void stepFrame(){
        List<Creature> snapshot = List.copyOf(sim);
 
        
        snapshot.parallelStream().forEach(Creature::movement);
 
        snapshot.forEach(Creature::eat);
 
        if (++frameCounter >= TICKS_PER_LOGIC_UPDATE) {
            runSimulationTick();
            frameCounter = 0;
        }
 
        updatePopulationLabel();
        repaint();
    }
 
   
    private void updatePopulationLabel(){
        if (populationLabel == null) {
            return;
        }
        Map<Boolean, Long> counts = sim.stream()
                .collect(Collectors.partitioningBy(c -> c instanceof Predator, Collectors.counting()));
 
        populationLabel.setText("<html><font color='blue'>&#9679; Prey: " + counts.get(false) + "</font>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;"
                + "<font color='red'>&#9679; Predators: " + counts.get(true) + "</font></html>");
    }
 
    public void runSimulationTick(){ 
       
        sim.removeIf(Creature::starve);
 
       
        List.copyOf(sim).forEach(Creature::reproduce);
 
        
        grassList.parallelStream().forEach(Grass::tickGrowth);
    }
 
    public void repaint(){
        if (simPanel != null) {
            simPanel.repaint();
        }
    }
 
    
    private class SimPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
 
            g.setColor(new Color(80, 200, 80));
            grassList.stream()
                    .filter(Grass::isEdible)
                    .forEach(grass -> g.fillRect(grass.getX(), grass.getY(), GRASS_SIZE, GRASS_SIZE));
 
            List.copyOf(sim).forEach(c -> {
                g.setColor(c instanceof Predator ? Color.RED : Color.BLUE);
                g.fillOval(c.getX(), c.getY(), 20, 20);
            });
        }
    }
}