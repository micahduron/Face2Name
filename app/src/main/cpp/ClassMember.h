//
// Created by micah on 6/2/17.
//

#ifndef FACE2NAME_CLASSMEMBER_H
#define FACE2NAME_CLASSMEMBER_H

#include <jni.h>
#include "JNITypeUtils.h"
#include "TypeConverter.h"

namespace JNI {
    template <typename JavaT, typename Converter>
    class ClassMember;

    template<typename JavaT, typename NativeT = JavaT>
    class ClassMemberDescriptor {
    public:
        ClassMemberDescriptor(const char* memberName) :
                m_memberName{ memberName }
        {}

        ClassMember<JavaT, NativeT> operator () (JNIEnv* env, jobject obj) {
            if (m_jniEnv != env) {
                m_jniEnv = env;

                jclass objClass = env->GetObjectClass(obj);
                m_fieldId = env->GetFieldID(objClass, m_memberName, JNI::TypeTraits<JavaT>::signature);
            }
            return { m_jniEnv, obj, m_fieldId };
        };

    private:
        JNIEnv* m_jniEnv;
        jfieldID m_fieldId;
        const char* m_memberName;
    };

    template <typename JavaT, typename NativeT>
    class ClassMember {
        typedef JNI::TypeConverter<JavaT, NativeT> Converter;

    public:
        ClassMember(JNIEnv* env, jobject obj, jfieldID fieldId) :
                m_jniEnv{ env },
                m_javaObj{ obj },
                m_fieldId{ fieldId },
                convertType{ env }
        {}

        typename Converter::converted_type get() const {
            return convertType(getRaw());
        }

        JavaT getRaw() const {
            return JNI::FieldGetter<JavaT>(m_jniEnv, m_javaObj, m_fieldId);
        }

        ClassMember& set(const typename Converter::converted_type& value) {
            return setRaw(convertType(value));
        }

        ClassMember& setRaw(JavaT rawVal) {
            JNI::FieldSetter<JavaT>(m_jniEnv, m_javaObj, m_fieldId, rawVal);

            return *this;
        }

    private:
        JNIEnv* m_jniEnv;
        jobject m_javaObj;
        jfieldID m_fieldId;

        Converter convertType;
    };

    template <typename SrcT, typename DstT = SrcT>
    DstT GetMember(JNIEnv* env, jobject obj, const char* memberName);

    template <typename SrcT, typename DstT = SrcT>
    DstT GetMember(JNIEnv* env, jobject obj, jfieldID fieldId);

    template <typename SrcT, typename DstT>
    DstT GetMember(JNIEnv* env, jobject obj, const char* memberName) {
        jclass objClass = env->GetObjectClass(obj);
        jfieldID fieldId = env->GetFieldID(objClass, memberName, JNI::TypeTraits<SrcT>::signature);

        return JNI::GetMember<SrcT, DstT>(env, obj, fieldId);
    };

    template <typename SrcT, typename DstT>
    DstT GetMember(JNIEnv* env, jobject obj, jfieldID fieldId) {
        SrcT rawVal = JNI::FieldGetter<SrcT>(env, obj, fieldId);

        return JNI::TypeConverter<SrcT, DstT>::convert(env, rawVal);
    };

    template <typename SrcT, typename DstT = SrcT>
    DstT GetStaticMember(JNIEnv* env, jclass objClass, const char* memberName);

    template <typename SrcT, typename DstT = SrcT>
    DstT GetStaticMember(JNIEnv* env, jclass objClass, jfieldID fieldId);

    template <typename SrcT, typename DstT>
    DstT GetStaticMember(JNIEnv* env, jclass objClass, const char* memberName) {
        jfieldID fieldId = env->GetStaticFieldID(objClass, memberName, JNI::TypeTraits<SrcT>::signature);

        return JNI::GetStaticMember<SrcT, DstT>(env, objClass, fieldId);
    }

    template <typename SrcT, typename DstT>
    DstT GetStaticMember(JNIEnv* env, jclass objClass, jfieldID fieldId) {
        SrcT rawVal = JNI::StaticFieldGetter<SrcT>(env, objClass, fieldId);

        return JNI::TypeConverter<SrcT, DstT>::convert(env, rawVal);
    };
}

#endif //FACE2NAME_CLASSMEMBER_H
