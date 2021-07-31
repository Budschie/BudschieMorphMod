# Budschie's Morph Mod
This is a mod whose goal it is to recreate the experience of the more or less discontinued MetaMorphMod in the Minecraft version 1.16.5.
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

Here is a list of all sample abilities, an explenation of what they do will be added later (probably):

bmorph:boom; bmorph:climbing; bmorph:cookie_death; bmorph:eat_regen; bmorph:egg_yeet; bmorph:enderman_teleport; bmorph:extreme_swiftness; bmorph:fire_blaze; bmorph:fire_ghast; bmorph:fish_yeet; bmorph:flying; bmorph:insta_jump; bmorph:jumpboost; bmorph:llama_spit; bmorph:mob_attack; bmorph:more_damage; bmorph:nausea_on_hit; bmorph:night_vision; bmorph:no_fall_damage; bmorph:no_fire_damage; bmorph:no_knockback; bmorph:poison_on_hit; bmorph:slowfall; bmorph:slowness; bmorph:squid_boost; bmorph:swiftness; bmorph:water_breathing; bmorph:water_dislike; bmorph:water_sanic; bmorph:web_passthrough; bmorph:web_yeet; bmorph:wither_on_hit; bmorph:yeet;

So, that's it. Hopefully you've enjoyed this guide and could create a cool new data pack with abilities.
If there are some issues on creating this data pack, feel free to post them as an issue on my Github page.

## Special thanks

* The forge discord for helping me out when I had a programming issue.

* The metamorph mod (https://www.curseforge.com/minecraft/mc-mods/metamorph) for inspiring me to create this mod.

* My friends who playtested this mod.

































