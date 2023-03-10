/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package maretemodelmaker;

import java.util.ArrayList;

/**
 *
 * @author hwikgren
 */
public class Interpretation {
    private String transliteration;
    private Integer freq;
    private double relFreq;
    private ArrayList<OriginalMdc> originalsMDCsInAes;
    private ArrayList<OriginalLemma> originalsLemmasInAes;

    public Interpretation(String transliteration, Integer freq, double relFreq) {
        this.transliteration = transliteration;
        this.freq = freq;
        this.relFreq = relFreq;
    }

    public void setOriginalMDCs(ArrayList<OriginalMdc> originals) {
        this.originalsMDCsInAes = new ArrayList<>();
        this.originalsMDCsInAes = originals;
    }

    public ArrayList<OriginalMdc> getOriginalMDCs() {
        return originalsMDCsInAes;
    }
    
    public void setOriginalLemmas(ArrayList<OriginalLemma> originals) {
        this.originalsLemmasInAes = new ArrayList<>();
        this.originalsLemmasInAes = originals;
    }

    public ArrayList<OriginalLemma> getOriginalLemmas() {
        return originalsLemmasInAes;
    }
    
}
