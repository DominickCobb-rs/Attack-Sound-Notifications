package com.AttackSoundNotifications.ui;

public class EntryPanelState
{
	private String panelName;
	private String weaponId;
	private String audible_status;
	private String customSoundTextField_contents;
	private String replacing_value;

	public EntryPanelState(EntryPanel panel)
	{
		this.panelName = panel.getName();
		this.weaponId = panel.getWeaponIdString();
		this.audible_status = panel.getAudibleString();
		this.customSoundTextField_contents = panel.getCustomSoundPath();
		this.replacing_value = panel.getReplacingString();
	}

	public String getPanelName()
	{
		return panelName;
	}

	public String getWeaponId()
	{
		return weaponId;
	}

	public String getAudible()
	{
		return audible_status;
	}

	public String getCustomSoundPath()
	{
		return customSoundTextField_contents;
	}

	public String getReplacing()
	{
		return replacing_value;
	}
}