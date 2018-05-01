package com.example.casper.sshtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button mSendCommandButton;
    private ImageView mScanImage;
    private ProgressBar mScanProgressBar;
    private TextView mScanProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseUI();
    }

    private void initialiseUI() {
        mSendCommandButton = findViewById(R.id.sendCommandButton);
        mScanImage = findViewById(R.id.scanImage);
        mScanProgressBar = findViewById(R.id.scanProgressBar);
        mScanProgressText = findViewById(R.id.scanProgressText);

        mSendCommandButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSendCommandButton.setText("Sending Command!");
                mScanProgressBar.setVisibility(View.VISIBLE);
                mScanProgressText.setText("Connecting");
                mScanProgressText.setVisibility(View.VISIBLE);
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
        } catch(Exception e) {
            System.out.println(e);
        }
    }



    private class SendCommand extends AsyncTask<String, Integer, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            int count = urls.length;
            long totalSize = 0;
            return connectToPi("pi", "raspberry", "10.0.0.1", 22);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Bitmap result) {
            mScanProgressBar.setVisibility(View.GONE);
            mScanProgressText.setVisibility(View.GONE);
            mScanImage.setImageBitmap(result);
        }

        public Bitmap connectToPi(String userName, String password, String host, int port){
            //Initialise our Java Secure Channel
            JSch secureChannel = new JSch();
            //Ignore the public key
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            //Configure and setup a session on the secure channel
            try {
                Session session = secureChannel.getSession(userName, host, port);
                session.setConfig(config);
                session.setPassword(password);
                session.connect();

                ChannelShell channel = (ChannelShell)session.openChannel("shell");
                InputStream inStream = channel.getInputStream();
                OutputStream outStream = channel.getOutputStream();

                PrintWriter toChannel = new PrintWriter(new OutputStreamWriter(outStream), true);

                channel.connect();
                sendCommand("python demo/read_ris_example.py", toChannel);
                return getShellOutput(new InputStreamReader(inStream));
            } catch (Exception exception) {
                System.out.println(exception);
            }

        return null;
        }

        public void sendCommand(String command, PrintWriter toChannel) {
            try {
                toChannel.println(command);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        public Bitmap getShellOutput(InputStreamReader inputStream) {
            StringBuilder line = new StringBuilder();
            char toAppend = ' ';
            try {
                while(true){
                    try {
                        while (inputStream.ready()) {
                            toAppend = (char) inputStream.read();
                            if(toAppend == '\n')
                            {
                                if (line.toString().contains("fname:")) {
                                    Log.d("Filename received", line.toString());
                                    line.toString().split(":");
                                    URL fileName = new URL("http://10.0.0.1/irscans/" + line.toString().split(":")[1]);
                                    Bitmap bmp = BitmapFactory.decodeStream(fileName.openConnection().getInputStream());
                                    return bmp;
                                }
                                if (line.toString().contains("Reading")) {
                                    Log.d("Progress", line.toString());
                                    mScanProgressText.setText("Reading Scan");
                                }
                                if (line.toString().contains("thermogram")) {
                                    Log.d("Progress", line.toString());
                                    mScanProgressText.setText("Generating Thermogram");
                                }
                                if (line.toString().contains("Processing")) {
                                    Log.d("Progress", line.toString());
                                    mScanProgressText.setText("Processing Image");
                                }
                                if (line.toString().contains("phasemap")) {
                                    Log.d("Progress", line.toString());
                                    mScanProgressText.setText("Generating Phasemap");
                                }
                                line.setLength(0);
                            }
                            else {
                                line.append(toAppend);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("\n\n****error reading character****\n\n");
                        line.setLength(0);
                    }
                    Thread.sleep(1000);
                }
            }catch (Exception ex) {
                System.out.println(ex);
                try{
                    inputStream.close();
                }
                catch(Exception e)
                {}
            }
            return null;
        }
    }
}
