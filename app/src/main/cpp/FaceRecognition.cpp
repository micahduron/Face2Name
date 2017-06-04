//
// Created by micah on 6/3/17.
//

#include <android/log.h>
#include "FaceRecognition.h"

void FaceRecognition::trainModel(const std::vector<cv::Mat>& images, const std::vector<cv::String>& labelStrings) {
    assert(images.size() == labelStrings.size());

    std::vector<int> labelIndexes = addLabels(labelStrings);

    assert(labelIndexes.size() == images.size());

    m_faceRecognizer->train(images, labelIndexes);
}

void FaceRecognition::addToModel(cv::Mat image, cv::String label) {
    std::vector<cv::Mat> imageArray{ image };
    std::vector<cv::String> labelArray{ label };

    addToModel(imageArray, labelArray);
}

void FaceRecognition::addToModel(const std::vector<cv::Mat>& images, const std::vector<cv::String>& labelStrings) {
    std::vector<int> labelIndexes = addLabels(labelStrings);

    m_faceRecognizer->update(images, labelIndexes);
}

double FaceRecognition::identify(cv::Mat image, cv::String& labelString) {
    int predictedLabel;
    double predictionConfidence;

    m_faceRecognizer->predict(image, predictedLabel, predictionConfidence);

    __android_log_print(ANDROID_LOG_DEBUG, "FaceRecognition", "Prediction: Label = %d, Confidence = %f", predictedLabel, predictionConfidence);

    labelString = m_faceRecognizer->getLabelInfo(predictedLabel);

    return predictionConfidence;
}

std::vector<int> FaceRecognition::addLabels(const std::vector<cv::String>& labelStrings) {
    std::vector<int> labelIndexes;
    labelIndexes.reserve(labelStrings.size());

    for (size_t i = 0; i < labelStrings.size(); ++i, ++m_counter) {
        m_faceRecognizer->setLabelInfo(m_counter, labelStrings[i]);

        __android_log_print(ANDROID_LOG_DEBUG, "FaceRecognition", "Label '%s' added at index %d.", labelStrings[i].c_str(), m_counter);

        labelIndexes.push_back(m_counter);
    }
    return labelIndexes;
}