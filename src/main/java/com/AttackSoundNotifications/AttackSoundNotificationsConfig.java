package com.AttackSoundNotifications;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("combateventsnotifier")
public interface AttackSoundNotificationsConfig extends Config {
	////////////////////////
	// Section Definition //
	////////////////////////
	@ConfigSection(
			position = 1,
			name = "Notification Toggles",
			description = "Toggle sounds on and off"
	)
	String notificationToggleSection = "notificationToggleSection";

	@ConfigSection(
			position = 4,
			name = "Weapon Specific Sounds",
			description = "Choose the sound to play"
	)
	String WeaponSoundSection = "weaponSoundSection";

	/////////////////////////////////
	// Notification Toggle Section //
	/////////////////////////////////
	@ConfigItem(
			keyName = "missBoolean",
			name = "Missed attacks",
			description = "Anytime you hit 0 on your opponent",
			section = "notificationToggleSection",
			position = 1
	)
	default boolean missBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "maxBoolean",
			name = "Max hits",
			description = "Anytime you max on your opponent",
			section = "notificationToggleSection",
			position = 2
	)
	default boolean maxBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "anySpecMissBoolean",
			name = "Spec Miss",
			description = "Anytime you miss spec\n Will still play default sound if Missed attacks is enabled",
			section = "notificationToggleSection",
			position = 3
	)
	default boolean anySpecMissBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "anySpecHitBoolean",
			name = "Spec Hit",
			description = "Anytime you land spec\n",
			section = "notificationToggleSection",
			position = 4
	)
	default boolean anySpecHitBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "prioritizeMax",
			name = "Prioritize max over spec",
			description = "Play max sounds for max hits over custom weapon sounds, plays the custom sound if Spec Max Hit is enabled",
			section = "notificationToggleSection",
			position = 5
	)
	default boolean prioritizeMax(){
		return true;
	}

	@ConfigItem(
			keyName = "specMaxBoolean",
			name = "Spec Max Hit",
			description = "Replaces the default max hit sound with a custom one if Prioritize max is enabled",
			section = "notificationToggleSection",
			position = 6
	)
	default boolean globalSpecMaxBoolean(){
		return true;
	}

	///////////////////////////////
	// Weapon Spec Sound section //
	///////////////////////////////
	@ConfigItem(
			keyName = "specDefaultSoundBoolean",
			name = "Per-weapon sound",
			description = "Use an individual sound for each spec weapon",
			section = "weaponSoundSection",
			position = 1
	)
	default boolean useCustomSpecSound(){
		return true;
	}

	@ConfigItem(
			keyName = "arclightMissBoolean",
			name = "Missed Arclight specs",
			description = "Anytime you miss an Arclight spec",
			section = "weaponSoundSection",
			position = 2
	)
	default boolean arclightMissBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "arclightHitBoolean",
			name = "Landed Arclight specs",
			description = "Anytime you land an Arclight spec",
			section = "weaponSoundSection",
			position = 3
	)
	default boolean arclightHitBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "dwhMissBoolean",
			name = "Missed DWH specs",
			description = "Anytime you miss a dwh spec",
			section = "weaponSoundSection",
			position = 4
	)
	default boolean dwhMissBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "dwhHitBoolean",
			name = "Landed DWH specs",
			description = "Anytime you land a DWH spec",
			section = "weaponSoundSection",
			position = 5
	)
	default boolean dwhHitBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "dwhMaxBoolean",
			name = "Max DWH spec",
			description = "Anytime you max a DWH spec",
			section = "weaponSoundSection",
			position = 6
	)
	default boolean dwhMaxBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bgsMissBoolean",
			name = "Missed BGS specs",
			description = "Anytime you miss a bgs spec",
			section = "weaponSoundSection",
			position = 7
	)
	default boolean bgsMissBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bgsHitBoolean",
			name = "Landed BGS specs",
			description = "Anytime you land a BGS spec",
			section = "weaponSoundSection",
			position = 8
	)
	default boolean bgsHitBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bgsHitBoolean",
			name = "Max BGS spec",
			description = "Anytime you max a BGS spec",
			section = "weaponSoundSection",
			position = 9
	)
	default boolean bgsMaxBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bDaggerMissBoolean",
			name = "Missed bone dagger specs",
			description = "Anytime you miss a bone dagger spec",
			section = "weaponSoundSection",
			position = 9
	)
	default boolean bDaggerMissBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bDaggerHitBoolean",
			name = "Landed bone dagger specs",
			description = "Anytime you land a bone dagger spec",
			section = "weaponSoundSection",
			position = 10
	)
	default boolean bDaggerHitBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bDaggerMaxBoolean",
			name = "Max bone dagger spec",
			description = "Anytime you max a bone dagger spec",
			section = "weaponSoundSection",
			position = 11
	)
	default boolean bDaggerMaxBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "customHitSound",
			name = "Set Custom Sound",
			description = "Instructions to set custom sounds",
			section = "notificationSoundSection",
			position = 5,
			warning = "Reset this field if you accidentally remove it."
	)
	default String customHitSound() {return "NOTE: THIS IS A WIP. DON'T EXPECT ANYTHING TO CHANGE WHILE THIS MESSAGE STILL EXISTS\n" +
			"Adding a custom sound\n" +
			"1a. Navigate to your .runelite folder\n" +
			"1b. You can right-click the screenshot button in the top right of the runelite client.\n" +
			"1c. Then open screenshot folder, and navigate to the .runelite directory.\n" +
			"2. Create a folder called 'attacknotifications'.\n" +
			"2a. Create a subfolder for the type you want {arclight_spec_hit}.\n" +
			"3. Add your sound .wav file in the sub-folder .\n" +
			"\n" +
			"Acceptable Folder Names:\n" +
			"arclight_spec_hit\n" +
			"arclight_spec_miss\n" +
			"dwh_spec_hit\n" +
			"dwh_spec_miss\n" +
			"bgs_spec_hit\n" +
			"bgs_spec_miss\n" +
			"max\n";}
}
