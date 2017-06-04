//
// Created by micah on 6/1/17.
//

#ifndef FACE2NAME_JNITYPEUTILS_H
#define FACE2NAME_JNITYPEUTILS_H

#include <jni.h>
#include <cstdlib>

namespace JNI {
    template<typename JavaT>
    JavaT FieldGetter(JNIEnv *env, jobject obj, jfieldID fieldId) {
        // This function has been called with an unimplemented type. Crash the program.
        abort();
    }

#define DEFINE_FIELDGETTER(JNIType, GetterFn) \
    template <> JNIType FieldGetter<JNIType>(JNIEnv* env, jobject obj, jfieldID fieldId) { \
        return env->GetterFn(obj, fieldId); \
    }

    DEFINE_FIELDGETTER(jint, GetIntField)
    DEFINE_FIELDGETTER(jlong, GetLongField)
    DEFINE_FIELDGETTER(jdouble, GetDoubleField)

#undef DEFINE_FIELDGETTER

    template<typename JavaT>
    void FieldSetter(JNIEnv *env, jobject, jfieldID fieldId, JavaT val) {
        // This function has been called with an unimplemented type. Crash the program.
        abort();
    }

    template <typename JavaT>
    JavaT StaticFieldGetter(JNIEnv* env, jclass objClass, jfieldID fieldId) {
        // This function has been called with an unimplemented type. Crash the program.
        abort();
    }

#define DEFINE_STATICFIELDGETTER(JNIType, GetterFn) \
    template <> JNIType StaticFieldGetter(JNIEnv* env, jclass objClass, jfieldID fieldId) { \
        return env->GetterFn(objClass, fieldId); \
    }

    DEFINE_STATICFIELDGETTER(jint, GetStaticIntField)

#undef DEFINE_STATICFIELDGETTER

#define DEFINE_FIELDSETTER(JNIType, SetterFn) \
    template <> void FieldSetter<JNIType>(JNIEnv* env, jobject obj, jfieldID fieldId, JNIType val) { \
        env->SetterFn(obj, fieldId, val); \
    }

    DEFINE_FIELDSETTER(jint, SetIntField)
    DEFINE_FIELDSETTER(jlong, SetLongField)
    DEFINE_FIELDSETTER(jdouble, SetDoubleField)

#undef DEFINE_FIELDSETTER

    template <typename JavaT>
    struct TypeTraits {};

#define DEFINE_TYPETRAITS(JNIType, signatureStr) \
    template <> struct TypeTraits<JNIType> { \
        static constexpr auto signature = signatureStr; \
    };

    DEFINE_TYPETRAITS(jint, "I")
    DEFINE_TYPETRAITS(jlong, "J")
    DEFINE_TYPETRAITS(jdouble, "D")

#undef DEFINE_TYPETRAITS
}

#endif //FACE2NAME_JNITYPEUTILS_H