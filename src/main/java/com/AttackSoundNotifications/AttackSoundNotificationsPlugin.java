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
@PluginDescriptor(
	name = "Attack Sound Notifications"
)
public class AttackSoundNotificationsPlugin extends Plugin
{
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
	private boolean sound_played = false;
	private boolean readyToPlay = false;
	private boolean maxed = false;
	private HitSoundEnum soundToPlay;

	private static final String BASE_DIRECTORY = System.getProperty("user.home") + "/.runelite/combatnotifications/";
	public static final File MAX_HIT_FILE = new File(BASE_DIRECTORY, "max.wav");
	public static final File DEFAULT_MISS_FILE = new File(BASE_DIRECTORY, "default_miss.wav");
	public static final File ARCLIGHT_MISS_FILE = new File(BASE_DIRECTORY, "arclight_spec_miss.wav");
	public static final File ARCLIGHT_HIT_FILE = new File(BASE_DIRECTORY, "arclight_spec_hit.wav");
	public static final File DWH_MISS_FILE = new File(BASE_DIRECTORY, "dwh_spec_miss.wav");
	public static final File DWH_HIT_FILE = new File(BASE_DIRECTORY, "dwh_spec_hit.wav");
	public static final File BGS_MISS_FILE = new File(BASE_DIRECTORY, "bgs_spec_miss.wav");
	public static final File BGS_HIT_FILE = new File(BASE_DIRECTORY, "bgs_spec_hit.wav");

	private long lastClipMTime = CLIP_MTIME_UNLOADED;
	private static final long CLIP_MTIME_UNLOADED = -2;
	private static final long CLIP_MTIME_BUILTIN = -1;


	@Override
	protected void startUp() throws Exception
	{
		log.info("Combat Notifier started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Combat Notifier stopped!");
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (maxed) {
			log.debug("Playing maxed sound");
			playCustomSound(soundToPlay);
			readyToPlay = false;
			soundToPlay = null;
			maxed = false;
			lastSpecHitsplat = null;
			specialWeapon = null;
			lastSpecTarget = null;
		}

		if (lastSpecHitsplat != null && specialWeapon != null && lastSpecTarget != null)
		{
			log.debug("Special attack detected");
			if (lastSpecHitsplat.getAmount() > 0)
			{
				specialAttackHit(specialWeapon, lastSpecHitsplat, lastSpecTarget);
			}
			if (lastSpecHitsplat.getAmount() == 0)
			{
				specialAttackHit(specialWeapon, lastSpecHitsplat, lastSpecTarget);
			}

			specialWeapon = null;
			lastSpecHitsplat = null;
			lastSpecTarget = null;
		}

		if (readyToPlay) {
			playCustomSound(soundToPlay);
			readyToPlay = false;
			soundToPlay = null;
			maxed = false;
		}
	}

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

