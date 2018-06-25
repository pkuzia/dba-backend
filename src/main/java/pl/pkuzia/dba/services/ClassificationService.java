package pl.pkuzia.dba.services;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.stereotype.Component;
import pl.pkuzia.dba.domains.Classification;

import java.io.FileWriter;
import java.io.IOException;

@Component
public class ClassificationService {

    public Classification classifyDrive(String data) {
        ClassLoader classLoader = getClass().getClassLoader();
        Booster booster = null;
        DMatrix dataMatrix = null;
        try {
            booster = XGBoost.loadModel(classLoader.getResource("model.bin").getPath());
            FileWriter dataFile = new FileWriter(classLoader.getResource("classifyData.txt").getPath());
            dataFile.write(data);
            dataFile.close();
            dataMatrix = new DMatrix(classLoader.getResource("classifyData.txt").getPath());

        } catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            float[][] predict = booster.predict(dataMatrix);
            return mapPrediction(predict[0][0]);

        } catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
            return null;
        }
    }

    private Classification mapPrediction(float predict) {
        if (predict == 0.0) {
            return Classification.SOFT;
        } else if (predict == 1.0) {
            return Classification.OPTIMAL;
        } else if (predict == 2.0) {
            return Classification.HARD;
        }
        return Classification.UNKNOWN;
    }
}
