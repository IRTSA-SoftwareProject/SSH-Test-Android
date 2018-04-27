package com.example.casper.sshtest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button mSendCommandButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseUI();
    }

    private void initialiseUI() {
        mSendCommandButton = findViewById(R.id.sendCommandButton);
        mSendCommandButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSendCommandButton.setText("Sending Command!");
                SendCommand command = new SendCommand();
                command.execute();
            }
        });
    }

    public void execCommand(){
        //Set login and host details
        String uname = "pi";
        String pword = "raspberry";
        String host = "10.0.0.1";
        int port = 22;

        //create ssh session with Jsch and exec command
        try {
            JSch jsch = new JSch();

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");


            Session session = jsch.getSession(uname, host, port);
            session.setConfig(config);
            session.setPassword(pword);
            session.connect();
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("python demo/read_ris_example.py");
            channel.connect();
            channel.disconnect();
        } catch(Exception e){
            System.out.println(e);
        }
    }

    private class SendCommand extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... urls) {
            int count = urls.length;
            long totalSize = 0;
            execCommand();
            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {

        }
    }
}