		// This event runs prior to player and npc updating, making getInteracting() too early to call..
		// defer this with invokeLater(), but note that this will run after incrementing the server tick counter
		// so we capture the current server tick counter here for use in computing the final hitsplat tick
		final int serverTicks = client.getTickCount();
		clientThread.invokeLater(() ->
		{
			this.specialWeapon = usedSpecialWeapon();

			if (this.specialWeapon == null)
			{
				// unrecognized special attack weapon
				return;
			}

			Actor target = client.getLocalPlayer().getInteracting();
			lastSpecTarget = target instanceof NPC ? (NPC) target : null;
			hitsplatTick = serverTicks + getHitDelay(specialWeapon, target);
			log.debug("Special attack used - percent: {} weapon: {} server cycle {} hitsplat cycle {}", specialPercentage, specialWeapon, serverTicks, hitsplatTick);
		});
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
		sound_played = false;
		Actor target = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		if (hitsplatTick == client.getTickCount()) {
			lastSpecHitsplat = hitsplat;
		}
		if (hitsplat.isMine() && target != client.getLocalPlayer()) {
			switch (hitsplatApplied.getHitsplat().getHitsplatType()) {
				case HitsplatID.BLOCK_ME:
				miss(); break;
				case HitsplatID.DAMAGE_MAX_ME:
				max(); break;
			}
		}
	}

	private void miss()
	{
		log.info("BLOCK_ME");
		if (config.missBoolean()) {
			log.debug("Queueing missed attack sound fallback");
			readyToPlay = true;
			soundToPlay = HitSoundEnum.DEFAULT_MISS;
		}
	}
	private void max()
	{
		if (config.maxBoolean()) {
			log.debug("DAMAGE_MAX_ME");
			readyToPlay = true;
			soundToPlay = HitSoundEnum.MAX;
			maxed = true;
		}
	}
	private synchronized boolean playCustomSound(HitSoundEnum hitSoundEnum)
	{
		long currentMTime = hitSoundEnum.getFile().exists() ? hitSoundEnum.getFile().lastModified() : CLIP_MTIME_BUILTIN;
		if (clip == null || currentMTime != lastClipMTime || !clip.isOpen())
		{
			try
			{
				if (clip == null) {
					clip = AudioSystem.getClip();
				} else {
					clip.stop();
					clip.flush();
					clip.close();
				}
			}
			catch (LineUnavailableException e)
			{
				lastClipMTime = CLIP_MTIME_UNLOADED;
				log.warn("Unable to play sound", e);
				return false;
			}
			lastClipMTime = currentMTime;
			if (!tryLoadCustomSound(hitSoundEnum))
			{
				return false;
			}
		}
		clip.setFramePosition(0);
		clip.start();
		return true;
	}

	private boolean tryLoadCustomSound(HitSoundEnum hitSoundEnum)
	{
		if (hitSoundEnum.getFile().exists())
		{
			try (InputStream fileStream = new BufferedInputStream(new FileInputStream(hitSoundEnum.getFile()));
				 AudioInputStream sound = AudioSystem.getAudioInputStream(fileStream))
			{
				clip.open(sound);
				return true;
			}
			catch (UnsupportedAudioFileException | IOException | LineUnavailableException e)
			{
				log.warn("Unable to load sound", e);
			}
		}
		return false;
	}

	private SpecialWeapon usedSpecialWeapon()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			return null;
		}

		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		if (weapon == null)
		{
			return null;
		}

		for (SpecialWeapon specialWeapon : SpecialWeapon.values())
		{
			if (Arrays.stream(specialWeapon.getItemID()).anyMatch(id -> id == weapon.getId()))
			{
				return specialWeapon;
			}
		}
		return null;
	}

	private int getHitDelay(SpecialWeapon specialWeapon, Actor target)
	{
		// DORGESHUUN_CROSSBOW is the only ranged wep we support, so everything else is just melee and delay 1
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
		// Dorgeshuun special attack projectile, anim delay, and hitsplat is 60 + distance * 3 with the projectile
		// starting at 41 cycles. Since we are computing the delay when the spec var changes, and not when the
		// projectile first moves, this should be 60 and not 19
		final int cycles = 60 + distance * 3;
		// The server performs no rounding and instead delays (cycles / 30) cycles from the next cycle
		final int serverCycles = (cycles / 30) + 1;
		log.debug("Projectile distance {} cycles {} server cycles {}", distance, cycles, serverCycles);
		return serverCycles;
	}

	private int getHit(SpecialWeapon specialWeapon, Hitsplat hitsplat)
	{
		return specialWeapon.isDamage() ? hitsplat.getAmount() : 0;
	}

	private void specialAttackHit(SpecialWeapon specialWeapon, Hitsplat hitsplat, NPC target)
	{
		int hit = getHit(specialWeapon, hitsplat);

		log.debug("Special attack hit {} hitsplat {}", specialWeapon, hitsplat.getAmount());
		int [] dwhItemIds = SpecialWeapon.DRAGON_WARHAMMER.getItemID();
		int [] bgsItemIds = SpecialWeapon.BANDOS_GODSWORD.getItemID();
		int [] arclightItemIds = SpecialWeapon.ARCLIGHT.getItemID();
		if (arclightItemIds[0] == specialWeapon.getItemID()[0]) {
			if (hitsplat.getAmount() == 0 && config.arclightMissBoolean()) {
				log.debug("Arclight spec missed");
				readyToPlay = true;
				soundToPlay = HitSoundEnum.ARCLIGHT_MISS;
				return;
			}
			if (hitsplat.getAmount() != 0 && config.arclightHitBoolean()) {
				log.debug("Arclight spec hit");
				readyToPlay = true;
				soundToPlay = HitSoundEnum.ARCLIGHT_HIT;
				return;
			}
		}
		if (dwhItemIds[0] == specialWeapon.getItemID()[0]) {

		}
	}
	@Provides
	AttackSoundNotificationsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AttackSoundNotificationsConfig.class);
	}
}
