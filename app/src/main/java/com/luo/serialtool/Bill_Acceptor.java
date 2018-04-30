package com.luo.serialtool;

import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport.SerialPort;


/**
 * 作者：Administrator
 * 日期：2016.07.21
 * 说明：
 */
public abstract class Bill_Acceptor {
    protected static final String TAG = Bill_Acceptor.class.getSimpleName();
    protected SerialPort mSerialPort;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    protected ReceiveTHread mTHread;
    protected byte[] bytes = new byte[4];
    protected int index	= 0;
    protected int money	= 0;
    protected boolean allowAccept = false;
    protected boolean isReceive = true;
    protected boolean isStart = false;
    protected boolean starting = false;

    protected Bill_Acceptor(String device,int baudrate,int databits,int stopbits,char parity){
        mSerialPort = new SerialPort(new File(device), baudrate,databits,stopbits,parity);
        mInputStream = mSerialPort.getInputStream();
        mOutputStream = mSerialPort.getOutputStream();
    }

    protected boolean isNormal(){
        return mSerialPort.isNormal();
    }

    protected void data_received(int money) {
        Log.i("data_received", "money = " + money);
    }

    protected void notifchange(int moneyCount,int moneyType) {
    }
    protected void sendCmd(String msg, int b) {
        try {
            mOutputStream.write((byte) b);
            Log.i(TAG, msg + String.format(" 0x%02X", b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startReceiveThread() {
        if (mTHread == null) {
            Log.i(TAG, "receive thread is created");
            mTHread = new ReceiveTHread();
            mTHread.setDaemon(true);
            mTHread.start();
        }
    }

    public void stopReceiveThread() {
        if (mTHread != null) {
            isReceive = false;
            beforeStop();
            mTHread.interrupt();
            mTHread = null;
        }
    }

    public void closeSerialPort(){
        mSerialPort.closeSerialPort();
    }

    class ReceiveTHread extends Thread {
        private byte[] buf = new byte[4];
        private int len = -1;

        @Override
        public void run() {
            try {
                SystemClock.sleep(500);
                allowAccept = true;
                accept();
                while (isReceive) {
                    len = mInputStream.read(buf);
                    log(buf,len);
                    doBusiness(buf,len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                isReceive = false;
                afterStop();
                closeSerialPort();
                Log.i(TAG, "receive thread interrupt");
            }
        }

        private void log(byte[] buf, int len) {
            if (len <= 0){
                throw new IllegalArgumentException("纸币器串口读取异常");
            }
            if (buf.length < len ) {
                throw new ArrayIndexOutOfBoundsException("数组越界");
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb.append("   " + String.format(" 0x%02X", buf[i]));
            }
            Log.i(TAG, "length：" + len + sb.toString());
        }
    }

    protected void accept() {
        sendCmd("accept", 0x02);
    }

    protected void reject(){
        sendCmd("reject", 0x0F);
    }

    //hc纸币器调用此方法可能有问题，请勿用
    protected void query() {
        sendCmd("query", 0x0C);
    }

    protected void disable() {
        sendCmd("disable", 0x5E);
    }

    protected void enable() {
        sendCmd("enable", 0x3E);
    }

    public void reset() {
        sendCmd("reset", 0x30);
    }

    public abstract void start(onFinishListener listener);

    protected void beforeStop(){
        query();
    }
    protected void afterStop(){
        disable();
    }
    protected abstract void doBusiness(byte[] buf,int len);

    public interface onFinishListener {
        void onSuccess();

        void onFailed();
    }
}
