package com.example.android.camera2video;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.security.MessageDigest;

/**
 * Created by maxic on 2017/9/30.
 */

public class SegmentThread extends Thread
{
    private String filePath;//视频路径
    private String workingPath;//输出路径
    private String outName;//输出文件名
    private double startTime;//剪切起始时间
    private double endTime;//剪切结束时间
    private Handler handler;
    private static int leftSegments=0;


    public SegmentThread(String filePath,String workingPath,String outName,double startTime,double endTime,Handler handler)
    {
        this.filePath=filePath;
        this.workingPath=workingPath;
        this.outName=outName;
        this.startTime=startTime;
        this.endTime=endTime;
        this.handler=handler;
    }

    public static void setLeftSegments(int segmentsNum)
    {
        leftSegments=segmentsNum;
    }

    @Override
    public void run()
    {
        super.run();
        new VideoClip(filePath,workingPath,outName,startTime,endTime);
        leftSegments--;

        if(leftSegments==0)
        {
            while (TextUtils.isEmpty(workingPath))
            {
                try
                {
                    Thread.sleep(200);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            Message msg = new Message();
            msg.what = 0;
            handler.sendMessage(msg);
        }
    }
}
