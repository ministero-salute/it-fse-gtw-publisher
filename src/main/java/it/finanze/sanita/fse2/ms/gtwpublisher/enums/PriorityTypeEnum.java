package it.finanze.sanita.fse2.ms.gtwpublisher.enums;

public enum PriorityTypeEnum {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String description;

    PriorityTypeEnum(String description) {
        this.description = description;
    }

    public String getCode() {
        return this.description;
    }
}
