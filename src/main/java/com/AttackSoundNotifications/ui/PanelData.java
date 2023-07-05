package com.AttackSoundNotifications.ui;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

import com.AttackSoundNotifications.ui.AttackSoundNotificationsPanel.SoundOption;

public class PanelData {
    private final AtomicInteger weaponId;
    private final JCheckBox enable;
    private final JTextField soundFilePath;
    private final JComboBox<SoundOption> soundChoice;
    private final JComboBox<SoundOption> replacementChoice;
    private final ButtonGroup buttonGroup;

    public PanelData(AtomicInteger weaponId, JCheckBox enable, JTextField soundFilePath, JComboBox<SoundOption> soundChoice, JComboBox<SoundOption> replacementChoice, ButtonGroup buttonGroup) {
        this.weaponId = weaponId;
        this.enable = enable;
        this.soundFilePath = soundFilePath;
        this.soundChoice = soundChoice;
        this.replacementChoice = replacementChoice;
        this.buttonGroup = buttonGroup;
    }
    public boolean active() {
        return enable.isSelected();
    }

    public int getWeaponId() {
        return weaponId.get();
    }

    public boolean isCustomSound() {
        return enable.isSelected();
    }
    // Miss 0, Max 1, Spec miss 2, Spec hit 3, spec max 4
    public SoundOption getSoundReplacing() {
        return (SoundOption) replacementChoice.getSelectedItem();
    }

    public String getSoundFilePath() {
        return soundFilePath.getText();
    }

    public SoundOption getSoundChoice() {
        return (SoundOption) soundChoice.getSelectedItem();
    }
    public String getButtonGroupSetting() {
        Enumeration<AbstractButton> buttons = buttonGroup.getElements();
        while(buttons.hasMoreElements()) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()){
                return button.getText();
            }
        }
        return "None";
    }
}
