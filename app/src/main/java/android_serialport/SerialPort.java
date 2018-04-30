package android_serialport;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 作者：Administrator
 * 日期：2016.08.09
 * 说明：
 */
public class SerialPort {
    private static final String TAG = SerialPort.class.getSimpleName();
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private boolean started = false;
    private boolean isNormal = false;

    private File device;
    private int baudrate;
    private int databits;
    private int stopbits;
    private char parity;

    public SerialPort(File device, int baudrate,int databits,int stopbits,char parity) {
        this.device = device;
        this.baudrate = baudrate;
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
        start(device, baudrate, databits,stopbits,parity);
    }

    public boolean isNormal() {
        return start(device, baudrate, databits,stopbits,parity);
    }

    /**
     * 设置串口数据，校验位,速率，停止位
     * @param baudrate 类型 int 速率 取值 2400,4800,9600,115200
     * @param databits 类型 int数据位 取值 位7或8
     * @param stopbits 类型 int 停止位 取值1 或者 2
     * @param parity 类型 char 校验类型 取值N ,E, O,
     */
    private synchronized boolean start(File device, int baudrate,int databits,int stopbits,char parity) {
        if (started) {
            return isNormal;
        }
        started = true;
        isNormal = true;
        if (device == null || !device.exists()) {
            isNormal = false;
            return isNormal;
        }
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    isNormal = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                isNormal = false;
            }
        }
        mFd = open(device.getAbsolutePath(), baudrate, databits,stopbits,parity);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            isNormal = false;
        }else {
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
        }
        return isNormal;
    }

    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    public void closeSerialPort(){
        close();
    }

    private native static FileDescriptor open(String path,int baudrate,int databits, int stopbits, char parity);

    private native void close();

    static {
        System.loadLibrary("SerialPort");
    }
}
