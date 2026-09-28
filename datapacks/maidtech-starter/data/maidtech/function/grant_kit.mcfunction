execute if entity @s[name=!builder_1,name=!builder_2,name=!BBB,name=!mindcraft1,name=!mindcraft2,name=!AI-player1,name=!AI-player2] unless entity @s[tag=maidtech.kit_done] unless entity @s[tag=maidtech.kit_wait] run tag @s add maidtech.kit_wait
execute if entity @s[tag=maidtech.kit_wait] run schedule function maidtech:give_kit 5s
