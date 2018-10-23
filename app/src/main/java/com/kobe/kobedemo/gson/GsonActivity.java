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

public class GsonActivity extends AppCompatActivity {
    private final String tag = "gson";
    private String json = "{\"rst\": 100,\"msg\": \"ok\",\"data\": {\"cookie\": \"JSESSIONID=abcntKeuJhop56LGykfdw\"}}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gson);
        Gson gson = new Gson();
        JsonRootBean jsonRootBean = gson.fromJson(json,JsonRootBean.class);
        Log.i(tag,"==jsonRootBean.rst==" + jsonRootBean.getRst());
        Log.i(tag,"==jsonRootBean.msg==" + jsonRootBean.getMsg());
        Log.i(tag,"==jsonRootBean.data==" + jsonRootBean.getData().getCookie());
        Log.i(tag,"==to json==" + gson.toJson(jsonRootBean));

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
            Log.i(tag,"====file stringBuffer.toString()====" + stringBuffer.toString());
            JsonRootBean jsonRootBean2 = gson.fromJson(stringBuffer.toString(),JsonRootBean.class);
            Log.i(tag,"==file jsonRootBean.rst==" + jsonRootBean2.getRst());
            Log.i(tag,"==file jsonRootBean.msg==" + jsonRootBean2.getMsg());
            Log.i(tag,"==file jsonRootBean.data==" + jsonRootBean.getData().getCookie());
        }

    }


}
