package com.kobe.kobedemo.gson;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.kobe.kobedemo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GsonActivity extends AppCompatActivity {
    private final String tag = "gson";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gson);
        test2();
    }

    private void test2() {
        Gson gson = new Gson();
        Audio audio = new Audio();
        List<Audio.PackageCfg> packages = new ArrayList<>();
        Audio.PackageCfg pc0 = new Audio.PackageCfg();
        packages.add(pc0);
        Audio.PackageCfg pc1 = new Audio.PackageCfg("com.radio",1);
        packages.add(pc1);
        Audio.PackageCfg pc2 = new Audio.PackageCfg("com.music",1);
        packages.add(pc2);
        Audio.PackageCfg pc3 = new Audio.PackageCfg("com.video",1);
        packages.add(pc3);
        Audio.PackageCfg pc8 = new Audio.PackageCfg("com.ipod",1);
        packages.add(pc8);
        Audio.PackageCfg pc4 = new Audio.PackageCfg("com.bluetooth.music",1);
        packages.add(pc4);
        Audio.PackageCfg pc7 = new Audio.PackageCfg("com.mxmap",2);
        packages.add(pc7);
        Audio.PackageCfg pc9 = new Audio.PackageCfg("com.iflytek",3);
        packages.add(pc9);
        Audio.PackageCfg pc5 = new Audio.PackageCfg("com.bluetooth.phone",4);
        packages.add(pc5);
        Audio.PackageCfg pc6 = new Audio.PackageCfg("com.tbox.phone",5);
        packages.add(pc6);
        audio.packages = packages;
        Log.i(tag,"==to json==" + gson.toJson(audio));
        //从文件中读取配置信息，需要先把配置文件放到指定目录下
        File file = new File("/storage/emulated/legacy/Download/other/gsonTest.cfg");
        if(file != null && file.exists()) {
            Log.i(tag,"======read from file======");
            BufferedReader bufferedReader = null;
            FileReader reader = null;
            StringBuffer stringBuffer = new StringBuffer();
            try {
                String json = null;
                reader = new FileReader(file);
                bufferedReader = new BufferedReader(reader);
                while((json = bufferedReader.readLine()) != null) {
                    stringBuffer.append(json);
                }
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if(reader != null) {
                        reader.close();
                    }
                    if(bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            Audio audio2 = gson.fromJson(stringBuffer.toString(),Audio.class);
            Log.i(tag,"====file stringBuffer.toString()====" + audio2.toString());
        }
    }

}
