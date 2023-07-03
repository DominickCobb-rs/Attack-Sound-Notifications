# Attack Sound Notifications
**Have you ever needed audible confirmation for the following:**
* Special attack hit
* Special attack miss
* Auto attack miss
* Max hit
* BGS maxed
* DWH missed (AGAIN)

Well this plugin may be of use to you then. You have the option to toggle notification sounds in the event those things happen!

## Custom Audio
### CURRENT:
Default audio is packed into the repo. You can change the audio by 

1. Navigate to your .runelite folder (%userprofile%\.runelite\)
    a. You can right-click the screenshot button in the top right of the runelite client
    b. Then click open screenshot folder, and navigate to the .runelite directory
2. Create a folder called 'attacknotifications'
3. Add your sound .wav file in the folder

Acceptable File Names:
* max.wav
* miss.wav
* spec_miss.wav
* spec_hit.wav
* spec_max.wav
* arclight_spec_miss.wav
* arclight_spec_hit.wav
* dwh_spec_miss.wav
* dwh_spec_hit.wav
* dwh_spec_max.wav
* bgs_spec_miss.wav
* bgs_spec_hit.wav
* bgs_spec_max.wav
* bone_dagger_spec_miss.wav
* bone_dagger_spec_hit.wav
* bone_dagger_spec_default.wav

### FUTURE PLANS:
There will be a panel where you can choose the weapon, enter the audio file location on disc, and it'll play for when you want. The goal is something like the transmog plugin where you can choose a weapon and then choose its miss/hit/spec/max audio files if you so choose.

### Other
Ranged weapons have a different set of stuff to check for specs and their hits, so I'll probably have to look into that more too.

## Credit
Big shoutout to Ferraiic and their [hit-sounds](https://github.com/Hit-Sounds/hit-sounds) plugin. Used a **lot** of that code to make this work, and adapted some of the RuneLite Special Attack Counter plugin.