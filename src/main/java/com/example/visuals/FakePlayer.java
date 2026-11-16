package com.example.visuals;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Method;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

/**
 * FakePlayer: тренировочный манекен для PvP.
 * Существует ТОЛЬКО на клиенте (сервер о нём ничего не знает, видите его только вы).
 * Стоит на месте, получает урон от вашего оружия (от оружия в руке, с учётом отката атаки, крита, Остроты и Силы).
 *  - Тотем ВКЛ: когда HP доходит до 0, тотем "срабатывает" (анимация и звук), HP снова 20. Убить нельзя.
 *  - Тотем ВЫКЛ: на 0 HP манекен умирает, и функция сама выключается.
 */
public final class FakePlayer {
    private FakePlayer() {}

    /** Включён ли манекен (в конфиг не сохраняется: после перезапуска игры выключен). */
    public static boolean enabled = false;
    /** Бесконечный тотем в руке манекена. */
    public static boolean totem = false;

    /** Расстояние появления перед игроком, в блоках. */
    private static final double DIST = 2.5;
    /** Отрицательный id: не пересечётся с id сущностей сервера. */
    private static final int DUMMY_ID = -770011;
    /** Сколько тиков показываем падение после смерти, потом функция выключается. */
    private static final int DEATH_TICKS = 30;

    private static OtherClientPlayerEntity dummy;
    private static ClientWorld dummyWorld;
    private static boolean respawn = false;
    private static float pendingDamage = 0f;
    private static int dying = 0;
    /** Пока > 0, миксин NoOverlay не прячет частицы тотема (иначе поп манекена не видно). */
    public static int popTicks = 0;
    /** Сколько тиков рука манекена пустая после попа, потом появляется новый тотем. */
    private static int popGap = 0;

