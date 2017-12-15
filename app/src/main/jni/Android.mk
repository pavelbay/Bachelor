include $(CLEAR_VARS)
# make sure this path is available for libEasyAR.so
LOCAL_PATH := $(LOCAL_PATH_TOP)/jniLibs/armeabi-v7a
LOCAL_MODULE := EasyAR
LOCAL_SRC_FILES := C:\Users\Pavel\Documents\Studium\Semester7\Bachelor\app\src\main\jniLibs\armeabi-v7a\libEasyAR.so
include $(PREBUILT_SHARED_LIBRARY)
LOCAL_LDLIBS += -lGLESv2
LOCAL_SHARED_LIBRARIES += EasyAR