package com.chipsee.volume;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.Context;

import com.chipsee.volume.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.R.string.ok;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Volume";

    public static final String[] getVolume = { "su", "-c","/system/bin/tinymix","4","|","/system/bin/busybox","awk","\'{print $4}\'"};
    public static  String[] setVolume = {"su", "-c","/system/bin/tinymix","4"};
    String config_path = "/data/misc/audio/audio.conf";
    private SeekBar sb_normal;
    private TextView txt_cur;
    private Context mContext;

    AudioConfig audioConfig;

    class AudioConfig
    {
        public String hpvolume = "60";

        public void readConfigFile(String file_name) throws IOException {
            File file = new File(file_name);
            FileInputStream in = null;
            BufferedReader reader;

            try
            {
                in = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(in));
                String line = reader.readLine();

                while(line != null)
                {
                    String[] key_val = line.split(":");
                    String key = key_val[0].trim();
                    String val = key_val[1].trim();

                    if(key.contains("hpvolume")) {
                        hpvolume = val;
                    }
                    line = reader.readLine();
                }
            }catch (Exception e)
            {
                messageBox(e.getMessage());
            }
            finally {
                if(in != null)
                    in.close();
            }
        }

        public void writeConfigFile(String file_name) throws IOException {
            File f = new File(file_name);
            FileOutputStream out = null;
            StringBuilder file_content = new StringBuilder();

            file_content.append("hpvolume:" + hpvolume +"\r\n");
            Log.i(TAG, "writeConfigFile: " + file_content.toString());
            try
            {
                out = new FileOutputStream(f);
                out.write(file_content.toString().getBytes());
                out.flush();
            }
            catch (Exception e)
            {
                messageBox(e.getMessage());
            }
            finally {
                if(out != null)
                    out.close();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        bindViews();
    }

    private void bindViews() {
        sb_normal = (SeekBar) findViewById(R.id.seekBar);
        txt_cur = (TextView) findViewById(R.id.textView);
        audioConfig = new AudioConfig();
        String volume = run(getVolume);
        int progress = Integer.parseInt(volume);
        txt_cur.setText("Current Volume:" + volume);
        sb_normal.setProgress(progress);

        sb_normal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String[] tempVolume = new String[setVolume.length+1];
                for(int i=0; i<setVolume.length; i++){
                    tempVolume[i] = setVolume[i];
                }
                tempVolume[setVolume.length] =String.valueOf(progress);

                Process proc = null;
                try {
                    proc = Runtime.getRuntime().exec(tempVolume);
                }catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    proc.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                txt_cur.setText("Current Volume:" + progress);

                // write config
                audioConfig.hpvolume = String.valueOf(progress);
                Log.i(TAG, "onProgressChanged: hpvolume is " + audioConfig.hpvolume);

                File file = new File(config_path);
                if(file.exists()) {
                    Log.i(TAG, "onProgressChanged: file exists");
                    try {
                        audioConfig.writeConfigFile(config_path.toString());
                        Log.i(TAG, "onProgressChanged: ok");
                    }catch (Exception e)
                    {
                        messageBox(e.getMessage());
                    }
                } else {
                    Log.i(TAG, "onProgressChanged: file not exists");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public static synchronized String run(String[] cmd) {
        String result = "";
        String line = "";
        InputStream is = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(cmd);
            is = proc.getInputStream();
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            // 每次读取1行
            while ((line = buf.readLine()) != null) {
                result += line;
            }
            if (is != null) {
                buf.close();
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void messageBox(CharSequence msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setNegativeButton(ok, null);
        builder.show();
    }
}
