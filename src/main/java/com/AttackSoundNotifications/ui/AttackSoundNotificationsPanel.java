// License from ScreenMarkerPluginPanel
/*
 * Copyright (c) 2018, Kamiel, <https://github.com/Kamielvf>
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
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

import com.google.gson.reflect.TypeToken;
import com.AttackSoundNotifications.AttackSoundNotificationsPlugin;
import static com.AttackSoundNotifications.AttackSoundNotificationsPlugin.CONFIG_GROUP;
import static com.AttackSoundNotifications.AttackSoundNotificationsPlugin.PANEL_PREFIX;
import net.runelite.api.ChatMessageType;
import net.runelite.api.HitsplatID;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.inject.Inject;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AttackSoundNotificationsPanel extends PluginPanel
{
	private InputStream returnSound;
	private List<EntryPanel> entryPanelList = new ArrayList<>();

	// Default Sound Files //
	public static final String DEFAULT_MAX_FILE = "/audio/default_max.wav";
	public static final String DEFAULT_MISS_FILE = "/audio/default_miss.wav";
	public static final String DEFAULT_SPEC_MISS_FILE = "/audio/default_spec_miss.wav";
	public static final String DEFAULT_SPEC_HIT_FILE = "/audio/default_spec_hit.wav";
	public static final String DEFAULT_SPEC_MAX_FILE = "/audio/default_spec_max.wav";
	public static final String FILE_NOT_FOUND = "/audio/file_not_found.wav";
	////////////////////////

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
	////////////////////////

	private final AttackSoundNotificationsPlugin plugin;
	private Boolean startup = false;

	public enum SoundOption
	{
		SPECIAL_HIT("Default Special Hit Sound"), SPECIAL_MISS("Default Special Miss Sound"), SPECIAL_MAX("Default Special Max Sound"), MAX("Default Max Sound"), MISS("Default Miss Sound"), CUSTOM_SOUND("Custom sound"),
		;

		private String displayValue;

		SoundOption(String displayValue)
		{
			this.displayValue = displayValue;
		}

		public String getDisplayValue()
		{
			return displayValue;
		}

		// This method can be used to get the enum from the string representation
		public static SoundOption fromString(String displayValue)
		{
			for (SoundOption option : SoundOption.values())
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

	public enum Condition
	{
		SPECIAL_HIT("Special Attack Hit"), SPECIAL_MISS("Special Attack Miss"), SPECIAL_MAX("Special Attack Max"), MAX("Non-Special Max"), MISS("Non-Special Miss");

		private String displayValue;

		Condition(String displayValue)
		{
			this.displayValue = displayValue;
		}

		public String getDisplayValue()
		{
			return displayValue;
		}

		// This method can be used to get the enum from the string representation
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
		add(entryPanel, BorderLayout.CENTER);

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
		this.revalidate();
		this.repaint();
	}

	private void addNewEntry()
	{
		GridBagConstraints entryConstraints = new GridBagConstraints();
		entryConstraints.gridx = 0;
		entryConstraints.gridy = entryPanel.getComponentCount() + 1;
		entryConstraints.weightx = 1;
		entryConstraints.fill = GridBagConstraints.NONE;
		entryConstraints.anchor = GridBagConstraints.NORTH;

		EntryPanel newEntryPanel = new EntryPanel(this, plugin);

		entryPanelList.add(newEntryPanel);

		entryPanel.add(newEntryPanel.getMainPanel(), entryConstraints);

		reloadPanels();
	}

	private void loadEntryPanels(JPanel parentPanel)
	{
		startup = true;
		List<EntryPanelState> panelStates = plugin.gson.fromJson(plugin.configManager.getConfiguration(CONFIG_GROUP, PANEL_PREFIX), new TypeToken<List<EntryPanelState>>()
		{
		}.getType());
		if (panelStates != null)
		{
			for (EntryPanelState panelState : panelStates)
			{
				if (panelState.getPanelName() != null)
				{
					log.debug("Found panel " + panelState.getPanelName());
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
					if (panelState.getPlaying() != null)
					{
						SoundOption playing = SoundOption.fromString(panelState.getPlaying());
						newPanel.setPlaying(playing);
						if (playing == SoundOption.CUSTOM_SOUND)
						{
							newPanel.makeSoundInputVisible();
						}
						newPanel.setCustomSoundPath(panelState.getCustomSoundPath());
					}
					entryPanelList.add(newPanel);
					GridBagConstraints entryConstraints = new GridBagConstraints();
					entryConstraints.gridx = 0;
					entryConstraints.gridy = parentPanel.getComponentCount() + 1;
					entryConstraints.weightx = 1;
					entryConstraints.fill = GridBagConstraints.NONE;
					entryConstraints.anchor = GridBagConstraints.NORTH;
					parentPanel.add(newPanel.getMainPanel(), entryConstraints);
				}

			}
			startup = false;
		}
		else
		{
			log.debug("Found no panels");
		}
	}

	public void save()
	{
		if (!startup)
		{
			log.debug("Saving");
			List<EntryPanelState> panelStates = entryPanelList.stream().map(EntryPanelState::new).collect(Collectors.toList());
			plugin.configManager.setConfiguration(CONFIG_GROUP, PANEL_PREFIX, plugin.gson.toJson(panelStates));
			log.debug("Wrote " + plugin.gson.toJson(panelStates) + "to " + CONFIG_GROUP + " " + PANEL_PREFIX);
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
		log.debug("Total panels: " + entryPanelList.size());
		for (EntryPanel panel : entryPanelList)
		{
			// If it's the weaponID
			if ((weaponId == panel.getWeaponId() && panel.getAudible()) || panel.getWeaponId() == -1)
			{
				log.debug("Found weapon in panels");
				log.debug("Skipping panel...");
				log.debug("name :" + panel.getName());
				log.debug("weaponId :" + panel.getWeaponId());
				log.debug("audible  :" + panel.getAudible());
				log.debug("soundPath:" + panel.getCustomSoundPath());
				log.debug("playing  :" + panel.getPlaying());
				log.debug("replacing:" + panel.getReplacing());
				// Only special attacks
				switch (panel.getReplacing())
				{
					// Spec Missed
					case MISS:
					{
						if (!usedSpecialAttack)
						{
							if (hitType == HitsplatID.BLOCK_ME)
							{
								if (panel.getPlaying() == SoundOption.CUSTOM_SOUND)
								{
									returnSound = loadCustomSound(panel.getCustomSoundPath());
									if (returnSound != null)
									{
										log.debug("Found custom sound");
									}
									else
									{
										plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Couldn't find custom sound file:" + panel.getCustomSoundPath(), null);
										if (plugin.config.cantFind())
										{
											returnSound = loadDefaultSound(DEFAULT_MISS_FILE);
										}
										else
										{
											returnSound = null;
										}
									}
								}
								else
								{
									returnSound = getDefaultSoundChoice(panel.getPlaying());
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
							log.debug("Max is being replaced");
							if (hitType == HitsplatID.DAMAGE_MAX_ME)
							{
								if (panel.getPlaying() == SoundOption.CUSTOM_SOUND)
								{
									returnSound = loadCustomSound(panel.getCustomSoundPath());
									if (returnSound != null)
									{
										log.debug("Found custom sound");
									}
									else
									{
										plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Couldn't find custom sound file:" + panel.getCustomSoundPath(), null);
										if (plugin.config.cantFind())
										{
											returnSound = loadDefaultSound(DEFAULT_SPEC_MAX_FILE);
										}
										else
										{
											returnSound = null;
										}
									}
								}
								else
								{
									returnSound = getDefaultSoundChoice(panel.getPlaying());
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
								if (panel.getPlaying() == SoundOption.CUSTOM_SOUND)
								{
									returnSound = loadCustomSound(panel.getCustomSoundPath());
									if (returnSound != null)
									{
										log.debug("Found custom sound");
									}
									else
									{
										plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Couldn't find custom sound file:" + panel.getCustomSoundPath(), null);
										if (plugin.config.cantFind())
										{
											returnSound = loadDefaultSound(DEFAULT_SPEC_MISS_FILE);
										}
										else
										{
											returnSound = null;
										}
									}
								}
								else
								{
									returnSound = getDefaultSoundChoice(panel.getPlaying());
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
								if (panel.getPlaying() == SoundOption.CUSTOM_SOUND)
								{
									returnSound = loadCustomSound(panel.getCustomSoundPath());
									if (returnSound != null)
									{
										log.debug("Found custom sound");
									}
									else
									{
										plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Couldn't find custom sound file:" + panel.getCustomSoundPath(), null);
										if (plugin.config.cantFind())
										{
											returnSound = loadDefaultSound(DEFAULT_SPEC_HIT_FILE);
										}
										else
										{
											returnSound = null;
										}
									}
								}
								else
								{
									returnSound = getDefaultSoundChoice(panel.getPlaying());
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
								if (panel.getPlaying() == SoundOption.CUSTOM_SOUND)
								{
									returnSound = loadCustomSound(panel.getCustomSoundPath());
									if (returnSound != null)
									{
										log.debug("Found custom sound");
									}
									else
									{
										plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Couldn't find custom sound file:" + panel.getCustomSoundPath(), null);
										if (plugin.config.cantFind())
										{
											returnSound = loadDefaultSound(DEFAULT_SPEC_MAX_FILE);
										}
										else
										{
											returnSound = null;
										}
									}
								}
								else
								{
									returnSound = getDefaultSoundChoice(panel.getPlaying());
								}
								return returnSound;
							}
						}
					}
				}
			}
			else
			{
				log.debug("Skipping panel...");
				log.debug("name :" + panel.getName());
				log.debug("weaponId :" + panel.getWeaponId());
				log.debug("audible  :" + panel.getAudible());
				log.debug("soundPath:" + panel.getCustomSoundPath());
				log.debug("playing  :" + panel.getPlaying());
				log.debug("replacing:" + panel.getReplacing());
			}
		}
		return null;
	}

	public InputStream getDefaultSoundChoice(SoundOption choice)
	{
		switch (choice)
		{
			case SPECIAL_HIT:
			{
				return loadDefaultSound(DEFAULT_SPEC_HIT_FILE);
			}
			case SPECIAL_MAX:
			{
				return loadDefaultSound(DEFAULT_SPEC_MAX_FILE);
			}
			case SPECIAL_MISS:
			{
				return loadDefaultSound(DEFAULT_SPEC_MISS_FILE);
			}
			case MISS:
			{
				return loadDefaultSound(DEFAULT_MISS_FILE);
			}
			case MAX:
			{
				return loadDefaultSound(DEFAULT_MAX_FILE);
			}
			case CUSTOM_SOUND:
			{
				log.warn("How did you even get here? This shouldn't happen");
				return loadDefaultSound(FILE_NOT_FOUND);
			}
		}
		return null;
	}

	public InputStream loadDefaultSound(String filePath)
	{
		return AttackSoundNotificationsPlugin.class.getResourceAsStream(filePath);
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
			plugin.playCustomSound(loadDefaultSound(FILE_NOT_FOUND));
		}
	}

	public void playDefaultSound(InputStream sound)
	{
		plugin.playCustomSound(sound);
	}
}
