package com.hbau.taihang;

import javax.swing.*;
import java.awt.*;

public class ScreenController {
    private final JFrame frame;
    private final GameSettings settings;
    private final WordManager wordManager = new WordManager();
    private WaveManager waveManager;

    private StartScreen startScreen;
    private SettingsScreen settingsScreen;
    private HelpScreen helpScreen;
    private AtlasScreen atlasScreen;
    private GamePanel gamePanel;
    private InfoPanel infoPanel;
    private GameEngine engine;

    public ScreenController(JFrame frame, GameSettings settings) {
        this.frame = frame;
        this.settings = settings;
        showStartScreen();
    }

    public void showStartScreen() {
        if (engine != null) {
            engine.stop();
        }
        startScreen = new StartScreen(this::showGameScreen, this::showSettingsScreen,
                this::showHelpScreen, this::showAtlasScreen, () -> System.exit(0));
        frame.setContentPane(startScreen);
        frame.revalidate();
        frame.repaint();
    }

    public void showAtlasScreen() {
        atlasScreen = new AtlasScreen(this::showStartScreen);
        frame.setContentPane(atlasScreen);
        frame.revalidate();
        frame.repaint();
    }

    public void showSettingsScreen() {
        settingsScreen = new SettingsScreen(settings,
                this::showStartScreen,
                () -> {
                    JOptionPane.showMessageDialog(frame, "保存成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    showStartScreen();
                });
        JScrollPane scrollPane = new JScrollPane(settingsScreen);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(0xF2FAFF));
        scrollPane.getViewport().setBackground(new Color(0xF2FAFF));
        frame.setContentPane(scrollPane);
        frame.revalidate();
        frame.repaint();
    }

    public void showHelpScreen() {
        helpScreen = new HelpScreen(this::showStartScreen);
        JScrollPane scrollPane = new JScrollPane(helpScreen);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        frame.setContentPane(scrollPane);
        frame.revalidate();
        frame.repaint();
    }

    public void showGameScreen() {
        gamePanel = new GamePanel(wordManager);
        infoPanel = new InfoPanel();
        waveManager = new WaveManager();
        engine = new GameEngine(gamePanel, wordManager, waveManager);
        engine.applySettings(settings);
        gamePanel.setEngine(engine);
        gamePanel.setOnReturnToMenu(() -> {
            if (engine != null) {
                engine.stop();
            }
            showStartScreen();
        });
        gamePanel.setOnExit(() -> {
            if (engine != null) engine.stop();
            System.exit(0);
        });
        infoPanel.setEngine(engine);
        frame.setTitle("Taihang Defender");
        JPanel root = new JPanel(new BorderLayout());
        root.add(gamePanel, BorderLayout.CENTER);
        root.add(infoPanel, BorderLayout.EAST);
        frame.setContentPane(root);
        frame.revalidate();
        frame.repaint();
        engine.start();
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    public GameSettings getSettings() {
        return settings;
    }
}
