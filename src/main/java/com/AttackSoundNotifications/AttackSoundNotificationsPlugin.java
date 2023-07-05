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
		"special", "sounds", "notifications" }, loadWhenOutdated = true, enabledByDefault = false)
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
	// // Probably don't need hitsplatTick for our purposes, we're doing dummy work not calculations
	// expected tick the hitsplat will happen on
	//private int hitsplatTick;

	// most recent hitsplat and the target it was on
	// This should only ever be the hitsplat the player applies to another creature
	private Hitsplat lastSpecHitsplat;
	//private NPC lastSpecTarget;
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
		//lastSpecTarget = null;
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
		
		
		// We don't need most of this stuff, but we do need it
		// This event runs prior to player and npc updating, making getInteracting() too
		// early to call..
		// defer this with invokeLater(), but note that this will run after incrementing
		// the server tick counter
		// so we capture the current server tick counter here for use in computing the
		// final hitsplat tick
		//final int serverTicks = client.getTickCount();
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
			//usedSpecialWeapon();

			/*if (specialWeapon == -1) {
				// unrecognized special attack weapon
				return;
			}

			Actor target = client.getLocalPlayer().getInteracting();
			lastSpecTarget = target instanceof NPC ? (NPC) target : null;
			hitsplatTick = serverTicks + getHitDelay(specialWeapon, target);
			*/
			
			/*log.debug("Special attack used - percent: {} weapon: {} server cycle {} hitsplat cycle {}",
					specialPercentage, specialWeapon, serverTicks, hitsplatTick);*/
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
	
	// We PROBABLY don't need this stuff
	/*
	private void usedSpecialWeapon() {
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment != null) {

			Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
			if (weapon != null) {
				specialWeapon = weapon.getId();
			} else specialWeapon = -1;
		} else specialWeapon = -1;
	}
	
	private int getHitDelay(int specialWeapon, Actor target) {
		// DORGESHUUN_CROSSBOW is the only ranged wep we support, so everything else is
		// just melee and delay 1
		if (specialWeapon != ItemID.DORGESHUUN_CROSSBOW || target == null)
			return 1;

		Player player = client.getLocalPlayer();
		if (player == null)
			return 1;

		WorldPoint playerWp = player.getWorldLocation();
		if (playerWp == null)
			return 1;

		WorldArea targetArea = target.getWorldArea();
		if (targetArea == null)
			return 1;

		final int distance = targetArea.distanceTo(playerWp);
		// Dorgeshuun special attack projectile, anim delay, and hitsplat is 60 +
		// distance * 3 with the projectile
		// starting at 41 cycles. Since we are computing the delay when the spec var
		// changes, and not when the
		// projectile first moves, this should be 60 and not 19
		final int cycles = 60 + distance * 3;
		// The server performs no rounding and instead delays (cycles / 30) cycles from
		// the next cycle
		final int serverCycles = (cycles / 30) + 1;
		log.debug("Projectile distance {} cycles {} server cycles {}", distance, cycles, serverCycles);
		return serverCycles;
	}

	private boolean specialAttackHit(SpecialWeapon specialWeapon, Hitsplat hitsplat, NPC target) {
		log.debug("Special attack hit {} hitsplat {}", specialWeapon, hitsplat.getAmount());

		if (config.useCustomSpecSound()) {
			// Arclight
			if (arclightItemIds[0] == specialWeapon.getItemID()[0]) {
				if (hitsplat.getAmount() == 0 && config.arclightMissBoolean()) {
					log.debug("Arclight spec missed");
					soundToPlay = loadCustomSound(HitSoundEnum.ARCLIGHT_MISS.getFile());
					if (soundToPlay != null)
						log.debug("Found custom Arclight miss sound");
					else
						soundToPlay = loadDefaultSound(DEFAULT_ARCLIGHT_MISS_FILE);
					return true;
				}
				if (hitsplat.getAmount() != 0 && config.arclightHitBoolean()) {
					log.debug("Arclight spec hit");
					soundToPlay = loadCustomSound(HitSoundEnum.ARCLIGHT_HIT.getFile());
					if (soundToPlay != null)
						log.debug("Found custom Arclight hit sound");
					else
						soundToPlay = loadDefaultSound(DEFAULT_ARCLIGHT_HIT_FILE);
					return true;
				}
			}
			// DWH
			if (dwhItemIds[0] == specialWeapon.getItemID()[0] || dwhItemIds[1] == specialWeapon.getItemID()[0]) {
				if (hitsplat.getAmount() != 0 && config.dwhHitBoolean()) {
					log.debug("DWH spec hit");
					if (maxed) {
						soundToPlay = loadCustomSound(HitSoundEnum.DWH_MAX.getFile());
						if (soundToPlay != null)
							log.debug("Found custom DWH max sound");
						else
							soundToPlay = loadDefaultSound(DEFAULT_DWH_MAX_FILE);
						log.debug("Assigned sound to DWH max");
						return true;
					} else {
						soundToPlay = loadCustomSound(HitSoundEnum.DWH_HIT.getFile());
						if (soundToPlay != null)
							log.debug("Found custom DWH hit sound");
						else
							soundToPlay = loadDefaultSound(DEFAULT_DWH_HIT_FILE);
						log.debug("Assigned sound to DWH hit");
						return true;
					}
				}
				if (hitsplat.getAmount() == 0 && config.dwhMissBoolean()) {
					log.debug("DWH spec missed");
					soundToPlay = loadCustomSound(HitSoundEnum.DWH_MISS.getFile());
					if (soundToPlay != null)
						log.debug("Found custom DWH miss sound");
					else
						soundToPlay = loadCustomSound(HitSoundEnum.DWH_MISS.getFile());
					return true;
				}
			}

			if (bgsItemIds[0] == specialWeapon.getItemID()[0]) {
				if (hitsplat.getAmount() != 0 && config.bgsHitBoolean()) {
					log.debug("BGS spec hit");
					if (maxed) {
						soundToPlay = loadCustomSound(HitSoundEnum.BGS_MAX.getFile());
						if (soundToPlay != null)
							log.debug("Found custom bgs max sound");
						else
							soundToPlay = loadDefaultSound(DEFAULT_BGS_MAX_FILE);
						log.debug("Assigned sound to BGS max");
					} else {
						soundToPlay = loadCustomSound(HitSoundEnum.BGS_HIT.getFile());
						if (soundToPlay != null)
							log.debug("Found custom bgs hit sound");
						else
							soundToPlay = loadDefaultSound(DEFAULT_BGS_HIT_FILE);

						log.debug("Assigned sound to BGS hit");
					}
					return true;
				}
				if (hitsplat.getAmount() == 0 && config.bgsMissBoolean()) {
					log.debug("BGS spec missed");
					soundToPlay = loadCustomSound(HitSoundEnum.BGS_MISS.getFile());
					if (soundToPlay != null)
						log.debug("Found custom bgs max sound");
					else
						soundToPlay = loadDefaultSound(DEFAULT_BGS_MISS_FILE);
					return true;
				}

			}

			if (boneDaggerItemIds[0] == specialWeapon.getItemID()[0]
					|| boneDaggerItemIds[1] == specialWeapon.getItemID()[0]
					|| boneDaggerItemIds[2] == specialWeapon.getItemID()[0]
					|| boneDaggerItemIds[3] == specialWeapon.getItemID()[0]) {
				if (hitsplat.getAmount() != 0 && config.bDaggerHitBoolean()) {
					log.debug("Bone dagger spec hit");
					if (maxed) {
						soundToPlay = loadCustomSound(HitSoundEnum.BONE_DAGGER_MAX.getFile());
						if (soundToPlay != null)
							log.debug("Found custom bone dagger max sound");
						else
							soundToPlay = loadDefaultSound(DEFAULT_BONE_DAGGER_MAX_FILE);
					} else {
						soundToPlay = loadCustomSound(HitSoundEnum.BONE_DAGGER_HIT.getFile());
						if (soundToPlay != null)
							log.debug("Found custom bone dagger hit sound");
						else
							soundToPlay = loadDefaultSound(DEFAULT_BONE_DAGGER_HIT_FILE);
					}
					return true;
				}
				if (hitsplat.getAmount() == 0 && config.bDaggerMissBoolean()) {
					log.debug("Bone dagger spec missed");
					soundToPlay = loadCustomSound(HitSoundEnum.BONE_DAGGER_MISS.getFile());
					if (soundToPlay != null)
						log.debug("Found custom bone dagger miss sound");
					else
						soundToPlay = loadDefaultSound(DEFAULT_BONE_DAGGER_MISS_FILE);
					return true;
				}
			}
		} else {
			if (hitsplat.getAmount() != 0) {
				soundToPlay = loadCustomSound(HitSoundEnum.SPEC_HIT.getFile());
				if (soundToPlay != null)
					log.debug("Found custom spec hit sound");
				else
					soundToPlay = loadDefaultSound(DEFAULT_SPEC_HIT_FILE);
				return true;
			}
			if (hitsplat.getAmount() == 0) {
				soundToPlay = loadCustomSound(HitSoundEnum.SPEC_MISS.getFile());
				if (soundToPlay != null)
					log.debug("Found custom spec hit sound");
				else
					soundToPlay = loadDefaultSound(DEFAULT_SPEC_MISS_FILE);
				return true;
			}
		}
		return false;
	}
	*/

	@Provides
	AttackSoundNotificationsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(AttackSoundNotificationsConfig.class);
	}
}
