package com.hbau.taihang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.InputMethodEvent;
import java.text.AttributedCharacterIterator;

/**
 * Simple typing input field. Forwards completed words to GameEngine via WordManager.
 */
public class TypingField extends JTextField {
    private final WordManager wordManager;
    private GameEngine engine;
    private final GamePanel panel;

    private volatile boolean composing = false;

    public TypingField(WordManager wordManager) {
        this(wordManager, null);
    }

    public TypingField(WordManager wordManager, GamePanel panel) {
        this.wordManager = wordManager;
        this.panel = panel;
        setFont(getFont().deriveFont(16f));
        // Handle IME composition: when composing (input method active) we should not treat
        // intermediate Latin letters (pinyin) as final input. Use InputMethodListener to
        // detect composition and defer processing until composition is committed.
        addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(InputMethodEvent event) {
                AttributedCharacterIterator aci = event.getText();
                if (aci != null) {
                    // composition text present
                    composing = true;
                } else {
                    // composition finished/committed
                    composing = false;
                    SwingUtilities.invokeLater(() -> processText());
                }
            }

            @Override
            public void caretPositionChanged(InputMethodEvent event) {
                // ignore
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // If IME composition is ongoing, ignore intermediate key events
                if (composing) return;
                processText();
            }
        });
    }

    private void processText() {
        if (engine != null && engine.isAwaitingPerkChoice()) {
            setForeground(Color.DARK_GRAY);
            return;
        }
        String text = getText().trim();
        // Case-sensitive behavior: pass the raw text through to WordManager.
        wordManager.updateInput(text);
        if (text.isEmpty()) {
            setForeground(Color.BLACK);
            return;
        }
        if (!wordManager.isPrefix(text)) {
            // Clear input immediately on wrong typing
            setForeground(new Color(0xD32F2F));
            if (panel != null) {
                panel.playTypingFailFeedback(text);
            }
            SwingUtilities.invokeLater(() -> setText(""));
            wordManager.clearInput();
            return;
        }
        setForeground(Color.BLACK);
        if (wordManager.matchesCurrentWord(text)) {
            if (panel != null) {
                panel.playTypingSuccessFeedback(text);
            }
            if (engine != null) engine.onWordTyped(text);
            setText("");
            wordManager.clearInput();
        }
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }
}

