package com.AttackSoundNotifications;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AttackSoundNotificationsTest
{	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AttackSoundNotificationsPlugin.class);
		RuneLite.main(args);
	}
}