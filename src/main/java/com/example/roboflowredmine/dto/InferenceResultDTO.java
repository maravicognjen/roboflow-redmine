package com.example.roboflowredmine.dto;

import java.util.ArrayList;
import java.util.List;

public class InferenceResultDTO {

    public Integer count = 0;

    public String imageBase64;

    public List<PredictionDTO> predictions = new ArrayList<>();
}