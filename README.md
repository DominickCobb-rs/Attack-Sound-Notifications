# Attack Sound Notifications
**Have you ever needed audible confirmation for the following:**
* Special attack hit
* Special attack miss
* Auto attack miss
* Max hit

Well this plugin may be of use to you then. You have the option to toggle notification sounds in the event those things happen!

Currently, max hit trumps all of them and will play the max hit sound. This is intended, but isn't unchangeable in the future.

## Custom Audio
Current plan is to have the audio files reside in a folder called 'attacknotifications' in the .runelite folder. Each should be statically named according to their item/hit.

Currently no custom audio files can be used. I haven't even uploaded them into this repo. Need to learn how to put them in the repo and get this in the plugin hub. The plan is to have subfolders in the attacknotifications folder and allow a random one to be chosen. If there's just one it'll play that.

## Credit
Big shoutout to Ferraiic and his [hit-sounds](https://github.com/Hit-Sounds/hit-sounds) plugin. Used a lot of his code to make this work, and adapted some of the RuneLite Special Attack Counter plugin.