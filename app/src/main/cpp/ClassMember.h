//
// Created by micah on 6/2/17.
//

#ifndef FACE2NAME_CLASSMEMBER_H
#define FACE2NAME_CLASSMEMBER_H

#include <jni.h>
#include "JNITypeConverters.h"
#include "JNITypeUtils.h"

namespace JNI {
    template <typename JavaT, typename Converter>
    class ClassMember;

    template<typename JavaT, typename Converter = JNI::Converter::Identity<JavaT>>
    class ClassMemberDescriptor {
    public:
        ClassMemberDescriptor(const char* memberName) :
                m_memberName{ memberName }
        {}

        ClassMember<JavaT, Converter> operator () (JNIEnv* env, jobject obj) {
            if (m_jniEnv == nullptr) {
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

    template <typename JavaT, typename Converter>
    class ClassMember {
    public:
        ClassMember(JNIEnv* env, jobject obj, jfieldID fieldId) :
                m_jniEnv{ env },
                m_javaObj{ obj },
                m_fieldId{ fieldId }
        {}

        typename Converter::converted_type get() const {
            JavaT rawVal = getRaw();
            Converter converter;

            return converter(m_jniEnv, rawVal);
        }

        JavaT getRaw() const {
            return JNI::FieldGetter<JavaT>(m_jniEnv, m_javaObj, m_fieldId);
        }

        ClassMember& set(const typename Converter::converted_type& value) {
            Converter converter;

            return setRaw(converter(m_jniEnv, value));
        }

        ClassMember& setRaw(JavaT rawVal) {
            JNI::FieldSetter<JavaT>(m_jniEnv, m_javaObj, m_fieldId, rawVal);

            return *this;
        }

    private:
        JNIEnv* m_jniEnv;
        jobject m_javaObj;
        jfieldID m_fieldId;
    };

    template <typename JavaT, typename Converter = JNI::Converter::Identity<JavaT>>
    typename Converter::converted_type GetStaticMember(JNIEnv* env, jclass objClass, const char* memberName) {
        jfieldID fieldId = env->GetStaticFieldID(objClass, memberName, JNI::TypeTraits<JavaT>::signature);
        JavaT rawVal = JNI::StaticFieldGetter<JavaT>(env, objClass, fieldId);
        Converter converter;

        return converter(env, rawVal);
    };
}

#endif //FACE2NAME_CLASSMEMBER_H
