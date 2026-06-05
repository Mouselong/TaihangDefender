package com.hbau.taihang;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
// ...existing code...

/**
 * Main game canvas. Responsible for drawing entities and forwarding input.
 */
public class GamePanel extends JPanel {
    private GameEngine engine;
    private final WordManager wordManager;
    private final GameRenderer renderer;
    private final TypingField typingField;
    private final TowerCardButton buyDroneButton;
    private final TowerCardButton buyIrrigationButton;
    private final TowerCardButton buyPesticideButton;
    private final JPopupMenu shopMenu;
    private final JMenuItem droneItem;
    private final JMenuItem irrigationItem;
    private final JMenuItem pesticideItem;
    private Point hoverPoint;
    private Tower hoveredTower;
    private Enemy hoveredEnemy;
    private final GameInputController inputController;
    private final UndoButton undoButton;
    private final List<FadeEffect> effects = new ArrayList<>();
    private final List<ShakeEffect> shakes = new ArrayList<>();
    private final List<SuccessEffect> successes = new ArrayList<>();
    private final List<WordFeedbackEffect> wordFeedbacks = new ArrayList<>();
    private final List<Rectangle> perkCardBounds = new ArrayList<>();
    private final List<RippleEffect> ripples = new ArrayList<>();
    private final List<FireworkParticle> fireworks = new ArrayList<>();
    private final Timer uiTimer;
    private boolean gameOverDialogShown = false;
    private Runnable onReturnToMenu;
    private Runnable onExit;
    private JButton pauseButton;
    private long introStartAt = 0L;
    private final long INTRO_DURATION_MS = 6000L;

    public GamePanel(WordManager wordManager) {
        this.wordManager = wordManager;
        setBackground(new Color(0xE8F5E9));
        setLayout(null); // we'll position typing field manually
        setFocusable(true);

        renderer = new GameRenderer(this);

        typingField = new TypingField(wordManager, this);
        typingField.setBounds(10, 610, 380, 28);
        add(typingField);

        // PvZ-like card buttons (title/cost/role)
        buyDroneButton = new TowerCardButton(TowerShop.TowerType.DRONE, "无人机塔", "远程点杀", "优先前排虫害", "攻12 / 射260 / 速0.9s", 50, new Color(0x1E88E5));
        buyDroneButton.addActionListener(e -> selectTowerType(TowerShop.TowerType.DRONE));
        add(buyDroneButton);

        buyIrrigationButton = new TowerCardButton(TowerShop.TowerType.IRRIGATION, "灌溉塔", "控场减速", "命中后减速周围敌人", "攻6 / 射220 / 速1.2s", 30, new Color(0x43A047));
        buyIrrigationButton.addActionListener(e -> selectTowerType(TowerShop.TowerType.IRRIGATION));
        add(buyIrrigationButton);

        buyPesticideButton = new TowerCardButton(TowerShop.TowerType.PESTICIDE, "农药塔", "溅射清群", "命中后对附近敌人溅射", "攻18 / 射240 / 速1.4s", 70, new Color(0x8E24AA));
        buyPesticideButton.addActionListener(e -> selectTowerType(TowerShop.TowerType.PESTICIDE));
        add(buyPesticideButton);

        shopMenu = new JPopupMenu();
        droneItem = new JMenuItem("购买无人机塔(50)");
        droneItem.addActionListener(e -> selectTowerType(TowerShop.TowerType.DRONE));
        irrigationItem = new JMenuItem("购买灌溉塔(30)");
        irrigationItem.addActionListener(e -> selectTowerType(TowerShop.TowerType.IRRIGATION));
        pesticideItem = new JMenuItem("购买农药塔(70)");
        pesticideItem.addActionListener(e -> selectTowerType(TowerShop.TowerType.PESTICIDE));
        shopMenu.add(droneItem);
        shopMenu.add(irrigationItem);
        shopMenu.add(pesticideItem);

        // Undo button
        undoButton = new UndoButton("撤销 (U)");
        undoButton.setBounds(940, 610, 120, 28);
        undoButton.addActionListener(e -> {
            if (engine != null) {
                Tower removed = engine.undoLastPlacedTower();
                if (removed != null) {
                    playUndoAnimation(removed.getX(), removed.getY());
                    repaint();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
            SwingUtilities.invokeLater(() -> typingField.requestFocusInWindow());
        });
        add(undoButton);
        // help button - 优化样式
        JButton helpButton = new JButton("帮");
        helpButton.setToolTipText("帮助 (按键说明)");
        helpButton.setBounds(10, 10, 40, 32);
        helpButton.setFont(FontLibrary.titleFont(13f));
        helpButton.setForeground(Color.WHITE);
        helpButton.setBackground(new Color(0x66BB6A));
        helpButton.setBorderPainted(false);
        helpButton.setFocusPainted(false);
        helpButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "农大护田快速说明:\n"
                            + "1/2/3: 切换塔型（无人机/灌溉/农药）\n"
                            + "每波结束: 按 1/2/3 或点卡片选择增益\n"
                            + "U: 撤销最近放塔\n"
                            + "Esc: 取消或回到输入框\n"
                            + "鼠标: 移动查看范围，左键放置，右键菜单\n"
                            + "打字反馈: 正确有绿色提示，错误有红色提示\n"
                            + "提示: 输入农学词汇可攒能量，提升护田效率。",
                    "帮助", JOptionPane.INFORMATION_MESSAGE);
        });
        add(helpButton);

        // pause button - 优化样式
        pauseButton = new JButton("暂停");
        pauseButton.setToolTipText("暂停/继续 (P)");
        pauseButton.setBounds(56, 10, 70, 32);
        pauseButton.setFont(FontLibrary.titleFont(13f));
        pauseButton.setForeground(Color.WHITE);
        pauseButton.setBackground(new Color(0x42A5F5));
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.addActionListener(e -> {
            togglePause();
        });
        add(pauseButton);

