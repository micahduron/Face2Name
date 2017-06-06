//
// Created by micah on 6/5/17.
//

#ifndef FACE2NAME_TYPECONVERTER_H
#define FACE2NAME_TYPECONVERTER_H

#include <utility>
#include <jni.h>

namespace JNI {
    template <typename JavaT, typename NativeT>
    class TypeConverter {
    public:
        typedef NativeT converted_type;

        TypeConverter(JNIEnv* env) : m_jniEnv{env} {}

        NativeT operator () (const JavaT& src) const {
            return TypeConverter::convert(m_jniEnv, src);
        }

        NativeT operator () (JavaT&& src) const {
            return TypeConverter::convert(m_jniEnv, std::move(src));
        }

        JavaT operator () (const NativeT& dst) const {
            return TypeConverter::convert(m_jniEnv, dst);
        }

        JavaT operator () (NativeT&& dst) const {
            return TypeConverter::convert(m_jniEnv, std::move(dst));
        }

        static NativeT convert(JNIEnv* env, const JavaT& src) {
            return {src};
        }

        static NativeT convert(JNIEnv* env, JavaT&& src) {
            return {std::move(src)};
        }

        static JavaT convert(JNIEnv* env, const NativeT& dst) {
            return {dst};
        }

        static JavaT convert(JNIEnv* env, NativeT&& dst) {
            return {std::move(dst)};
        }

    private:
        JNIEnv* m_jniEnv;
    };

    template <typename T>
    class TypeConverter<T, T> {
    public:
        typedef T converted_type;

        TypeConverter(JNIEnv* env) : m_jniEnv{env} {}

        T operator () (const T& src) const {
            return TypeConverter::convert(m_jniEnv, src);
        }

        T operator () (T&& src) const {
            return TypeConverter::convert(m_jniEnv, std::move(src));
        }

        static T convert(JNIEnv* env, const T& src) {
            return {src};
        }

        static T convert(JNIEnv* env, T&& src) {
            return {std::move(src)};
        }

    private:
        JNIEnv* m_jniEnv;
    };

    template <typename PtrT>
    class TypeConverter<jlong, PtrT*> {
    public:
        typedef PtrT* converted_type;

        TypeConverter(JNIEnv* env) : m_jniEnv{env} {}

        PtrT* operator () (jlong longVal) const noexcept {
            return TypeConverter::convert(m_jniEnv, longVal);
        }

        jlong operator () (PtrT* ptrVal) const noexcept {
            return TypeConverter::convert(m_jniEnv, ptrVal);
        }

        static PtrT* convert(JNIEnv* env, jlong longVal) noexcept {
            return reinterpret_cast<PtrT*>(longVal);
        }

        static jlong convert(JNIEnv* env, PtrT* ptrVal) noexcept {
            return reinterpret_cast<jlong>(ptrVal);
        }

    private:
        JNIEnv* m_jniEnv;
    };

    template <typename StrT>
    class TypeConverter<jstring, StrT> {
    public:
        typedef StrT converted_type;

        TypeConverter(JNIEnv* env) : m_jniEnv{env} {}

        StrT operator () (jstring javaString) const {
            return TypeConverter::convert(m_jniEnv, javaString);
        }

        jstring operator () (const StrT& cppString) const {
            return TypeConverter::convert(m_jniEnv, cppString);
        }

        static StrT convert(JNIEnv* env, jstring javaString) {
            const char* str = env->GetStringUTFChars(javaString, 0);
            StrT cppString = str;

            env->ReleaseStringUTFChars(javaString, str);

            return cppString;
        }

        static jstring convert(JNIEnv* env, StrT cppString) {
            return env->NewStringUTF(cppString.c_str());
        }

    private:
        JNIEnv* m_jniEnv;
    };
}
#endif //FACE2NAME_TYPECONVERTER_H
