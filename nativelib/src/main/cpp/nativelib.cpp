#include <jni.h>
#include <string>
#include <jni.h>
#include <opencv2/opencv.hpp>

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bhushan_android_nativelib_NativeLib_nativeDetectBlink(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray imageData,
        jint width,
        jint height,
        jstring faceCascadePath,
        jstring eyeCascadePath
) {
    jbyte *data = env->GetByteArrayElements(imageData, nullptr);
    cv::Mat gray(height, width, CV_8UC1, (unsigned char *) data);

    const char *facePath = env->GetStringUTFChars(faceCascadePath, nullptr);
    const char *eyePath = env->GetStringUTFChars(eyeCascadePath, nullptr);

    // Load Haar cascades (ensure XMLs are in app files dir or assets and loaded!)
    static cv::CascadeClassifier face_cascade, eye_cascade;
    static bool loaded = false;
    if (!loaded) {
        face_cascade.load(facePath);
        eye_cascade.load(eyePath);
        loaded = true;
    }
    std::vector<cv::Rect> faces;
    face_cascade.detectMultiScale(gray, faces, 1.1, 3, 0, cv::Size(100, 100));
    bool isSleeping = false;
    for (const auto &face: faces) {
        cv::Mat faceROI = gray(face);
        std::vector<cv::Rect> eyes;
        eye_cascade.detectMultiScale(faceROI, eyes, 1.1, 3, 0, cv::Size(30, 30));
        if (eyes.size() < 2) isSleeping = true;
        else isSleeping = false; // crude: both eyes not detected == blink
    }
    env->ReleaseByteArrayElements(imageData, data, 0);
    return isSleeping;
}