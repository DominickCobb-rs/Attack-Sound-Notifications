package com.AttackSoundNotifications;

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
	public static final String DEFAULT_MAX_FILE = "/default_max.wav";
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

	// Currently supported item IDs //
	private static final int dwh = SpecialWeapon.DRAGON_WARHAMMER.getItemID()[0];
	private static final int dwhOrn = SpecialWeapon.DRAGON_WARHAMMER.getItemID()[1];
	private static final int bgs = SpecialWeapon.BANDOS_GODSWORD.getItemID()[0];
	private static final int bgsOrn = SpecialWeapon.BANDOS_GODSWORD.getItemID()[1];
	private static final int arclight = SpecialWeapon.ARCLIGHT.getItemID()[0];
	private static final int boneDagger = SpecialWeapon.BONE_DAGGER.getItemID()[0];
	private static final int boneDaggerP = SpecialWeapon.BONE_DAGGER.getItemID()[1];
	private static final int boneDaggerPP = SpecialWeapon.BONE_DAGGER.getItemID()[2];
	private static final int boneDaggerPPP = SpecialWeapon.BONE_DAGGER.getItemID()[3];
	//////////////////////////////////

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
		if (lastSpecHitsplat != null) {
			log.debug("Attack detected");
			if (specialWeapon != null) {
				switch (lastSpecHitsplat.getHitsplatType()) {
					case HitsplatID.DAMAGE_MAX_ME: {
						if (specialWeapon.getItemID()[0] == dwh || specialWeapon.getItemID()[0] == dwhOrn) {
							switch (config.dwhMaxOption()) {
								case NONE: {
									log.debug("None sound is selected for dwh spec");
								} break;
								case GLOBALSPECIAL: {
									log.debug("Playing global special sound");
									soundToPlay = loadCustomSound(SPEC_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_MAX_FILE);
								} break;
								case GLOBALMAX: {
									log.debug("Playing default max sound");
									soundToPlay = loadCustomSound(SPEC_MAX_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_MAX_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing dwh max sound");
									soundToPlay = loadCustomSound(DWH_MAX_FILE);
									if (soundToPlay != null)
										log.debug("Loaded dwh max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_DWH_MAX_FILE);
								} break;
							}
						} 
						else if (specialWeapon.getItemID()[0] == bgs || specialWeapon.getItemID()[0] == bgsOrn) {
							switch (config.bgsMaxOption()) {
								case NONE: {
									log.debug("None sound is selected for bgs spec");
								} break;
								case GLOBALSPECIAL: {
									log.debug("Playing global special sound");
									soundToPlay = loadCustomSound(SPEC_MAX_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_MAX_FILE);
								} break;
								case GLOBALMAX: {
									log.debug("Playing default max sound");
									soundToPlay = loadCustomSound(MAX_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_MAX_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing dwh max sound");
									soundToPlay = loadCustomSound(BGS_MAX_FILE);
									if (soundToPlay != null)
										log.debug("Loaded bgs max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_BGS_MAX_FILE);
								} break;
							}
						} 
						else if (specialWeapon.getItemID()[0] == boneDagger
								|| specialWeapon.getItemID()[0] == boneDaggerP
								|| specialWeapon.getItemID()[0] == boneDaggerPP
								|| specialWeapon.getItemID()[0] == boneDaggerPPP) {
							switch (config.bDaggerMaxOption()) {
								case NONE: {
									log.debug("None sound is selected for bone dagger spec");
								} break;
								case GLOBALSPECIAL: {
									log.debug("Playing global special sound");
									soundToPlay = loadCustomSound(SPEC_MAX_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_MAX_FILE);
								} break;
								case GLOBALMAX: {
									log.debug("Playing default max sound");
									soundToPlay = loadCustomSound(MAX_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_MAX_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing bone dagger max sound");
									soundToPlay = loadCustomSound(BONE_DAGGER_MAX_FILE);
									if (soundToPlay != null)
										log.debug("Loaded bone dagger max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_BONE_DAGGER_MAX_FILE);
								} break;
							} break;
						}
					} break;
						
					case HitsplatID.DAMAGE_ME: {
						if (specialWeapon.getItemID()[0] == dwh || specialWeapon.getItemID()[0] == dwhOrn) {
							switch (config.dwhHitOption()) {
								case NONE: {
									log.debug("None sound is selected for dwh spec");
								} break;
								case GLOBALSPECIAL: {
									log.debug("Playing global special sound");
									soundToPlay = loadCustomSound(SPEC_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_HIT_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing dwh max sound");
									soundToPlay = loadCustomSound(DWH_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded dwh max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_DWH_HIT_FILE);
								} break;
							}
						} 
						else if (specialWeapon.getItemID()[0] == bgs || specialWeapon.getItemID()[0] == bgsOrn) {
							switch (config.bgsHitOption()) {
								case NONE: {
									log.debug("None sound is selected for bgs spec");
								} break;
								case GLOBALSPECIAL: {
									log.debug("Playing global special sound");
									soundToPlay = loadCustomSound(SPEC_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_HIT_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing dwh max sound");
									soundToPlay = loadCustomSound(BGS_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded dwh max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_BGS_HIT_FILE);
								} break;
							}
						} 
						else if (specialWeapon.getItemID()[0] == boneDagger
								|| specialWeapon.getItemID()[0] == boneDaggerP
								|| specialWeapon.getItemID()[0] == boneDaggerPP
								|| specialWeapon.getItemID()[0] == boneDaggerPPP) {
							switch (config.bDaggerHitOption()) {
								case NONE: {
									log.debug("None sound is selected for bone dagger spec");
								} break; 
								case GLOBALSPECIAL: {
									log.debug("Playing global special sound");
									soundToPlay = loadCustomSound(SPEC_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_HIT_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing bone dagger max sound");
									soundToPlay = loadCustomSound(BONE_DAGGER_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded bone dagger max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_BONE_DAGGER_HIT_FILE);
								} break;
							}
						} else if (specialWeapon.getItemID()[0] == arclight) {
							switch (config.arclightHitOption()) {
								case NONE: {
									log.debug("None sound is selected for arclight spec");
								} break;
								case GLOBALSPECIAL: {
									log.debug("Playing global special sound");
									soundToPlay = loadCustomSound(SPEC_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_HIT_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing bone dagger max sound");
									soundToPlay = loadCustomSound(ARCLIGHT_HIT_FILE);
									if (soundToPlay != null)
										log.debug("Loaded bone dagger max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_ARCLIGHT_HIT_FILE);
								} break;
							}
						}
					} break;

					case HitsplatID.BLOCK_ME: {
						if (specialWeapon.getItemID()[0] == dwh || specialWeapon.getItemID()[0] == dwhOrn) {
							switch (config.dwhMissOption()) {
								case NONE: {
									log.debug("None sound is selected for dwh spec");
								} break;
								case GLOBALSPECIAL: {
									log.debug("Playing global special miss sound");
									soundToPlay = loadCustomSound(SPEC_MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec miss file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_MISS_FILE);
								} break;
								case GLOBALMISS: {
									log.debug("Playing default miss sound");
									soundToPlay = loadCustomSound(MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec miss file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_MISS_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing dwh miss sound");
									soundToPlay = loadCustomSound(DWH_MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded dwh miss hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_DWH_MISS_FILE);
								} break;
							}
						} 
						else if (specialWeapon.getItemID()[0] == bgs || specialWeapon.getItemID()[0] == bgsOrn) {
							switch (config.bgsMissOption()) {
								case NONE: {
									log.debug("None sound is selected for bgs spec");
								} break;
								case GLOBALSPECIAL: {
									log.debug("Playing global special miss sound");
									soundToPlay = loadCustomSound(SPEC_MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec miss file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_MISS_FILE);
								} break;
								case GLOBALMISS: {
									log.debug("Playing default miss sound");
									soundToPlay = loadCustomSound(MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom miss file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_MISS_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing bgs miss sound");
									soundToPlay = loadCustomSound(BGS_MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom bgs miss file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_BGS_MISS_FILE);
								} break;
							}
						} 
						else if (specialWeapon.getItemID()[0] == boneDagger
								|| specialWeapon.getItemID()[0] == boneDaggerP
								|| specialWeapon.getItemID()[0] == boneDaggerPP
								|| specialWeapon.getItemID()[0] == boneDaggerPPP) {
							switch (config.bDaggerMissOption()) {
								case NONE: {
									log.debug("None sound is selected for bone dagger spec");
								} break;
								case GLOBALSPECIAL: {
									log.debug("Playing global special miss sound");
									soundToPlay = loadCustomSound(SPEC_MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_MISS_FILE);
								} break;
								case GLOBALMISS: {
									log.debug("Playing default miss sound");
									soundToPlay = loadCustomSound(MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom max hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_MISS_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing bone dagger miss sound");
									soundToPlay = loadCustomSound(BONE_DAGGER_MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom bone dagger miss hit file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_BONE_DAGGER_MISS_FILE);
								} break;
							} break;
						}
						else if (specialWeapon.getItemID()[0] == arclight){
							switch (config.arclightMissOption()) {
								case NONE: {
									log.debug("None sound is selected for arclight spec");
								} break;
								case GLOBALSPECIAL: {
									log.debug("Playing global special sound");
									soundToPlay = loadCustomSound(SPEC_MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom spec miss file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_SPEC_MISS_FILE);
								} break;
								case GLOBALMISS: {
									log.debug("Playing default miss sound");
									soundToPlay = loadCustomSound(MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom global miss file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_MISS_FILE);
								} break;
								case WEAPONSPECIFIC: {
									log.debug("Playing arclight miss sound");
									soundToPlay = loadCustomSound(ARCLIGHT_MISS_FILE);
									if (soundToPlay != null)
										log.debug("Loaded custom arclight miss file");
									else
										soundToPlay = loadDefaultSound(DEFAULT_ARCLIGHT_MISS_FILE);
								} break;
							}
						}
					}
						break;
				}
			} else {
				log.debug("Non-Special");
				switch (lastSpecHitsplat.getHitsplatType()) {
					case HitsplatID.DAMAGE_MAX_ME: {
						if (config.maxBoolean()) {
							log.debug("Playing default max sound");
							soundToPlay = loadCustomSound(MAX_HIT_FILE);
							if (soundToPlay != null)
								log.debug("Loaded custom max hit file");
							else
								soundToPlay = loadDefaultSound(DEFAULT_MAX_FILE);
						}
					}
						break;
					case HitsplatID.BLOCK_ME: {
						if (config.missBoolean()) {
							log.debug("Playing default miss sound");
							soundToPlay = loadCustomSound(MISS_FILE);
							if (soundToPlay != null)
								log.debug("Loaded custom miss file");
							else
								soundToPlay = loadDefaultSound(DEFAULT_MISS_FILE);
						}
					}
						break;
				}
			}
		}
		if (soundToPlay != null) {
			playCustomSound(soundToPlay);
			specialWeapon = null;
			lastSpecHitsplat = null;
			lastSpecTarget = null;
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
		if (hitsplat.isMine() && target != client.getLocalPlayer()) {
			lastSpecHitsplat = hitsplat;
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
	/*
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
