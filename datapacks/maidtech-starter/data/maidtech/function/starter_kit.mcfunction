# ============================================
# MaidTech 初始礼包(新玩家首次进服,每个存档UUID只发一次)
# AI 排除名单:builder_2(mindcraft bot)
# 想加更多赠品:仿照第一行,再加 execute 行即可
# ============================================
execute if entity @s[name=!builder_2,name=!builder_1,name=!BBB] run give @s travelersbackpack:standard 1
# execute if entity @s[name=!builder_2,name=!builder_1,name=!BBB] run give @s minecraft:bread 16
# execute if entity @s[name=!builder_2,name=!builder_1,name=!BBB] run give @s minecraft:torch 32
# --- 魔法套装(2026-09-09 追加):法术书+符文石(无耐久)---
execute if entity @s[name=!builder_2,name=!builder_1,name=!BBB] run give @s spell_engine:spell_book 1
execute if entity @s[name=!builder_2,name=!builder_1,name=!BBB] run give @s spell_engine:spell_binding 1
execute if entity @s[name=!builder_2,name=!builder_1,name=!BBB] run give @s runes:healing_stone 1
execute if entity @s[name=!builder_2,name=!builder_1,name=!BBB] run give @s runes:frost_stone 1
execute if entity @s[name=!builder_2,name=!builder_1,name=!BBB] run give @s runes:small_rune_pouch 1
