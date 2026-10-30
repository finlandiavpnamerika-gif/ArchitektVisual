package com.example.visuals;

import com.example.visuals.mixin.MinecraftClientAccessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Оффлайн альт-менеджер: хранит только ники и подменяет Session клиента.
 * Работает в одиночке, по LAN и на серверах с online-mode=false.
 */
public final class AltManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("visualsmod/alts");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern NAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualsmod_alts.json");

    static class Data {
        List<String> accounts = new ArrayList<>();
    }

    private static Data data = new Data();

    private AltManager() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        try {
            Data loaded = GSON.fromJson(Files.readString(FILE), Data.class);
            if (loaded != null && loaded.accounts != null) data = loaded;
        } catch (Exception e) {
            LOGGER.error("Failed to load alts", e);
        }
    }

    private static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(data));
        } catch (Exception e) {
            LOGGER.error("Failed to save alts", e);
        }
    }

    public static boolean isValid(String name) {
        return name != null && NAME.matcher(name).matches();
    }

    public static List<String> getAccounts() {
        return Collections.unmodifiableList(data.accounts);
    }

    /** false, если ник некорректный или уже есть в списке */
    public static boolean add(String name) {
        if (!isValid(name)) return false;
        for (String existing : data.accounts) {
            if (existing.equalsIgnoreCase(name)) return false;
        }
        data.accounts.add(name);
        save();
        return true;
    }

    public static void remove(String name) {
        if (data.accounts.remove(name)) save();
    }

    /** Тот же алгоритм UUID, что использует ванильный оффлайн-режим. */
    public static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static String currentName() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }

    /**
     * Подменяет Session клиента. Ник применится при следующем заходе на сервер / в мир.
     *
     * @return true, если сессия реально сменилась на {@code name}
     */
    public static boolean use(String name) {
        if (!isValid(name)) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!(mc instanceof MinecraftClientAccessor accessor)) {
            LOGGER.error("MinecraftClientAccessor mixin is not applied - is it listed in visualsmod.mixins.json?");
            return false;
        }
        try {
            accessor.setSession(new Session(name, offlineUuid(name), "0", Optional.empty(), Optional.empty()));
        } catch (Exception e) {
            LOGGER.error("Failed to switch session", e);
            return false;
        }
        boolean ok = name.equals(currentName());
        if (ok) LOGGER.info("Switched to offline account {}", name);
        else LOGGER.error("Session was set, but current name is still {}", currentName());
        return ok;
    }
}
