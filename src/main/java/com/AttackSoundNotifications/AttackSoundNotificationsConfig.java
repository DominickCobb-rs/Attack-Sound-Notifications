package com.AttackSoundNotifications;

import com.AttackSoundNotifications.AttackSoundNotificationsPlugin;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("combateventsnotifier")
public interface AttackSoundNotificationsConfig extends Config {
	public enum MaxSoundOption {
		NONE("None"),
		GLOBALMAX("Global Max"),
		GLOBALSPECIAL("Special Max"),
		WEAPONSPECIFIC("Weapon Specific");

		private final String displayText;

		MaxSoundOption(String displayText) {
			this.displayText = displayText;
		}

		public String getText(){
			return displayText;
		}

		@Override
		public String toString() {
			return displayText;
		}
	}

	public enum HitSoundOption {
		NONE("None"),
		GLOBALSPECIAL("Special Hit"),
		WEAPONSPECIFIC("Weapon Specific");

		private final String displayText;

		HitSoundOption(String displayText) {
			this.displayText = displayText;
		}

		public String getText(){
			return displayText;
		}

		@Override
		public String toString() {
			return displayText;
		}
	}

	public enum MissSoundOption {
		NONE("None"),
		GLOBALMISS("Global Miss"),
		GLOBALSPECIAL("Special Miss"),
		WEAPONSPECIFIC("Weapon Specific");

		private final String displayText;

		MissSoundOption(String displayText) {
			this.displayText = displayText;
		}

		public String getText(){
			return displayText;
		}

		@Override
		public String toString() {
			return displayText;
		}
	}
	
	////////////////////////
	// Section Definition //
	////////////////////////
	@Range(
		min = 0,
		max = 200
	)
	@ConfigItem(
		position = 1,
		keyName = "Volume",
		name = "Volume",
		description = "Control how loud the audio should be"
	)
	default int Volume()
	{
		return 25;
	}
	
	@ConfigSection(
			position = 2,
			name = "Notification Toggles",
			description = "Toggle sounds on and off"
	)
	String notificationToggleSection = "notificationToggleSection";

	@ConfigSection(
			position = 3,
			name = "Spec Miss Sounds",
			description = "Specify what spec misses should play audio"
	)
	String missSoundSection = "missSoundSection";

	@ConfigSection(
			position = 4,
			name = "Spec Hit Sounds",
			description = "Specify what spec hits should play audio"
	)
	String hitSoundSection = "hitSoundSection";

	@ConfigSection(
			position = 5,
			name = "Spec Max Sounds",
			description = "Specify what spec max should play audio"
	)
	String maxSoundSection = "maxSoundSection";
	/////////////////////////////////
	// Notification Toggle Section //
	/////////////////////////////////

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

	// Miss options
	@ConfigItem(
			keyName = "arclightMissOption",
			name = "Arclight",
			description = "Anytime you miss an Arclight spec",
			section = "missSoundSection",
			position = 1
	)
	default MissSoundOption arclightMissOption() {
		return MissSoundOption.WEAPONSPECIFIC;
	}

	@ConfigItem(
			keyName = "dwhMissOption",
			name = "DWH",
			description = "Anytime you miss a dwh spec",
			section = "missSoundSection",
			position = 2
	)
	default MissSoundOption dwhMissOption() {
		return MissSoundOption.WEAPONSPECIFIC;
	}

	@ConfigItem(
			keyName = "bgsMissOption",
			name = "BGS",
			description = "Anytime you miss a bgs spec",
			section = "missSoundSection",
			position = 3
	)
	default MissSoundOption bgsMissOption() {
		return MissSoundOption.WEAPONSPECIFIC;
	}

	@ConfigItem(
			keyName = "bDaggerMissBoolean",
			name = "B Dagger",
			description = "Anytime you miss a bone dagger spec",
			section = "missSoundSection",
			position = 4
	)
	default MissSoundOption bDaggerMissOption() {
		return MissSoundOption.WEAPONSPECIFIC;
	}

	// Hit options
	@ConfigItem(
			keyName = "arclightHitOption",
			name = "Arclight",
			description = "Anytime you land an Arclight spec",
			section = "hitSoundSection",
			position = 1
	)
	default HitSoundOption arclightHitOption() {
		return HitSoundOption.WEAPONSPECIFIC;
	}

	@ConfigItem(
			keyName = "dwhHitOption",
			name = "DWH",
			description = "Anytime you land a DWH spec",
			section = "hitSoundSection",
			position = 2
	)
	default HitSoundOption dwhHitOption() {
		return HitSoundOption.WEAPONSPECIFIC;
	}

	@ConfigItem(
			keyName = "bgsHitOption",
			name = "BGS",
			description = "Anytime you land a BGS spec",
			section = "hitSoundSection",
			position = 3
	)
	default HitSoundOption bgsHitOption() {
		return HitSoundOption.WEAPONSPECIFIC;
	}

	@ConfigItem(
			keyName = "bDaggerHitOption",
			name = "B Dagger",
			description = "Anytime you land a bone dagger spec",
			section = "hitSoundSection",
			position = 4
	)
	default HitSoundOption bDaggerHitOption() {
		return HitSoundOption.WEAPONSPECIFIC;
	}
	// Max hit options
	@ConfigItem(
			keyName = "dwhMaxOption",
			name = "DWH",
			description = "Anytime you max a DWH spec",
			section = "maxSoundSection",
			position = 1
	)
	default MaxSoundOption dwhMaxOption() {
		return MaxSoundOption.WEAPONSPECIFIC;
	}

	@ConfigItem(
			keyName = "bgsMaxBoolean",
			name = "BGS",
			description = "Anytime you max a BGS spec",
			section = "maxSoundSection",
			position = 2
	)
	default MaxSoundOption bgsMaxOption() {
		return MaxSoundOption.WEAPONSPECIFIC;
	}

	@ConfigItem(
			keyName = "bDaggerMaxBoolean",
			name = "B Dagger",
			description = "Anytime you max a bone dagger spec",
			section = "maxSoundSection",
			position = 3
	)
	default MaxSoundOption bDaggerMaxOption() {
		return MaxSoundOption.WEAPONSPECIFIC;
	}
	
	// Instructions to add custom sounds
	@ConfigItem(
			keyName = "customHitSound",
			name = "Set Custom Sound",
			description = "Instructions to set custom sounds",
			section = "notificationSoundSection",
			position = 6,
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


