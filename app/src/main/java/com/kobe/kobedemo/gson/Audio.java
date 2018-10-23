package com.kobe.kobedemo.gson;

import java.util.List;

/**
 * @author: kobe
 * @date: 2018/10/23 9:19
 * @decribe:
 */
public class Audio {

    public String project = "S171";
    public String version = "V1.0";
    public List<PackageCfg> packages;

    @Override
    public String toString() {
        StringBuffer packages = new StringBuffer("[");
        for(PackageCfg pc : this.packages) {
            packages.append(pc.toString());
            packages.append(",");
        }
        packages.append("]");
        return "Audio{" + "project='" + project + '\'' + ", version='" + version + '\'' + ", packages=" + packages + '}';
    }

    public static class PackageCfg {

        public String packageName = "com.default";
        public int level = 0;

        public PackageCfg() {
        }
        public PackageCfg(String packageName,int level) {
            this.packageName = packageName;
            this.level = level;
        }

        @Override
        public String toString() {
            return "{" + "packageName='" + packageName + '\'' + ", level=" + level + '}';
        }
    }
}
