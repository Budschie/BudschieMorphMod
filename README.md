# Budschie's Morph Mod
This is a mod whose goal it is to recreate the experience of the more or less discontinued MetaMorphMod in the Minecraft version 1.17.1.
## What can this mod do?
This mod has several features, such as following:

* It implements a system where you can kill a mob and get its "soul", so-to-speak. You can collect that soul by just walking into it. Then, you can morph into that mob by pressing a key on your keyboard (just look in the config and search for "Budschie's Morph Mod Controls", you can find all controls there).
* The mod has abilities for vanilla mobs: You can fly as a bat, you can shoot fire balls as a blaze, you can teleport as an enderman, etc. etc.

* You can turn in any player that you've killed and whose soul you've collected.

* There is a gamerule for keeping the morph inventory on death. It is set to true as a default value, but you can turn it off if you wish so.

* If you are an admin on a server, you can blacklist certain mobs with "*/morphblacklist add &#60;morph to blacklist&#62;*". There are also a couple of other commands, for example */morph &#60;entity to morph to&#62;*, */playermorph &#60;player name to morph to&#62;* and */demorph*. All of this commands require a permission level of 2 (the default op permission level), as they all ignore the blacklist.

* You can fully customize which abilities belong to which morphs via data packs. **A tutorial for this can be found below.**

## Tutorial for data pack creators
As already said above, you can take advantage of the data pack system that minecraft provides and you can ship abilities for entities
in said data packs. Note: This guide is inspired by the more indepth-tutorial on the Minecraft Wiki: https://minecraft.fandom.com/wiki/Tutorials/Creating_a_data_pack. I'd recommend you checking it out if you have any problems.

**First step**: You need to create a folder anywhere (for example, you could create this folder on your desktop).
Then, you need to go inside this folder and create a file named "pack.mcmeta" (it is important that the file extension is .mcmeta. Make sure that
Windows displays file extensions, otherwise this will not work.) Then edit this file, and set this as a content:

```json
{
  "pack": {
    "pack_format": 7,
    "description": "Tutorial Data Pack"
  }
}
```

*Note: You can replace "Tutorial Data Pack" with your data pack name.*

**Second step**: In the folder of your future data pack, create the folders "*data*", followed by a folder named "*tutorial_datapack*" (this of course is just an example, you can put any name in there), create yet another folder named "*morph_abilities*". Now you have the folder structure
"*data/tutorial_datapack/morph_abilities*". After checking that, you can now finally proceed to the next, and most fun part.

