package com.luo.serialtool;

import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;

/**
 * 作者：Administrator
 * 日期：2016.07.15
 * 说明：
 */
public class ICT_BillAcceptor extends Bill_Acceptor {

    public ICT_BillAcceptor(String device,int baudrate,int databits,int stopbits,char parity) {
        super(device, baudrate,databits,stopbits,parity);
    }

    @Override
    public void start(final onFinishListener listener) {
        if(!isNormal()){
            if (listener != null) {
                listener.onFailed();
            }
            return;
        }
        if (isStart) {
            if (listener != null) {
                listener.onSuccess();
            }
            return;
        } else if (!starting) {
            new Thread() {
                @Override
                public void run() {
                    starting = true;
                    int available = -1;
                    int delayTime = 500;
                    byte[] buf = new byte[4];
                    try {
                        reset();
                        while (mInputStream.available() <= 2 && delayTime >= 0){
                            SystemClock.sleep(20);
                            delayTime -= 20;
                        }

                        available = mInputStream.available();
                        Log.i(TAG, "available = " + available);
                        if (available > 0) {
                            mInputStream.read(buf);
                        }
                        for (int i = 0; i < 3; i++) {
                            if (buf[i] == (byte) 0x80 && buf[i + 1] == (byte) 0x8F) {
                                allowAccept = true;
                                isStart = true;
                                startReceiveThread();
                                if (listener != null) {
                                    listener.onSuccess();
                                }
                                Log.i(TAG, "bill acceptor enable");
                                return;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        starting = false;
                        if (!isStart){
                            closeSerialPort();
                        }
                    }
                    if (listener != null) {
                        listener.onFailed();
                    }
                }
            }.start();
        }
    }

    @Override
    protected void doBusiness(byte[] buf, int len) {
        if (len == 2){
            bytes = buf;
            if (bytes[index] == (byte)0x81){
                index++;
                if (allowAccept) {
                    switch (bytes[index]) {
                        case (byte) 0x40:
                            money = 1;
                            break;
                        case (byte) 0x41:
                            break;
                        case (byte) 0x42:
                            money = 5;
                            break;
                        case (byte) 0x43:
                            money = 10;
                            break;
                        case (byte) 0x44:
                            //money = 50;
                            break;
                        case (byte) 0x45:
                            //money = 100;
                            break;
                        case (byte) 0x46:
                            money = 20;
                            break;
                        default:
                            index = 0;
                            break;
                    }
                }
                if (money > 0){
                    accept();
                    data_received(money);
                    money = 0;
                }else{
                    reject();
                }
            }
            index = 0;
        }
    }
}
