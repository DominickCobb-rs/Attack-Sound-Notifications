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
    SPEC_MISS("Miss", AttackSoundNotificationsPlugin.SPEC_MISS_FILE),
    SPEC_HIT("Hit", AttackSoundNotificationsPlugin.SPEC_HIT_FILE),
    SPEC_MAX("Hit", AttackSoundNotificationsPlugin.SPEC_MAX_FILE),
    ARCLIGHT_MISS("Miss", AttackSoundNotificationsPlugin.ARCLIGHT_MISS_FILE),
    ARCLIGHT_HIT("Normal", AttackSoundNotificationsPlugin.ARCLIGHT_HIT_FILE),
    DWH_MISS("Miss", AttackSoundNotificationsPlugin.DWH_MISS_FILE),
    DWH_HIT("Max", AttackSoundNotificationsPlugin.DWH_HIT_FILE),
    DWH_MAX("Normal", AttackSoundNotificationsPlugin.DWH_MAX_FILE),
    BGS_MISS("Miss", AttackSoundNotificationsPlugin.BGS_MISS_FILE),
    BGS_HIT("Normal", AttackSoundNotificationsPlugin.BGS_HIT_FILE),
    BGS_MAX("Max", AttackSoundNotificationsPlugin.BGS_MAX_FILE),
    BONE_DAGGER_MISS("Miss", AttackSoundNotificationsPlugin.BONE_DAGGER_MISS_FILE),
    BONE_DAGGER_HIT("Normal", AttackSoundNotificationsPlugin.BONE_DAGGER_HIT_FILE),
    BONE_DAGGER_MAX("Max", AttackSoundNotificationsPlugin.BONE_DAGGER_MAX_FILE);

    private final String hittype;
    private final File file;
}