**Third step**: You can now create an unlimited amount of .json files here (the name doesn't really matter), and you can set this as the content of those json files:

```json
{
    "entity_type": "<insert mob id, an example would be minecraft:horse>",
    "grant":
    [
        "<put your first ability here>",
        "<second ability goes here>",
        "<put your third ability here>",
        "<you can add as many abilities as you like to this array>"
    ],
    "revoke":
    [
        "<put the ability ids that you want to remove here>"
    ]
}
```

A concrete example would be:

```json
{
    "entity_type": "minecraft:iron_golem",
    "grant":
    [
        "bmorph:mob_attack",
        "bmorph:yeet",
        "bmorph:slowness",
        "bmorph:no_knockback",
        "bmorph:more_damage"
    ],
    "revoke":
    [

    ]
}
```

Now, let's explain what's going on in the file above:

* First, you have this JSON-string called "entity_type". It defines to which entity you assing these abilities. In this example, we are adding the abilities for the Iron Golem.
* Now comes the JSON-array called "grant". In here, you can put a list of id's of abilities. I've chosen the abilities "*bmorph:mob_attack*" (which means that mobs will attack you if you are morphed), "*bmorph:yeet*" ("yeets" non-player enemies into the air), "*bmorph:slowness*"
 (which reduces your speed), "*bmorph:no_knockback*" (which removes knockback for you if you are morphed) and, last but not least, "*bmorph:more_damage*" (which simply increases your damage).
* Now, we have yet another JSON-array called "revoke". You can revoke any abilities that were previously set with this JSON-array. If for example, I were to give dragons the ability to shoot webs, you could disallow this by putting this ability id in this JSON-array. So, it is just like "grant", but does the opposite.

Then, you can copy your data pack folder into the datapacks directory of your minecraft world. You can refer to this article on the Minecraft Wiki for more help: https://minecraft.fandom.com/wiki/Tutorials/Installing_a_data_pack.

If something doesn't work, look at the logs. If you have malformed JSON, or either your provided ability or your provided entity is not known, you should be informed in the logs by my mod.

Here is a list of all sample abilities (Is active ability => if you have to press a button for the ability to work):

**OUTDATED!**

| **Ability** | **Description** | **Ability ID** | **Is active ability?** |
|---|---|---|---|
| *BOOM!* | The name explains it all. This ability allows you to explode. | bmorph:boom | Yes |
| Climbing | This ability allows you to climb like spider man. | bmorph:climbing | No |
| Cookie very yummy | Cookies are very yummy. Until you eat them. *Do not try this at home!* | bmorph:cookie_death | No |
| Eat Regeneration | Regenerates 4 HP when eating any food instantly. | bmorph:eat_regen | No |
| Egg Throwing | Some morphs have super powers, for example flying like batman and climbing like spiderman. And you... can throw eggs. | bmorph:egg_yeet | Yes |
| Enderman Teleportation | This ability allows you to teleport to a block that you are looking at in a radius of roughly 50 blocks. This ability has a cooldown of 40 ticks (2 seconds). | bmorph:enderman_teleport | Yes |
| *Extreme* Swiftness! | With this ability, your speed greatly increases | bmorph:extreme_swiftness | No |
| Blaze Fire | As the name suggests, this ability allows you to shoot blaze fire balls. | bmorph:fire_blaze | Yes |
| Ghast Fire | With this ability you can fire ghast fire balls. How original... | bmorph:fire_ghast | Yes |
| Fish Yeet | The name says it all. Good look trying to find the one mob in the game using this ability. JK you can't. | bmorph:fish_yeet | Yes |
| Flying | ***Flying is not enabled on this server.*** | bmorph:flying | No |
| Insta Climb | This ability allows you to instantly get on top of a block if you walk against it (much like if you were to walk on stairs). | bmorph:insta_jump | No |
| Jump Boost | This ability grants you a jump boost. | bmorph:jumpboost | No |
| Llama Spit™ | You ever wanted to be a llama, eating grass, existing and *spitting on people*? Well, now you can with this ability. | bmorph:llama_spit | Yes |
| Mob Attack | This is probably the most confusing ability: If a morph has this ability, it will be attacked by any nearby hostile mob, like if you were unmorphed. If your morph doesn't have this ability however, you won't get attacked. So, to summarize: Having this ability => Getting attacked Not having this ability => NOT getting attacked  So, it is essentially just like a  backwards ability really. | bmorph:mob_attack | No |
| More Damage | This ability allows you to deal a total of 3 HP more of damage. | bmorph:more_damage | No |
| Nause on Hit | Title says it all really. | bmorph:nausea_on_hit | No |
| Night Vision | https://minecraft.fandom.com/wiki/Night_Vision | bmorph:night_vision | No |
| No Fall Damage | No Fall Damage. | bmorph:no_fall_damage | No |
| No Fire Damage. | No Fire Damage. | bmorph:no_fire_damage | No |
| No Knockback. | ... | bmorph:no_knockback | No |
| Poison on hit | plz read the name of this ability. | bmorph:poison_on_hit | No |
| Slowfall | This ability grants you &#60;insert effect&#62;. | bmorph:slowfall | No |
| Slowness | This ability grants you &#60;insert another effect&#62;. | bmorph:slowness | No |
| Squid Boost | This is an exciting ability. The Squid Boost is a ability that helps you to immerse yourself in the daily life of a squid. It allows you to boost yourself into the direction you are looking, whilst at the same time applies a blindness effect to any nearby player. | bmorph:squid_boost | Yes |
| Swiftness | Like the Extreme Swiftness effect, but worse. | bmorph:swiftness | No |
| Water Breathing | Simply Water Breathing. | bmorph:water_breathing | No |
| Water Dislike | This ability makes sure that Endermen will be damaged by water. | bmorph:water_dislike | No |
| Water Sanic | If you have this ability, you will be granted the extreme power of Water Sanic. | bmorph:water_sanic | No |
| Web Passthrough | The name might suggest that this could be used for easily navigating through webs, but in reality, this is just a buggy mess. | bmorph:web_passthrough | No |
| Web Yeet | You can use this ability to farm cobweb/strings. As the name suggets, you can yeet webs with this ability. But beware, as this might expose you to an extreme level of spider ASMR. | bmorph:web_yeet | Yes |
| Wither on Hit | Yet another self-explanatory effect-on-hit thingy. | bmorph:wither_on_hit | No |
| ***YEET!*** | Last but not least, we have the ***YEET!***-Ability. It allows you to yeet mobs into the air when attacking them. Sadly, yeeting players is not supported yet ;( | bmorph:yeet | No |

So, that's it. Hopefully you've enjoyed this guide and could create a cool new data pack with abilities.
If there are some issues on creating this data pack, feel free to post them as an issue on my Github page.

## Special thanks
* Everyone that have participated in this project by contributing ideas or reporting issues.

* The forge discord for helping me out when I had a programming issue.

* The metamorph mod (https://www.curseforge.com/minecraft/mc-mods/metamorph) for inspiring me to create this mod.

* My friends who playtested this mod.

































