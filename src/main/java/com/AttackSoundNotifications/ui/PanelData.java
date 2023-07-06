package com.AttackSoundNotifications.ui;

import javax.swing.*;
import com.AttackSoundNotifications.ui.AttackSoundNotificationsPanel.Condition;
import com.AttackSoundNotifications.ui.AttackSoundNotificationsPanel.SoundOption;

public class PanelData {
    private final Integer weaponId;
    private final JLabel enable;
    private final JTextField soundFilePath;
    private final JComboBox<SoundOption> soundChoice;
    private final JComboBox<Condition> replacementChoice;
    private final JLabel name;

    public PanelData(Integer weaponId, JLabel enable, JTextField soundFilePath,
            JComboBox<SoundOption> soundChoice, JComboBox<Condition> replacementChoice, JLabel name) {
        this.weaponId = weaponId;
        this.enable = enable;
        this.soundFilePath = soundFilePath;
        this.soundChoice = soundChoice;
        this.replacementChoice = replacementChoice;
        this.name = name;
    }

    public String getName() {
        return (String) name.getText();
    }

    public boolean active() {
        if (enable.getIcon() == EntryPanel.AUDIBLE_ICON || enable.getIcon() == EntryPanel.AUDIBLE_HOVER_ICON)
            return true;
        else
            return false;
    }

    public int getWeaponId() {
        return weaponId;
    }

    public Condition getSoundReplacing() {
        return (Condition) replacementChoice.getSelectedItem();
    }

    public String getSoundFilePath() {
        return soundFilePath.getText();
    }

    public SoundOption getSoundOption() {
        return (SoundOption) soundChoice.getSelectedItem();
    }
}
