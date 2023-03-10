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
public class Word {
    private String encoding;
    private ArrayList<Interpretation> interpretations;
    
    public Word(String mdc) {
        this.encoding = mdc;
        this.interpretations = new ArrayList<>();
    }

    public void setInterpret(Interpretation interpret) {
        this.interpretations.add(interpret);
    }
    
    
}
