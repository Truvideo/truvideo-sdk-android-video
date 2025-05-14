LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
LOCAL_MODULE := libkrisp-audio
LOCAL_LDLIBS:= -lz
LOCAL_SRC_FILES := noisecancellibs/$(TARGET_ARCH_ABI)/libkrisp-audio-sdk-static.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
LOCAL_MODULE := libopenblas
LOCAL_LDLIBS:= -lz
LOCAL_SRC_FILES := noisecancellibs/$(TARGET_ARCH_ABI)/libopenblas.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
LOCAL_MODULE := libresample
LOCAL_LDLIBS:= -lz
LOCAL_SRC_FILES := noisecancellibs/$(TARGET_ARCH_ABI)/libresample.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
LOCAL_SRC_FILES := native-lib.cpp
LOCAL_MODULE := native-lib
LOCAL_SHARED_LIBRARIES := libkrisp-audio
LOCAL_STATIC_LIBRARIES := libopenblas libresample
LOCAL_C_INCLUDES := $(LOCAL_PATH)/noisecancellibs/
LOCAL_LDLIBS    := -llog -L$(LOCAL_PATH)/noisecancellibs/
include $(BUILD_SHARED_LIBRARY)