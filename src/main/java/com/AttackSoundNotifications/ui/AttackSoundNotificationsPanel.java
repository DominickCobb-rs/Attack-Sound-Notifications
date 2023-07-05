package com.AttackSoundNotifications.ui;

import com.AttackSoundNotifications.AttackSoundNotificationsConfig;
import com.AttackSoundNotifications.AttackSoundNotificationsPlugin;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.HitsplatID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;

import com.google.inject.Provides;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AttackSoundNotificationsPanel extends PluginPanel {
    // Default Files //
    public static final String DEFAULT_MAX_FILE = "/default_max.wav";
    public static final String DEFAULT_MISS_FILE = "/default_miss.wav";
    public static final String DEFAULT_SPEC_MISS_FILE = "/default_spec_miss.wav";
    public static final String DEFAULT_SPEC_HIT_FILE = "/default_spec_hit.wav";
    public static final String DEFAULT_SPEC_MAX_FILE = "/default_spec_max.wav";
    ///////////////////
    private Client client;
    private AttackSoundNotificationsConfig config;
    private AttackSoundNotificationsPlugin parent;

    public enum SoundOption {
        SPECIAL_HIT("Special Hit"),
        SPECIAL_MISS("Special Miss"),
        SPECIAL_MAX("Special Max"),
        MAX("Max"),
        MISS("Miss");

        private String displayValue;

        SoundOption(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }

        // This method can be used to get the enum from the string representation
        public static SoundOption fromString(String displayValue) {
            for (SoundOption option : SoundOption.values()) {
                if (option.displayValue.equalsIgnoreCase(displayValue)) {
                    return option;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.displayValue;
        }
    }

    private InputStream returnSound;

    private JButton addEntry;
    private List<NewEntryPanel> newEntryPanelList = new ArrayList<>();

    @Inject
    public AttackSoundNotificationsPanel(AttackSoundNotificationsPlugin parent, ChatboxPanelManager chatboxPanelManager,
            ChatboxItemSearch searchProvider,
            SpriteManager spriteManager, ItemManager itemManager, Client client,
            AttackSoundNotificationsConfig config) {
        this.parent = parent;
        this.client = client;
        this.config = config;
        AttackSoundNotificationsPanel panel = this;
        this.setLayout(new GridBagLayout());
        GridBagConstraints entryConstraints = new GridBagConstraints();
        entryConstraints.gridx = 0;
        entryConstraints.gridy = this.getComponentCount();
        entryConstraints.weightx = 1;
        entryConstraints.fill = GridBagConstraints.HORIZONTAL;
        entryConstraints.anchor = GridBagConstraints.NORTH;
        panel.setMaximumSize(new Dimension(225, this.getMaximumSize().height));
        addEntry = new JButton("Add sound swap");
        panel.add(addEntry, entryConstraints);
        
        
        addEntry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewEntry(searchProvider, chatboxPanelManager, spriteManager, itemManager);
                reloadPanels();
            }
        });
    }

    public void reloadPanels() {
        this.revalidate();
        this.repaint();
    }

    private void addNewEntry(ChatboxItemSearch searchProvider,
            ChatboxPanelManager chatboxPanelManager, SpriteManager spriteManager, ItemManager itemManager) {
        GridBagConstraints entryConstraints = new GridBagConstraints();
        entryConstraints.gridx = 0;
        entryConstraints.gridy = this.getComponentCount();
        entryConstraints.weightx = 1;
        entryConstraints.fill = GridBagConstraints.NONE;
        NewEntryPanel newEntryPanel = new NewEntryPanel(this, searchProvider, chatboxPanelManager,
        spriteManager, itemManager);
        newEntryPanelList.add(newEntryPanel);
        this.add(newEntryPanel.getEntryPanel(), entryConstraints);
    }

    public void removeNewEntryPanel(NewEntryPanel panel) {
        newEntryPanelList.remove(panel);
    }

    public InputStream fetchSound(Integer hitType, Integer weaponId, Boolean usedSpecialAttack) {
        for (NewEntryPanel panel : newEntryPanelList) {
            PanelData data = panel.getPanelData();
            // If it's the weaponID
            if (weaponId == data.getWeaponId() && data.active()) {
                log.debug("Found weapon in panels");
                // Only special attacks
                switch (data.getSoundReplacing()) {
                    // Spec Missed
                    case MISS: {
                        if (!usedSpecialAttack) {
                            if (hitType == HitsplatID.BLOCK_ME) {
                                if (data.getButtonGroupSetting() == "custom") {
                                    returnSound = loadCustomSound(data.getSoundFilePath());
                                    if (returnSound != null)
                                        log.debug("Found custom sound");
                                    else {
                                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
                                                "Couldn't find custom sound file:" + data.getSoundFilePath(), null);
                                        if (config.cantFind())
                                            returnSound = loadDefaultSound(DEFAULT_MISS_FILE);
                                        else
                                            returnSound = null;
                                    }
                                } else {
                                    returnSound = getDefaultSoundChoice(data.getSoundChoice());
                                }
                                return returnSound;
                            }
                        }
                    }
                        break;
                    case MAX: {
                        if (!usedSpecialAttack) {
                            log.debug("Max is being replaced");
                            if (hitType == HitsplatID.DAMAGE_MAX_ME) {
                                if (data.getButtonGroupSetting() == "custom") {
                                    returnSound = loadCustomSound(data.getSoundFilePath());
                                    if (returnSound != null)
                                        log.debug("Found custom sound");
                                    else {
                                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
                                                "Couldn't find custom sound file:" + data.getSoundFilePath(), null);
                                        if (config.cantFind())
                                            returnSound = loadDefaultSound(DEFAULT_SPEC_MAX_FILE);
                                        else
                                            returnSound = null;
                                    }
                                } else {
                                    returnSound = getDefaultSoundChoice(data.getSoundChoice());
                                }
                                return returnSound;
                            }
                        }
                    }
                        break;
                    case SPECIAL_MISS: {
                        if (usedSpecialAttack) {
                            if (hitType == HitsplatID.BLOCK_ME) {
                                if (data.getButtonGroupSetting() == "custom") {
                                    returnSound = loadCustomSound(data.getSoundFilePath());
                                    if (returnSound != null)
                                        log.debug("Found custom sound");
                                    else {
                                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
                                                "Couldn't find custom sound file:" + data.getSoundFilePath(), null);
                                        if (config.cantFind())
                                            returnSound = loadDefaultSound(DEFAULT_SPEC_MISS_FILE);
                                        else
                                            returnSound = null;
                                    }
                                } else {
                                    returnSound = getDefaultSoundChoice(data.getSoundChoice());
                                }
                                return returnSound;
                            }
                        }
                    }
                        break;
                    // Spec Hit
                    case SPECIAL_HIT: {
                        if (usedSpecialAttack) {
                            if (hitType == HitsplatID.DAMAGE_ME) {
                                if (data.getButtonGroupSetting() == "custom") {
                                    returnSound = loadCustomSound(data.getSoundFilePath());
                                    if (returnSound != null)
                                        log.debug("Found custom sound");
                                    else {
                                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
                                                "Couldn't find custom sound file:" + data.getSoundFilePath(), null);
                                        if (config.cantFind())
                                            returnSound = loadDefaultSound(DEFAULT_SPEC_HIT_FILE);
                                        else
                                            returnSound = null;
                                    }
                                } else {
                                    returnSound = getDefaultSoundChoice(data.getSoundChoice());
                                }
                                return returnSound;
                            }
                        }
                    }
                        break;
                    // Spec Maxed
                    case SPECIAL_MAX: {
                        if (usedSpecialAttack) {
                            if (hitType == HitsplatID.DAMAGE_MAX_ME) {
                                if (data.getButtonGroupSetting() == "custom") {
                                    returnSound = loadCustomSound(data.getSoundFilePath());
                                    if (returnSound != null)
                                        log.debug("Found custom sound");
                                    else {
                                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
                                                "Couldn't find custom sound file:" + data.getSoundFilePath(), null);
                                        if (config.cantFind())
                                            returnSound = loadDefaultSound(DEFAULT_SPEC_MAX_FILE);
                                        else
                                            returnSound = null;
                                    }
                                } else {
                                    returnSound = getDefaultSoundChoice(data.getSoundChoice());
                                }
                                return returnSound;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private InputStream getDefaultSoundChoice(SoundOption choice) {
        switch (choice) {
            case SPECIAL_HIT: {
                return loadDefaultSound(DEFAULT_SPEC_HIT_FILE);
            }
            case SPECIAL_MAX: {
                return loadDefaultSound(DEFAULT_SPEC_MAX_FILE);
            }
            case SPECIAL_MISS: {
                return loadDefaultSound(DEFAULT_SPEC_MISS_FILE);
            }
            case MISS: {
                return loadDefaultSound(DEFAULT_MISS_FILE);
            }
            case MAX: {
                return loadDefaultSound(DEFAULT_MAX_FILE);
            }
        }
        return null;
    }

    public InputStream loadDefaultSound(String filePath) {
        return AttackSoundNotificationsPlugin.class.getResourceAsStream(filePath);
    }

    private BufferedInputStream loadCustomSound(String fileName) {
        try {
            return new BufferedInputStream(new FileInputStream(fileName));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public void findCustomSound(String filePath) {
        InputStream soundStream = loadCustomSound(filePath);
        if (soundStream != null) {
            parent.playCustomSound(soundStream);
        } else {
            parent.playCustomSound(loadDefaultSound(DEFAULT_SPEC_MISS_FILE));
        }
    }
    public void playDefaultSound(SoundOption sound) {
        InputStream soundStream = getDefaultSoundChoice(sound);
        parent.playCustomSound(soundStream);
    }

    @Provides
    AttackSoundNotificationsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AttackSoundNotificationsConfig.class);
    }
}