    /** Вызвать один раз в VisualsClient.onInitializeClient() после Combat.register(). */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(FakePlayer::tick);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> forget());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            enabled = false;
            forget();
        });

        // Слушатель в ПОЗДНЕЙ фазе: Combat и остальные получают событие удара раньше нас.
        Identifier late = Identifier.of("visualsmod", "fakeplayer");
        AttackEntityCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, late);
        AttackEntityCallback.EVENT.register(late, (player, world, hand, entity, hit) -> {
            if (dummy == null || entity != dummy) return ActionResult.PASS;

            ClientPlayerEntity me = MinecraftClient.getInstance().player;
            if (me != null && player == me && dying == 0) {
                // --- урон, как в ваниле: атрибут оружия * откат, крит x1.5 ---
                float c = me.getAttackCooldownProgress(0.5f);
                String itemId = Registries.ITEM.getId(me.getMainHandStack().getItem()).getPath();
                double table = itemDamage(itemId);
                float dmg = table >= 0
                        ? (float) (table + effectBonus(me))   // меч/топор/кирка/... + Сила/Слабость
                        : (float) me.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);   // прочие предметы и кулак
                if (dmg < 0f) dmg = 0f;
                dmg *= 0.2f + c * c * 0.8f;
                float bonus = sharpnessBonus(me) * c;   // зачарование масштабируется откатом, крит его не усиливает
                boolean crit = c > 0.9f && me.fallDistance > 0 && !me.isOnGround() && !me.isClimbing()
                        && !me.isTouchingWater() && !me.hasStatusEffect(StatusEffects.BLINDNESS)
                        && !me.hasVehicle() && !me.isSprinting();
                if (crit) dmg *= 1.5f;
                pendingDamage = dmg + bonus;

                // сильный удар в спринте сбрасывает спринт и тормозит игрока
                if (c > 0.9f && me.isSprinting()) {
                    me.setSprinting(false);
                    me.setVelocity(me.getVelocity().multiply(0.6, 1.0, 0.6));
                }
                resetCooldown(me);   // откат атаки сбрасывается, как при настоящем ударе
            }
            // FAIL = пакет атаки на сервер НЕ отправляется (манекена там нет)
            return ActionResult.FAIL;
        });
    }

    private static final String[] MATS = {"wooden", "golden", "stone", "copper", "iron", "diamond", "netherite"};
    private static final double[] SWORD = {4, 4, 5, 5, 6, 7, 8};
    private static final double[] AXE = {7, 7, 9, 9, 9, 9, 10};
    private static final double[] PICK = {2, 2, 3, 3, 4, 5, 6};
    private static final double[] SHOVEL = {2.5, 2.5, 3.5, 3.5, 4.5, 5.5, 6.5};

    /** Полный урон оружия по таблице (как в ваниле, включая базовую единицу), -1 если предмет не в таблице. */
    private static double itemDamage(String id) {
        if (id.equals("mace")) return 6.0;
        if (id.equals("trident")) return 9.0;
        int us = id.lastIndexOf('_');
        if (us < 0) return -1;
        String mat = id.substring(0, us);
        String kind = id.substring(us + 1);
        int m = -1;
        for (int i = 0; i < MATS.length; i++) if (MATS[i].equals(mat)) m = i;
        if (m < 0) return -1;
        switch (kind) {
            case "sword": return SWORD[m];
            case "axe": return AXE[m];
            case "pickaxe": return PICK[m];
            case "shovel": return SHOVEL[m];
            case "hoe": return 1.0;
            default: return -1;
        }
    }

    /** Сила: +3 урона за уровень, Слабость: -4 за уровень. */
    private static float effectBonus(ClientPlayerEntity me) {
        float b = 0f;
        StatusEffectInstance str = me.getStatusEffect(StatusEffects.STRENGTH);
        if (str != null) b += 3.0f * (str.getAmplifier() + 1);
        StatusEffectInstance weak = me.getStatusEffect(StatusEffects.WEAKNESS);
        if (weak != null) b -= 4.0f * (weak.getAmplifier() + 1);
        return b;
    }

    private static Method resetMethod;
    private static boolean resetSearched = false;

    /** Сброс отката атаки. Имя метода менялось между версиями, поэтому ищем его на лету. */
    private static void resetCooldown(ClientPlayerEntity me) {
        if (!resetSearched) {
            resetSearched = true;
            resetMethod = findNoArg(me.getClass(), "method_7350", "resetLastAttackedTicks",
                    "resetAttackCooldown", "resetAttackStrengthTicker");
        }
        try {
            if (resetMethod != null) resetMethod.invoke(me);
        } catch (Exception ignored) {
        }
    }

    private static Method findNoArg(Class<?> c, String... names) {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            for (String n : names) {
                try {
                    Method m = k.getDeclaredMethod(n);
                    m.setAccessible(true);
                    return m;
                } catch (NoSuchMethodException ignored) {
                }
            }
        }
        return null;
    }

    /** Бонус Остроты на оружии в основной руке: 0.5 * уровень + 0.5 (как в ваниле). */
    private static float sharpnessBonus(ClientPlayerEntity me) {
        try {
            ClientWorld w = MinecraftClient.getInstance().world;
            if (w == null) return 0f;
            RegistryEntry<Enchantment> sharp = w.getRegistryManager()
                    .getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS);
            int lvl = EnchantmentHelper.getLevel(sharp, me.getMainHandStack());
            return lvl > 0 ? 0.5f * lvl + 0.5f : 0f;
        } catch (Exception e) {
            return 0f;
        }
    }

    /** Кнопка "Поставить передо мной" (заодно оживляет и лечит манекена). */
    public static void respawnInFront() {
        respawn = true;
    }

    private static void forget() {
        dummy = null;
        dummyWorld = null;
        pendingDamage = 0f;
        dying = 0;
        popGap = 0;
        respawn = false;
    }

    private static void remove() {
        if (dummy != null) dummy.discard();
        dummy = null;
        dummyWorld = null;
        pendingDamage = 0f;
        dying = 0;
        popGap = 0;
    }

    private static void tick(MinecraftClient mc) {
        if (popTicks > 0) popTicks--;
        ClientWorld world = mc.world;
        ClientPlayerEntity me = mc.player;
        if (world == null || me == null) {
            forget();
            return;
        }

        // другой мир (портал в другое измерение) или сущность убрали снаружи
        if (dummy != null && (dummyWorld != world || dummy.isRemoved())) {
            dummy = null;
            dummyWorld = null;
            pendingDamage = 0f;
            dying = 0;
            popGap = 0;
        }

        if (!enabled) {
            remove();
            respawn = false;
            return;
        }

        if (respawn) {
            remove();
            respawn = false;
        }
        if (dummy == null) spawn(world, me);
        if (dummy == null) return;

        // смерть: даём доиграть анимацию падения, потом выключаем функцию
        if (dying > 0) {
            if (++dying > DEATH_TICKS) {
                enabled = false;
                remove();
            }
            return;
        }

        // тотем в руке (в левой, как в PvP)
        if (popGap > 0) {
            popGap--;   // рука пустая: старый тотем "сгорел", скоро появится новый
        } else {
            boolean hasTotem = dummy.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
            if (hasTotem != totem) {
                dummy.equipStack(EquipmentSlot.OFFHAND, totem ? new ItemStack(Items.TOTEM_OF_UNDYING) : ItemStack.EMPTY);
            }
        }

        // урон от удара
        if (pendingDamage > 0f) {
            float dmg = pendingDamage;
            pendingDamage = 0f;
            applyHit(mc, dmg);
        }
    }

    private static void applyHit(MinecraftClient mc, float dmg) {
        if (dummy.hurtTime > 0) return;   // i-frames: пока hurtTime > 0, новый удар не засчитывается
        float hp = dummy.getHealth() - dmg;
        dummy.hurtTime = 10;              // красная вспышка; Combat/HitSound/TargetHUD увидят попадание

        if (hp > 0f) {
            dummy.setHealth(hp);
            Combat.playSound(mc, "entity.player.hurt", 1.0f, 1.0f);
        } else if (totem) {
            // бесконечный тотем: "сработал", HP снова полное, потом в руке появляется новый
            dummy.setHealth(dummy.getMaxHealth());
            dummy.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);   // тотем "сгорел"
            popGap = 12;                                                // через 12 тиков появится новый
            popTicks = 35;                                              // разрешаем частицы тотема
            mc.particleManager.addEmitter(dummy, ParticleTypes.TOTEM_OF_UNDYING, 30);
            Combat.playSound(mc, "item.totem.use", 1.0f, 1.0f);
        } else {
            dummy.setHealth(0f);             // смерть (падение); Combat засчитает килл
            Combat.playSound(mc, "entity.player.death", 1.0f, 1.0f);
            dying = 1;
        }
    }

    private static void spawn(ClientWorld world, ClientPlayerEntity me) {
        float yaw = me.getYaw();
        double rad = Math.toRadians(yaw);
        double x = me.getX() - Math.sin(rad) * DIST;
        double z = me.getZ() + Math.cos(rad) * DIST;

        OtherClientPlayerEntity e = new OtherClientPlayerEntity(world, new GameProfile(UUID.randomUUID(), "Dummy"));
        e.setId(DUMMY_ID);
        e.setPosition(x, me.getY(), z);
        float face = yaw + 180.0f;   // лицом к игроку
        e.setYaw(face);
        e.setPitch(0.0f);
        e.setHeadYaw(face);
        e.setBodyYaw(face);
        e.resetPosition();           // без этого первый кадр "летит" из точки (0,0,0)
        world.addEntity(e);

        dummy = e;
        dummyWorld = world;
    }
}
