//
// Created by micah on 6/2/17.
//

#ifndef FACE2NAME_FACEMODEL_H
#define FACE2NAME_FACEMODEL_H

#include <vector>
#include <opencv2/core.hpp>

class FaceModel {
public:
    FaceModel(size_t initialSize) {
        m_faceVec.reserve(initialSize);
        m_labelVec.reserve(initialSize);
    }

    void addToModel(cv::Mat face, cv::String label) {
        m_faceVec.push_back(face);
        m_labelVec.push_back(label);
    }

    const std::vector<cv::Mat>& getModelFaces() const noexcept {
        return m_faceVec;
    }

    const std::vector<cv::String>& getModelLabels() const noexcept {
        return m_labelVec;
    }

private:
    std::vector<cv::Mat> m_faceVec;
    std::vector<cv::String> m_labelVec;
};

#endif //FACE2NAME_FACEMODEL_H
