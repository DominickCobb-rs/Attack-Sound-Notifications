package com.AttackSoundNotifications.enums;

import com.AttackSoundNotifications.AttackSoundNotificationsPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;

@Getter
@RequiredArgsConstructor
public enum HitSoundEnum {
    MAX("Max", AttackSoundNotificationsPlugin.MAX_HIT_FILE),
    DEFAULT_MISS("Miss", AttackSoundNotificationsPlugin.DEFAULT_MISS_FILE),
    ARCLIGHT_MISS("Miss", AttackSoundNotificationsPlugin.ARCLIGHT_MISS_FILE),
    ARCLIGHT_HIT("Normal", AttackSoundNotificationsPlugin.ARCLIGHT_HIT_FILE),
    DWH_MISS("Miss", AttackSoundNotificationsPlugin.DWH_MISS_FILE),
    DWH_HIT("Normal", AttackSoundNotificationsPlugin.DWH_HIT_FILE),
    BGS_MISS("Miss", AttackSoundNotificationsPlugin.BGS_MISS_FILE),
    BGS_HIT("Normal", AttackSoundNotificationsPlugin.BGS_HIT_FILE);

    private final String hittype;
    private final File file;
}