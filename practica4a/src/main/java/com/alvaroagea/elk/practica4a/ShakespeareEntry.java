package com.alvaroagea.elk.practica4a;

public class ShakespeareEntry {
    private final int lineID;
    private final String playName;
    private final int speechNumber;
    private final String lineNumber;
    private final String speaker;
    private final String textEntry;
    private final float scoring;


    public ShakespeareEntry(int lineID, String playName, int speechNumber,
                            String lineNumber, String speaker, String textEntry, float scoring) {
        this.lineID = lineID;
        this.playName = playName;
        this.speechNumber = speechNumber;
        this.lineNumber = lineNumber;
        this.speaker = speaker;
        this.textEntry = textEntry;
        this.scoring = scoring;
    }

    public int getLineID() {
        return lineID;
    }

    public String getPlayName() {
        return playName;
    }

    public int getSpeechNumber() {
        return speechNumber;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getTextEntry() {
        return textEntry;
    }

    public float getScoring() {
        return scoring;
    }
}