        // return to menu / exit button - 优化样式
        JButton returnButton = new JButton("返回");
        returnButton.setToolTipText("返回主菜单 (Esc)");
        returnButton.setBounds(132, 10, 70, 32);
        returnButton.setFont(FontLibrary.titleFont(13f));
        returnButton.setForeground(Color.WHITE);
        returnButton.setBackground(new Color(0xFF7043));
        returnButton.setBorderPainted(false);
        returnButton.setFocusPainted(false);
        returnButton.addActionListener(e -> {
            if (onReturnToMenu != null) {
                // pause first to be safe
                if (engine != null) engine.pause();
                SwingUtilities.invokeLater(onReturnToMenu);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        });
        add(returnButton);

        // UI timer to update undo button text and drive simple effects
        uiTimer = new Timer(80, ev -> {
            updateUndoButton();
            // remove finished effects
            long now = System.currentTimeMillis();
            effects.removeIf(fe -> now - fe.startAt > fe.duration);
            shakes.removeIf(se -> now - se.startAt > se.duration);
            successes.removeIf(se -> now - se.startAt > se.duration);
            wordFeedbacks.removeIf(we -> now - we.startAt > we.duration);
            ripples.removeIf(r -> now - r.startAt > r.duration);
            fireworks.removeIf(fw -> now - fw.startAt > fw.duration || fw.dead);
            repaint();
            // If game ended, show a one-time dialog offering return or exit
            if (engine != null && engine.isGameOver() && !gameOverDialogShown) {
                gameOverDialogShown = true;
                String[] options = new String[] {"返回主菜单", "退出游戏"};
                int choice = JOptionPane.showOptionDialog(this,
                        "游戏结束！\n请选择要执行的操作：",
                        "游戏结束",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);
                if (choice == 0) {
                    if (onReturnToMenu != null) SwingUtilities.invokeLater(onReturnToMenu);
                } else if (choice == 1) {
                    if (onExit != null) SwingUtilities.invokeLater(onExit);
                    else System.exit(0);
                }
            }
        });

        // Input handling is delegated to GameInputController to keep GamePanel focused on UI/layout.
        inputController = new GameInputController(this);
        inputController.install();
        updateUndoButton();
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
        typingField.setEngine(engine);
        updateShopButtons();
        renderer.setEngine(engine);
        inputController.setEngine(engine);
        updateUndoButton();
        // show onboarding overlay on first engine attachment
        if (engine != null) startIntro();
        // reset one-time game over dialog flag when new engine attached
        gameOverDialogShown = false;
        SwingUtilities.invokeLater(() -> typingField.requestFocusInWindow());
    }

