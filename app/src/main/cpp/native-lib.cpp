#include <jni.h>
#include <android/log.h>
#include <opencv2/core.hpp>
#include "FaceRecognition.h"
#include "TypeConverter.h"
#include "ClassMember.h"
#include "FaceModel.h"

static thread_local JNI::ClassMemberDescriptor<jlong, FaceRecognition*> faceRecogPtrDesc("mNativePtr");
static thread_local JNI::ClassMemberDescriptor<jlong, FaceModel*> faceModelPtrDesc("mNativePtr");
static thread_local JNI::ClassMemberDescriptor<jdouble> confidenceThresholdDesc("mConfidenceThreshold");

static int RECOG_SUCCESS;
static int FACE_FOUND;


extern "C"
JNIEXPORT void JNICALL
Java_edu_ucsc_cmps115_1spring2017_face2name_CV_FaceRecognition_00024FaceModel_native_1addToModel(
        JNIEnv *env, jobject instance, jlong imagePtr, jstring label_) {
    auto faceModelPtr = faceModelPtrDesc(env, instance);

    cv::Mat faceImage = *reinterpret_cast<cv::Mat*>(imagePtr);
    auto label = JNI::TypeConverter<jstring, cv::String>::convert(env, label_);

    faceModelPtr.get()->addToModel(faceImage, label);
}

extern "C"
JNIEXPORT void JNICALL
Java_edu_ucsc_cmps115_1spring2017_face2name_CV_FaceRecognition_00024FaceModel_native_1initialize(
        JNIEnv *env, jobject instance, jint initSize) {
    auto faceModelPtr = faceModelPtrDesc(env, instance);

    faceModelPtr.set(new FaceModel(static_cast<size_t>(initSize)));

    __android_log_write(ANDROID_LOG_DEBUG, "FaceModel", "Allocated native FaceModel pointer");
}

extern "C"
JNIEXPORT void JNICALL
Java_edu_ucsc_cmps115_1spring2017_face2name_CV_FaceRecognition_00024FaceModel_close(JNIEnv *env,
                                                                               jobject instance) {

    auto faceModelPtr = faceModelPtrDesc(env, instance);
    FaceModel* currPtr = faceModelPtr.get();

    faceModelPtr.set(nullptr);

    delete currPtr;

    __android_log_write(ANDROID_LOG_DEBUG, "FaceModel", "Deallocated native FaceModel pointer");
}

extern "C"
JNIEXPORT void JNICALL
Java_edu_ucsc_cmps115_1spring2017_face2name_CV_FaceRecognition_native_1initialize(JNIEnv *env,
                                                                                  jobject instance) {
    auto faceRecogPtr = faceRecogPtrDesc(env, instance);

    faceRecogPtr.set(new FaceRecognition);

    __android_log_write(ANDROID_LOG_DEBUG, "FaceRecognition", "Allocated native FaceRecognition pointer");

    jclass faceRecognitionClass = env->GetObjectClass(instance);

    RECOG_SUCCESS = JNI::GetStaticMember<jint>(env, faceRecognitionClass, "RECOG_SUCCESS");
    FACE_FOUND = JNI::GetStaticMember<jint>(env, faceRecognitionClass, "FACE_FOUND");
}

extern "C"
JNIEXPORT void JNICALL
Java_edu_ucsc_cmps115_1spring2017_face2name_CV_FaceRecognition_native_1addToModel(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jlong matPtr,
                                                                                  jstring id) {
    auto faceRecogPtr = faceRecogPtrDesc(env, instance);
    cv::Mat faceImage = *reinterpret_cast<cv::Mat*>(matPtr);
    cv::String labelString = JNI::TypeConverter<jstring, cv::String>::convert(env, id);

    faceRecogPtr.get()->addToModel(faceImage, labelString);
}

extern "C"
JNIEXPORT void JNICALL
Java_edu_ucsc_cmps115_1spring2017_face2name_CV_FaceRecognition_native_1identify(JNIEnv *env,
                                                                                jobject instance,
                                                                                jlong faceImagePtr,
                                                                                jobject identResult) {
    auto faceRecogPtr = faceRecogPtrDesc(env, instance);
    cv::Mat faceImage = *reinterpret_cast<cv::Mat*>(faceImagePtr);

    cv::String labelString;
    double recognitionConfidence = faceRecogPtr.get()->identify(faceImage, labelString);

    jclass identResultClass = env->GetObjectClass(identResult);
    jmethodID setMethodId = env->GetMethodID(identResultClass, "set", "(ILjava/lang/String;)V");

    jdouble confidenceThreshold = confidenceThresholdDesc(env, instance).getRaw();

    if (recognitionConfidence < confidenceThreshold) {
        __android_log_print(ANDROID_LOG_DEBUG, "FaceRecognition", "Recognized this face. Label = '%s'", labelString.c_str());

        jstring labelStringJ = env->NewStringUTF(labelString.c_str());

        env->CallVoidMethod(identResult, setMethodId, RECOG_SUCCESS | FACE_FOUND, labelStringJ);
    } else {
        __android_log_write(ANDROID_LOG_DEBUG, "FaceRecognition", "Did not recognize this face");

        env->CallVoidMethod(identResult, setMethodId, RECOG_SUCCESS, NULL);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_edu_ucsc_cmps115_1spring2017_face2name_CV_FaceRecognition_close(JNIEnv *env,
                                                                     jobject instance) {

    auto faceRecogPtr = faceRecogPtrDesc(env, instance);
    FaceRecognition* currPtr = faceRecogPtr.get();

    faceRecogPtr.set(nullptr);

    delete currPtr;

    __android_log_write(ANDROID_LOG_DEBUG, "FaceRecognition", "Deallocated native FaceRecognition pointer");
}

extern "C"
JNIEXPORT void JNICALL
Java_edu_ucsc_cmps115_1spring2017_face2name_CV_FaceRecognition_native_1trainModel(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jobject model) {
    FaceModel* modelPtr = faceModelPtrDesc(env, model).get();
    auto faceRecogPtr = faceRecogPtrDesc(env, instance);

    faceRecogPtr.get()->trainModel(modelPtr->getModelFaces(), modelPtr->getModelLabels());
}