package com.AttackSoundNotifications.ui;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.AttackSoundNotifications.AttackSoundNotificationsPlugin;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.game.chatbox.ChatboxPanelManager;

import javax.swing.*;
import javax.swing.border.Border;

import com.AttackSoundNotifications.ui.AttackSoundNotificationsPanel.SoundOption;

import lombok.extern.slf4j.Slf4j;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

@Slf4j
public class NewEntryPanel {
    private JPanel mainPanel;
    private JLabel weaponIconLabel;
    private JLabel defaultAudioRadioLabel;
    private JLabel customAudioRadioLabel;
    private JButton removeEntry;
    private JCheckBox enableCheckBox;
    private JTextField customSoundTextField;
    private JRadioButton defaultSoundOption;
    private JRadioButton customSoundOption;
    private JButton findSound;
    private JButton printData;
    private PanelData panelData;
    private AttackSoundNotificationsPanel parent;

    JComboBox<SoundOption> replacing;
    
    JComboBox<SoundOption> playing;

    public NewEntryPanel(AttackSoundNotificationsPanel parent, ChatboxItemSearch searchProvider,
        ChatboxPanelManager chatboxPanelManager, SpriteManager spriteManager, ItemManager itemManager) {
        this.parent = parent;
        weaponIconLabel = new JLabel();
        defaultAudioRadioLabel = new JLabel("Default Audio");
        customAudioRadioLabel = new JLabel("Custom Audio");
        removeEntry = new JButton("Remove Entry");
        enableCheckBox = new JCheckBox("Enable");
        customSoundTextField = new JTextField();
        defaultSoundOption = new JRadioButton("default");
        customSoundOption = new JRadioButton("custom");
        replacing = new JComboBox<>(new DefaultComboBoxModel<>(SoundOption.values()));
        playing = new JComboBox<>(new DefaultComboBoxModel<>(SoundOption.values()));

        printData = new JButton("Print datamap to console");

        weaponIconLabel.setFocusable(false);
        defaultAudioRadioLabel.setFocusable(false);
        customAudioRadioLabel.setFocusable(false);
        removeEntry.setFocusable(false);
        enableCheckBox.setFocusable(false);
        customSoundTextField.setFocusable(false);
        defaultSoundOption.setFocusable(false);
        customSoundOption.setFocusable(false);
        replacing.setFocusable(false);
        playing.setFocusable(false);
        printData.setFocusable(false);
        
        
        mainPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(225, super.getPreferredSize().height);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        Border line = BorderFactory.createMatteBorder(3, 3, 3, 3, new Color(-15132391));
        Border padding = BorderFactory.createEmptyBorder(5, 0, 5, 0);
        Border partialBorder = BorderFactory.createCompoundBorder(line, padding);
        Border fullBorder = BorderFactory.createCompoundBorder(padding, partialBorder);
        mainPanel.setBorder(fullBorder);
        mainPanel.setFocusable(true);
        
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(enableCheckBox, gbc);
        
        removeEntry = new JButton();
        removeEntry.setContentAreaFilled(false);
        removeEntry.setIcon(new ImageIcon(getClass().getResource("/icons/delete.png")));
        removeEntry.setMaximumSize(new Dimension(30, 30));
        removeEntry.setMinimumSize(new Dimension(30, 30));
        removeEntry.setPreferredSize(new Dimension(30, 30));
        removeEntry.setRequestFocusEnabled(false);
        removeEntry.setRolloverEnabled(false);
        removeEntry.setSelected(false);
        removeEntry.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(removeEntry, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(weaponIconLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(replacing, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(playing, gbc);

        findSound = new JButton();
        findSound.setText("Test the sound");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(findSound, gbc);

        printData = new JButton();
        printData.setText("Print Panel Data");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(printData, gbc);

        JPanel textFieldPanel = new JPanel();
        textFieldPanel.setLayout(new BoxLayout(textFieldPanel, BoxLayout.X_AXIS));
        customSoundTextField.setVisible(false);
        textFieldPanel.add(customSoundTextField);
        textFieldPanel.setMaximumSize(new Dimension(225, mainPanel.getMaximumSize().height));

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(textFieldPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        mainPanel.add(customAudioRadioLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        mainPanel.add(defaultAudioRadioLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(customSoundOption, gbc);

        defaultSoundOption.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(defaultSoundOption, gbc);

        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(customSoundOption);
        buttonGroup.add(defaultSoundOption);

        AtomicInteger weaponId = new AtomicInteger(-1);
        ImageIcon weaponIcon;
        try {
            BufferedImage icon = ImageIO
                    .read(AttackSoundNotificationsPlugin.class.getResourceAsStream("/icons/panelIcon.png"));
            weaponIcon = new ImageIcon(icon);
            weaponIconLabel.setIcon(weaponIcon);
        } catch (IOException e) {
            weaponIcon = null;
        }

        weaponIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        weaponIconLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                searchProvider
                        .tooltipText("Select your weapon")
                        .onItemSelected((itemId) -> {
                            if (itemId == null) {
                                System.out.print("Failed to get itemId");
                            } else {
                                weaponId.set(itemId);
                                System.out.print(Integer.toString(weaponId.get()));
                                BufferedImage weaponSprite = itemManager.getImage(weaponId.get(), 0, false);
                                ImageIcon newWeaponIcon = new ImageIcon(weaponSprite);
                                weaponIconLabel.setIcon(newWeaponIcon);
                            }
                        })
                        .build();
                chatboxPanelManager.openInput(searchProvider);
            }
        });

        removeEntry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelf();
                parent.remove(mainPanel);
                parent.reloadPanels();
            }
        });

        panelData = new PanelData(weaponId, enableCheckBox, customSoundTextField, playing, replacing, buttonGroup);
        mainPanel.setName(Integer.toString(panelData.hashCode())); // Set name to the hashCode of weaponEntry for identification

        printData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("panelID            : "+panelData.hashCode());
                System.out.println("Is enabled         : "+panelData.active());
                System.out.println("weaponId           : "+panelData.getWeaponId());
                System.out.println("We're replacing    : "+panelData.getSoundReplacing());
                System.out.println("Using audio        : "+panelData.getButtonGroupSetting());
                if(panelData.getButtonGroupSetting().equals("custom")) {
                System.out.println("Custom Sound File  : "+panelData.getSoundFilePath());
                } else {
                System.out.println("Default Sound File : "+panelData.getSoundChoice());
                }
            }
        });

        ButtonGroup customSoundToggle = new ButtonGroup();
        customSoundToggle.add(defaultSoundOption);
        customSoundToggle.add(customSoundOption);

        defaultSoundOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Default sound option selected
                playing.setVisible(true);
                customSoundTextField.setVisible(false);
                parent.reloadPanels();
            }
        });
        customSoundOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Custom sound option selected
                playing.setVisible(false);
                customSoundTextField.setVisible(true);
                parent.reloadPanels();
            }
        });

        findSound.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(panelData.getButtonGroupSetting().equals("custom")) parent.findCustomSound(panelData.getSoundFilePath());
                else parent.playDefaultSound(panelData.getSoundChoice());
            }
        });

        log.debug("Created entry panel "+ mainPanel.getName());
    }
    public JPanel getEntryPanel(){
        return mainPanel;
    }
    public PanelData getPanelData(){
        return panelData;
    }
    public void removeSelf() {
        parent.removeNewEntryPanel(this);
}
}