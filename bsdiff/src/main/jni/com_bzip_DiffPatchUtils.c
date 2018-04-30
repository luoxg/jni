#include <jni.h>
#include "com_bzip_DiffPatchUtils.h"


JNIEXPORT jint JNICALL Java_com_bzip_DiffPatchUtils_genDiff
        (JNIEnv *env,jclass cls, jstring old, jstring new, jstring patch) {

    int argc = 4;
    char * argv[argc];
    argv[0] = "bsdiff";
    argv[1] = (char*) ((*env)->GetStringUTFChars(env, old, 0));
    argv[2] = (char*) ((*env)->GetStringUTFChars(env, new, 0));
    argv[3] = (char*) ((*env)->GetStringUTFChars(env, patch, 0));

    printf("old apk = %s \n", argv[1]);
    printf("new apk = %s \n", argv[2]);
    printf("patch = %s \n", argv[3]);

    int ret = bsdiff_main(argc, argv);

    printf("genDiff result = %d ", ret);

    (*env)->ReleaseStringUTFChars(env, old, argv[1]);
    (*env)->ReleaseStringUTFChars(env, new, argv[2]);
    (*env)->ReleaseStringUTFChars(env, patch, argv[3]);

    return ret;
}


JNIEXPORT jint JNICALL Java_com_bzip_DiffPatchUtils_patch
        (JNIEnv *env, jclass cls,jstring old, jstring new, jstring patch) {

    int argc = 4;
    char * argv[argc];
    argv[0] = "bspatch";
    argv[1] = (char*) ((*env)->GetStringUTFChars(env, old, 0));
    argv[2] = (char*) ((*env)->GetStringUTFChars(env, new, 0));
    argv[3] = (char*) ((*env)->GetStringUTFChars(env, patch, 0));

    printf("old apk = %s \n", argv[1]);
    printf("patch = %s \n", argv[3]);
    printf("new apk = %s \n", argv[2]);

    int ret = bspatch_main(argc, argv);

    printf("patch result = %d ", ret);

    (*env)->ReleaseStringUTFChars(env, old, argv[1]);
    (*env)->ReleaseStringUTFChars(env, new, argv[2]);
    (*env)->ReleaseStringUTFChars(env, patch, argv[3]);

    return ret;
}