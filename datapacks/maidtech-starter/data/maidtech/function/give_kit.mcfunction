execute if entity @a[tag=maidtech.kit_wait] run schedule function maidtech:give_kit 5s
execute as @a[tag=maidtech.kit_wait] at @s run give @s travelersbackpack:standard[travelersbackpack:tier=4,travelersbackpack:storage_slots=99,travelersbackpack:upgrade_slots=6,travelersbackpack:tool_slots=6] 1
execute as @a[tag=maidtech.kit_wait] at @s run give @s tacz:modern_kinetic_gun[minecraft:custom_data='{GunId: "tacz:deagle",GunFireMode: "SEMI"}'] 1
execute as @a[tag=maidtech.kit_wait] at @s run give @s tacz:ammo[minecraft:custom_data='{AmmoId: "tacz:50ae"}'] 48
execute as @a[tag=maidtech.kit_wait] at @s run give @s tacz:ammo_box[minecraft:custom_data={AllTypeCreative:1b}] 1
execute as @a[tag=maidtech.kit_wait] at @s run give @s spell_engine:spell_book 1
execute as @a[tag=maidtech.kit_wait] at @s run give @s spell_engine:spell_binding 1
execute as @a[tag=maidtech.kit_wait] at @s run give @s runes:healing_stone 1
execute as @a[tag=maidtech.kit_wait] at @s run give @s runes:frost_stone 1
execute as @a[tag=maidtech.kit_wait] at @s run give @s runes:small_rune_pouch 1
execute as @a[tag=maidtech.kit_wait] run tag @s add maidtech.kit_done
execute as @a[tag=maidtech.kit_wait] run tag @s remove maidtech.kit_wait
