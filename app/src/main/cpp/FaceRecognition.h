//
// Created by micah on 6/1/17.
//

#ifndef FACE2NAME_FACERECOGNITION_H
#define FACE2NAME_FACERECOGNITION_H

#include <vector>
#include <opencv2/core.hpp>
#include <opencv2/face.hpp>

class FaceRecognition {
public:
    FaceRecognition() : m_faceRecognizer{ cv::face::createLBPHFaceRecognizer() }
    {}

    void trainModel(const std::vector<cv::Mat>& images, const std::vector<cv::String>& labelStrings);

    void addToModel(cv::Mat image, cv::String label);

    void addToModel(const std::vector<cv::Mat>& images, const std::vector<cv::String>& labelStrings);

    double identify(cv::Mat image, cv::String& labelString);

private:
    std::vector<int> addLabels(const std::vector<cv::String>& labelStrings);

    cv::Ptr<cv::face::FaceRecognizer> m_faceRecognizer;
    int m_counter = 0;
};

#endif //FACE2NAME_FACERECOGNITION_H
