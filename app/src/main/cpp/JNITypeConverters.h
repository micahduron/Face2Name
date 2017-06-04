//
// Created by micah on 6/1/17.
//

#ifndef FACE2NAME_JNITYPECONVERTERS_H
#define FACE2NAME_JNITYPECONVERTERS_H

#include <jni.h>

namespace JNI {
    namespace Converter {
        template <typename T>
        class Identity {
        public:
            typedef T converted_type;

            T operator () (JNIEnv* env, const T& val) const noexcept {
                return val;
            }
        };

        template <typename PtrType>
        class Pointer {
        public:
            typedef PtrType* converted_type;

            PtrType* operator () (JNIEnv* env, jlong longVal) const noexcept {
                return reinterpret_cast<PtrType*>(longVal);
            }

            jlong operator () (JNIEnv* env, PtrType* ptrVal) const noexcept {
                return reinterpret_cast<jlong>(ptrVal);
            }
        };

        template <typename CPPString>
        class String {
        public:
            typedef CPPString converted_type;

            CPPString operator () (JNIEnv* env, jstring javaStr) const noexcept {
                const char* str = env->GetStringUTFChars(javaStr, 0);
                CPPString cppString(str);

                env->ReleaseStringUTFChars(javaStr, str);

                return cppString;
            }

            jstring operator () (JNIEnv* env, const CPPString& cppString) const noexcept {
                return env->NewStringUTF(cppString.c_str());
            }
        };

        template <typename CPPString>
        CPPString convertJString(JNIEnv* env, jstring javaStr) {
            String<CPPString> converter;

            return converter(env, javaStr);
        }
    }
}

#endif //FACE2NAME_JNITYPECONVERTERS_H
