package com.example.android.camera2video;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by maxic on 2017/10/2.
 */

public class UploadThread extends Thread
{
    private String fileFolderPath;
    private String fileFolderName;
    private String liveFilePath;
    private String liveFileName;
    private String targetURL= "http://monterosa.d2.comp.nus.edu.sg/~team02/index.php";
    private Handler handler;
    private String mode;

    public UploadThread(String fileFolderPath, Handler handler, String mode)
    {
        this.mode=mode;
        if(mode.equals("VIDEO"))
        {
            this.fileFolderPath = fileFolderPath;
            this.handler = handler;
            this.fileFolderName = fileFolderPath.substring(fileFolderPath.lastIndexOf("/") + 1, fileFolderPath.length());
        }
        else
        {
            this.liveFilePath=fileFolderPath;
            this.liveFileName=fileFolderPath.substring(fileFolderPath.lastIndexOf("/")+1,fileFolderPath.lastIndexOf("_"));
            this.handler=handler;
        }
    }

    @Override
    public void run()
    {
        super.run();
        try
        {
            if(mode.equals("VIDEO"))    UploadFilesInFolder(fileFolderPath, targetURL);
            else if(mode.equals("LIVE")) UploadOnLiveFile(liveFilePath,targetURL);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void UploadFilesInFolder(String filesPath, String targetURL) throws IOException, InterruptedException
    {
        System.out.println("targetUrl: " + targetURL + "  filePath: " + filesPath);

        //file not exists
        if (TextUtils.isEmpty(filesPath))
        {
            return;
        }

        //创建HttpClientUtil实例
        HttpClientUtil httpClient = new HttpClientUtil();


        File[] fileFloder = new File(filesPath).listFiles();
        if (fileFloder == null)
        {
            Log.d("MSG", "STILL NOT WORKING!!!!");
            return;
        }

        int receivedFileNum = 0;
        try
        {
            for (File f : fileFloder)
            {
                if (f.isFile())
                {
                    String response = "";
                    while (!response.equals(("200")))
                    {
                        HttpClientUtil.MultipartForm form = httpClient.new MultipartForm();
                        //设置form属性、参数
                        form.setAction(targetURL);
                        File file = new File(f.getPath());
                        form.addNormalField("mode","VIDEO");
                        if (receivedFileNum + 1 == fileFloder.length)
                            form.addNormalField("segment_number", new Integer(receivedFileNum + 1).toString());
                        else
                            form.addNormalField("segment_number", new Integer(-1).toString());

                        form.addNormalField("filefoldername", fileFolderName);
                        //form.addNormalField("tel", "15122946685");
                        //提交表单
                        form.addFileField("myFile", f);
                        response = HttpClientUtil.submitForm(form);

                        if (!response.equals("200")) Thread.sleep(300);
                        else receivedFileNum++;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        Message msg = new Message();
        if (receivedFileNum == fileFloder.length)
        {
            msg.what = 1;
        }
        else
        {
            msg.what = 2;
        }
        //Thread.sleep(1000);
        handler.sendMessage(msg);


    }

    public void UploadOnLiveFile(String filePath,String targetURL) throws InterruptedException
    {
        HttpClientUtil httpClient = new HttpClientUtil();

        File f=new File(filePath);
        if(!f.exists() || !f.isFile())  return;

        String response = "";
        int attemps=0;
        while (!response.equals(("200"))&&attemps<2)
        {
            HttpClientUtil.MultipartForm form = httpClient.new MultipartForm();
            //设置form属性、参数
            form.setAction(targetURL);
            File file = new File(f.getPath());
            if(!file.exists()) return;
            form.addFileField("myFile", f);
            form.addNormalField("mode","LIVE");
            form.addNormalField("filefoldername", liveFileName);
            //form.addNormalField("tel", "15122946685");
            //提交表单
            response = HttpClientUtil.submitForm(form);

            if (!response.equals("200"))
            {
                Thread.sleep(100);
                attemps++;
            }
        }

    }
}
