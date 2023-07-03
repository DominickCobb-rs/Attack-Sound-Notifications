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
			position = 2,
			name = "Spec Miss Sounds",
			description = "Specify what spec misses should play audio"
	)
	String missSoundSection = "missSoundSection";

	@ConfigSection(
			position = 3,
			name = "Spec Hit Sounds",
			description = "Specify what spec hits should play audio"
	)
	String hitSoundSection = "hitSoundSection";

	@ConfigSection(
			position = 3,
			name = "Spec Max Sounds",
			description = "Specify what spec max should play audio"
	)
	String maxSoundSection = "maxSoundSection";
	/////////////////////////////////
	// Notification Toggle Section //
	/////////////////////////////////

	@ConfigItem(
			keyName = "specDefaultSoundBoolean",
			name = "Custom spec sounds",
			description = "Use an different sounds for each spec option",
			section = "notificationToggleSection",
			position = 1
	)
	default boolean useCustomSpecSound(){
		return true;
	}

	@ConfigItem(
			keyName = "missBoolean",
			name = "Missed attacks",
			description = "Anytime you hit 0 on your opponent",
			section = "notificationToggleSection",
			position = 2
	)
	default boolean missBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "maxBoolean",
			name = "Max hits",
			description = "Anytime you max on your opponent",
			section = "notificationToggleSection",
			position = 3
	)
	default boolean maxBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "anySpecMissBoolean",
			name = "Spec Miss",
			description = "Anytime you miss spec\n Will still play default sound if Missed attacks is enabled",
			section = "notificationToggleSection",
			position = 4
	)
	default boolean anySpecMissBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "anySpecHitBoolean",
			name = "Spec Hit",
			description = "Anytime you land spec\n",
			section = "notificationToggleSection",
			position = 5
	)
	default boolean anySpecHitBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "prioritizeMax",
			name = "Prioritize max over spec",
			description = "Play max sounds for max hits over custom weapon sounds, plays the custom sound if Spec Max Hit is enabled",
			section = "notificationToggleSection",
			position = 6
	)
	default boolean prioritizeMax(){
		return true;
	}

	@ConfigItem(
			keyName = "specMaxBoolean",
			name = "Spec Max Hit",
			description = "Replaces the default max hit sound with a custom one if Prioritize max is enabled",
			section = "notificationToggleSection",
			position = 7
	)
	default boolean globalSpecMaxBoolean(){
		return true;
	}

	// Miss options
	@ConfigItem(
			keyName = "arclightMissBoolean",
			name = "Arclight",
			description = "Anytime you miss an Arclight spec",
			section = "missSoundSection",
			position = 1
	)
	default boolean arclightMissBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "dwhMissBoolean",
			name = "Dragon Warhammer",
			description = "Anytime you miss a dwh spec",
			section = "missSoundSection",
			position = 2
	)
	default boolean dwhMissBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bgsMissBoolean",
			name = "Bandos Godsword",
			description = "Anytime you miss a bgs spec",
			section = "missSoundSection",
			position = 3
	)
	default boolean bgsMissBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bDaggerMissBoolean",
			name = "Bone Dagger",
			description = "Anytime you miss a bone dagger spec",
			section = "missSoundSection",
			position = 4
	)
	default boolean bDaggerMissBoolean(){
		return true;
	}
	// Hit options
	@ConfigItem(
			keyName = "arclightHitBoolean",
			name = "Arclight",
			description = "Anytime you land an Arclight spec",
			section = "hitSoundSection",
			position = 1
	)
	default boolean arclightHitBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "dwhHitBoolean",
			name = "Dragon Warhammer",
			description = "Anytime you land a DWH spec",
			section = "hitSoundSection",
			position = 2
	)
	default boolean dwhHitBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bgsHitBoolean",
			name = "Bandos Godsword",
			description = "Anytime you land a BGS spec",
			section = "hitSoundSection",
			position = 3
	)
	default boolean bgsHitBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bDaggerHitBoolean",
			name = "Bone Dagger",
			description = "Anytime you land a bone dagger spec",
			section = "hitSoundSection",
			position = 4
	)
	default boolean bDaggerHitBoolean(){
		return true;
	}
	// Max hit options
	@ConfigItem(
			keyName = "dwhMaxBoolean",
			name = "Dragon Warhammer",
			description = "Anytime you max a DWH spec",
			section = "maxSoundSection",
			position = 1
	)
	default boolean dwhMaxBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bgsMaxBoolean",
			name = "Bandos Godsword",
			description = "Anytime you max a BGS spec",
			section = "maxSoundSection",
			position = 2
	)
	default boolean bgsMaxBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "bDaggerMaxBoolean",
			name = "Bone Dagger",
			description = "Anytime you max a bone dagger spec",
			section = "maxSoundSection",
			position = 3
	)
	default boolean bDaggerMaxBoolean(){
		return true;
	}
	
	// Instructions to add custom sounds
	@ConfigItem(
			keyName = "customHitSound",
			name = "Set Custom Sound",
			description = "Instructions to set custom sounds",
			section = "notificationSoundSection",
			position = 5,
			warning = "Reset this field if you accidentally remove it."
	)
	default String customHitSound() {
		return "Adding a custom sound\n" +
		"1a. Navigate to your .runelite folder\n" +
		"1b. You can right-click the screenshot button in the top right of the runelite client.\n" +
		"1c. Then open screenshot folder, and navigate to the .runelite directory.\n" +
		"2. Create a folder called 'attacknotifications'.\n" +
		"3. Add your sound .wav file in the folder.\n" +
		"\n" +
		"Acceptable File Names:\n" +
		"max.wav\n" +
		"miss.wav\n" +
		"spec_miss.wav\n" +
		"spec_hit.wav\n" +
		"spec_max.wav\n" +
		"arclight_spec_miss.wav\n" +
		"arclight_spec_hit.wav\n" +
		"dwh_spec_miss.wav\n" +
		"dwh_spec_hit.wav\n" +
		"dwh_spec_max.wav\n" +
		"bgs_spec_miss.wav\n" +
		"bgs_spec_hit.wav\n" +
		"bgs_spec_max.wav\n" +
		"bone_dagger_spec_miss.wav\n" +
		"bone_dagger_spec_hit.wav\n" +
		"bone_dagger_spec_default.wav\n";
	}
}


