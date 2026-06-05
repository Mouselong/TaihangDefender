package com.hbau.taihang;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final GameSettings settings = new GameSettings();
    private final ScreenController controller;

    public MainFrame() {
        setTitle("太行卫士：农大护田战");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 750);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 700));
        setLayout(new BorderLayout());
        controller = new ScreenController(this, settings);
    }
}

