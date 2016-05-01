# UtAdmin

A modern, web-based, server management tool for [UrbanTerror 4.2](http://www.urbanterror.info/home/) servers using [B3-Bot](http://forum.bigbrotherbot.net/index.php).

UtAdmin is written in Scala using the Play! Framework.

###Functionality
* Status Page with dynamic chatlog and online player list, talkback functionality, and the use of admin commands (Force Team, Kick, Ban, Slap, ...) from the website
* Detailed user page with aliases, online history, ip addresses, chatlog, penalties, geo-location map, ...
* Punish players or remove existing punishments
* Create multiple admin accounts with 3 different user levels
* Search for players, See all bans, special view for clan members and admins
* ... 

###Installation
Installation is quite simple, you just need Java/Scala, a MongoDB and the PlayFramework installed on your machine. And the gameserver must have a b3-bot running.

1. Install Java 8 and Scala
2. Install the [Play Framework](https://www.playframework.com/) and export the path of the activator binary
3. Install a MongoDB (default config will do)
4. `git clone` this repo
5. Configure your Rcon Password, game server ip, ... in `UtAdmin/app/controllers/UtServer.scala`
6. Start the UtAdmin Webserver with `activator run 80` in the UtAdmin Folder, this may take a few minutes at the first time. Default Login is `user:admin` `password:Administrator` - you should change that after you login for the first time ;)
7. ????
8. Profit.


### TODO - Limitations
* Right now only one game server is supported
