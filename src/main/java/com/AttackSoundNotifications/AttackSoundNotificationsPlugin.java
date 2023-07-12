/* Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2022, Ferrariic <ferrariictweet@gmail.com>
 * Copyright (c) 2018, Raqes <j.raqes@gmail.com>
 * Copyright (c) 2019, Ron Young <https://github.com/raiyni>
 * Copyright (c) 2023, Jacob Browder <https://github.com/DominickCobb-rs>
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
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

package com.AttackSoundNotifications;

import com.AttackSoundNotifications.ui.AttackSoundNotificationsPanel;
import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Hitsplat;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(name = "Attack Sound Notifications", description = "A plugin that plays sounds based on hitsplats and special attacks", tags = {
	"special", "sounds", "notifications"}, loadWhenOutdated = true, enabledByDefault = false)
public class AttackSoundNotificationsPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "attacknotifications";
	public static final String PANEL_PREFIX = "attackNotificationsPanel_";
	@Inject
	public Client client;

	@Inject
	public AttackSoundNotificationsConfig config;

	@Inject
	public ClientThread clientThread;

	@Inject
	public ClientToolbar clientToolbar;

	@Inject
	public ChatboxItemSearch searchProvider;

	@Inject
	public ChatboxPanelManager chatboxPanelManager;

	@Inject
	public SpriteManager spriteManager;

	@Inject
	public ItemManager itemManager;

	@Inject
	public ConfigManager configManager;

	@Inject
	public Gson gson;

	private AttackSoundNotificationsPanel pluginPanel;
	private Clip clip = null;
	private NavigationButton navButton;
	private int specialPercentage;
	private int specialWeapon;
	private boolean specced = false;
	private InputStream soundToPlay;

	@Provides
	AttackSoundNotificationsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AttackSoundNotificationsConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		pluginPanel = new AttackSoundNotificationsPanel(this);
		BufferedImage icon = ImageIO.read(getClass().getResourceAsStream("/icons/panelIcon.png"));
		navButton = NavigationButton.builder()
			.tooltip("Attack Sounds")
			.icon(icon)
			.priority(5)
			.panel(pluginPanel)
			.build();
		clientToolbar.addNavigation(navButton);
		log.info("Attack Sounds Notifier started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		pluginPanel.save();
		clientToolbar.removeNavigation(navButton);
		log.info("Attack Sounds Notifier stopped!");
	}

	// From the SpecialAttackCounter plugin
	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() != VarPlayer.SPECIAL_ATTACK_PERCENT)
		{
			return;
		}

		int specialPercentage = event.getValue();
		if (this.specialPercentage == -1 || specialPercentage >= this.specialPercentage)
		{
			this.specialPercentage = specialPercentage;
			return;
		}

		this.specialPercentage = specialPercentage;

		// We don't need most of this stuff, but we do need it for some reason >:(

		// This event runs prior to player and npc updating, making getInteracting() too
		// early to call..
		// defer this with invokeLater(), but note that this will run after incrementing
		// the server tick counter
		// so we capture the current server tick counter here for use in computing the
		// final hitsplat tick
		clientThread.invokeLater(() -> {
			if (specialWeapon == -1)
			{
				ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
				if (equipment != null)
				{
					Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
					if (weapon != null)
					{
						specialWeapon = weapon.getId();
					}
				}
			}
			specced = true;
		});
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		Actor target = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		if (hitsplat.isMine() && target != client.getLocalPlayer())
		{
			// Adapted from the SpecialAttackSounds plugin
			// Could already be assigned from the spec varbit change so we check
			// if it's already been assigned a value
			if (specialWeapon == -1)
			{
				ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
				if (equipment != null)
				{
					Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
					if (weapon != null)
					{
						specialWeapon = weapon.getId();
					}
				}
			}
			soundToPlay = pluginPanel.fetchSound(hitsplat.getHitsplatType(), specialWeapon, specced);
			if (soundToPlay != null)
			{
				playCustomSound(soundToPlay);
				soundToPlay = null;
			}
			specialWeapon = -1;
			specced = false;
		}
	}

	// The following two functions have been adapted from the Hit-Sounds plugin
	public synchronized boolean playCustomSound(InputStream streamName)
	{
		if (clip != null)
		{
			clip.stop();
			clip.flush();
			clip.close();
			clip = null;
		}
		if (streamName != null)
		{
			try
			{
				clip = AudioSystem.getClip();
			}
			catch (LineUnavailableException e)
			{
				log.warn("Unable to play sound", e);
				return false;
			}
			if (!tryLoadCustomSoundFile(streamName))
			{
				return false;
			}
		}

		FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float gain = 20f * (float) Math.log10(config.Volume() / 100f);
		gain = Math.min(gain, volume.getMaximum());
		gain = Math.max(gain, volume.getMinimum());
		volume.setValue(gain);

		clip.start();
		return true;
	}

	private boolean tryLoadCustomSoundFile(InputStream streamName)
	{
		try (AudioInputStream sound = AudioSystem.getAudioInputStream(streamName))
		{
			clip.open(sound);
			return true;
		}
		catch (UnsupportedAudioFileException | IOException | LineUnavailableException e)
		{
			log.warn("Unable to load sound", e);
		}
		return false;
	}
}
