package com.hbau.taihang;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    public enum SoundEffect {
        PLACE_TOWER,   // 放置塔
        TOWER_SHOOT,   // 塔射击
        ENEMY_DIE,     // 敌人死亡
        WORD_SUCCESS,  // 打字成功
        WAVE_START,    // 波次开始
        WAVE_COMPLETE, // 波次结束
        GAME_WIN,      // 胜利
        GAME_LOSE,     // 失败
        PERK_CHOICE    // 选择增益
    }

    private static SoundManager instance;
    
    // 音效映射
    private final Map<SoundEffect, Clip> soundClips = new HashMap<>();
    // BGM
    private Clip bgmClip;
    
    private boolean soundEnabled = true;
    private boolean bgmEnabled = true;
    private float soundVolume = 0.8f;  // 音效音量 0.0-1.0
    private float bgmVolume = 0.5f;    // BGM音量 0.0-1.0

    private SoundManager() {
        loadBuiltInSounds();
    }

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * 加载内置的简单音频（使用合成音）
     */
    private void loadBuiltInSounds() {
        // 尝试从资源加载，失败则使用内置合成
        try {
            // 先试着找资源文件夹
            loadResourceSounds();
        } catch (Exception e) {
            System.out.println("资源音效加载失败，使用内置合成音");
        }
    }

    private void loadResourceSounds() {
        // 资源文件夹查找逻辑
        String[] soundFiles = {
            "sounds/place_tower.wav",
            "sounds/tower_shoot.wav",
            "sounds/enemy_die.wav",
            "sounds/word_success.wav",
            "sounds/wave_start.wav",
            "sounds/wave_complete.wav",
            "sounds/game_win.wav",
            "sounds/game_lose.wav",
            "sounds/perk_choice.wav"
        };
        SoundEffect[] effects = SoundEffect.values();
        
        for (int i = 0; i < effects.length && i < soundFiles.length; i++) {
            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream(soundFiles[i]);
                if (is != null) {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    soundClips.put(effects[i], clip);
                    System.out.println("加载音效: " + soundFiles[i]);
                }
            } catch (Exception e) {
                // 失败不影响，继续
            }
        }
    }

    /**
     * 播放音效
     */
    public void playSound(SoundEffect effect) {
        if (!soundEnabled) return;
        
        Clip clip = soundClips.get(effect);
        if (clip != null) {
            try {
                clip.setFramePosition(0);  // 从头开始
                // 设置音量
                setClipVolume(clip, soundVolume);
                clip.start();
            } catch (Exception e) {
                // 播放失败没关系
            }
        } else {
            // 如果没有加载到音效，用系统蜂鸣代替
            playSystemBeep(effect);
        }
    }

    /**
     * 播放系统蜂鸣作为临时音效
     */
    private void playSystemBeep(SoundEffect effect) {
        if (!soundEnabled) return;
        try {
            int freq = 440;  // 默认频率
            int duration = 100;
            
            switch (effect) {
                case PLACE_TOWER: freq = 523; duration = 150; break;  // C5
                case TOWER_SHOOT: freq = 880; duration = 50; break;    // A5
                case ENEMY_DIE: freq = 392; duration = 200; break;     // G4
                case WORD_SUCCESS: freq = 659; duration = 100; break;  // E5
                case WAVE_START: freq = 440; duration = 300; break;    // A4
                case WAVE_COMPLETE: freq = 784; duration = 400; break; // G5
                case GAME_WIN: freq = 523; duration = 600; break;      // C5
                case GAME_LOSE: freq = 220; duration = 400; break;     // A3
                case PERK_CHOICE: freq = 698; duration = 120; break;   // F5
            }
            
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            // 忽略
        }
    }

    /**
     * 播放背景音乐循环
     */
    public void playBGM() {
        if (!bgmEnabled) return;
        
        stopBGM();
        
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("sounds/bgm.wav");
            if (is != null) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                bgmClip = AudioSystem.getClip();
                bgmClip.open(ais);
                setClipVolume(bgmClip, bgmVolume);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("BGM 开始播放");
            }
        } catch (Exception e) {
            System.out.println("BGM 资源未找到，游戏继续但无BGM");
        }
    }

    /**
     * 停止背景音乐
     */
    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    private void setClipVolume(Clip clip, float volume) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            }
        } catch (Exception e) {
            // 忽略
        }
    }

    // Getters and Setters
    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean enabled) { this.soundEnabled = enabled; }
    
    public boolean isBgmEnabled() { return bgmEnabled; }
    public void setBgmEnabled(boolean enabled) { 
        this.bgmEnabled = enabled; 
        if (!enabled) {
            stopBGM();
        }
    }
    
    public float getSoundVolume() { return soundVolume; }
    public void setSoundVolume(float volume) { 
        this.soundVolume = Math.max(0f, Math.min(1f, volume)); 
    }
    
    public float getBgmVolume() { return bgmVolume; }
    public void setBgmVolume(float volume) { 
        this.bgmVolume = Math.max(0f, Math.min(1f, volume)); 
        if (bgmClip != null) {
            setClipVolume(bgmClip, this.bgmVolume);
        }
    }

    /**
     * 清理资源
     */
    public void dispose() {
        stopBGM();
        for (Clip clip : soundClips.values()) {
            if (clip != null) {
                clip.close();
            }
        }
        soundClips.clear();
    }
}
