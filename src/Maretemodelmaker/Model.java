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
public class Model {
    private String modelName, modelSource, creator, organization, fundedBy, github;
    private ArrayList<Word> words;

    public Model(String name, String source) {
        this.modelName = name;
        this.modelSource = source;
        this.creator = "";
        this.organization = "";
        this.fundedBy = "";
        this.github = "";
        this.words = new ArrayList<>();
    }
    
    public void setWord(Word word) {
        this.words.add(word);
    }
}
