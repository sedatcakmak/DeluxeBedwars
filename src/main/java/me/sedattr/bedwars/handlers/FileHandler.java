package me.sedattr.bedwars.handlers;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
public class FileHandler {
    private final String name;
    private final File file;

    public FileHandler(String name) {
        this.file = new File(DeluxeBedwars.getInstance().getDataFolder(), name);
        this.name = name;

        try {
            reloadFile();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§8[§bDeluxeBedwars§8] §cFile Error:");
            e.printStackTrace();
        }
    }

    public void reloadFile() throws Exception {
        if (!file.exists())
            DeluxeBedwars.getInstance().saveResource(name, false);
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(file);

        if (name.equalsIgnoreCase("data.yml")) Variables.data = configuration;
        if (name.equalsIgnoreCase("items.yml")) Variables.items = configuration;
        if (name.equalsIgnoreCase("menus.yml")) Variables.menus = configuration;
        if (name.equalsIgnoreCase("cosmetics.yml")) Variables.cosmetics = configuration;
    }
}
