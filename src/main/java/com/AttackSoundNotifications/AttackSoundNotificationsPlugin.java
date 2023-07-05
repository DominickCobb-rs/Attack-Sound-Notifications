//.... something
/*
 * Copyright (c) 2019, Ron Young <https://github.com/raiyni>
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

// SpecialAttackCounter
/*
 * Copyright (c) 2018, Raqes <j.raqes@gmail.com>
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

// Hit-Sounds
 /*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2022, Ferrariic <ferrariictweet@gmail.com>
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

package com.AttackSoundNotifications;

import com.AttackSoundNotifications.ui.AttackSoundNotificationsPanel;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


import com.google.inject.Provides;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.Hitsplat;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.game.chatbox.ChatboxPanelManager;

import javax.sound.sampled.*;

import java.awt.image.BufferedImage;
import java.io.*;

@Slf4j
@PluginDescriptor(name = "Attack Sound Notifications", description = "A plugin that plays sounds based on hitsplats and special attacks", tags = {
		"special", "sounds", "notifications" }, loadWhenOutdated = true, enabledByDefault = true)
public class AttackSoundNotificationsPlugin extends Plugin {
	@Inject
	private Client client;
	@Inject
	private AttackSoundNotificationsConfig config;
	@Inject
	private ClientThread clientThread;
	@Inject
    private ClientToolbar clientToolbar;
	@Inject
    public ChatboxItemSearch searchProvider;
    @Inject 
    public ChatboxPanelManager chatboxPanelManager;
	@Inject
    public SpriteManager spriteManager;
    @Inject
    public ItemManager itemManager;
	private AttackSoundNotificationsPanel pluginPanel;
	private Clip clip = null;
	private NavigationButton navButton;
	private int specialPercentage;
	private int specialWeapon;
	private boolean specced=false;

	// most recent hitsplat and the target it was on
	// This should only ever be the hitsplat the player applies to another creature
	private Hitsplat lastSpecHitsplat;
	private InputStream soundToPlay;

	@Override
	protected void startUp() throws Exception {
		pluginPanel = new AttackSoundNotificationsPanel(this, chatboxPanelManager, searchProvider, spriteManager, itemManager, client, config);
		BufferedImage icon = ImageIO.read(getClass().getResourceAsStream("/icons/panelIcon.png"));//ImageIO.read(AttackSoundNotificationsPlugin.class.getResourceAsStream("/icon.png"));
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
	protected void shutDown() throws Exception {
		clientToolbar.removeNavigation(navButton);
		log.info("Attack Sounds Notifier stopped!");
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (lastSpecHitsplat != null) {
			log.debug("Attack detected");
			int hitType = lastSpecHitsplat.getHitsplatType();
			log.debug("Fetching sound with the following:");
			log.debug(Integer.toString(hitType));
			log.debug(Integer.toString(specialWeapon));
			log.debug(Boolean.toString(specced));
			soundToPlay = pluginPanel.fetchSound(hitType,specialWeapon,specced);
			if (soundToPlay==null) log.debug("No sound fetched");
		}
		if (soundToPlay != null) {
			playCustomSound(soundToPlay);
		}
		specialWeapon = -1;
		lastSpecHitsplat = null;
		specced = false;
		soundToPlay = null;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		if (event.getVarpId() != VarPlayer.SPECIAL_ATTACK_PERCENT) {
			return;
		}

		int specialPercentage = event.getValue();
		if (this.specialPercentage == -1 || specialPercentage >= this.specialPercentage) {
			this.specialPercentage = specialPercentage;
			return;
		}

		this.specialPercentage = specialPercentage;
		
		clientThread.invokeLater(() -> {
			ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
			if (equipment != null) {

				Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
				if (weapon != null) {
					specialWeapon = weapon.getId();
				} else specialWeapon = -1;
			} else specialWeapon = -1;
			specced = true;
			log.debug("Set specced to true");
		});
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
		Actor target = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		if (hitsplat.isMine() && target != client.getLocalPlayer()) {
			if(specialWeapon == -1){
				ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
				if (equipment != null) {

					Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
					if (weapon != null) {
						specialWeapon = weapon.getId();
					} else specialWeapon = -1;
				} else specialWeapon = -1;
			}
			lastSpecHitsplat = hitsplat;
		}
	}

	public synchronized boolean playCustomSound(InputStream streamName) {
		if (clip != null) {
			clip.stop();
			clip.flush();
			clip.close();
			clip = null;
		}
		if (streamName != null) {
			try {
				clip = AudioSystem.getClip();
			} catch (LineUnavailableException e) {
				log.warn("Unable to play sound", e);
				return false;
			}
			if (!tryLoadCustomSoundFile(streamName)) {
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

	private boolean tryLoadCustomSoundFile(InputStream streamName) {
		try (AudioInputStream sound = AudioSystem.getAudioInputStream(streamName)) {
			clip.open(sound);
			return true;
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			log.warn("Unable to load sound", e);
		}
		return false;
	}

	@Provides
	AttackSoundNotificationsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(AttackSoundNotificationsConfig.class);
	}
}