    public void setOnReturnToMenu(Runnable onReturnToMenu) {
        this.onReturnToMenu = onReturnToMenu;
    }

    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }

    /** Toggle pause/resume state from UI or keybinding. */
    public void togglePause() {
        if (engine == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (engine.isPaused()) {
            engine.resume();
            if (pauseButton != null) pauseButton.setText("暂停");
            typingField.setEnabled(true);
            buyDroneButton.setEnabled(true);
            buyIrrigationButton.setEnabled(true);
            buyPesticideButton.setEnabled(true);
        } else {
            engine.pause();
            if (pauseButton != null) pauseButton.setText("继续");
            typingField.setEnabled(false);
            buyDroneButton.setEnabled(false);
            buyIrrigationButton.setEnabled(false);
            buyPesticideButton.setEnabled(false);
        }
        SwingUtilities.invokeLater(() -> typingField.requestFocusInWindow());
    }

    private void updateShopButtons() {
        if (engine == null) {
            buyDroneButton.setEnabled(false);
            buyIrrigationButton.setEnabled(false);
            buyPesticideButton.setEnabled(false);
            return;
        }
        buyDroneButton.setEnabled(true);
        buyIrrigationButton.setEnabled(true);
        buyPesticideButton.setEnabled(true);
        int energy = engine.getEnergy();
        int droneCost = engine.getTowerCost(TowerShop.TowerType.DRONE);
        int irrigationCost = engine.getTowerCost(TowerShop.TowerType.IRRIGATION);
        int pesticideCost = engine.getTowerCost(TowerShop.TowerType.PESTICIDE);
        boolean selDrone = engine.getSelectedTowerType() == TowerShop.TowerType.DRONE;
        boolean selIrrigation = engine.getSelectedTowerType() == TowerShop.TowerType.IRRIGATION;
        boolean selPesticide = engine.getSelectedTowerType() == TowerShop.TowerType.PESTICIDE;
        buyDroneButton.setCardState(selDrone, energy >= droneCost);
        buyDroneButton.setCost(droneCost);
        buyIrrigationButton.setCardState(selIrrigation, energy >= irrigationCost);
        buyIrrigationButton.setCost(irrigationCost);
        buyPesticideButton.setCardState(selPesticide, energy >= pesticideCost);
        buyPesticideButton.setCost(pesticideCost);
        buyDroneButton.setText("");
        buyIrrigationButton.setText("");
        buyPesticideButton.setText("");

        buyDroneButton.setToolTipText(engine.getTowerShop().getTooltipText(TowerShop.TowerType.DRONE)
                .replaceFirst("价格 \\d+", "价格 " + droneCost));
        buyIrrigationButton.setToolTipText(engine.getTowerShop().getTooltipText(TowerShop.TowerType.IRRIGATION)
                .replaceFirst("价格 \\d+", "价格 " + irrigationCost));
        buyPesticideButton.setToolTipText(engine.getTowerShop().getTooltipText(TowerShop.TowerType.PESTICIDE)
                .replaceFirst("价格 \\d+", "价格 " + pesticideCost));
        droneItem.setText("购买无人机塔(" + droneCost + ")");
        irrigationItem.setText("购买灌溉塔(" + irrigationCost + ")");
        pesticideItem.setText("购买农药塔(" + pesticideCost + ")");
    }

    public void selectTowerType(TowerShop.TowerType type) {
        if (engine != null) {
            // 检查能量是否足够
            int cost = engine.getTowerCost(type);
            if (engine.getEnergy() < cost) {
                // 能量不足，触发对应的按钮提示
                if (type == TowerShop.TowerType.DRONE) {
                    buyDroneButton.triggerNotEnoughEnergy();
                } else if (type == TowerShop.TowerType.IRRIGATION) {
                    buyIrrigationButton.triggerNotEnoughEnergy();
                } else {
                    buyPesticideButton.triggerNotEnoughEnergy();
                }
                Toolkit.getDefaultToolkit().beep();
            } else {
                // 能量足够，正常选择
                engine.setSelectedTowerType(type);
                updateShopButtons();
                repaint();
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
        SwingUtilities.invokeLater(() -> typingField.requestFocusInWindow());
    }
    
    // Called by input controller to focus typing field
    public void focusTypingField() {
        SwingUtilities.invokeLater(() -> typingField.requestFocusInWindow());
    }

    // Return whether the typing field currently has keyboard focus. Used by
    // input handling to avoid global shortcuts interfering with text input.
    public boolean isTypingFieldFocused() {
        return typingField != null && typingField.isFocusOwner();
    }

    // Clear the typing input and reset WordManager's input state. Also request focus
    // back to the typing field so player can continue typing the next word.
    public void resetTypingField() {
        if (typingField != null) {
            SwingUtilities.invokeLater(() -> {
                typingField.setText("");
                typingField.requestFocusInWindow();
            });
        }
        // Ensure WordManager's lastInput is cleared as well. The GamePanel holds the
        // WordManager reference so we can clear its state here.
        try {
            // wordManager is final field in this class
            if (wordManager != null) wordManager.clearInput();
        } catch (Exception ex) {
            // ignore
        }
    }

    // Show shop popup at given coordinates (called by input controller)
    public void showShopMenu(int x, int y) {
        if (shopMenu != null) shopMenu.show(this, x, y);
    }

    // Allow input controller to update hover preview point used by renderer
    public void setHoverPoint(Point p) {
        this.hoverPoint = p;
        // 检测悬停的塔和敌人
        updateHoveredEntities(p);
        repaint();
    }
    
    private void updateHoveredEntities(Point p) {
        hoveredTower = null;
        hoveredEnemy = null;
        
        if (engine == null || p == null) {
            return;
        }
        
        // 检测悬停的塔
        for (Tower t : engine.getTowers()) {
            double dist = Math.hypot(t.getX() - p.x, t.getY() - p.y);
            if (dist <= 30) { // 塔的检测范围
                hoveredTower = t;
                break;
            }
        }
        
        // 如果没有悬停在塔上，检测敌人
        if (hoveredTower == null) {
            for (Enemy e : engine.getEnemies()) {
                double dist = Math.hypot(e.getX() - p.x, e.getY() - p.y);
                if (dist <= e.getRadius() + 10) { // 敌人的检测范围
                    hoveredEnemy = e;
                    break;
                }
            }
        }
    }

    private void updateUndoButton() {
        if (engine == null) {
            undoButton.setEnabled(false);
            undoButton.setText("撤销 (U)");
            undoButton.setProgress(0f);
            return;
        }
        if (engine.hasUndo()) {
            long ms = engine.getUndoTimeRemainingMs();
            double s = ms / 1000.0;
            undoButton.setEnabled(true);
            undoButton.setText(String.format("撤销 (U) %.1fs", s));
            float progress = (float) ms / Math.max(1f, engine.getUndoWindowMs());
            undoButton.setProgress(progress);
        } else {
            undoButton.setEnabled(false);
            undoButton.setText("撤销 (U)");
            undoButton.setProgress(0f);
        }
    }

    // Play a short fade animation at position (x,y) when a tower is undone
    public void playUndoAnimation(int x, int y) {
        effects.add(new FadeEffect(x, y, System.currentTimeMillis(), 600));
    }

    public void playInvalidPlacementAnimation(int x, int y) {
        shakes.add(new ShakeEffect(x, y, System.currentTimeMillis(), 700));
    }

    public void playPlaceSuccessAnimation(int x, int y) {
        successes.add(new SuccessEffect(x, y, System.currentTimeMillis(), 700));
    }

    public void playTypingSuccessFeedback(String text) {
        Rectangle r = typingField.getBounds();
        wordFeedbacks.add(new WordFeedbackEffect(r.x + r.width - 48, r.y - 8, true,
                "+10", System.currentTimeMillis(), 900));
    }

    public void playTypingFailFeedback(String text) {
        Rectangle r = typingField.getBounds();
        wordFeedbacks.add(new WordFeedbackEffect(r.x + r.width / 2, r.y - 8, false,
                "拼写错误", System.currentTimeMillis(), 900));
    }

    public Integer getPerkChoiceAt(int x, int y) {
        if (engine == null || !engine.isAwaitingPerkChoice()) {
            return null;
        }
        for (int i = 0; i < perkCardBounds.size(); i++) {
            if (perkCardBounds.get(i).contains(x, y)) {
                return i;
            }
        }
        return null;
    }

    private static class SuccessEffect {
        final int x, y;
        final long startAt;
        final long duration;
        SuccessEffect(int x, int y, long startAt, long duration) { this.x = x; this.y = y; this.startAt = startAt; this.duration = duration; }
    }

    private static class ShakeEffect {
        final int x, y;
        final long startAt;
        final long duration;
        ShakeEffect(int x, int y, long startAt, long duration) { this.x = x; this.y = y; this.startAt = startAt; this.duration = duration; }
    }

    private void startIntro() {
        introStartAt = System.currentTimeMillis();
    }

    private static class FadeEffect {
        final int x, y;
        final long startAt;
        final long duration;
        FadeEffect(int x, int y, long startAt, long duration) { this.x = x; this.y = y; this.startAt = startAt; this.duration = duration; }
    }

    public void playPerkRipple(int x, int y) {
        ripples.add(new RippleEffect(x, y, System.currentTimeMillis(), 800));
    }

    public void playFireworks() {
        long now = System.currentTimeMillis();
        int w = Math.max(400, getWidth());
        int h = Math.max(300, getHeight());
        // Add a screen flash particle at center
        fireworks.add(new FireworkParticle(w / 2, h / 2, 0, 0, new Color(255, 255, 220), now, 400));
        // Burst of 100 colorful particles from screen center
        for (int i = 0; i < 100; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double speed = 3.0 + Math.random() * 8.0;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed - 2.0;
            int cx = w / 2 + (int)(Math.random() * w / 4) - w / 8;
            int cy = h / 2 + (int)(Math.random() * h / 6) - h / 12;
            Color color = new Color(
                100 + (int)(Math.random() * 156),
                100 + (int)(Math.random() * 156),
                100 + (int)(Math.random() * 156));
            fireworks.add(new FireworkParticle(cx, cy, vx, vy, color, now, 1800 + (int)(Math.random() * 1200)));
        }
    }

    private static class RippleEffect {
        final int x, y;
        final long startAt, duration;
        RippleEffect(int x, int y, long startAt, long duration) { this.x = x; this.y = y; this.startAt = startAt; this.duration = duration; }
    }

    private static class FireworkParticle {
        double x, y, vx, vy;
        final Color color;
        final long startAt, duration;
        boolean dead;
        FireworkParticle(double x, double y, double vx, double vy, Color color, long startAt, long duration) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.color = color; this.startAt = startAt; this.duration = duration;
        }
    }

    private static class WordFeedbackEffect {
        final int x, y;
        final boolean success;
        final String text;
        final long startAt;
        final long duration;
        WordFeedbackEffect(int x, int y, boolean success, String text, long startAt, long duration) {
            this.x = x;
            this.y = y;
            this.success = success;
            this.text = text;
            this.startAt = startAt;
            this.duration = duration;
        }
    }

    private static class TowerCardButton extends JButton {
        private final TowerShop.TowerType type;
        private final String title;
        private final String role;
        private final String detail;
        private final String stats;
        private int cost;
        private final Color accent;
        private boolean selected;
        private boolean affordable;
        private boolean showNotEnoughEnergy;
        private Timer notEnoughEnergyTimer;

        TowerCardButton(TowerShop.TowerType type, String title, String role, String detail, String stats, int cost, Color accent) {
            this.type = type;
            this.title = title;
            this.role = role;
            this.detail = detail;
            this.stats = stats;
            this.cost = cost;
            this.accent = accent;
            this.showNotEnoughEnergy = false;
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        void triggerNotEnoughEnergy() {
            this.showNotEnoughEnergy = true;
            repaint();
            // 1.5秒后恢复原状
            if (notEnoughEnergyTimer != null) {
                notEnoughEnergyTimer.stop();
            }
            notEnoughEnergyTimer = new Timer(1500, e -> {
                this.showNotEnoughEnergy = false;
                repaint();
            });
            notEnoughEnergyTimer.setRepeats(false);
            notEnoughEnergyTimer.start();
        }

        void setCardState(boolean selected, boolean affordable) {
            this.selected = selected;
            this.affordable = affordable;
            repaint();
        }

        void setCost(int cost) {
            this.cost = cost;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                
                if (showNotEnoughEnergy) {
                    // 能量不足提示 - 红色边框
                    Color bg = new Color(255, 245, 245, 240);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
                    
                    // 渐变背景
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(0xFFCDD2), 0, h, new Color(0xEF9A9A));
                    g2.setPaint(gradient);
                    g2.fillRoundRect(0, 0, w, 45, 14, 14);
                    
                    // 红色警告图标
                    g2.setColor(new Color(0xD32F2F));
                    g2.fillOval(w/2 - 20, 12, 40, 28);
                    g2.setColor(Color.WHITE);
                    g2.setFont(FontLibrary.titleFont(18f));
                    drawCentered(g2, "!", w, 31);
                    
                    g2.setColor(new Color(0xB71C1C));
                    g2.setFont(FontLibrary.titleFont(14f));
                    drawCentered(g2, "能量不足", w, 58);
                    
                    g2.setColor(new Color(0x5D4037));
                    g2.setFont(FontLibrary.bodyFont(11f));
                    drawCentered(g2, "消耗: " + cost + " 能量", w, 75);
                    
                    // 震动效果边框
                    g2.setColor(new Color(0xD32F2F));
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                } else {
                    // 正常状态 - 更精美的卡片设计
                    Color bg = affordable ? new Color(255, 255, 255, 245) : new Color(250, 250, 250, 245);
                    Color border = selected ? new Color(0x1B5E20) : new Color(0x81C784);
                    
                    // 卡片背景阴影
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(3, 3, w - 3, h - 3, 14, 14);
                    
                    // 卡片主体
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
                    
                    // 渐变顶部装饰条
                    GradientPaint headerGradient = new GradientPaint(0, 0, accent.brighter(), 0, 45, accent);
                    g2.setPaint(headerGradient);
                    g2.fillRoundRect(0, 0, w, 45, 14, 14);
                    
                    // 顶部装饰图标区域
                    g2.setColor(new Color(255, 255, 255, 200));
                    g2.fillOval(w/2 - 18, 8, 36, 30);
                    
                    // 塔类型图标
                    g2.setColor(Color.WHITE);
                    g2.setFont(FontLibrary.titleFont(16f));
                    String icon = "";
                    if (title.contains("无人机")) icon = "飞";
                    else if (title.contains("灌溉")) icon = "水";
                    else if (title.contains("农药")) icon = "药";
                    drawCentered(g2, icon, w, 28);
                    
                    // 选中标记
                    if (selected) {
                        g2.setColor(new Color(0x1B5E20));
                        g2.setStroke(new BasicStroke(3f));
                        g2.drawRoundRect(1, 1, w - 3, h - 3, 13, 13);
                        
                        // 选中角标
                        g2.fillPolygon(new int[]{w - 25, w - 5, w - 5}, 
                                      new int[]{5, 5, 25}, 3);
                        g2.setColor(Color.WHITE);
                        g2.setFont(FontLibrary.bodyFont(9f));
                        g2.drawString("选", w - 20, 18);
                    }
                    
                    // 塔名称
                    g2.setColor(new Color(0x1B5E20));
                    g2.setFont(FontLibrary.titleFont(14f));
                    drawCentered(g2, title, w, 58);
                    
                    // 角色定位
                    g2.setFont(FontLibrary.bodyFont(12f));
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue()));
                    drawCentered(g2, "【" + role + "】", w, 75);
                    
                    // 详细描述
                    g2.setColor(new Color(0x5D4037));
                    g2.setFont(FontLibrary.bodyFont(11f));
                    drawCentered(g2, detail, w, 92);
                    
                    // 属性条
                    g2.setColor(new Color(0xE8F5E9));
                    g2.fillRoundRect(10, 100, w - 20, 18, 9, 9);
                    g2.setColor(accent);
                    g2.setFont(FontLibrary.bodyFont(10f));
                    drawCentered(g2, stats, w, 113);
                    
                    // 能量消耗
                    g2.setColor(new Color(0xFFF8E1));
                    g2.fillRoundRect(10, h - 32, w - 20, 26, 13, 13);
                    
                    // 能量图标
                    g2.setColor(new Color(0xFFC107));
                    g2.fillOval(16, h - 28, 16, 16);
                    g2.setColor(new Color(0xFF8F00));
                    g2.setFont(FontLibrary.bodyFont(10f));
                    g2.drawString("●", 18, h - 16);
                    
                    // 能量数值
                    g2.setColor(affordable ? new Color(0x2E7D32) : new Color(0xD32F2F));
                    g2.setFont(FontLibrary.titleFont(15f));
                    g2.drawString(String.valueOf(cost), 38, h - 14);
                    
                    g2.setColor(new Color(0x5D4037));
                    g2.setFont(FontLibrary.bodyFont(11f));
                    g2.drawString("能量", 60, h - 13);
                }
            } finally {
                g2.dispose();
            }
        }

        private void drawCentered(Graphics2D g2, String text, int width, int y) {
            int tw = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, Math.max(4, (width - tw) / 2), y);
        }
    }

    private static class UndoButton extends JButton {
        private float progress = 0f;

        UndoButton(String text) {
            super(text);
        }

        void setProgress(float progress) {
            this.progress = Math.max(0f, Math.min(1f, progress));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (progress <= 0f) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setColor(new Color(0x43A047));
                int h = 3;
                int w = Math.round(getWidth() * progress);
                g2.fillRect(0, getHeight() - h, w, h);
            } finally {
                g2.dispose();
            }
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int h = getHeight();
        int y = h - 42;
        int padding = 12;
        int undoW = 130;
        int inputH = 30;
        int spacing = 12;

        int totalWidth = getWidth();
        boolean compact = totalWidth < 980;
        int btnH = 130;
        // Compute available width for typing field + 3 shop buttons between left padding and undo button
        int available = Math.max(0, totalWidth - padding * 2 - undoW - spacing);

        // Preferred sizes
        int prefFieldW = 420;
        int prefBtnW = 160;

        // Start by assigning preferred widths; if not enough space, shrink buttons first then field
        int fieldW = Math.min(prefFieldW, available - 3 * (prefBtnW + spacing));
        if (fieldW < 150) fieldW = 150; // minimal typing field

        int remaining = available - fieldW;
        int btnW = prefBtnW;
        // If remaining space insufficient for preferred buttons, shrink buttons equally
        int needForBtns = 3 * prefBtnW + 2 * spacing;
        if (remaining < needForBtns) {
            int availForBtns = Math.max(3 * 60, remaining - 2 * spacing); // ensure min button width 60
            btnW = Math.max(60, availForBtns / 3);
        }

        // Now position components; ensure buttons do not overlap the undo button
        int undoX = Math.max(padding, totalWidth - padding - undoW);
        // compute maximum space available for buttons area (between left padding and undo button - spacing)
        int maxButtonsArea = Math.max(0, undoX - spacing - padding);
        // available for field + 3 buttons and internal spacings
        int availForControls = maxButtonsArea - 3 * spacing;
        // ensure fieldW not exceed availForControls - min space for buttons
        int minBtnW = 60;
        int minNeeded = 3 * minBtnW;
        if (availForControls < minNeeded + 150) {
            // not enough space, shrink fieldW to minimum
            fieldW = Math.max(100, availForControls - minNeeded);
        }
        // recompute remaining area for buttons after field
        int remainingForBtns = Math.max(0, maxButtonsArea - fieldW - spacing);
        if (remainingForBtns < 3 * minBtnW + 2 * spacing) {
            btnW = Math.max(minBtnW, (remainingForBtns - 2 * spacing) / 3);
        } else {
            btnW = Math.min(btnW, (remainingForBtns - 2 * spacing) / 3);
        }

        // Always use two rows like PvZ cards: cards row above input row.
        int inputY = y;
        int cardsY = inputY - btnH - 8;
        int fieldFullW = Math.max(120, totalWidth - padding * 2);
        typingField.setBounds(padding, inputY, fieldFullW, inputH);
        undoButton.setBounds(undoX, inputY, undoW, inputH);

        int totalBtnsW = 3 * btnW + 2 * spacing;
        int startX = Math.max(padding, (maxButtonsArea - totalBtnsW) / 2 + padding);
        int bx = startX;
        buyDroneButton.setBounds(bx, cardsY, btnW, btnH);
        bx += btnW + spacing;
        buyIrrigationButton.setBounds(bx, cardsY, btnW, btnH);
        bx += btnW + spacing;
        buyPesticideButton.setBounds(bx, cardsY, btnW, btnH);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!uiTimer.isRunning()) {
            uiTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        uiTimer.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // Delegate drawing to renderer to separate rendering responsibilities from input/logic.
            renderer.draw(g2, hoverPoint);
        } finally {
            g2.dispose();
        }

        // draw undo fade effects on top
        Graphics2D g3 = (Graphics2D) g.create();
        try {
            long now = System.currentTimeMillis();
            for (FadeEffect fe : effects) {
                double t = (now - fe.startAt) / (double) fe.duration;
                if (t < 0 || t > 1) continue;
                float alpha = (float) (1.0 - t);
                int radius = (int) (24 + 20 * t);
                g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.8f));
                g3.setColor(new Color(0xFF7043));
                g3.fillOval(fe.x - radius, fe.y - radius, radius * 2, radius * 2);
            }
        } finally {
            g3.dispose();
        }

        // draw invalid placement shake/pulse effects
        Graphics2D g4 = (Graphics2D) g.create();
        try {
            long now = System.currentTimeMillis();
            for (ShakeEffect se : shakes) {
                double t = (now - se.startAt) / (double) se.duration;
                if (t < 0 || t > 1) continue;
                float alpha = (float) (1.0 - t);
                int radius = (int) (16 + 40 * (1 - Math.abs(0.5 - t)));
                g4.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.9f));
                g4.setColor(new Color(0xD32F2F));
                g4.fillOval(se.x - radius, se.y - radius, radius * 2, radius * 2);
                g4.setComposite(AlphaComposite.SrcOver);
                g4.setColor(new Color(0xB71C1C));
                g4.setStroke(new BasicStroke(2f));
                g4.drawOval(se.x - radius, se.y - radius, radius * 2, radius * 2);
            }
            // success placement effects
            for (SuccessEffect s : successes) {
                double t = (now - s.startAt) / (double) s.duration;
                if (t < 0 || t > 1) continue;
                float alpha = (float) (1.0 - t);
                int radius = (int) (8 + 30 * t);
                g4.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.85f));
                g4.setColor(new Color(0x81C784));
                g4.fillOval(s.x - radius, s.y - radius, radius * 2, radius * 2);
                g4.setComposite(AlphaComposite.SrcOver);
                g4.setColor(new Color(0x388E3C));
                g4.setStroke(new BasicStroke(1.5f));
                g4.drawOval(s.x - radius, s.y - radius, radius * 2, radius * 2);
            }
        } finally {
            g4.dispose();
        }

        Graphics2D gWord = (Graphics2D) g.create();
        try {
            long now = System.currentTimeMillis();
            Rectangle box = typingField.getBounds();
            for (WordFeedbackEffect we : wordFeedbacks) {
                double t = (now - we.startAt) / (double) we.duration;
                if (t < 0 || t > 1) continue;
                float alpha = (float) (1.0 - t);
                int rise = (int) (28 * t);
                gWord.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                gWord.setFont(FontLibrary.titleFont(15f));
                gWord.setColor(we.success ? new Color(0x2E7D32) : new Color(0xC62828));
                int tw = gWord.getFontMetrics().stringWidth(we.text);
                int x = we.x - tw / 2;
                int y = we.y - rise;
                gWord.drawString(we.text, x, y);
                gWord.setStroke(new BasicStroke(2f));
                gWord.drawRoundRect(box.x - 4, box.y - 4, box.width + 8, box.height + 8, 10, 10);
                if (we.success) {
                    gWord.setColor(new Color(0xA5D6A7));
                    gWord.drawString("输入正确", box.x, box.y - 10);
                } else {
                    gWord.setColor(new Color(0xEF9A9A));
                    gWord.drawString("输入错误", box.x, box.y - 10);
                }
            }
        } finally {
            gWord.dispose();
        }

        // draw onboarding overlay if active
        if (introStartAt > 0) {
            long now = System.currentTimeMillis();
            long elapsed = now - introStartAt;
            if (elapsed < INTRO_DURATION_MS) {
                Graphics2D g5 = (Graphics2D) g.create();
                try {
                    int w = Math.min(480, getWidth() - 40);
                    int h = 120;
                    int x = (getWidth() - w) / 2;
                    int y = 40;
                    g5.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
                    g5.setColor(new Color(255, 255, 255, 230));
                    g5.fillRoundRect(x, y, w, h, 12, 12);
                    g5.setColor(new Color(0x1B5E20));
                    g5.setFont(FontLibrary.titleFont(16f));
                    g5.drawString("快捷键提示", x + 16, y + 26);
                    g5.setFont(FontLibrary.bodyFont(14f));
                    g5.setColor(new Color(0x2E2E2E));
                    g5.drawString("1/2/3: 切换塔型    U: 撤销    Esc: 取消/返回输入", x + 16, y + 56);
                    g5.drawString("每波结束三选一增益；正确打字有绿提示，错误有红提示。", x + 16, y + 82);
                } finally {
                    g5.dispose();
                }
            } else {
                introStartAt = 0L; // disable after duration
            }
        }

        drawRipples((Graphics2D) g);
        drawFireworks((Graphics2D) g);
        drawHoverTooltip((Graphics2D) g);
        drawPerkOverlay((Graphics2D) g);
    }
    
    private void drawHoverTooltip(Graphics2D g2Base) {
        if (hoverPoint == null || (hoveredTower == null && hoveredEnemy == null)) {
            return;
        }
        
        Graphics2D g2 = (Graphics2D) g2Base.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int tooltipX = hoverPoint.x + 20;
            int tooltipY = hoverPoint.y - 10;
            
            // 确保提示框不超出屏幕
            if (tooltipX > getWidth() - 200) {
                tooltipX = hoverPoint.x - 180;
            }
            if (tooltipY < 60) {
                tooltipY = hoverPoint.y + 30;
            }
            
            // 创建提示内容
            StringBuilder content = new StringBuilder();
            if (hoveredTower != null) {
                content.append(hoveredTower.getDisplayName()).append("\n");
                content.append("◆ 攻击力: ").append(hoveredTower.getAttackPower()).append("\n");
                content.append("◆ 射程: ").append(hoveredTower.getRange()).append("\n");
                content.append("◆ 攻速: ").append(String.format("%.2f", 1000.0 / hoveredTower.getAttackCooldownMs())).append(" 次/秒");
            } else if (hoveredEnemy != null) {
                content.append(hoveredEnemy.getRankName()).append("\n");
                content.append("◆ 生命值: ").append(hoveredEnemy.getHp()).append("\n");
                content.append("◆ 速度: ").append(String.format("%.2f", hoveredEnemy.getSpeed())).append("\n");
                if (hoveredEnemy.getSlowMultiplier() < 1.0) {
                    content.append("◆ 减速: ").append(String.format("%.0f%%", (1 - hoveredEnemy.getSlowMultiplier()) * 100)).append("\n");
                }
                content.append("◆ 分数: ").append(hoveredEnemy.getScoreValue());
            }
            
            // 计算提示框尺寸
            g2.setFont(FontLibrary.bodyFont(12f));
            String[] lines = content.toString().split("\n");
            int maxWidth = 0;
            for (String line : lines) {
                int w = g2.getFontMetrics().stringWidth(line);
                if (w > maxWidth) maxWidth = w;
            }
            int padding = 12;
            int tooltipW = maxWidth + padding * 2;
            int tooltipH = lines.length * 20 + padding * 2;
            
            // 绘制背景
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(tooltipX, tooltipY, tooltipW, tooltipH, 10, 10);
            
            // 绘制边框
            g2.setColor(new Color(0x4CAF50));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(tooltipX, tooltipY, tooltipW, tooltipH, 10, 10);
            
            // 绘制文字
            g2.setColor(Color.WHITE);
            g2.setFont(FontLibrary.bodyFont(12f));
            int lineY = tooltipY + padding + 14;
            for (String line : lines) {
                g2.drawString(line, tooltipX + padding, lineY);
                lineY += 20;
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawRipples(Graphics2D g2Base) {
        Graphics2D g2 = (Graphics2D) g2Base.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            long now = System.currentTimeMillis();
            for (RippleEffect r : ripples) {
                double t = (now - r.startAt) / (double) r.duration;
                if (t < 0 || t > 1) continue;
                float alpha = (float) (1.0 - t);
                int maxR = 160;
                for (int ring = 0; ring < 3; ring++) {
                    int radius = (int) (5 + ring * 16 + maxR * t);
                    if (radius <= 0) continue;
                    float ringAlpha = alpha * (0.6f - ring * 0.15f);
                    if (ringAlpha <= 0) continue;
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ringAlpha));
                    g2.setColor(new Color(0xFFD700));
                    g2.setStroke(new BasicStroke(Math.max(1f, 4f * (1f - (float) t) - ring * 0.8f)));
                    g2.drawOval(r.x - radius, r.y - radius, radius * 2, radius * 2);
                }
                // bright center flash
                int flashR = (int) (30 * (1.0 - t * 1.5));
                if (flashR > 0) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.7f));
                    g2.setColor(new Color(255, 255, 220));
                    g2.fillOval(r.x - flashR, r.y - flashR, flashR * 2, flashR * 2);
                }
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawFireworks(Graphics2D g2Base) {
        Graphics2D g2 = (Graphics2D) g2Base.create();
        try {
            long now = System.currentTimeMillis();
            for (FireworkParticle p : fireworks) {
                double t = (now - p.startAt) / (double) p.duration;
                if (t < 0 || t > 1) { p.dead = true; continue; }
                p.x += p.vx;
                p.y += p.vy;
                p.vy += 0.10;
                p.vx *= 0.985;
                p.vy *= 0.985;
                // Screen flash particle (speed 0)
                if (Math.abs(p.vx) < 0.1 && Math.abs(p.vy) < 0.1 && t < 0.3) {
                    float flashAlpha = (float) Math.max(0, 0.3 - t) / 0.3f;
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flashAlpha * 0.5f));
                    g2.setColor(new Color(255, 255, 220));
                    g2.fillRect(0, 0, g2Base.getClipBounds() != null ? g2Base.getClipBounds().width : getWidth(),
                        g2Base.getClipBounds() != null ? g2Base.getClipBounds().height : getHeight());
                    continue;
                }
                float alpha = (float) Math.max(0, 1.0 - t * t * t);
                int sz = Math.max(2, (int) (10 * (1.0 - t * 0.7)));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                // Bright core
                g2.setColor(new Color(
                    Math.min(255, p.color.getRed() + 60),
                    Math.min(255, p.color.getGreen() + 60),
                    Math.min(255, p.color.getBlue() + 60),
                    (int) (alpha * 255)));
                g2.fillOval((int) p.x - sz / 2, (int) p.y - sz / 2, sz, sz);
                // Outer glow
                int glowSz = sz + 4;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.4f));
                g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int) (alpha * 100)));
                g2.fillOval((int) p.x - glowSz / 2, (int) p.y - glowSz / 2, glowSz, glowSz);
                // Trail
                int tsz = Math.max(1, sz * 2 / 3);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.6f));
                g2.setColor(new Color(255, 255, 200, (int) (alpha * 180)));
                g2.fillOval((int) (p.x - p.vx * 2) - tsz / 2, (int) (p.y - p.vy * 2) - tsz / 2, tsz, tsz);
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawPerkOverlay(Graphics2D g2Base) {
        perkCardBounds.clear();
        if (engine == null || !engine.isAwaitingPerkChoice()) {
            return;
        }
        java.util.List<GameEngine.PerkChoice> choices = engine.getPerkChoices();
        if (choices.isEmpty()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g2Base.create();
        try {
            // 半透明背景
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            int cardW = 260;
            int cardH = 160;
            int gap = 24;
            int totalW = choices.size() * cardW + (choices.size() - 1) * gap;
            int startX = Math.max(20, (getWidth() - totalW) / 2);
            int y = Math.max(120, getHeight() / 2 - cardH / 2);

            // 标题背景装饰
            g2.setColor(new Color(0x1B5E20));
            g2.fillRoundRect(getWidth()/2 - 180, y - 60, 360, 45, 22, 22);
            g2.setColor(new Color(0x2E7D32));
            g2.fillRoundRect(getWidth()/2 - 176, y - 56, 352, 37, 18, 18);
            
            // 标题文字
            g2.setFont(FontLibrary.titleFont(22f));
            g2.setColor(Color.WHITE);
            String title = "◆ 波次结束：三选一增益 ◆";
            int tw = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (getWidth() - tw) / 2, y - 28);

            // 增益卡片
            for (int i = 0; i < choices.size(); i++) {
                GameEngine.PerkChoice c = choices.get(i);
                int x = startX + i * (cardW + gap);
                Rectangle rect = new Rectangle(x, y, cardW, cardH);
                perkCardBounds.add(rect);

                // 卡片阴影
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(x + 4, y + 4, cardW, cardH, 18, 18);
                
                // 卡片背景
                GradientPaint cardBg = new GradientPaint(x, y, new Color(255, 255, 255), 
                                                        x, y + cardH, new Color(245, 245, 250));
                g2.setPaint(cardBg);
                g2.fillRoundRect(x, y, cardW, cardH, 18, 18);
                
                // 卡片边框
                g2.setColor(new Color(0x4CAF50));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(x, y, cardW, cardH, 18, 18);
                
                // 顶部装饰条
                Color[] perkColors = {new Color(0xFF7043), new Color(0x42A5F5), new Color(0x66BB6A)};
                GradientPaint headerGrad = new GradientPaint(x, y, perkColors[i].brighter(), 
                                                          x, y + 40, perkColors[i]);
                g2.setPaint(headerGrad);
                g2.fillRoundRect(x, y, cardW, 42, 18, 18);
                
                // 数字标记
                g2.setColor(Color.WHITE);
                g2.fillOval(x + cardW/2 - 18, y + 8, 36, 28);
                g2.setColor(perkColors[i].darker());
                g2.setFont(FontLibrary.titleFont(20f));
                String numStr = String.valueOf(i + 1);
                int numW = g2.getFontMetrics().stringWidth(numStr);
                g2.drawString(numStr, x + cardW/2 - numW/2, y + 28);
                
                // 增益标题
                g2.setFont(FontLibrary.titleFont(16f));
                g2.setColor(new Color(0x1B5E20));
                String titleStr = c.getTitle();
                int titleW = g2.getFontMetrics().stringWidth(titleStr);
                g2.drawString(titleStr, x + cardW/2 - titleW/2, y + 70);
                
                // 分隔线
                g2.setColor(new Color(0xA5D6A7));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(x + 20, y + 80, x + cardW - 20, y + 80);
                
                // 增益描述
                g2.setFont(FontLibrary.bodyFont(12f));
                g2.setColor(new Color(0x37474F));
                drawWrappedText(g2, c.getDescription(), x + 15, y + 100, cardW - 30, 18);
                
                // 底部装饰
                g2.setColor(new Color(0xFFF9C4));
                g2.fillRoundRect(x + 15, y + cardH - 28, cardW - 30, 20, 10, 10);
                g2.setFont(FontLibrary.bodyFont(10f));
                g2.setColor(new Color(0xF57F17));
                String bottomStr = "◇ 选择此增益 ◇";
                int bottomW = g2.getFontMetrics().stringWidth(bottomStr);
                g2.drawString(bottomStr, x + cardW/2 - bottomW/2, y + cardH - 14);
            }

            // 底部提示
            g2.setFont(FontLibrary.bodyFont(13f));
            g2.setColor(new Color(0xE8F5E9));
            String hint = "按 1/2/3 或点击卡片选择";
            int hintW = g2.getFontMetrics().stringWidth(hint);
            g2.drawString(hint, (getWidth() - hintW) / 2, y + cardH + 40);
        } finally {
            g2.dispose();
        }
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        // 过滤掉可能导致问题的特殊字符（如控制字符）
        StringBuilder cleanText = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 32) { // 只保留可打印字符
                cleanText.append(c);
            }
        }
        
        // 简单的换行逻辑，按整字换行
        String[] words = cleanText.toString().split("\\s+");
        StringBuilder line = new StringBuilder();
        int cy = y;
        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            if (g2.getFontMetrics().stringWidth(testLine) <= maxWidth) {
                if (line.length() == 0) {
                    line.append(word);
                } else {
                    line.append(" ").append(word);
                }
            } else {
                if (line.length() > 0) {
                    g2.drawString(line.toString(), x, cy);
                    cy += lineHeight;
                }
                // 检查单个词是否超过宽度，超过的话就强制截断
                if (g2.getFontMetrics().stringWidth(word) > maxWidth) {
                    // 对于过长的词，逐个字符添加
                    StringBuilder longWord = new StringBuilder();
                    for (int i = 0; i < word.length(); i++) {
                        char c = word.charAt(i);
                        String test = longWord.toString() + c;
                        if (g2.getFontMetrics().stringWidth(test) <= maxWidth) {
                            longWord.append(c);
                        } else {
                            g2.drawString(longWord.toString(), x, cy);
                            cy += lineHeight;
                            longWord = new StringBuilder();
                            longWord.append(c);
                        }
                    }
                    line = longWord;
                } else {
                    line = new StringBuilder(word);
                }
            }
        }
        if (line.length() > 0) {
            g2.drawString(line.toString(), x, cy);
        }
    }
}
