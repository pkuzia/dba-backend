package pl.pkuzia.dba.responses;

import pl.pkuzia.dba.domains.Classification;

public class ClassificationResponse {

    private Classification classification;

    public ClassificationResponse(Classification classification) {
        this.classification = classification;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }
}