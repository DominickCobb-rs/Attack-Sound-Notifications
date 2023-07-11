package com.AttackSoundNotifications;

import static com.AttackSoundNotifications.AttackSoundNotificationsPlugin.CONFIG_GROUP;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(CONFIG_GROUP)
public interface AttackSoundNotificationsConfig extends Config
{

	@Range(min = 0, max = 200)
	@ConfigItem(position = 1, keyName = "Volume", name = "Volume", description = "Control how loud the audio should be")
	default int Volume()
	{
		return 25;
	}

	@ConfigItem(keyName = "cantFind", name = "Play fallback sounds", description = "If your custom audio file can't be found, play the default", position = 2)
	default boolean cantFind()
	{
		return true;
	}

	// Instructions to add custom sounds
	@ConfigItem(keyName = "customHitSound", name = "Set Custom Sound", description = "Instructions to set custom sounds", position = 3, warning = "Reset this field if you accidentally remove it.")
	default String customHitSound()
	{
		return "Adding a custom sound:\n" +
			"Add your custom sound file anywhere you want\n" +
			"Copy the absolute filepath\n" +
			"Paste the filepath into the text box under the dropdown menus\n" +
			"Click \"Test the sound\" to make sure the plugin found it\n\n" +
			"You'll receive a chat message in-game if your file can't be found when you attack.";
	}
}
