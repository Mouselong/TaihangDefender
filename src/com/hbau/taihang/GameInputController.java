package com.hbau.taihang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

/**
 * Handles mouse and keyboard input for the game panel.
 * Responsible for previewing tower placement, drag-to-place behavior and key bindings.
 */
public class GameInputController {
    private final GamePanel panel;
    private GameEngine engine;
    private boolean dragging = false;
    private Point previewPoint;

    public GameInputController(GamePanel panel) {
        this.panel = panel;
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    public void install() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    panel.showShopMenu(e.getX(), e.getY());
                    return;
                }
                if (engine != null && engine.isAwaitingPerkChoice()) {
                    dragging = false;
                    previewPoint = null;
                    panel.setHoverPoint(null);
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (engine != null && engine.getSelectedTowerType() != null) {
                        dragging = true;
                        updatePreview(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    panel.showShopMenu(e.getX(), e.getY());
                    return;
                }
                if (engine != null && engine.isAwaitingPerkChoice()) {
                    Integer idx = panel.getPerkChoiceAt(e.getX(), e.getY());
                    dragging = false;
                    previewPoint = null;
                    panel.setHoverPoint(null);
                    if (idx != null && engine.choosePerk(idx)) {
                        panel.playPerkRipple(e.getX(), e.getY());
                        panel.playFireworks();
                        panel.resetTypingField();
                        panel.repaint();
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(e) && dragging) {
                    attemptPlace(e.getX(), e.getY());
                    dragging = false;
                    previewPoint = null;
                    panel.setHoverPoint(null);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updatePreview(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updatePreview(e.getX(), e.getY());
            }
        });

        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "selectDroneTower");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "selectIrrigationTower");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), "selectPesticideTower");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escapeAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "undoLastTower");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "togglePause");

        actionMap.put("selectDroneTower", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (engine != null && engine.isAwaitingPerkChoice()) {
                    if (engine.choosePerk(0)) {
                        int cardW = 230;
                        int gap = 18;
                        int totalW = Math.min(3, engine.getPerkChoices().size()) * cardW
                                + (Math.min(3, engine.getPerkChoices().size()) - 1) * gap;
                        int startX = Math.max(20, (panel.getWidth() - totalW) / 2);
                        int cx = startX + cardW / 2;
                        int cy = panel.getHeight() / 2;
                        panel.playPerkRipple(cx, cy);
                        panel.playFireworks();
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                    panel.repaint();
                    return;
                }
                panel.selectTowerType(TowerShop.TowerType.DRONE);
            }
        });
        actionMap.put("selectIrrigationTower", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (engine != null && engine.isAwaitingPerkChoice()) {
                    if (engine.choosePerk(1)) {
                        int cardW = 230;
                        int gap = 18;
                        int totalW = Math.min(3, engine.getPerkChoices().size()) * cardW
                                + (Math.min(3, engine.getPerkChoices().size()) - 1) * gap;
                        int startX = Math.max(20, (panel.getWidth() - totalW) / 2);
                        int cx = startX + cardW + gap + cardW / 2;
                        int cy = panel.getHeight() / 2;
                        panel.playPerkRipple(cx, cy);
                        panel.playFireworks();
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                    panel.repaint();
                    return;
                }
                panel.selectTowerType(TowerShop.TowerType.IRRIGATION);
            }
        });
        actionMap.put("selectPesticideTower", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (engine != null && engine.isAwaitingPerkChoice()) {
                    if (engine.choosePerk(2)) {
                        int cardW = 230;
                        int gap = 18;
                        int totalW = Math.min(3, engine.getPerkChoices().size()) * cardW
                                + (Math.min(3, engine.getPerkChoices().size()) - 1) * gap;
                        int startX = Math.max(20, (panel.getWidth() - totalW) / 2);
                        int cx = startX + (cardW + gap) * 2 + cardW / 2;
                        int cy = panel.getHeight() / 2;
                        panel.playPerkRipple(cx, cy);
                        panel.playFireworks();
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                    panel.repaint();
                    return;
                }
                panel.selectTowerType(TowerShop.TowerType.PESTICIDE);
            }
        });
        actionMap.put("escapeAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dragging) {
                    dragging = false;
                    previewPoint = null;
                    panel.setHoverPoint(null);
                } else {
                    panel.focusTypingField();
                }
            }
        });
        actionMap.put("undoLastTower", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (engine != null) {
                    Tower removed = engine.undoLastPlacedTower();
                    if (removed != null) {
                        panel.playUndoAnimation(removed.getX(), removed.getY());
                        panel.repaint();
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
        actionMap.put("togglePause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panel.isTypingFieldFocused()) {
                    return;
                }
                panel.togglePause();
            }
        });
    }

    private void updatePreview(int x, int y) {
        if (engine == null) {
            panel.setHoverPoint(null);
            return;
        }
        Point2D.Double snapped = engine.getSnappedPlacementPoint(x, y);
        previewPoint = new Point((int) snapped.x, (int) snapped.y);
        panel.setHoverPoint(previewPoint);
    }

    private void attemptPlace(int x, int y) {
        if (engine == null) return;
        if (engine.isAwaitingPerkChoice()) {
            return;
        }
        if (engine.getSelectedTowerType() == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        boolean ok = engine.purchaseTowerAt(engine.getSelectedTowerType(), x, y);
        if (ok) {
            panel.playPlaceSuccessAnimation((int) ((Point2D.Double) engine.getSnappedPlacementPoint(x, y)).x, (int) ((Point2D.Double) engine.getSnappedPlacementPoint(x, y)).y);
            panel.repaint();
        } else {
            panel.playInvalidPlacementAnimation(x, y);
            panel.repaint();
        }
    }
}