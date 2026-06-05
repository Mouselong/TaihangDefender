package com.hbau.taihang;

import javax.swing.*;
import java.awt.Font;

/**
 * Program entry point. Creates the main frame and starts the game.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            // Ensure UI uses a font that supports CJK when possible. If an embedded font
            // has been registered by FontLibrary it will be returned; otherwise this
            // will pick a sensible platform fallback.
            Font uiFont = FontLibrary.bodyFont(14f);
            java.util.Enumeration<?> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object val = UIManager.get(key);
                if (val instanceof javax.swing.plaf.FontUIResource) {
                    UIManager.put(key, new javax.swing.plaf.FontUIResource(uiFont));
                }
            }

            if (!FontLibrary.isCjkFontAvailable()) {
                JOptionPane.showMessageDialog(null,
                        "检测到当前环境可能缺少中文字体，若出现方块/乱码，请先运行 scripts\\build-and-package.ps1 或将 Noto 字体放入 resources/fonts/。",
                        "字体提示", JOptionPane.WARNING_MESSAGE);
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

