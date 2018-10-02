package pl.pkuzia.dba.controllers;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.pkuzia.dba.domains.Classification;
import pl.pkuzia.dba.responses.ClassificationResponse;
import pl.pkuzia.dba.services.ClassificationService;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static pl.pkuzia.dba.domains.Classification.*;

/**
 * Created by Przemysław Kuzia on 01.05.2018.
 */
@RestController
public class MachineLearningController {

    @Autowired
    private ClassificationService classificationService;

    @RequestMapping(value = "/ml-data", method = RequestMethod.POST)
    public ResponseEntity saveMLData(@RequestParam(value = "data") String data) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            FileWriter dataFile = new FileWriter(classLoader.getResource("data.txt").getPath(), true);
            dataFile.write(data);
            dataFile.write("\n");
            dataFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/classify", method = RequestMethod.POST)
    public ResponseEntity classifyData(@RequestParam(value = "data") String data) {
        Classification classification = classificationService.classifyDrive(data);
        ClassificationResponse classificationResponse = new ClassificationResponse(classification);
        return new ResponseEntity(classificationResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/learn-model", method = RequestMethod.POST)
    public void learnModel() {

        DMatrix trainMat = null;
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            trainMat = new DMatrix(classLoader.getResource("learn-data.t").getPath());
        } catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
        }
        DMatrix testMat = null;
        DMatrix testMat2 = null;
        try {
            testMat = new DMatrix(classLoader.getResource("test-data.t").getPath());
            testMat2 = new DMatrix(classLoader.getResource("test-data").getPath());
        } catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("eta", 0.5);
        params.put("num_class", 4);
        params.put("max_depth", 3);
        params.put("silent", 0);
        params.put("booster", "gbtree");
        params.put("objective", "multi:softmax");


        HashMap<String, DMatrix> watches = new HashMap<String, DMatrix>();
        watches.put("train", trainMat);
        watches.put("test", testMat);

        int round = 2;
        Booster booster = null;
        try {
            booster = XGBoost.train(trainMat, params, round, watches, null, null);

        } catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
        }
        try {
            float[][] predicts = booster.predict(testMat);
            System.out.println(calculateError(covertPredictsArray(predicts), testMat.getLabel()));
            confusionMatrixList(covertPredictsArray(predicts), testMat.getLabel());
            booster.saveModel("model.bin");
            float[][] predicts2 = booster.predict(testMat2);
            System.out.println(predicts2);
        } catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
        }
    }

    void confusionMatrixList(List<Float> predictsArray, float[] labels) {

        /* A - Soft, B - Optimal, C - Aggressive */

        int ATruePositive = 0;
        int BTruePositive = 0;
        int CTruePositive = 0;

        int AMistakeToB = 0;
        int AMistakeToC = 0;

        int BMistakeToA = 0;
        int BMistakeToC = 0;

        int CMistakeToA = 0;
        int CMistakeToB = 0;

        for (int i = 0; i < predictsArray.size(); i++) {
            Classification type = mapToClassificationType(labels[i]);
            if (predictsArray.get(i) == labels[i]) {
                switch (type) {
                    case SOFT: {
                        ATruePositive++;
                    }
                    case OPTIMAL: {
                        BTruePositive++;
                    }
                    case HARD: {
                        CTruePositive++;
                    }
                }
            } else {
                switch (type) {
                    case SOFT: {
                        if (mapToClassificationType(predictsArray.get(i)) == OPTIMAL) {
                            AMistakeToB++;
                        }
                        if (mapToClassificationType(predictsArray.get(i)) == HARD) {
                            AMistakeToC++;
                        }
                    }
                    case OPTIMAL: {
                        if (mapToClassificationType(predictsArray.get(i)) == SOFT) {
                            BMistakeToA++;
                        }
                        if (mapToClassificationType(predictsArray.get(i)) == HARD) {
                            BMistakeToC++;
                        }
                    }
                    case HARD: {
                        if (mapToClassificationType(predictsArray.get(i)) == SOFT) {
                            CMistakeToA++;
                        }
                        if (mapToClassificationType(predictsArray.get(i)) == OPTIMAL) {
                            CMistakeToB++;
                        }
                    }
                }
            }
        }
        System.out.println("Complete");
    }

    float calculateError(List<Float> predictsArray, float[] labels) {
        int score = 0;
        for (int i = 0; i < predictsArray.size(); i++) {
            if (predictsArray.get(i) == labels[i]) {
                score ++;
            }
        }
        return 100 - (float)score / predictsArray.size() * 100;
    }

    public List<Float> covertPredictsArray(float[][] predicts) {
        List<Float> predictsArray = new ArrayList<Float>();
        for (float[] predictItem: predicts) {
            predictsArray.add(predictItem[0]);
        }
        return predictsArray;
    }
}
