// License from ScreenMarkerPluginPanel
/*
 * Copyright (c) 2018, Kamiel, <https://github.com/Kamielvf>
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * Copyright (c) 2023, DominickCobb-rs <https://github.com/DominickCobb-rs>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

import com.AttackSoundNotifications.AttackSoundNotificationsPlugin;
import static com.AttackSoundNotifications.AttackSoundNotificationsPlugin.CONFIG_GROUP;
import static com.AttackSoundNotifications.AttackSoundNotificationsPlugin.PANEL_PREFIX;
import com.google.gson.reflect.TypeToken;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.api.ChatMessageType;
import net.runelite.api.HitsplatID;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

public class AttackSoundNotificationsPanel extends PluginPanel
{
	private InputStream returnSound;
	private final List<EntryPanel> entryPanelList = new ArrayList<>();

	// Panel Construction //
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;
	private final JLabel addSound = new JLabel(ADD_ICON);
	private final JLabel title = new JLabel();
	public final JPanel entryPanel = new JPanel(new GridBagLayout());

	static
	{
		final BufferedImage addIcon = ImageUtil.loadImageResource(AttackSoundNotificationsPlugin.class, "/icons/add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
	}

	private final AttackSoundNotificationsPlugin plugin;
	private Boolean startup = false;

	public enum Condition
	{
		SPECIAL_HIT("Special Attack Hit"), SPECIAL_MISS("Special Attack Miss"), SPECIAL_MAX("Special Attack Max"), MAX("Non-Special Max"), MISS("Non-Special Miss");

		private final String displayValue;

		Condition(String displayValue)
		{
			this.displayValue = displayValue;
		}

		public String getDisplayValue()
		{
			return displayValue;
		}

		public static Condition fromString(String displayValue)
		{
			for (Condition option : Condition.values())
			{
				if (option.displayValue.equalsIgnoreCase(displayValue))
				{
					return option;
				}
			}
			return null;
		}

		@Override
		public String toString()
		{
			return this.displayValue;
		}
	}

	// Panels >:(
	@Inject
	public AttackSoundNotificationsPanel(AttackSoundNotificationsPlugin plugin)
	{
		this.plugin = plugin;
		setLayout(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBorder(new EmptyBorder(11, 10, 10, 10));

		title.setText("Attack Sound Notifications");
		title.setForeground(Color.WHITE);

		northPanel.add(title, BorderLayout.WEST);
		northPanel.add(addSound, BorderLayout.EAST);
		
		add(northPanel, BorderLayout.NORTH);
		add(entryPanel, BorderLayout.SOUTH);

		addSound.setToolTipText("Add new sound");
		addSound.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addSound.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				addNewEntry();
				save();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				addSound.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				addSound.setIcon(ADD_ICON);
			}
		});
		loadEntryPanels(entryPanel);
	}

	public void reloadPanels()
	{
		plugin.clientThread.invokeLater(() -> {
			this.revalidate();
			this.repaint();
		});
	}

	private void addNewEntry()
	{
		GridBagConstraints entryConstraints = new GridBagConstraints();
		entryConstraints.gridx = 0;
		entryConstraints.weightx = 1;
		entryConstraints.fill = GridBagConstraints.HORIZONTAL;
		entryConstraints.anchor = GridBagConstraints.NORTH;

		EntryPanel newEntryPanel = new EntryPanel(this, plugin);

		entryPanelList.add(newEntryPanel);

		entryPanel.add(newEntryPanel.getMainPanel(), entryConstraints);

		reloadPanels();
	}

	private void loadEntryPanels(JPanel parentPanel)
	{
		startup = true;
		List<EntryPanelState> panelStates = plugin.gson.fromJson(
			plugin.configManager.getConfiguration(CONFIG_GROUP, PANEL_PREFIX
			), new TypeToken<List<EntryPanelState>>()
			{
			}.getType());
		if (panelStates != null)
		{
			for (EntryPanelState panelState : panelStates)
			{
				if (panelState.getPanelName() != null)
				{
					EntryPanel newPanel = new EntryPanel(this, plugin);
					newPanel.setPanelName(panelState.getPanelName());
					if (panelState.getWeaponId() != null)
					{
						newPanel.setWeaponId(panelState.getWeaponId());
					}
					if (panelState.getAudible() != null)
					{
						newPanel.setAudible(panelState.getAudible());
					}
					if (panelState.getReplacing() != null)
					{
						Condition replacing = Condition.fromString(panelState.getReplacing());
						newPanel.setReplacing(replacing);
					}
					if (panelState.getCustomSoundPath() != null)
					{
						newPanel.setCustomSoundPath(panelState.getCustomSoundPath());
					}
					entryPanelList.add(newPanel);
					GridBagConstraints entryConstraints = new GridBagConstraints();
					entryConstraints.gridx = 0;
					entryConstraints.weightx = 1;
					entryConstraints.fill = GridBagConstraints.HORIZONTAL;
					entryConstraints.anchor = GridBagConstraints.NORTHWEST;
					parentPanel.add(newPanel.getMainPanel(), entryConstraints);
				}

			}
			startup = false;
		}
	}

	public void save()
	{
		if (!startup)
		{
			List<EntryPanelState> panelStates = entryPanelList.stream().map(EntryPanelState::new).collect(Collectors.toList());
			plugin.configManager.setConfiguration(CONFIG_GROUP, PANEL_PREFIX, plugin.gson.toJson(panelStates));
		}

	}

	public void removeEntryPanel(EntryPanel panel)
	{
		entryPanelList.remove(panel);
		save();
	}

	// Sounds! //
	public InputStream fetchSound(Integer hitType, Integer weaponId, Boolean usedSpecialAttack)
	{
		for (EntryPanel panel : entryPanelList)
		{
			if ((weaponId == panel.getWeaponId() || panel.getWeaponId() == -1) && panel.getAudible() && !panel.getCustomSoundPath().isEmpty())
			{
				switch (panel.getReplacing())
				{
					case MISS:
					{
						if (!usedSpecialAttack)
						{
							if (hitType == HitsplatID.BLOCK_ME)
							{
								returnSound = loadCustomSound(panel.getCustomSoundPath());
								if (returnSound == null)
								{
									plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Couldn't find custom sound file:" + panel.getCustomSoundPath(), null);
								}
								return returnSound;
							}
						}
					}
					break;
					case MAX:
					{
						if (!usedSpecialAttack)
						{
							if (hitType == HitsplatID.DAMAGE_MAX_ME)
							{
								returnSound = loadCustomSound(panel.getCustomSoundPath());
								if (returnSound == null)
								{
									plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Couldn't find custom sound file:" + panel.getCustomSoundPath(), null);
								}
								return returnSound;
							}
						}
					}
					break;
					case SPECIAL_MISS:
					{
						if (usedSpecialAttack)
						{
							if (hitType == HitsplatID.BLOCK_ME)
							{
								returnSound = loadCustomSound(panel.getCustomSoundPath());
								if (returnSound == null)
								{
									plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Couldn't find custom sound file:" + panel.getCustomSoundPath(), null);
								}
								return returnSound;
							}
						}
					}
					break;
					case SPECIAL_HIT:
					{
						if (usedSpecialAttack)
						{
							if (hitType == HitsplatID.DAMAGE_ME)
							{
								returnSound = loadCustomSound(panel.getCustomSoundPath());
								if (returnSound == null)
								{
									plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Couldn't find custom sound file:" + panel.getCustomSoundPath(), null);
								}
								return returnSound;
							}
						}
					}
					break;
					case SPECIAL_MAX:
					{
						if (usedSpecialAttack)
						{
							if (hitType == HitsplatID.DAMAGE_MAX_ME)
							{
								returnSound = loadCustomSound(panel.getCustomSoundPath());
								if (returnSound == null)
								{
									plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Couldn't find custom sound file:" + panel.getCustomSoundPath(), null);
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

	private BufferedInputStream loadCustomSound(String fileName)
	{
		try
		{
			return new BufferedInputStream(new FileInputStream(fileName));
		}
		catch (FileNotFoundException e)
		{
			return null;
		}
	}

	public void findCustomSound(String filePath)
	{
		InputStream soundStream = loadCustomSound(filePath);
		if (soundStream != null)
		{
			plugin.playCustomSound(soundStream);
		}
		else
		{
            // Plays the system default system oops noise
			java.awt.Toolkit.getDefaultToolkit().beep();
		}
	}

	public void playDefaultSound(InputStream sound)
	{
		plugin.playCustomSound(sound);
	}
}
