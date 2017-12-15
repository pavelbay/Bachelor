LOCAL_PATH_TOP := $(call my-dir)
include $(CLEAR_VARS)
# make sure this path is available for libEasyAR.so
LOCAL_PATH := $(LOCAL_PATH_TOP)/../jniLibs/armeabi-v7a
LOCAL_MODULE := EasyAR
LOCAL_SRC_FILES := libEasyAR.so
include $(PREBUILT_SHARED_LIBRARY)
LOCAL_LDLIBS += -lGLESv2
LOCAL_SHARED_LIBRARIES += EasyAR