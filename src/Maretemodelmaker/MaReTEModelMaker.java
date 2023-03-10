/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package maretemodelmaker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import com.google.gson.*;

/**
 *
 * @author Heidi Jauhiainen
 * University of Helsinki, department of Digital Humanities
 * Machine-readable Texts for Egyptologists project
 * Funded by Kone Foundation
 * 
 * This tool writes transliteration models as wordlists into JSON-files
 */
public class MaReTEModelMaker {

    /**
     * @param args the command line arguments
     * 0 list of wordslists that are turned into one model (separated by comma ',')
     * 1 name of the model (is written in the beginning of the json file)
     * 2 source of the model (is written in the beginning of the json file)
     * 
     * Requires gson-2.10.jar
     * 
     * prints to stdout
     */
    
    private static TreeMap<String, TreeMap<String, TreeMap<Integer, ArrayList<String>>>> words;
    private static Model model;
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        words = new TreeMap<>();
        String[] fileArray = args[0].split(",");
        for (String file : fileArray) {
            //readWordsFromBinary(file);
            readWords(file);
        }
        String modelName = args[1];
        String modelSource = args[2];
        model = new Model(modelName, modelSource);
        //printJson(words);
        printGson(words);
    }
    
    private static void readWords(String filename) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line;
            String[] lineArray;
            TreeMap<String, TreeMap<Integer, ArrayList<String>>> translits;
            TreeMap<Integer, ArrayList<String>> counts;
            ArrayList<String> originals;
            while ((line = reader.readLine()) != null) {
                lineArray = line.split("\t");
                translits = new TreeMap<>();
                if (words.containsKey(lineArray[0])) {
                    translits = words.get(lineArray[0]);
                }
                counts = new TreeMap<>();
                if (translits.containsKey(lineArray[1])) {
                    counts = translits.get(lineArray[1]);
                }
                originals = new ArrayList<>();
                int count = Integer.parseInt(lineArray[2]);
                int oldCount = 0;
                originals = new ArrayList<>();
                if (!counts.isEmpty()) {
                    oldCount = counts.firstKey();
                    originals = counts.firstEntry().getValue();
                    counts.remove(oldCount);
                }
                if (lineArray.length > 3) {
                    for (int i=3; i<lineArray.length; i++) {
                        if (!originals.contains(lineArray[i])) {
                            originals.add(lineArray[i]);
                        }
                    }
                }
                counts.put(oldCount+count, originals);
                translits.put(lineArray[1], counts);
                words.put(lineArray[0], translits);
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
        }
    }
    
    private static void printGson(TreeMap<String, TreeMap<String, TreeMap<Integer, ArrayList<String>>>> words) {
        DecimalFormat df = new DecimalFormat("##0.00");
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        for (Map.Entry<String, TreeMap<String, TreeMap<Integer, ArrayList<String>>>> entry : words.entrySet()) {
            TreeMap<String, TreeMap<Integer, ArrayList<String>>> translits = entry.getValue();
            Map<Integer, TreeMap<String, ArrayList<String>>> sortedTranslits = sortTranslits(translits);
            ArrayList<String> originals = new ArrayList<>();
            int freq = getFreq(translits);
            String mdc = entry.getKey();
            Word word = new Word(mdc);
            for (Map.Entry<Integer, TreeMap<String, ArrayList<String>>> entry2 : sortedTranslits.entrySet()) {
                for (Map.Entry<String, ArrayList<String>> entry3 : entry2.getValue().entrySet()) {
                    String translit = entry3.getKey();
                    int count = entry2.getKey();
                    String relFreq = df.format((count*1.0/freq)*100.0);
                    Double relFreqNr = Double.parseDouble(relFreq);
                    originals = entry3.getValue();
                    Interpretation interpret = new Interpretation(translit, count, relFreqNr);
                    if (!originals.isEmpty()) {
                        ArrayList<OriginalLemma> originalLemmas = new ArrayList<>();
                        for (String orig : originals) {
                            if (orig.contains("lemma:")) {
                                OriginalLemma lemma = new OriginalLemma(orig);
                                originalLemmas.add(lemma);
                            }
                        }
                        interpret.setOriginalLemmas(originalLemmas);
                    }
                    word.setInterpret(interpret);
                }
            }
            model.setWord(word);
        }
        String jsonOutput = gson.toJson(model);
        System.out.println(jsonOutput);
    }
    
    private static Map<Integer, TreeMap<String, ArrayList<String>>> sortTranslits(TreeMap<String, TreeMap<Integer, ArrayList<String>>> translits) {
        TreeMap<Integer, TreeMap<String, ArrayList<String>>> sortedTranslits = new TreeMap<>();
        TreeMap<String, ArrayList<String>> newTranslits;
        for (Map.Entry<String, TreeMap<Integer, ArrayList<String>>> entry : translits.entrySet()) {
            TreeMap<Integer, ArrayList<String>> counts = entry.getValue();
            newTranslits = new TreeMap<>();
            if (sortedTranslits.containsKey(counts.firstKey())) {
                newTranslits = sortedTranslits.get(counts.firstKey());
            }
            newTranslits.put(entry.getKey(), counts.get(counts.firstKey()));
            sortedTranslits.put(counts.firstKey(), newTranslits);
        }
        return sortedTranslits.descendingMap();
    }
    
    private static Integer getFreq(TreeMap<String, TreeMap<Integer, ArrayList<String>>> translits) {
        int count = 0;
        for (Map.Entry<String, TreeMap<Integer, ArrayList<String>>> entry : translits.entrySet()) {
            count += entry.getValue().firstKey();
        }
        return count;
    }
    
}
