package com.AttackSoundNotifications;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("combateventsnotifier")
public interface AttackSoundNotificationsConfig extends Config {
	@ConfigSection(
			position = 1,
			name = "Notification Toggles",
			description = "Toggle sounds on and off"
	)
	String notificationToggleSection = "notificationToggleSection";

	@ConfigSection(
			position = 2,
			name = "Sound Choices",
			description = "Choose the sound to play"
	)
	String NotificationSoundSection = "notificationSoundSection";

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
			keyName = "arclightMissBoolean",
			name = "Missed Arclight specs",
			description = "Anytime you miss an Arclight spec",
			section = "notificationToggleSection",
			position = 3
	)
	default boolean arclightMissBoolean(){
		return true;
	}

	@ConfigItem(
			keyName = "arclightHitBoolean",
			name = "Landed Arclight specs",
			description = "Anytime you land an Arclight spec",
			section = "notificationToggleSection",
			position = 4
	)
	default boolean arclightHitBoolean(){
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
	default String customHitSound() {return "NOTE: THIS IS A WIP. DON'T EXPECT ANYTHING TO CHANGE WHILE THIS MESSAGE STILL EXISTS" +
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
