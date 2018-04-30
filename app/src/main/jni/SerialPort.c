//
// Created by Administrator on 2016.08.09.
//
#include "android_serialport_SerialPort.h"
#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
#include "android/log.h"

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

static const char *TAG = "SerialPort";

static int mTtyfd = -1;
struct termios mTermios;

int set_Parity(int , int , int , char );
static speed_t getBaudrate(jint );

JNIEXPORT jobject JNICALL Java_android_1serialport_SerialPort_open
        (JNIEnv *env, jclass thiz, jstring path, jint baudrate, jint databits, jint stopbits, jchar parity){
    int fd;
    speed_t speed;
    jobject mFileDescriptor;

    /* Check arguments */
    {
        speed = getBaudrate(baudrate);
        if (speed == -1) {
            /* TODO: throw an exception */
            LOGE("Invalid baudrate");
            return NULL;
        }
    }

    /* Opening device */
    {
        jboolean iscopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
        LOGD("Opening serial port %s", path_utf);
        fd = open(path_utf,O_RDWR | O_SYNC | 0 );
        LOGD("open() fd = %d", fd);
        (*env)->ReleaseStringUTFChars(env, path, path_utf);
        if (fd == -1) {
            /* Throw an exception */
            LOGE("Cannot open port");
            /* TODO: throw an exception */
            return NULL;
        }
    }

    /* Configure device */
    {
//        struct termios cfg;
        LOGD("Configuring serial port");
        if (tcgetattr(fd, &mTermios)) {
            LOGE("tcgetattr() failed");
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }
        cfmakeraw(&mTermios);
        mTtyfd = fd;
        if(set_Parity(baudrate, databits, stopbits, parity) < 0){
            return NULL;
        }
    }
    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
        jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
        mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
        (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint) fd);
    }

    return mFileDescriptor;
}

JNIEXPORT void
Java_android_1serialport_SerialPort_close(JNIEnv *env, jobject thiz) {
    jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
    jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

    jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
    jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

    jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
    jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

    LOGD("close(fd = %d)", descriptor);
    close(descriptor);
}

/**
* 设置串口数据，校验位,速率，停止位
* @param nBits 类型 int数据位 取值 位7或8
* @param nEvent 类型 char 校验类型 取值N ,E, O,,S
* @param mSpeed 类型 int 速率 取值 2400,4800,9600,115200
* @param mStop 类型 int 停止位 取值1 或者 2
*/
int set_Parity(int nSpeed, int nBits, int nStop, char nEvent) {
    LOGD("set_Parity:nBits=%d,nEvent=%c,nSpeed=%d,nStop=%d", nBits, nEvent, nSpeed, nStop);
    switch (nBits) {//设置数据位数
        case 7:
            mTermios.c_cflag &= ~CSIZE;
            mTermios.c_cflag |= CS7;
            LOGD("nBits:%d,invalid param", 7);
            break;
        case 8:
            mTermios.c_cflag &= ~CSIZE;
            mTermios.c_cflag |= CS8;
            LOGD("nBits:%d,invalid param", 8);
            break;
        default:
            LOGD("nBits:%d,invalid param", nBits);
            break;
    }
    switch (nEvent) {//设置校验位
        case 'O':
            mTermios.c_cflag |= PARENB;//enable parity checking
            mTermios.c_cflag |= PARODD;//奇校验位
//            newtio.c_iflag |= (INPCK | ISTRIP);
//            newtio.c_iflag |= INPCK;//Disable parity checking
            LOGD("nEvent:%c,invalid param", 'O');
            break;
        case 'E':
            mTermios.c_cflag |= PARENB;//
            mTermios.c_cflag &= ~PARODD;//偶校验位
            LOGD("nEvent:%c,invalid param", 'E');
            break;
        case 'N':
            mTermios.c_cflag &= ~PARENB;//清除校验位
            LOGD("nEvent:%c,invalid param", 'N');
            break;
        //case 'S':
            // newtio.c_cflag &= ~PARENB;//清除校验位
            // newtio.c_cflag &=~CSTOPB;
            // newtio.c_iflag |=INPCK;//Disable parity checking
            // break;
        default:
            LOGD("nEvent:%c,invalid param", nEvent);
            break;
    }

    //设置速率
    speed_t speed = getBaudrate(nSpeed);
    if(speed == -1){
        speed = B9600;
    }
    cfsetispeed(&mTermios,speed);
    cfsetospeed(&mTermios,speed);
    LOGD("nSpeed:%d,invalid param", nSpeed);

    switch (nStop) {//设置停止位
        case 1:
            mTermios.c_cflag &= ~CSTOPB;
            LOGD("nStop:%d,invalid param", 1);
            break;
        case 2:
            mTermios.c_cflag |= CSTOPB;
            LOGD("nStop:%d,invalid param", 2);
            break;
        default:
            LOGD("nStop:%d,invalid param", nStop);
            break;
    }
//    newtio.c_cc[VTIME] = 0;//设置等待时间
//    newtio.c_cc[VMIN] = 0;//设置最小接收字符
    tcflush(mTtyfd, TCIFLUSH);
    if (tcsetattr(mTtyfd, TCSANOW, &mTermios) != 0) {
        LOGE("options set error");
        return -1;
    }
    return 1;
}

static speed_t getBaudrate(jint baudrate) {
    switch (baudrate) {
        case 0:
            return B0;
        case 50:
            return B50;
        case 75:
            return B75;
        case 110:
            return B110;
        case 134:
            return B134;
        case 150:
            return B150;
        case 200:
            return B200;
        case 300:
            return B300;
        case 600:
            return B600;
        case 1200:
            return B1200;
        case 1800:
            return B1800;
        case 2400:
            return B2400;
        case 4800:
            return B4800;
        case 9600:
            return B9600;
        case 19200:
            return B19200;
        case 38400:
            return B38400;
        case 57600:
            return B57600;
        case 115200:
            return B115200;
        case 230400:
            return B230400;
        case 460800:
            return B460800;
        case 500000:
            return B500000;
        case 576000:
            return B576000;
        case 921600:
            return B921600;
        case 1000000:
            return B1000000;
        case 1152000:
            return B1152000;
        case 1500000:
            return B1500000;
        case 2000000:
            return B2000000;
        case 2500000:
            return B2500000;
        case 3000000:
            return B3000000;
        case 3500000:
            return B3500000;
        case 4000000:
            return B4000000;
        default:
            return -1;
    }
}  