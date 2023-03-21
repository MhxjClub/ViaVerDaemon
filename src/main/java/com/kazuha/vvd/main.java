package com.kazuha.vvd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import org.apache.commons.io.FileUtils;
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
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class main extends JavaPlugin implements Listener {
    public JSONObject objectfinal = null;
    Boolean getted = false;
    ViaAPI api;
    List<String> nan;
    FileConfiguration configuration;
    HashMap<Integer, String> versionmap = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        saveResource("version.json", false);
        try {
            objectfinal = JSONObject.parseObject(FileUtils.readFileToString(new File(getDataFolder(), "version.json")));
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                JSONArray array = objectfinal.getJSONArray("map");
                for (int i = 0; i < array.size(); i++) {
                    int finalI = i;
                    array.getJSONObject(i).keySet().forEach(s -> {
                        if (array.getJSONObject(finalI).getString(String.valueOf(s)) != null) {
                            versionmap.put(Integer.parseInt(s), array.getJSONObject(finalI).getString(s));
                        }
                    });

                }
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


    public void simpleHTTPDownload(String Version) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        String HTTP_URL = String.format("https://ghproxy.com/https://github.com/ViaVersion/ViaVersion/releases/download/%s/ViaVersion-%s.jar", Version, Version);

        try {
            int contentLength = getConnection(HTTP_URL).getContentLength();
            getLogger().info("[D-ASYNC-DOWNLOADER] LENGTH = " + contentLength);
            if (contentLength > 32) {
                InputStream is = getConnection(HTTP_URL).getInputStream();
                bis = new BufferedInputStream(is);
                FileOutputStream fos = new FileOutputStream(this.getDataFolder().getParentFile() + "/" + HTTP_URL.substring(HTTP_URL.lastIndexOf("/") + 1));
                bos = new BufferedOutputStream(fos);
                int b = 0;
                byte[] byArr = new byte[1024];
                while ((b = bis.read(byArr)) != -1) {
                    bos.write(byArr, 0, b);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public HttpURLConnection getConnection(String httpUrl) throws Exception {
        URL url = new URL(httpUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.connect();
        return connection;

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
                    while (!getted) {
                        try {
                            JSONObject object = JSON.parseObject(http("https://ghproxy.com/https://raw.githubusercontent.com/MhxjClub/public/main/api/version.json"));
                            if (object == null) continue;
                            getLogger().info("云端version.json获取成功:");
                            getLogger().info("最后更新:" + object.getString("last-update"));
                            getLogger().info("最后支持MC版本:" + object.getString("latest-mcversion"));
                            getLogger().info("对应ViaVersion版本:" + object.getString("viaver-number"));

                            if (api.getServerVersion().highestSupportedVersion() > object.getInteger("version")) {
                                getLogger().warning("[警告] 云端version.json已过期。部分玩家可能出现版本无法识别的问题。");
                            }
                            JSONArray array = object.getJSONArray("map");
                            for (int i = 0; i < array.size(); i++) {
                                int finalI = i;
                                array.getJSONObject(i).keySet().forEach(s -> {
                                    if (array.getJSONObject(finalI).getString(String.valueOf(s)) != null) {
                                        versionmap.put(Integer.parseInt(s), array.getJSONObject(finalI).getString(String.valueOf(s)));
                                        getLogger().info("[Mapping] " + s + " -> " + array.getJSONObject(finalI).getString(String.valueOf(s)));
                                    }
                                });

                            }
                            getted = true;
                            if (object.getInteger("version") > objectfinal.getInteger("version")) {
                                getLogger().info("[E-ASYNC-UPDATER] 检查到更新，正在保存version.json");
                                File file = new File(getDataFolder(), "version.json");
                                Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
                                writer.write(JSON.toJSONString(object, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                                        SerializerFeature.WriteDateUseDateFormat));
                                writer.flush();
                                writer.close();
                            }
                            if (!object.getString("viaver-number").equalsIgnoreCase(api.getVersion())) {
                                getLogger().info(String.format("[V-ASYNC-UPDATER] 找到 ViaVersion 更新。 [%s -> %s]正在下载。", api.getVersion(), object.getString("viaver-number")));
                                simpleHTTPDownload(object.getString("viaver-number"));
                                getLogger().info("[V-ASYNC-UPDATER] 任务已完成。");
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                            getLogger().info("无法从云端获取各版本信息。");
                            getLogger().info("正在重试。");
                        }
                    }
                }
        );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (versionmap.isEmpty()) return;
        if (api.getServerVersion().supportedVersions().contains(api.getPlayerVersion(e.getPlayer().getUniqueId()))) {
            return;
        }
        if (api.getPlayerVersion(e.getPlayer().getUniqueId()) > api.getServerVersion().highestSupportedVersion()) {
            nan = configuration.getStringList("player-version-higher");
        } else {
            nan = configuration.getStringList("player-version-lower");
        }
        for (String ec : nan) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Trans(ec, e.getPlayer())));
        }
    }

    public String Trans(String Raw, Player p) {
        Raw = Raw.replace("%ver%", versionmap.get(api.getPlayerVersion(p)));
        Raw = Raw.replace("%pro%", String.valueOf(api.getPlayerVersion(p)));
        Raw = Raw.replace("%sver%", versionmap.get(api.getServerVersion().highestSupportedVersion()));
        Raw = Raw.replace("%spro%", String.valueOf(api.getServerVersion().highestSupportedVersion()));
        return Raw;
    }

}