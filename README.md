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
Default audio is packed into the repo for the 5 times audio can be played.

To use custom audio:
1. Create a .wav file
2. Open the plugin panel
3. Click the second dropdown and select "Custom Audio" and select the file by either:
    * Enter the full filepath into the empty text box
    * Click the folder and select the file on your computer
    * **IMPORTANT** THE FILE MUST BE A .WAV FILE
4. Test the sound with the button
    * If the sound can't be found, a windows error sound will play

### FUTURE PLANS:
1. There will be a panel where you can choose the weapon, enter the audio file location on disc, and it'll play for when you want. The goal is something like the transmog plugin where you can choose a weapon and then choose its miss/hit/spec/max audio files if you so choose.
2. I need to work out some logic issues in the way sound choices happen. It's currently a tangled spaghetti mess of what plays and when. When everything is on, it works as intended. If you toggle something off, there's really no telling exactly what it'll do from an outside perspective.

### Other
Ranged weapons have a different set of stuff to check for specs and their hits, so I'll probably have to look into that more too.

## Credit
Big shoutout to Ferraiic and their [hit-sounds](https://github.com/Hit-Sounds/hit-sounds) plugin. Used a **lot** of that code to make this work, and adapted some of the RuneLite Special Attack Counter plugin.