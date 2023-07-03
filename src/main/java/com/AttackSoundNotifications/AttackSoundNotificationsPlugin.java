package com.AttackSoundNotifications;

import com.AttackSoundNotifications.enums.HitSoundEnum;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.NPC;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.specialcounter.SpecialWeapon;

import java.util.Arrays;
import javax.sound.sampled.*;
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
	private Clip clip = null;

	private int specialPercentage;
	private SpecialWeapon specialWeapon;
	// expected tick the hitsplat will happen on
	private int hitsplatTick;
	// most recent hitsplat and the target it was on
	private Hitsplat lastSpecHitsplat;
	private NPC lastSpecTarget;
	private boolean maxed = false;
	private InputStream soundToPlay;
	private static final String BASE_DIRECTORY = System.getProperty("user.home") + "/.runelite/attacknotifications/";

	// Custom Files //
	public static final File MAX_HIT_FILE = new File(BASE_DIRECTORY, "max.wav");
	public static final File MISS_FILE = new File(BASE_DIRECTORY, "miss.wav");
	public static final File SPEC_MISS_FILE = new File(BASE_DIRECTORY, "spec_miss.wav");
	public static final File SPEC_HIT_FILE = new File(BASE_DIRECTORY, "spec_hit.wav");
	public static final File SPEC_MAX_FILE = new File(BASE_DIRECTORY, "spec_max.wav");
	public static final File ARCLIGHT_MISS_FILE = new File(BASE_DIRECTORY, "arclight_spec_miss.wav");
	public static final File ARCLIGHT_HIT_FILE = new File(BASE_DIRECTORY, "arclight_spec_hit.wav");
	public static final File DWH_MISS_FILE = new File(BASE_DIRECTORY, "dwh_spec_miss.wav");
	public static final File DWH_HIT_FILE = new File(BASE_DIRECTORY, "dwh_spec_hit.wav");
	public static final File DWH_MAX_FILE = new File(BASE_DIRECTORY, "dwh_spec_max.wav");
	public static final File BGS_MISS_FILE = new File(BASE_DIRECTORY, "bgs_spec_miss.wav");
	public static final File BGS_HIT_FILE = new File(BASE_DIRECTORY, "bgs_spec_hit.wav");
	public static final File BGS_MAX_FILE = new File(BASE_DIRECTORY, "bgs_spec_max.wav");
	public static final File BONE_DAGGER_MISS_FILE = new File(BASE_DIRECTORY, "bone_dagger_spec_miss.wav");
	public static final File BONE_DAGGER_HIT_FILE = new File(BASE_DIRECTORY, "bone_dagger_spec_hit.wav");
	public static final File BONE_DAGGER_MAX_FILE = new File(BASE_DIRECTORY, "bone_dagger_spec_default.wav");
	//////////////////

	// Default Files //
	public static final String DEFAULT_MAX_HIT_FILE = "/default_max.wav";
	public static final String DEFAULT_MISS_FILE = "/default_miss.wav";
	public static final String DEFAULT_SPEC_MISS_FILE = "/default_spec_miss.wav";
	public static final String DEFAULT_SPEC_HIT_FILE = "/default_spec_hit.wav";
	public static final String DEFAULT_SPEC_MAX_FILE = "/default_spec_max.wav";
	public static final String DEFAULT_ARCLIGHT_MISS_FILE = "/default_arclight_spec_miss.wav";
	public static final String DEFAULT_ARCLIGHT_HIT_FILE = "/default_arclight_spec_hit.wav";
	public static final String DEFAULT_DWH_MISS_FILE = "/default_dwh_spec_miss.wav";
	public static final String DEFAULT_DWH_HIT_FILE = "/default_dwh_spec_hit.wav";
	public static final String DEFAULT_DWH_MAX_FILE = "/default_dwh_spec_max.wav";
	public static final String DEFAULT_BGS_MISS_FILE = "/default_bgs_spec_miss.wav";
	public static final String DEFAULT_BGS_HIT_FILE = "/default_bgs_spec_hit.wav";
	public static final String DEFAULT_BGS_MAX_FILE = "/default_bgs_spec_max.wav";
	public static final String DEFAULT_BONE_DAGGER_MISS_FILE = "/default_bone_dagger_spec_miss.wav";
	public static final String DEFAULT_BONE_DAGGER_HIT_FILE = "/default_bone_dagger_spec_hit.wav";
	public static final String DEFAULT_BONE_DAGGER_MAX_FILE = "/default_bone_dagger_spec_max.wav";
	///////////////////

	@Override
	protected void startUp() throws Exception {
		log.info("Attack Sounds Notifier started!");
	}

	@Override
	protected void shutDown() throws Exception {
		log.info("Attack Sounds Notifier stopped!");
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (lastSpecHitsplat != null && specialWeapon != null && lastSpecTarget != null) {
			log.debug("Special attack detected");
			if (lastSpecHitsplat.getAmount() > 0 && config.anySpecHitBoolean()) {
				if (maxed && config.prioritizeMax()) {
					if (config.globalSpecMaxBoolean()) {
						soundToPlay = loadCustomSound(HitSoundEnum.SPEC_MAX.getFile());
						if (soundToPlay != null)
							log.debug("Assigned custom spec max sound");
						else
							soundToPlay = loadDefaultSound(DEFAULT_SPEC_MAX_FILE);
					}
					if ((config.bDaggerMaxBoolean() || config.bgsMaxBoolean() || config.dwhMaxBoolean()) && !config.useCustomSpecSound()) {
						specialAttackHit(specialWeapon, lastSpecHitsplat, lastSpecTarget);
					}
					log.debug("Playing maxed spec sound");
				} else {
					specialAttackHit(specialWeapon, lastSpecHitsplat, lastSpecTarget);
					log.debug("Playing hit spec sound");
				}
			} else if (lastSpecHitsplat.getAmount() == 0 && config.anySpecMissBoolean()) {
				boolean specSound = specialAttackHit(specialWeapon, lastSpecHitsplat, lastSpecTarget);
				if (specSound) {
					log.debug("Playing spec equal to 0 sound");
				}
			}
		} else if (maxed) {
			log.debug("Playing maxed sound");
		} else if (soundToPlay != null) {
			log.debug("Playing sound");
		}
		if (soundToPlay != null) {
			playCustomSound(soundToPlay);
			specialWeapon = null;
			lastSpecHitsplat = null;
			lastSpecTarget = null;
			maxed = false;
			soundToPlay = null;
		}
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

		// This event runs prior to player and npc updating, making getInteracting() too
		// early to call..
		// defer this with invokeLater(), but note that this will run after incrementing
		// the server tick counter
		// so we capture the current server tick counter here for use in computing the
		// final hitsplat tick
		final int serverTicks = client.getTickCount();
		clientThread.invokeLater(() -> {
			this.specialWeapon = usedSpecialWeapon();

			if (this.specialWeapon == null) {
				// unrecognized special attack weapon
				return;
			}

			Actor target = client.getLocalPlayer().getInteracting();
			lastSpecTarget = target instanceof NPC ? (NPC) target : null;
			hitsplatTick = serverTicks + getHitDelay(specialWeapon, target);
			log.debug("Special attack used - percent: {} weapon: {} server cycle {} hitsplat cycle {}",
					specialPercentage, specialWeapon, serverTicks, hitsplatTick);
		});
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
		Actor target = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		if (hitsplatTick == client.getTickCount()) {
			lastSpecHitsplat = hitsplat;
		}
		if (hitsplat.isMine() && target != client.getLocalPlayer()) {
			switch (hitsplatApplied.getHitsplat().getHitsplatType()) {
				case HitsplatID.BLOCK_ME:
					miss();
					break;
				case HitsplatID.DAMAGE_MAX_ME:
					maxed = true;
					max();
					break;
			}
		}
	}

	private void miss() {
		log.info("BLOCK_ME");
		if (config.missBoolean()) {
			log.debug("Queueing missed attack sound fallback");
			try {
				soundToPlay = new BufferedInputStream(new FileInputStream(HitSoundEnum.MISS.getFile()));
			} catch (FileNotFoundException e) {
				soundToPlay = loadDefaultSound(DEFAULT_MISS_FILE);
			}
		}
	}

	private void max() {
		if (config.maxBoolean()) {
			log.debug("DAMAGE_MAX_ME");
			soundToPlay = loadCustomSound(HitSoundEnum.MAX.getFile());
			if (soundToPlay != null)
				log.debug("Found custom maxsound");
			else
				soundToPlay = loadDefaultSound(DEFAULT_MAX_HIT_FILE);
		}
	}

	private synchronized boolean playCustomSound(InputStream streamName) {
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

	private InputStream loadDefaultSound(String filePath) {
		return AttackSoundNotificationsPlugin.class.getResourceAsStream(filePath);
	}

	private BufferedInputStream loadCustomSound(File fileName) {
		try {
			return new BufferedInputStream(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	private SpecialWeapon usedSpecialWeapon() {
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null) {
			return null;
		}

		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		if (weapon == null) {
			return null;
		}

		for (SpecialWeapon specialWeapon : SpecialWeapon.values()) {
			if (Arrays.stream(specialWeapon.getItemID()).anyMatch(id -> id == weapon.getId())) {
				return specialWeapon;
			}
		}
		return null;
	}

	private int getHitDelay(SpecialWeapon specialWeapon, Actor target) {
		// DORGESHUUN_CROSSBOW is the only ranged wep we support, so everything else is
		// just melee and delay 1
		if (specialWeapon != SpecialWeapon.DORGESHUUN_CROSSBOW || target == null)
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
		int[] dwhItemIds = SpecialWeapon.DRAGON_WARHAMMER.getItemID();
		int[] bgsItemIds = SpecialWeapon.BANDOS_GODSWORD.getItemID();
		int[] arclightItemIds = SpecialWeapon.ARCLIGHT.getItemID();
		int[] boneDaggerItemIds = SpecialWeapon.BONE_DAGGER.getItemID();
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

	@Provides
	AttackSoundNotificationsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(AttackSoundNotificationsConfig.class);
	}
}
