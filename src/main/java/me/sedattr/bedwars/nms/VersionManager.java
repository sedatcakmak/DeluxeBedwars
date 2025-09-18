package me.sedattr.bedwars.nms;

import org.bukkit.Bukkit;

public class VersionManager {
    NMS nms;
    String version;

    public VersionManager() {
        version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

        if (version == null || version.equals(""))
            return;
        switch (version) {
            case "v1_16_R3":
                nms = new v1_16_R3();
                break;
            case "v1_16_R2":
                nms = new v1_16_R2();
                break;
            case "v1_16_R1":
                nms = new v1_16_R1();
                break;
            case "v1_15_R1":
                nms = new v1_15_R1();
                break;
            case "v1_14_R1":
                nms = new v1_14_R1();
                break;
            case "v1_13_R1":
                nms = new v1_13_R1();
                break;
            case "v1_13_R2":
                nms = new v1_13_R2();
                break;
            case "v1_12_R1":
                nms = new v1_12_R1();
                break;
            case "v1_11_R1":
                nms = new v1_11_R1();
                break;
            case "v1_10_R1":
                nms = new v1_10_R1();
                break;
            case "v1_9_R1":
                nms = new v1_9_R1();
                break;
            case "v1_9_R2":
                nms = new v1_9_R2();
                break;
            case "v1_8_R3":
                nms = new v1_8_R3();
                break;
            case "v1_8_R2":
                nms = new v1_8_R2();
                break;
            case "v1_8_R1":
                nms = new v1_8_R1();
                break;
            default:
                System.out.println("Can't find your server version!");
        }
    }

    public NMS getNMS() {
        return nms;
    }
}
