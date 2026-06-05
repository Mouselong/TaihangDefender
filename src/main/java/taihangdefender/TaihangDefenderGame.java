package taihangdefender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TaihangDefenderGame {
    public static void main(String[] args) {
        if (args.length > 0 && "--self-check".equals(args[0])) {
            GameState state = new GameState(900, 600, new Random(7));
            int before = state.getEnergy();
            int gained = state.processTypedWord("orchard");
            boolean placed = state.placeTower(2, 300, TowerType.DRONE_TOWER);
            System.out.println("self-check: energy-before=" + before + ", gained=" + gained + ", placed=" + placed + ", lanes=" + state.getLaneCount());
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Taihang Defender");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new GamePanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    static class GamePanel extends JPanel {
        private final GameState state = new GameState(900, 600, new Random());
        private final Timer timer;
        private final JTextField input = new JTextField();
        private final JLabel status = new JLabel();
        private TowerType selectedTower = TowerType.DRONE_TOWER;

        GamePanel() {
            setPreferredSize(new Dimension(900, 600));
            setLayout(new BorderLayout());
            setBackground(new Color(223, 246, 201));

            JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton drone = new JButton("1 Drone Tower");
            JButton irrigation = new JButton("2 Irrigation Tower");
            JButton pesticide = new JButton("3 Pesticide Tower");
            drone.addActionListener(e -> selectedTower = TowerType.DRONE_TOWER);
            irrigation.addActionListener(e -> selectedTower = TowerType.IRRIGATION_TOWER);
            pesticide.addActionListener(e -> selectedTower = TowerType.PESTICIDE_TOWER);
            topBar.add(drone);
            topBar.add(irrigation);
            topBar.add(pesticide);
            topBar.add(status);
            add(topBar, BorderLayout.NORTH);

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.add(new JLabel(" Type an English word to gain energy: "), BorderLayout.WEST);
            bottom.add(input, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            input.addActionListener(e -> {
                state.processTypedWord(input.getText());
                input.setText("");
                refreshStatus();
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int lane = state.yToLane(e.getY());
                    if (lane >= 0) {
                        state.placeTower(lane, e.getX(), selectedTower);
                        refreshStatus();
                    }
                }
            });

            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('1'), "tower1");
            getActionMap().put("tower1", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { selectedTower = TowerType.DRONE_TOWER; }
            });
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('2'), "tower2");
            getActionMap().put("tower2", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { selectedTower = TowerType.IRRIGATION_TOWER; }
            });
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('3'), "tower3");
            getActionMap().put("tower3", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { selectedTower = TowerType.PESTICIDE_TOWER; }
            });

            timer = new Timer(33, e -> {
                state.tick(0.033);
                refreshStatus();
                repaint();
            });
            timer.start();
            refreshStatus();
        }

        private void refreshStatus() {
            status.setText("Energy: " + state.getEnergy() + "  Orchard HP: " + state.getOrchardHealth() + "  Selected: " + selectedTower.label);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int laneHeight = state.getHeight() / state.getLaneCount();
            for (int lane = 0; lane < state.getLaneCount(); lane++) {
                int y = lane * laneHeight;
                g2.setColor(lane % 2 == 0 ? new Color(209, 238, 186) : new Color(192, 227, 168));
                g2.fillRect(0, y, state.getWidth(), laneHeight);
                g2.setColor(Color.DARK_GRAY);
                g2.drawLine(0, y, state.getWidth(), y);
            }

            g2.setColor(new Color(100, 60, 20));
            g2.fillRect(0, 0, 18, state.getHeight());

            for (Tower tower : state.getTowers()) {
                g2.setColor(tower.type.color);
                g2.fillRoundRect((int) tower.x - 14, tower.lane * laneHeight + laneHeight / 2 - 14, 28, 28, 8, 8);
            }

            for (Pest pest : state.getPests()) {
                g2.setColor(new Color(156, 81, 17));
                int y = pest.lane * laneHeight + laneHeight / 2;
                g2.fillOval((int) pest.x - 12, y - 12, 24, 24);
            }
        }
    }

    enum TowerType {
        DRONE_TOWER("Drone Tower", 20, 210, 14, 1.0, 0, new Color(70, 130, 180)),
        IRRIGATION_TOWER("Irrigation Tower", 18, 180, 7, 1.2, 0, new Color(70, 171, 113)),
        PESTICIDE_TOWER("Pesticide Tower", 26, 160, 26, 1.8, 35, new Color(174, 127, 61));

        final String label;
        final int cost;
        final double range;
        final int damage;
        final double cooldownSec;
        final int splashRadius;
        final Color color;

        TowerType(String label, int cost, double range, int damage, double cooldownSec, int splashRadius, Color color) {
            this.label = label;
            this.cost = cost;
            this.range = range;
            this.damage = damage;
            this.cooldownSec = cooldownSec;
            this.splashRadius = splashRadius;
            this.color = color;
        }
    }

    enum Perk {
        ENERGY_MULTIPLIER,
        DISCOUNTED_TOWERS,
        HIGH_OUTPUT_DRONES
    }

    static class GameState {
        private static final int LANES = 5;
        private final int width;
        private final int height;
        private final Random random;
        private final List<Tower> towers = new ArrayList<>();
        private final List<Pest> pests = new ArrayList<>();
        private final ObjectPool<Pest> pestPool = new ObjectPool<>(Pest::new);
        private int energy = 40;
        private int orchardHealth = 20;
        private int wordsTyped;
        private int energyMultiplier = 2;
        private int towerDiscount;
        private boolean droneBoost;
        private double spawnAccumulator;

        GameState(int width, int height, Random random) {
            this.width = width;
            this.height = height;
            this.random = random;
        }

        int processTypedWord(String text) {
            if (text == null) return 0;
            String cleaned = text.trim().toLowerCase();
            if (!cleaned.matches("[a-z]{2,}")) return 0;
            int gain = cleaned.length() * energyMultiplier;
            energy += gain;
            wordsTyped++;
            if (wordsTyped % 5 == 0) {
                applyRandomPerk();
            }
            return gain;
        }

        boolean placeTower(int lane, int x, TowerType towerType) {
            if (lane < 0 || lane >= LANES || x < 40 || x > width - 20 || towerType == null) return false;
            int cost = Math.max(5, towerType.cost - towerDiscount);
            if (energy < cost) return false;
            energy -= cost;
            towers.add(new Tower(lane, x, towerType));
            return true;
        }

        void tick(double dtSec) {
            spawnAccumulator += dtSec;
            double spawnInterval = Math.max(0.5, 2.0 - (wordsTyped / 20.0));
            if (spawnAccumulator >= spawnInterval) {
                spawnAccumulator = 0;
                spawnPest(random.nextInt(LANES));
            }

            for (Tower tower : towers) {
                tower.cooldown -= dtSec;
                if (tower.cooldown > 0) continue;
                Pest target = findTarget(tower);
                if (target == null) continue;

                int damage = tower.type.damage;
                if (droneBoost && tower.type == TowerType.DRONE_TOWER) damage += 6;
                target.hp -= damage;
                if (tower.type == TowerType.IRRIGATION_TOWER && !target.slowed) {
                    target.speed = Math.max(target.baseSpeed * 0.65, target.speed * 0.85);
                    target.slowed = true;
                }
                if (tower.type == TowerType.PESTICIDE_TOWER) {
                    for (Pest nearby : pests) {
                        if (nearby != target && nearby.lane == target.lane && Math.abs(nearby.x - target.x) <= tower.type.splashRadius) {
                            nearby.hp -= damage / 2;
                        }
                    }
                }
                tower.cooldown = tower.type.cooldownSec;
            }

            Iterator<Pest> it = pests.iterator();
            while (it.hasNext()) {
                Pest pest = it.next();
                pest.x -= pest.speed * dtSec;
                if (pest.hp <= 0) {
                    it.remove();
                    pestPool.release(pest);
                    energy += 2;
                    continue;
                }
                if (pest.x <= 15) {
                    it.remove();
                    pestPool.release(pest);
                    orchardHealth = Math.max(0, orchardHealth - 1);
                }
            }
        }

        private Pest findTarget(Tower tower) {
            for (Pest pest : pests) {
                if (pest.lane == tower.lane && pest.x >= tower.x && pest.x - tower.x <= tower.type.range) {
                    return pest;
                }
            }
            return null;
        }

        private void applyRandomPerk() {
            Perk[] perks = EnumSet.allOf(Perk.class).toArray(new Perk[0]);
            Perk perk = perks[random.nextInt(perks.length)];
            switch (perk) {
                case ENERGY_MULTIPLIER -> energyMultiplier += 1;
                case DISCOUNTED_TOWERS -> towerDiscount += 2;
                case HIGH_OUTPUT_DRONES -> droneBoost = true;
            }
        }

        private void spawnPest(int lane) {
            Pest pest = pestPool.acquire();
            pest.reset(lane, width - 1, 40 + wordsTyped, 42 + random.nextInt(25));
            pests.add(pest);
        }

        int yToLane(int y) {
            if (y < 0 || y >= height) return -1;
            return y / (height / LANES);
        }

        int getLaneCount() { return LANES; }
        int getWidth() { return width; }
        int getHeight() { return height; }
        int getEnergy() { return energy; }
        int getOrchardHealth() { return orchardHealth; }
        List<Tower> getTowers() { return towers; }
        List<Pest> getPests() { return pests; }
    }

    static class Tower {
        final int lane;
        final double x;
        final TowerType type;
        double cooldown;

        Tower(int lane, int x, TowerType type) {
            this.lane = lane;
            this.x = x;
            this.type = type;
        }
    }

    static class Pest {
        int lane;
        double x;
        int hp;
        double speed;
        double baseSpeed;
        boolean slowed;

        void reset(int lane, double x, int hp, double speed) {
            this.lane = lane;
            this.x = x;
            this.hp = hp;
            this.speed = speed;
            this.baseSpeed = speed;
            this.slowed = false;
        }
    }

    static class ObjectPool<T> {
        private final Deque<T> pool = new ArrayDeque<>();
        private final Factory<T> factory;

        ObjectPool(Factory<T> factory) {
            this.factory = factory;
        }

        T acquire() {
            return pool.isEmpty() ? factory.create() : pool.pop();
        }

        void release(T item) {
            if (item != null) {
                pool.push(item);
            }
        }
    }

    interface Factory<T> {
        T create();
    }
}
