/*
 * Copyright (c) 2018, Kamiel, <https://github.com/Kamielvf>
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
// Chatbox Item Search - From runelite/client/plugins/banktags/tabs/TabInterface
/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * Copyright (c) 2018, Ron Young <https://github.com/raiyni>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.AttackSoundNotifications.ui;

import java.util.concurrent.atomic.AtomicInteger;

import com.AttackSoundNotifications.AttackSoundNotificationsPlugin;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.AttackSoundNotifications.ui.AttackSoundNotificationsPanel.Condition;
import com.AttackSoundNotifications.ui.AttackSoundNotificationsPanel.SoundOption;

import lombok.extern.slf4j.Slf4j;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
public class NewEntryPanel extends JPanel {
    private JPanel mainPanel;
    // Data fields //
    private JLabel name = new JLabel();
    private JLabel weaponIconLabel = new JLabel();
    private JLabel removeEntry = new JLabel();
    private JLabel audible = new JLabel();
    private JTextField customSoundTextField = new JTextField();
    private JLabel selectFile = new JLabel();
    private JButton testSound = new JButton("Play the sound");
    private PanelData panelData;
    private JComboBox<Condition> replacing = new JComboBox<>(new DefaultComboBoxModel<>(Condition.values()));
    private JComboBox<SoundOption> playing = new JComboBox<>(new DefaultComboBoxModel<>(SoundOption.values()));
    private ImageIcon weaponIcon = new ImageIcon(
            ImageUtil.loadImageResource(AttackSoundNotificationsPlugin.class, "/icons/panelIcon.png"));
    /////////////////
    private AttackSoundNotificationsPanel parent;
    private JPanel entryPanel;
    // Not my stuff - From the ScreenMarkerPluginPanel //
    private final FlatTextField nameInput = new FlatTextField();
    private final JLabel rename = new JLabel("Rename");
    private final JLabel save = new JLabel("Save");
    private final JLabel cancel = new JLabel("Cancel");
    private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
            BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));
    //////////////////
    // Icons //
    public static final ImageIcon AUDIBLE_ICON;
    public static final ImageIcon AUDIBLE_HOVER_ICON;
    public static final ImageIcon INAUDIBLE_ICON;
    public static final ImageIcon INAUDIBLE_HOVER_ICON;
    public static final ImageIcon REMOVE_ICON;
    public static final ImageIcon REMOVE_HOVER_ICON;
    public static final ImageIcon OPEN_ICON;
    public static final ImageIcon OPEN_HOVER_ICON;
    static {
        final BufferedImage audibleImg = ImageUtil.loadImageResource(AttackSoundNotificationsPlugin.class,
                "/icons/on.png");
        AUDIBLE_ICON = new ImageIcon(audibleImg);
        AUDIBLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(audibleImg, -100));

        final BufferedImage inaudibleImg = ImageUtil.loadImageResource(AttackSoundNotificationsPlugin.class,
                "/icons/off.png");
        INAUDIBLE_ICON = new ImageIcon(inaudibleImg);
        INAUDIBLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(inaudibleImg, -100));

        final BufferedImage removeImg = ImageUtil.loadImageResource(AttackSoundNotificationsPlugin.class,
                "/icons/delete.png");
        REMOVE_ICON = new ImageIcon(removeImg);
        REMOVE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(removeImg, -100));
        
        final BufferedImage openImg = ImageUtil.loadImageResource(AttackSoundNotificationsPlugin.class,
                "/icons/open_icon.png");
        OPEN_ICON = new ImageIcon(openImg);
        
        final BufferedImage openBrightImage = ImageUtil.loadImageResource(AttackSoundNotificationsPlugin.class,
                "/icons/open_bright_icon.png");
        OPEN_HOVER_ICON = new ImageIcon(openBrightImage);
    }
    ///////////

    public NewEntryPanel(AttackSoundNotificationsPanel parent, ChatboxItemSearch searchProvider,
            ChatboxPanelManager chatboxPanelManager, SpriteManager spriteManager, ItemManager itemManager,
            JPanel entryPanel) {
        this.setName("Custom Sound " + (entryPanel.getComponentCount() + 1));
        JPanel chooserPanel = new JPanel(new BorderLayout());
        JPanel removePanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel iconPanel = new JPanel(new BorderLayout());
        JPanel textEntry = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new BorderLayout());
        this.parent = parent;
        this.entryPanel = entryPanel;

        customSoundTextField.setFocusable(false);
        
        replacing.setFocusable(false);
        replacing.setToolTipText("When to play the sound");

        playing.setFocusable(false);
        playing.setToolTipText("What sound to play");

        testSound.setFocusable(false);
        testSound.setToolTipText("Play the selected sound, if it exists");
        AtomicInteger weaponId = new AtomicInteger(-1);

        JPanel nameWrapper = new JPanel(new BorderLayout());
        nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        nameWrapper.setBorder(NAME_BOTTOM_BORDER);

        JPanel nameActions = new JPanel(new BorderLayout(3, 0));
        nameActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JPanel resetOptions = new JPanel(new BorderLayout());
        resetOptions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel reset = new JLabel("Reset");
        reset.setFont(FontManager.getRunescapeSmallFont());
        reset.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
        reset.setHorizontalAlignment(SwingConstants.CENTER);
        reset.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                int confirm = -1;
                boolean shiftHeld = mouseEvent.isShiftDown();
                if (!shiftHeld) {
                    confirm = JOptionPane.showConfirmDialog(NewEntryPanel.this,
                            "Reset this sound swap?",
                            "Confirm", JOptionPane.YES_NO_OPTION);
                } else
                    confirm = 0;
                if (confirm == 0) {
                    NewEntryPanel.this.setName("Custom Sound " + entryPanel.getComponentCount());
                    nameInput.setText("Custom Sound " + entryPanel.getComponentCount());
                    audible.setIcon(AUDIBLE_ICON);
                    weaponIconLabel.setIcon(weaponIcon);
                    weaponId.set(-1);
                    replacing.setSelectedIndex(0);
                    playing.setSelectedIndex(0);
                    textEntry.setVisible(false);
                }
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                reset.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                reset.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
            }
        });

        save.setVisible(false);
        save.setFont(FontManager.getRunescapeSmallFont());
        save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
        save.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                save();
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
            }
        });

        cancel.setVisible(false);
        cancel.setFont(FontManager.getRunescapeSmallFont());
        cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
        cancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                cancel();
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
            }
        });

        rename.setFont(FontManager.getRunescapeSmallFont());
        rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
        rename.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                nameInput.setEditable(true);
                updateNameActions(true);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
            }
        });

        nameActions.add(save, BorderLayout.EAST);
        nameActions.add(cancel, BorderLayout.WEST);
        nameActions.add(rename, BorderLayout.CENTER);

        nameInput.setText(this.getName());
        nameInput.setBorder(null);
        nameInput.setEditable(false);
        nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        nameInput.getTextField().setForeground(Color.WHITE);
        nameInput.getTextField().setHorizontalAlignment(JTextField.CENTER);
        nameInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    save();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancel();
                }
            }
        });

        nameWrapper.add(nameInput, BorderLayout.CENTER);
        nameWrapper.setPreferredSize(new Dimension(200, 20));

        resetOptions.add(reset, BorderLayout.CENTER);

        removeEntry.setIcon(new ImageIcon(getClass().getResource("/icons/delete.png")));
        removeEntry.setToolTipText("Remove this sound");

        removeEntry.setBorder(new EmptyBorder(2, 2, 2, 2));

        removePanel.add(removeEntry, BorderLayout.EAST);
        removePanel.add(nameActions, BorderLayout.WEST);
        removePanel.add(resetOptions, BorderLayout.CENTER);
        removePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        weaponIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weaponIconLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        weaponIconLabel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
        chooserPanel.add(replacing, BorderLayout.EAST);
        chooserPanel.add(weaponIconLabel, BorderLayout.WEST);
        chooserPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        topPanel.add(removePanel, BorderLayout.NORTH);
        topPanel.add(chooserPanel, BorderLayout.CENTER);
        topPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        topPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        audible.setHorizontalAlignment(SwingConstants.CENTER);
        audible.setToolTipText("Enable/Disable");
        iconPanel.add(audible, BorderLayout.WEST);
        iconPanel.add(playing, BorderLayout.EAST);
        iconPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        iconPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        selectFile.setIcon(OPEN_ICON);
        selectFile.setVisible(false);
        selectFile.setToolTipText("Find the file in on your computer");

        textEntry.setVisible(false);
        textEntry.add(new JLabel("Enter your custom sound here:"), BorderLayout.NORTH);
        textEntry.add(customSoundTextField, BorderLayout.CENTER);
        textEntry.add(selectFile, BorderLayout.EAST);
        textEntry.setBorder(new EmptyBorder(5, 8, 5, 8));
        textEntry.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        buttons.add(textEntry, BorderLayout.NORTH);
        buttons.add(testSound, BorderLayout.SOUTH);
        buttons.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(topPanel, BorderLayout.NORTH);
        bottomContainer.add(iconPanel, BorderLayout.CENTER);
        bottomContainer.add(buttons, BorderLayout.SOUTH);
        bottomContainer.setBorder(new EmptyBorder(8, 0, 8, 0));
        bottomContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setFocusable(true);
        mainPanel.add(nameWrapper, BorderLayout.NORTH);
        mainPanel.add(bottomContainer, BorderLayout.SOUTH);
        Border emptyBorder = new EmptyBorder(0, 0, 0, 0);
        MatteBorder topRightBorder = new MatteBorder(3, 0, 0, 3, ColorScheme.DARKER_GRAY_COLOR.brighter());
        MatteBorder bottomLeftBorder = new MatteBorder(0, 3, 3, 0, ColorScheme.DARKER_GRAY_COLOR.darker());
        CompoundBorder firstBorder = new CompoundBorder(topRightBorder, emptyBorder);
        CompoundBorder semiFinalBorder = new CompoundBorder(bottomLeftBorder, firstBorder);
        CompoundBorder finalBorder = new CompoundBorder(new EmptyBorder(1, 0, 1, 0), semiFinalBorder);

        mainPanel.setBorder(finalBorder);
        playing.setPreferredSize(new Dimension(170, 32));
        replacing.setPreferredSize(playing.getPreferredSize());

        audible.setIcon(AUDIBLE_ICON);
        audible.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Set default weapon icon //
        weaponIconLabel.setIcon(weaponIcon);
        weaponIconLabel.setToolTipText("Choose the weapon to play the sound for");
        // ACTIONS //
        weaponIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        weaponIconLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                searchProvider
                        .tooltipText("Select your weapon")
                        .onItemSelected((itemId) -> {
                            if (itemId == null) {
                                log.warn("Failed to get itemId");
                            } else {
                                weaponId.set(itemId);
                                log.debug("Found itemId: " + Integer.toString(weaponId.get()));
                                BufferedImage weaponSprite = itemManager.getImage(weaponId.get(), 0, false);
                                ImageIcon newWeaponIcon = new ImageIcon(weaponSprite);
                                weaponIconLabel.setIcon(newWeaponIcon);
                            }
                        })
                        .build();
                chatboxPanelManager.openInput(searchProvider);
            }
        });

        audible.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (audible.getIcon() == AUDIBLE_HOVER_ICON) {
                    audible.setIcon(INAUDIBLE_HOVER_ICON);
                } else {
                    audible.setIcon(AUDIBLE_HOVER_ICON);
                }
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                if (audible.getIcon() == AUDIBLE_ICON) {
                    audible.setIcon(AUDIBLE_HOVER_ICON);
                } else {
                    audible.setIcon(INAUDIBLE_HOVER_ICON);
                }
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                if (audible.getIcon() == AUDIBLE_HOVER_ICON) {
                    audible.setIcon(AUDIBLE_ICON);
                } else {
                    audible.setIcon(INAUDIBLE_ICON);
                }
            }
        });

        removeEntry.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                boolean shiftHeld = mouseEvent.isShiftDown();
                if (!shiftHeld) {
                    int confirm = JOptionPane.showConfirmDialog(NewEntryPanel.this,
                            "Delete this sound swap?",
                            "Confirm", JOptionPane.YES_NO_OPTION);
                    if (confirm != 0) {
                        return; // If user doesn't confirm, we do nothing more
                    }
                }
                removeSelf();
                entryPanel.remove(mainPanel);
                parent.reloadPanels();

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                removeEntry.setIcon(REMOVE_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                removeEntry.setIcon(REMOVE_ICON);
            }
        });

        panelData = new PanelData(weaponId, audible, customSoundTextField, playing, replacing, name);
        mainPanel.setName(Integer.toString(panelData.hashCode())); // Set name to the hashCode of weaponEntry for
                                                                   // identification

        playing.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Default sound option selected
                if (playing.getSelectedItem().equals(SoundOption.CUSTOM_SOUND)) {
                    textEntry.setVisible(true);
                    selectFile.setVisible(true);
                } else {
                    textEntry.setVisible(false);
                    selectFile.setVisible(false);
                }
            }
        });

        selectFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		selectFile.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // start at application current directory
                fileChooser.setFileFilter(new FileNameExtensionFilter(".wav sound files", "wav"));
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION){
                    String filePath = fileChooser.getSelectedFile().getPath();
                    if(filePath.endsWith(".wav")){
                        customSoundTextField.setText(filePath);
                    } else {
                        JOptionPane.showMessageDialog(NewEntryPanel.this,
                            "Acceptable file types: .wav",
                            "Bad file", JOptionPane.ERROR_MESSAGE);
                    }
                }
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				selectFile.setIcon(OPEN_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				selectFile.setIcon(OPEN_ICON);
			}
		});

        testSound.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelData.getSoundChoice() == SoundOption.CUSTOM_SOUND)
                    parent.findCustomSound(panelData.getSoundFilePath());
                else
                    parent.playDefaultSound(panelData.getSoundChoice());
            }
        });

        log.debug("Created entry panel " + mainPanel.getName());
    }

    // ALSO NOT MINE //
    private void save() {
        // plugin.updateConfig();
        nameInput.setEditable(false);
        updateNameActions(false);
    }

    private void cancel() {
        nameInput.setEditable(false);
        nameInput.setText(this.getName());
        updateNameActions(false);
    }

    private void updateNameActions(boolean saveAndCancel) {
        save.setVisible(saveAndCancel);
        cancel.setVisible(saveAndCancel);
        rename.setVisible(!saveAndCancel);

        if (saveAndCancel) {
            nameInput.getTextField().requestFocusInWindow();
            nameInput.getTextField().selectAll();
        }
    }

    /////////////////////////
    public JPanel getEntryPanel() {
        return mainPanel;
    }

    public PanelData getPanelData() {
        return panelData;
    }

    public void removeSelf() {
        parent.removeNewEntryPanel(this);
    }
}