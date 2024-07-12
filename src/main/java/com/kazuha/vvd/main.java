package com.kazuha.vvd;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class main extends JavaPlugin implements Listener {
    public JsonObject objectfinal = null;
    private ViaAPI api;
    List<String> nan;
    FileConfiguration configuration;
    HashMap<Integer, String> versionmap = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Gson gson = new Gson();
        saveResource("version.json", false);
        try {
            objectfinal = gson.fromJson(new FileReader(new File(getDataFolder(), "version.json")), JsonObject.class);
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                JsonObject obj = objectfinal.getAsJsonObject("map");
                obj.entrySet().forEach(entry -> {
                     getLogger().info("[MAPPING]" + entry.getKey() +" -> " + entry.getValue().getAsString());
                    versionmap.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsString());
                });
                getLogger().info("[MAPPING] 本地version.json读取成功。");
            });

        } catch (IOException e) {
            throw new RuntimeException("FAILED TO READ version.json From The Folder.认真的？你把他删了？");
        }
        configuration = getConfig();
        initVersion();

        Bukkit.getPluginManager().registerEvents(this, this);
        api = Via.getAPI();
    }




    public String http(String httpKey) throws Exception {
        URL url = new URL(httpKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(true);
        connection.setDoOutput(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String input;
        StringBuilder builder = new StringBuilder();
        while ((input = br.readLine()) != null) {
            builder.append(input);
        }
        br.close();
        return builder.toString();
    }

    public void initVersion() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                JsonObject object = new Gson().fromJson(http(getConfig().getString("update-url")), JsonObject.class);
                getLogger().info("云端version.json获取成功:");
                getLogger().info("最后更新:" + object.get("last-update").getAsString());
                getLogger().info("最后支持MC版本:" + object.get("latest-mcversion").getAsString());

                if (api.getServerVersion().highestSupportedProtocolVersion().getVersion() > object.get("version").getAsInt()) {
                    getLogger().warning("[警告] 云端version.json已过期。部分玩家可能出现版本无法识别的问题。");
                }
                JsonObject obj = object.getAsJsonObject("map");
                obj.entrySet().forEach(entry -> {
                    versionmap.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsString());
                    getLogger().info("[MAPPING]" + entry.getKey() +" -> " + entry.getValue().getAsString());
                });
                getLogger().info("[MAPPING] 分析成功。");
                if (object.get("version").getAsInt() > objectfinal.get("version").getAsInt()) {
                    getLogger().info("[E-ASYNC-UPDATER] 检查到更新，正在保存version.json");
                    File file = new File(getDataFolder(), "version.json");
                    FileOutputStream stream = new FileOutputStream(file);
                    stream.write(new Gson().toJson(object).getBytes(StandardCharsets.UTF_8));
                    stream.close();
                }

            } catch (Exception e) {
                getLogger().info("无法从云端获取各版本信息。将使用本地文件");
            }
        }
        );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this,()->{
                    if (versionmap.isEmpty()) return;
        if (api.getServerVersion().lowestSupportedProtocolVersion().getOriginalVersion() == api.getPlayerProtocolVersion(e.getPlayer().getUniqueId()).getOriginalVersion()) {
            return;
        }
        if(!(configuration.contains("player-version-higher") && configuration.contains("player-version-lower")))return;
        if (api.getPlayerVersion(e.getPlayer().getUniqueId()) > api.getServerVersion().highestSupportedProtocolVersion().getOriginalVersion()) {
            nan = configuration.getStringList("player-version-higher");
        } else {
            nan = configuration.getStringList("player-version-lower");
        }
        for (String ec : nan) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Trans(ec, e.getPlayer())));
        }
        },getConfig().getLong("send-delay"));
    }

    public String Trans(String Raw, Player p) {
        Raw = Raw.replace("%ver%", versionmap.get(api.getPlayerVersion(p.getUniqueId())));
        Raw = Raw.replace("%pro%", String.valueOf(api.getPlayerVersion(p.getUniqueId())));
        Raw = Raw.replace("%sver%", versionmap.get(api.getServerVersion().highestSupportedProtocolVersion().getVersion()));
        Raw = Raw.replace("%spro%", String.valueOf(api.getServerVersion().highestSupportedProtocolVersion().getVersion()));
        return Raw;
    }

}