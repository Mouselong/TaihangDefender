package com.hbau.taihang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HelpScreen extends JPanel {
    private final Image background = ScreenAssets.loadOrCreateBackground(1280, 720);
    private final Runnable onBack;

    public HelpScreen(Runnable onBack) {
        this.onBack = onBack;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 40, 30, 40));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 240));
        card.setBorder(new EmptyBorder(28, 36, 28, 36));
        card.setMaximumSize(new Dimension(760, Integer.MAX_VALUE));

        JLabel title = UiTheme.titleLabel("帮助", 34f);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = UiTheme.subtitleLabel("先看规则，再开始保护果园！", 18f);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        card.add(section("游戏目标",
                "保护果园免受虫害侵袭！",
                "通过输入词汇获得能量，建造防御塔消灭害虫。"));
        card.add(Box.createVerticalStrut(12));

        card.add(section("塔类型介绍",
                "● 无人机塔 - 50能量：远程点射，优先攻击最前面的害虫",
                "● 灌溉塔 - 30能量：范围减速，让周围敌人移动变慢",
                "● 农药塔 - 70能量：高伤害溅射，攻击时对周围敌人造成伤害"));
        card.add(Box.createVerticalStrut(12));

        card.add(section("增益系统",
                "每波结束后可选择3种增益之一：",
                "● 塔伤害提升、射程增加、攻速加快",
                "● 词汇能量奖励、最大能量提升",
                "● 敌人减速、暴击几率、能量吸取等"));
        card.add(Box.createVerticalStrut(12));

        card.add(section("操作说明",
                "键盘快捷键：",
                "  1/2/3 - 快速切换塔类型",
                "  U - 撤销最近放置的塔（3秒内）",
                "  P - 暂停/继续游戏",
                "鼠标操作：",
                "  左键 - 放置选中的塔",
                "  右键 - 弹出快捷菜单",
                "  悬停 - 查看塔或敌人详细属性"));
        card.add(Box.createVerticalStrut(12));

        card.add(section("提示技巧",
                "鼠标悬停在塔或敌人上可查看详细属性！",
                "优先放置在敌人必经之路的前方。",
                "合理搭配不同类型的塔效果更佳！"));
        card.add(Box.createVerticalStrut(20));

        JButton back = UiTheme.primaryButton("返回开始界面", UiTheme.BTN_SIZE_MEDIUM);
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> this.onBack.run());
        card.add(back);

        add(card, BorderLayout.CENTER);
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

    private JPanel section(String titleText, String... lines) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(true);
        section.setBackground(new Color(0xF8FBF8));
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7E6D7), 1, true),
                new EmptyBorder(12, 16, 12, 16)));
        section.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.setMaximumSize(new Dimension(680, Integer.MAX_VALUE));

        JLabel title = UiTheme.accentLabel(titleText, 20f);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(title);
        section.add(Box.createVerticalStrut(6));
        for (String line : lines) {
            JLabel item = UiTheme.bodyLabel(line, 16f);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            section.add(item);
            section.add(Box.createVerticalStrut(4));
        }
        return section;
    }
}