# MaidTech 进服礼包 v4(指令发放版)
# 用法:/execute as <玩家名> run function maidtech:kit
# 名单由执行者控制,无自动发放;AI 不会获得
# --- 背包:下界合金级(99储物格 + 6升级槽 + 6工具槽) ---
give @s travelersbackpack:standard[travelersbackpack:tier=4,travelersbackpack:storage_slots=99,travelersbackpack:upgrade_slots=6,travelersbackpack:tool_slots=6] 1
# --- 裸枪:沙漠之鹰(半自动)+ .50AE 弹 48 发 ---
give @s tacz:modern_kinetic_gun[minecraft:custom_data='{GunId: "tacz:deagle",GunFireMode: "SEMI"}'] 1
give @s tacz:ammo[minecraft:custom_data='{AmmoId: "tacz:50ae"}'] 48
# --- 全类型创造弹药盒(对任意弹药右键即无限补给) ---
give @s tacz:ammo_box[minecraft:custom_data={AllTypeCreative:1b}] 1
# --- 魔法套装(无耐久)---
give @s spell_engine:spell_book 1
give @s spell_engine:spell_binding 1
give @s runes:healing_stone 1
give @s runes:frost_stone 1
give @s runes:small_rune_pouch 1
