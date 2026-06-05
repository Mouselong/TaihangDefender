package com.hbau.taihang;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PestAtlasScreen extends JPanel {
    private static final String RESOURCE_DIR = "resources/pests/";

    private final Image background = ScreenAssets.loadOrCreateBackground(1280, 720);
    private final Runnable onBack;
    private final List<AtlasEntry> entries = new ArrayList<>();

    private final JLabel pageLabel = UiTheme.accentLabel("", 16f);
    private final JLabel titleLabel = UiTheme.titleLabel("", 30f);
    private final JLabel imageLabel = new JLabel();
    private final JTextArea introArea = new JTextArea();
    private final JButton prevButton = UiTheme.button("上一页", new Color(0x78909C), UiTheme.BUTTON_TEXT, new Dimension(140, 40), false);
    private final JButton nextButton = UiTheme.button("下一页", new Color(0x2E7D32), Color.WHITE, new Dimension(140, 40), false);
    private final JButton backButton = UiTheme.button("返回开始界面", new Color(0x1E88E5), Color.WHITE, new Dimension(180, 40), false);

    private int currentIndex = 0;

    public PestAtlasScreen(Runnable onBack) {
        this.onBack = onBack;
        buildEntries();
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 18, 10, 18);

        JPanel card = UiTheme.cardPanel(820, 600);
        card.setBackground(new Color(255, 255, 255, 242));
        card.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        pageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(520, 280));
        imageLabel.setMinimumSize(new Dimension(520, 280));
        imageLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7E6D7), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(0xF8FBF8));

        introArea.setEditable(false);
        introArea.setOpaque(false);
        introArea.setLineWrap(true);
        introArea.setWrapStyleWord(true);
        introArea.setFont(FontLibrary.bodyFont(18f));
        introArea.setForeground(UiTheme.BODY);
        introArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        introArea.setMaximumSize(new Dimension(700, 150));
        introArea.setBorder(new EmptyBorder(4, 8, 4, 8));

        prevButton.addActionListener(e -> showPage(currentIndex - 1));
        nextButton.addActionListener(e -> showPage(currentIndex + 1));
        backButton.addActionListener(e -> this.onBack.run());

        card.add(pageLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(imageLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(introArea);
        card.add(Box.createVerticalStrut(18));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        nav.setOpaque(false);
        nav.add(prevButton);
        nav.add(nextButton);
        nav.add(backButton);
        card.add(nav);

        add(card, gc);
        showPage(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 28, 28);
        } finally {
            g2.dispose();
        }
    }

    private void showPage(int index) {
        if (entries.isEmpty()) {
            return;
        }
        currentIndex = Math.max(0, Math.min(index, entries.size() - 1));
        AtlasEntry entry = entries.get(currentIndex);

        pageLabel.setText((currentIndex + 1) + " / " + entries.size());
        titleLabel.setText(entry.displayName);
        introArea.setText(entry.introduction);
        introArea.setCaretPosition(0);

        ImageIcon icon = entry.icon;
        imageLabel.setIcon(icon);
        imageLabel.setText(icon == null ? "暂无图片" : null);
        imageLabel.setFont(FontLibrary.bodyFont(18f));
        imageLabel.setForeground(UiTheme.BODY);

        prevButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < entries.size() - 1);
    }

    private void buildEntries() {
        entries.add(new AtlasEntry("东亚飞蝗", "东亚飞蝗 .jpg", "东亚飞蝗群聚性强、迁飞速度快，适宜时容易形成大面积暴发。幼虫和成虫均能取食禾本科作物，田间一旦密度升高，危害会迅速扩大。"));
        entries.add(new AtlasEntry("地老虎", "地老虎.jpg", "地老虎常在夜间活动，幼虫喜欢咬断幼苗茎基部，容易造成缺苗断垄。播种后至苗期是重点防治阶段。"));
        entries.add(new AtlasEntry("小菜蛾", "小菜蛾 .jpg", "小菜蛾是十字花科蔬菜的重要害虫，幼虫喜欢啃食叶片并留下不规则孔洞。该虫繁殖快、世代短，防治上要注意连续监测。"));
        entries.add(new AtlasEntry("桃蚜", "桃蚜.jpg", "桃蚜常群集在嫩梢和叶背吸汁，导致叶片卷曲、发黄，还可能诱发煤污病并传播多种病毒。春季和嫩梢生长期尤其需要留意。"));
        entries.add(new AtlasEntry("玉米螟", "玉米螟.jpg", "玉米螟幼虫会钻蛀玉米茎秆和穗部，影响植株输导和灌浆，严重时会导致倒伏和减产。抽雄吐丝期是重点关注时期。"));
        entries.add(new AtlasEntry("金针虫", "金针虫.jpg", "金针虫多在土中活动，主要咬食根系和播种后的种子、幼芽，容易造成出苗不齐和幼苗枯死。整地和播种前后防治很关键。"));
    }

    private ImageIcon loadIcon(String fileName, String displayName) {
        BufferedImage image = loadImage(fileName);
        if (image == null) {
            image = createPlaceholder(displayName);
        }
        return new ImageIcon(scaleToFit(image, 520, 280));
    }

    private BufferedImage loadImage(String fileName) {
        for (String candidate : candidateNames(fileName)) {
            BufferedImage image = loadFromClasspath(RESOURCE_DIR + candidate);
            if (image != null) return image;
            image = loadFromClasspath("pests/" + candidate);
            if (image != null) return image;
            File local = new File("resources/pests", candidate);
            if (local.exists()) {
                try {
                    return ImageIO.read(local);
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    private BufferedImage loadFromClasspath(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException ex) {
            return null;
        }
    }

    private List<String> candidateNames(String fileName) {
        List<String> result = new ArrayList<>();
        result.add(fileName);
        String trimmed = fileName.trim();
        if (!result.contains(trimmed)) result.add(trimmed);
        if (trimmed.endsWith(".jpg")) {
            String withoutExt = trimmed.substring(0, trimmed.length() - 4).trim();
            String normalized = withoutExt + ".jpg";
            if (!result.contains(normalized)) result.add(normalized);
            String spaced = withoutExt + " .jpg";
            if (!result.contains(spaced)) result.add(spaced);
            String png = withoutExt + ".png";
            if (!result.contains(png)) result.add(png);
        }
        return result;
    }

    private BufferedImage createPlaceholder(String title) {
        BufferedImage img = new BufferedImage(520, 280, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, new Color(0xF1F8E9), 0, 280, new Color(0xDCEDC8)));
            g.fillRoundRect(0, 0, 520, 280, 20, 20);
            g.setColor(new Color(0x558B2F));
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(2, 2, 516, 276, 20, 20);
            g.setFont(FontLibrary.titleFont(28f));
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(title);
            g.drawString(title, Math.max(16, (520 - tw) / 2), 140);
            g.setFont(FontLibrary.bodyFont(16f));
            String hint = "图片资源未找到，已显示占位图";
            int hw = g.getFontMetrics().stringWidth(hint);
            g.setColor(new Color(0x33691E));
            g.drawString(hint, Math.max(16, (520 - hw) / 2), 175);
        } finally {
            g.dispose();
        }
        return img;
    }

    private Image scaleToFit(BufferedImage image, int maxW, int maxH) {
        double scale = Math.min(maxW / (double) image.getWidth(), maxH / (double) image.getHeight());
        int w = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(image.getHeight() * scale));
        return image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    private final class AtlasEntry {
        final String displayName;
        final String sourceFile;
        final String introduction;
        final ImageIcon icon;

        AtlasEntry(String displayName, String sourceFile, String introduction) {
            this.displayName = displayName;
            this.sourceFile = sourceFile;
            this.introduction = introduction;
            this.icon = loadIcon(sourceFile, displayName);
        }
    }
}
