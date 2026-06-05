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

public class AtlasScreen extends JPanel {
    private static final String RESOURCE_DIR = "resources/pests/";
    
    private final Image background = ScreenAssets.loadOrCreateBackground(1280, 720);
    private final Runnable onBack;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    
    public AtlasScreen(Runnable onBack) {
        this.onBack = onBack;
        this.cardLayout = new CardLayout();
        
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // 顶部标题和导航
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        // 标题区域
        JPanel titleBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0x2E7D32), 
                                                        0, getHeight(), new Color(0x1B5E20));
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        titleBar.setLayout(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(0, 90));
        
        JLabel titleLabel = new JLabel("太行卫士图鉴", SwingConstants.CENTER);
        titleLabel.setFont(FontLibrary.titleFont(32f));
        titleLabel.setForeground(Color.WHITE);
        
        JButton backBtn = new JButton("← 返回");
        backBtn.setFont(FontLibrary.titleFont(14f));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> onBack.run());
        
        titleBar.add(backBtn, BorderLayout.WEST);
        titleBar.add(titleLabel, BorderLayout.CENTER);
        
        // 先初始化内容区域
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        
        // 导航按钮
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        navPanel.setOpaque(false);
        
        JButton pestButton = UiTheme.purpleButton("害虫图鉴", UiTheme.BTN_SIZE_MEDIUM);
        JButton towerButton = UiTheme.secondaryButton("塔图鉴", UiTheme.BTN_SIZE_MEDIUM);
        JButton perkButton = UiTheme.orangeButton("增益图鉴", UiTheme.BTN_SIZE_MEDIUM);
        
        pestButton.addActionListener(e -> cardLayout.show(contentPanel, "PEST"));
        towerButton.addActionListener(e -> cardLayout.show(contentPanel, "TOWER"));
        perkButton.addActionListener(e -> cardLayout.show(contentPanel, "PERK"));
        
        navPanel.add(pestButton);
        navPanel.add(towerButton);
        navPanel.add(perkButton);
        
        topPanel.add(titleBar, BorderLayout.NORTH);
        topPanel.add(navPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 添加内容
        contentPanel.add(createPestPanel(), "PEST");
        contentPanel.add(createTowerPanel(), "TOWER");
        contentPanel.add(createPerkPanel(), "PERK");
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setColor(new Color(255, 255, 255, 55));
            g2.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 28, 28);
        } finally {
            g2.dispose();
        }
    }
    
    // ========== 害虫图鉴 ==========
    private JComponent createPestPanel() {
        List<PestEntry> pestEntries = new ArrayList<>();
        pestEntries.add(new PestEntry("东亚飞蝗", "东亚飞蝗 .jpg", "东亚飞蝗群聚性强、迁飞速度快，适宜时容易形成大面积暴发。幼虫和成虫均能取食禾本科作物，田间一旦密度升高，危害会迅速扩大。"));
        pestEntries.add(new PestEntry("地老虎", "地老虎.jpg", "地老虎常在夜间活动，幼虫喜欢咬断幼苗茎基部，容易造成缺苗断垄。播种后至苗期是重点防治阶段。"));
        pestEntries.add(new PestEntry("小菜蛾", "小菜蛾 .jpg", "小菜蛾是十字花科蔬菜的重要害虫，幼虫喜欢啃食叶片并留下不规则孔洞。该虫繁殖快、世代短，防治上要注意连续监测。"));
        pestEntries.add(new PestEntry("桃蚜", "桃蚜.jpg", "桃蚜常群集在嫩梢和叶背吸汁，导致叶片卷曲、发黄，还可能诱发煤污病并传播多种病毒。春季和嫩梢生长期尤其需要留意。"));
        pestEntries.add(new PestEntry("玉米螟", "玉米螟.jpg", "玉米螟幼虫会钻蛀玉米茎秆和穗部，影响植株输导和灌浆，严重时会导致倒伏和减产。抽雄吐丝期是重点关注时期。"));
        pestEntries.add(new PestEntry("金针虫", "金针虫.jpg", "金针虫多在土中活动，主要咬食根系和播种后的种子、幼芽，容易造成出苗不齐和幼苗枯死。整地和播种前后防治很关键。"));
        
        return createSlideshowPanel("害虫", pestEntries, new Color(0x8E24AA));
    }
    
    // ========== 塔图鉴 ==========
    private JComponent createTowerPanel() {
        List<TowerEntry> towerEntries = new ArrayList<>();
        towerEntries.add(new TowerEntry("无人机塔", "远程精准攻击，优先锁定前排敌人。科技助农的好帮手！", "50 能量", 12, 2.6f, 1.1f));
        towerEntries.add(new TowerEntry("灌溉塔", "大范围减速，命中后大幅降低敌人移动速度。水利工程的智慧！", "30 能量", 6, 2.2f, 0.8f));
        towerEntries.add(new TowerEntry("农药塔", "范围溅射伤害，对周围敌人都能造成伤害。生态防治的利器！", "70 能量", 18, 2.4f, 0.7f));
        
        return createTowerCardPanel(towerEntries);
    }
    
    // ========== 增益图鉴 ==========
    private JComponent createPerkPanel() {
        List<PerkEntry> perkEntries = new ArrayList<>();
        int index = 1;
        perkEntries.add(new PerkEntry(index++, "智慧灌溉", "全塔伤害永久 +15%", new Color(0x1E88E5), true));
        perkEntries.add(new PerkEntry(index++, "学子勤学", "每次正确输入额外能量 +3", new Color(0x43A047), true));
        perkEntries.add(new PerkEntry(index++, "科技兴农", "20秒内全塔伤害提高且灌溉减速更强", new Color(0x00ACC1), false));
        perkEntries.add(new PerkEntry(index++, "遥感监测", "全塔射程永久 +25%", new Color(0x5E35B1), true));
        perkEntries.add(new PerkEntry(index++, "虫害预警", "当前场上敌人移速 -20%（持续10秒）", new Color(0xFB8C00), false));
        perkEntries.add(new PerkEntry(index++, "土壤肥力", "能量上限永久 +20", new Color(0x8D6E63), true));
        perkEntries.add(new PerkEntry(index++, "生态修复", "立即恢复 2 点生命值", new Color(0x26A69A), false));
        perkEntries.add(new PerkEntry(index++, "雷电防护", "对场上所有敌人造成 15 点伤害", new Color(0x7CB342), false));
        perkEntries.add(new PerkEntry(index++, "机械升级", "全塔攻击速度永久 +15%", new Color(0x6D4C41), true));
        perkEntries.add(new PerkEntry(index++, "丰收在望", "击杀得分永久 +50%", new Color(0xFBC02D), true));
        perkEntries.add(new PerkEntry(index++, "校友捐赠", "随机获得1座免费塔", new Color(0xEC407A), false));
        perkEntries.add(new PerkEntry(index++, "护田屏障", "8秒内敌人冻结移速", new Color(0x5C6BC0), false));
        perkEntries.add(new PerkEntry(index++, "农大能量站", "立即获得 30 点能量", new Color(0xFF7043), false));
        perkEntries.add(new PerkEntry(index++, "植保强化", "全塔暴击率永久 +10%", new Color(0x26A69A), true));
        perkEntries.add(new PerkEntry(index++, "学术激励", "下次正确输入获得双倍能量", new Color(0x42A5F5), false));
        perkEntries.add(new PerkEntry(index++, "寒潮来袭", "下一波所有敌人移速 -15%", new Color(0x78909C), false));
        perkEntries.add(new PerkEntry(index++, "沃土增肥", "最大生命值永久 +1", new Color(0x8BC34A), true));
        perkEntries.add(new PerkEntry(index++, "设备回收", "返还最近一座塔花费能量的 80%", new Color(0xFFA726), false));
        perkEntries.add(new PerkEntry(index++, "生物防治", "15秒内敌人每秒受到 2 点伤害", new Color(0x9CCC65), false));
        perkEntries.add(new PerkEntry(index++, "围栏加固", "在果园前放置防护墙", new Color(0xA1887F), false));
        perkEntries.add(new PerkEntry(index++, "光合作用", "击杀敌人额外获得 2 点能量", new Color(0x66BB6A), true));
        perkEntries.add(new PerkEntry(index++, "精准喷施", "下一次塔攻击伤害翻倍", new Color(0xAB47BC), false));
        
        return createPerkListPanel(perkEntries);
    }
    
    // ========== 通用滑动面板 ==========
    private <T> JComponent createSlideshowPanel(String title, List<T> entries, Color accentColor) {
        JPanel card = new JPanel();
        card.setBackground(new Color(255, 255, 255, 242));
        card.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        
        JLabel pageLabel = UiTheme.accentLabel("1 / " + entries.size(), 16f);
        pageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = UiTheme.titleLabel("", 30f);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel imageLabel = new JLabel();
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
        
        JTextArea introArea = new JTextArea();
        introArea.setEditable(false);
        introArea.setOpaque(false);
        introArea.setLineWrap(true);
        introArea.setWrapStyleWord(true);
        introArea.setFont(FontLibrary.bodyFont(18f));
        introArea.setForeground(UiTheme.BODY);
        introArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        introArea.setMaximumSize(new Dimension(700, 150));
        introArea.setBorder(new EmptyBorder(4, 8, 4, 8));
        
        JButton prevButton = UiTheme.grayButton("上一页", UiTheme.BTN_SIZE_SMALL);
        JButton nextButton = UiTheme.button("下一页", accentColor, Color.WHITE, UiTheme.BTN_SIZE_SMALL, false);
        
        final int[] currentIndex = {0};
        
        prevButton.addActionListener(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updatePestPage(currentIndex[0], entries, pageLabel, titleLabel, imageLabel, introArea, prevButton, nextButton);
            }
        });
        
        nextButton.addActionListener(e -> {
            if (currentIndex[0] < entries.size() - 1) {
                currentIndex[0]++;
                updatePestPage(currentIndex[0], entries, pageLabel, titleLabel, imageLabel, introArea, prevButton, nextButton);
            }
        });
        
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        nav.setOpaque(false);
        nav.add(prevButton);
        nav.add(nextButton);
        
        card.add(pageLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(imageLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(introArea);
        card.add(Box.createVerticalStrut(18));
        card.add(nav);
        
        updatePestPage(0, entries, pageLabel, titleLabel, imageLabel, introArea, prevButton, nextButton);
        
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 18, 10, 18);
        wrapper.add(card, gc);
        
        return wrapper;
    }
    
    private <T> void updatePestPage(int index, List<T> entries, JLabel pageLabel, JLabel titleLabel,
                                   JLabel imageLabel, JTextArea introArea, JButton prevButton, JButton nextButton) {
        if (entries.isEmpty()) return;
        
        T entry = entries.get(index);
        pageLabel.setText((index + 1) + " / " + entries.size());
        
        if (entry instanceof PestEntry) {
            PestEntry pest = (PestEntry) entry;
            titleLabel.setText(pest.displayName);
            introArea.setText(pest.introduction);
            ImageIcon icon = loadPestIcon(pest.sourceFile, pest.displayName);
            imageLabel.setIcon(icon);
            imageLabel.setText(icon == null ? "暂无图片" : null);
        }
        
        prevButton.setEnabled(index > 0);
        nextButton.setEnabled(index < entries.size() - 1);
    }
    
    // ========== 塔卡片面板 ==========
    private JComponent createTowerCardPanel(List<TowerEntry> entries) {
        JPanel content = new JPanel();
        content.setBackground(new Color(0xF1F8E9));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(Box.createVerticalStrut(30));
        
        for (TowerEntry entry : entries) {
            content.add(createTowerCard(entry));
            content.add(Box.createVerticalStrut(25));
        }
        
        content.add(Box.createVerticalStrut(30));
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(0xF1F8E9));
        scrollPane.getViewport().setBackground(new Color(0xF1F8E9));
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        
        return scrollPane;
    }
    
    private JPanel createTowerCard(TowerEntry entry) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.setColor(new Color(0xA5D6A7));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        
        card.setLayout(new BorderLayout(25, 0));
        card.setPreferredSize(new Dimension(0, 170));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        card.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        // 左侧图标
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0x66BB6A), 
                                                        0, getHeight(), new Color(0x2E7D32));
                g2.setPaint(gradient);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        iconPanel.setPreferredSize(new Dimension(110, 110));
        iconPanel.setLayout(new GridBagLayout());
        
        JLabel iconLabel = new JLabel(entry.name.substring(0, 1), SwingConstants.CENTER);
        iconLabel.setFont(FontLibrary.titleFont(44f));
        iconLabel.setForeground(Color.WHITE);
        iconPanel.add(iconLabel);
        
        card.add(iconPanel, BorderLayout.WEST);
        
        // 中间文字
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(entry.name);
        nameLabel.setFont(FontLibrary.titleFont(24f));
        nameLabel.setForeground(new Color(0x1B5E20));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(entry.description);
        descLabel.setFont(FontLibrary.bodyFont(17f));
        descLabel.setForeground(new Color(0x37474F));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel costLabel = new JLabel("成本: " + entry.cost);
        costLabel.setFont(FontLibrary.bodyFont(16f));
        costLabel.setForeground(new Color(0xE65100));
        costLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(descLabel);
        textPanel.add(Box.createVerticalStrut(12));
        textPanel.add(costLabel);
        
        // 右侧属性
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        
        JLabel damageLabel = new JLabel("伤害: " + entry.damage);
        damageLabel.setFont(FontLibrary.bodyFont(16f));
        damageLabel.setForeground(new Color(0x263238));
        damageLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        JLabel rangeLabel = new JLabel("射程: " + entry.range);
        rangeLabel.setFont(FontLibrary.bodyFont(16f));
        rangeLabel.setForeground(new Color(0x263238));
        rangeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        JLabel speedLabel = new JLabel("攻速: " + entry.attackSpeed + " 次/秒");
        speedLabel.setFont(FontLibrary.bodyFont(16f));
        speedLabel.setForeground(new Color(0x263238));
        speedLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        statsPanel.add(damageLabel);
        statsPanel.add(Box.createVerticalStrut(8));
        statsPanel.add(rangeLabel);
        statsPanel.add(Box.createVerticalStrut(8));
        statsPanel.add(speedLabel);
        
        card.add(textPanel, BorderLayout.CENTER);
        card.add(statsPanel, BorderLayout.EAST);
        
        return card;
    }
    
    // ========== 增益列表面板 ==========
    private JComponent createPerkListPanel(List<PerkEntry> entries) {
        JPanel content = new JPanel();
        content.setBackground(new Color(0xF1F8E9));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(Box.createVerticalStrut(30));
        
        for (PerkEntry entry : entries) {
            content.add(createPerkCard(entry));
            content.add(Box.createVerticalStrut(22));
        }
        
        content.add(Box.createVerticalStrut(30));
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(0xF1F8E9));
        scrollPane.getViewport().setBackground(new Color(0xF1F8E9));
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        
        return scrollPane;
    }
    
    private JPanel createPerkCard(PerkEntry entry) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint bgGradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(0xFAFAFA));
                g2.setPaint(bgGradient);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                
                g2.setColor(new Color(0xE0E0E0));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        
        card.setLayout(new BorderLayout(25, 0));
        card.setPreferredSize(new Dimension(0, 150));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        card.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 30));
        
        JPanel iconWrapper = new JPanel();
        iconWrapper.setOpaque(false);
        iconWrapper.setLayout(new GridBagLayout());
        
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, entry.color.brighter(), 
                                                        getWidth(), getHeight(), entry.color);
                g2.setPaint(gradient);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(2, 2, getWidth() - 5, getHeight() - 5);
            }
        };
        iconPanel.setPreferredSize(new Dimension(90, 90));
        iconPanel.setLayout(new GridBagLayout());
        
        JLabel iconLabel = new JLabel(String.valueOf(entry.index), SwingConstants.CENTER);
        iconLabel.setFont(FontLibrary.titleFont(32f));
        iconLabel.setForeground(Color.WHITE);
        iconPanel.add(iconLabel);
        
        iconWrapper.add(iconPanel);
        card.add(iconWrapper, BorderLayout.WEST);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(entry.name);
        nameLabel.setFont(FontLibrary.titleFont(24f));
        nameLabel.setForeground(new Color(0x0D47A1));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(entry.description);
        descLabel.setFont(FontLibrary.bodyFont(18f));
        descLabel.setForeground(new Color(0x212121));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel stackLabel = new JLabel(entry.stackable ? "◆ 可叠加" : "◆ 一次性");
        stackLabel.setFont(FontLibrary.bodyFont(15f));
        stackLabel.setForeground(entry.stackable ? new Color(0x4CAF50) : new Color(0xFF9800));
        stackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(descLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(stackLabel);
        
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    // ========== 图片加载 ==========
    private ImageIcon loadPestIcon(String fileName, String displayName) {
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
    
    // ========== 数据类 ==========
    private static class PestEntry {
        final String displayName;
        final String sourceFile;
        final String introduction;
        PestEntry(String displayName, String sourceFile, String introduction) {
            this.displayName = displayName;
            this.sourceFile = sourceFile;
            this.introduction = introduction;
        }
    }
    
    private static class TowerEntry {
        final String name;
        final String description;
        final String cost;
        final int damage;
        final float range;
        final float attackSpeed;
        TowerEntry(String name, String description, String cost, int damage, float range, float attackSpeed) {
            this.name = name;
            this.description = description;
            this.cost = cost;
            this.damage = damage;
            this.range = range;
            this.attackSpeed = attackSpeed;
        }
    }
    
    private static class PerkEntry {
        final int index;
        final String name;
        final String description;
        final Color color;
        final boolean stackable;
        PerkEntry(int index, String name, String description, Color color, boolean stackable) {
            this.index = index;
            this.name = name;
            this.description = description;
            this.color = color;
            this.stackable = stackable;
        }
    }
}
