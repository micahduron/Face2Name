#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_edu_ucsc_cmps115_1spring2017_face2name_MainScreen_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
