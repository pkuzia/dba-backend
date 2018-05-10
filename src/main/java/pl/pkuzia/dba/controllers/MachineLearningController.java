package pl.pkuzia.dba.controllers;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ml.dmlc.xgboost4j.java.DMatrix;

/**
 * Created by Przemysław Kuzia on 01.05.2018.
 */
@RestController
public class MachineLearningController {

    @RequestMapping("/hello")
    public String sayHello(@RequestParam(value = "name") String name) {
        return "Hello " + name;
    }


    @RequestMapping("ml-test")
    public void mlTest() {

        DMatrix trainMat = null;
        try {
            trainMat = new DMatrix("/Users/Przemo/Praca/Projekty/driving-behaviour-analyzer-web/src/main/resources/seismic");
        } catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
        }
        DMatrix testMat = null;
        try {
            testMat = new DMatrix("/Users/Przemo/Praca/Projekty/driving-behaviour-analyzer-web/src/main/resources/seismic.t");
        } catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("eta", 1.0);
        params.put("num_class", 4);
        params.put("max_depth", 10);
//        params.put("max_depth", 2);8
        params.put("silent", 0);
        params.put("booster", "gbtree");
        params.put("objective", "multi:softmax");


        HashMap<String, DMatrix> watches = new HashMap<String, DMatrix>();
        watches.put("train", trainMat);
        watches.put("test", testMat);

        int round = 60;
        Booster booster = null;
        try {
            booster = XGBoost.train(trainMat, params, round, watches, null, null);

        } catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
        }
        try {
            float[][] predicts = booster.predict(testMat);
            System.out.println(calculateError(covertPredictsArray(predicts), testMat.getLabel()));
            booster.saveModel("model.bin");
        } catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
        }
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
