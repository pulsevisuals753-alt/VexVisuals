package com.vexvisuals;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class ModuleManager {
    public static final List<Module> ALL = new ArrayList<>();

    public static Module FPS, COORDS, DIRECTION, PING, CPS, SPEED, SESSION, KEYS, ARMOR;
    public static Module FULLBRIGHT, ESP, SPRINT;

    private ModuleManager() {}

    private static Module add(Module m) {
        ALL.add(m);
        return m;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("vexvisuals.properties");
    }

    public static void init() {
        FPS = add(new Module("fps", "FPS", "Кадры в секунду", Category.HUD, true, true));
        COORDS = add(new Module("coords", "Координаты", "Твои X, Y, Z", Category.HUD, true, true));
        DIRECTION = add(new Module("direction", "Направление", "Куда ты смотришь", Category.HUD, false, true));
        PING = add(new Module("ping", "Пинг", "Задержка до сервера", Category.HUD, false, true));
        CPS = add(new Module("cps", "CPS", "Клики в секунду (ЛКМ | ПКМ)", Category.HUD, false, true));
        SPEED = add(new Module("speed", "Скорость", "Скорость движения в блоках в секунду", Category.HUD, false, true));
        SESSION = add(new Module("session", "Сессия", "Сколько времени ты в игре", Category.HUD, false, true));
        KEYS = add(new Module("keys", "Клавиши", "WASD и кнопки мыши на экране", Category.HUD, false, true));
        ARMOR = add(new Module("armor", "Броня", "Броня и прочность на экране", Category.HUD, false, true));

        FULLBRIGHT = add(new Fullbright());
        ESP = add(new Module("esp", "Контур игроков", "Подсвечивает игроков сквозь стены", Category.WORLD, false, false));

        SPRINT = add(new AutoSprint());
        load();
    }

    public static void tick(MinecraftClient mc) {
        for (Module m : ALL) {
            if (m.enabled) m.onTick(mc);
        }
    }

    public static void load() {
        Path path = file();
        if (!Files.exists(path)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        for (Module m : ALL) {
            try {
                String en = p.getProperty(m.id + ".enabled");
                if (en != null) m.enabled = Boolean.parseBoolean(en);
                String col = p.getProperty(m.id + ".color");
                if (col != null) m.color = Integer.parseInt(col);
                String sc = p.getProperty(m.id + ".scale");
                if (sc != null) m.scale = Float.parseFloat(sc);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : ALL) {
            p.setProperty(m.id + ".enabled", Boolean.toString(m.enabled));
            p.setProperty(m.id + ".color", Integer.toString(m.color));
            p.setProperty(m.id + ".scale", Float.toString(m.scale));
        }
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "VexVisuals");
        } catch (IOException ignored) {
        }
    }

    /** Fullbright: client-side night vision, no potion icon. */
    static class Fullbright extends Module {
        Fullbright() {
            super("fullbright", "Яркость", "Полная яркость, как с ночным зрением", Category.WORLD, false, false);
        }

        @Override
        public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null) return;
            StatusEffectInstance cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (cur == null || cur.getDuration() < 300) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
            }
        }

        @Override
        public void onDisable() {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    /** Auto sprint while holding forward. */
    static class AutoSprint extends Module {
        AutoSprint() {
            super("sprint", "Авто-спринт", "Бежишь сам, пока идёшь вперёд", Category.PLAYER, false, false);
        }

        @Override
        public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null || mc.currentScreen != null) return;
            if (mc.options.forwardKey.isPressed() && !p.isSneaking() && !p.horizontalCollision
                    && !p.isUsingItem() && p.getHungerManager().getFoodLevel() > 6) {
                p.setSprinting(true);
            }
        }
    }
  }
