/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package mareteaesvocabularymaker;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Heidi Jauhiainen
 University of Helsinki, department of Digital Humanities
 Machine-readable Texts for Egyptologists project
 Funded by Kone Foundation
 
 This tool extract words that have encoding from the Thesaurus Linguae Aegyptiaca data release 
 that was published in Json-form by Simon Schweitzer 
 as "AES - Ancient Egyptian Sentences; Corpus of Ancient Egyptian sentences for corpus-linguistic research."
 https://github.com/simondschweitzer/aes
 
 The transliterations are normalised to the conventions used in the Ramses Translitteration Software corpus.
 Produces a list of encoded words - transliteration pairs, their frequency, and original hieros and lemma-form fields from Aes.
 */
public class MaReTEAesVocabularyMaker {

    /**
     * @param args the command line arguments
     * 0 = name of the folder containing the aes json-files
     * 
     * writes to file named aesWords, a folder to save it to can be given as argument
     * 
     */

    private static TreeMap<String, TreeMap<String, TreeMap<Integer, ArrayList<String>>>> words;
    private static boolean separated;
    private static TreeMap<Character, Character> letters;
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String folderName = args[0];
        String folder = "";
        
        //
        //change to true to produce words with character separated by whitespace as in Ramses corpora
        //e.g. 'n f r' instead of 'nfr'
        separated = false;
        readLetters("LetterConversion.txt");
        getWords(folderName);
        printWords(folder);
    }
    
    //Read the Aes files in the folder given
    //and get the tansliteration (= hieros), hieros (= hiero) and lemma_form
    private static void getWords(String folderName) {
        BufferedReader reader = null;
        File folder = new File(folderName);
        words = new TreeMap<>();
        String line = "";
        for (File file : folder.listFiles()) {
            try {
                reader = new BufferedReader(new FileReader(file));
                String translit = "", hieros, original = "", lemma_form = "";
                boolean lemma = false;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("\"token")) {
                        
                        while (!line.equals("]")) {

                            while (!line.equals("}")) {
                                 if (line.equals("{")) {
                                    translit = "";
                                    lemma_form = "";
                                    hieros = "";
                                }

                                 if (line.startsWith("\"mdc")) {
                                     lemma = true;
                                     original = getToken(line);
                                 }
                                 if (line.startsWith("\"lemma_form")) {
                                     lemma_form = getToken(line).trim();
                                     translit = getToken(line);
                                     
                                 }
                                 if (line.startsWith("\"hiero\"") && !translit.equals("") && !translit.equals("_") && !translit.matches(".*[⸢\\[\\?].*")) {
                                     hieros = getToken(line);
                                     hieros = editMdc(hieros);
                                     translit = editNumber(translit, original);
                                     translit = cleanTranslit(translit);
                                     translit = transcribeLemma(translit);
                                     if (!hieros.isBlank()) {
                                         addWord(hieros, translit, original, lemma_form);
                                     }
                                 }
                                 line = reader.readLine();
                                 if (line != null) {
                                     line = line.trim();
                                 }
                            }
                            lemma = false;
                            line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            else {
                                line = line.trim();
                            }
                         }
                     }
                }
            }
            catch (Exception e) {
                System.out.println(e+"\t"+line);
            }
            finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(MaReTEAesVocabularyMaker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    //find the value in the JSON formatted line
    private static String getToken(String line) {
        String word = line.split("\"")[3];
        return word;
    }
    
    //Clean extra characters from tranliterations
    private static String cleanTranslit(String translit) {
        translit = translit.replaceAll("~", "-");
        if (translit.matches(".*\\u2329.*") || translit.matches(".*\\[.*")) {
            translit = translit.replaceAll("\\{[^\\}]*\\}", "");
        }
        translit = translit.replaceAll("[\\{\\}\\[\\]]", "");
        translit = translit.replaceAll("[\\(\\)!〈〉\\u2329\\u232A:]", "");
        translit = translit.replaceAll("[⸮\\?\\u2E22\\u2E23]", "");
        translit = translit.replaceAll("-$", "");
        //both . and , are used in Aes (, is more frequent
        //sometimes Aes has both . and , for the same word (e.g. 2,nw.t, 2.nw.t for 2 W24 X1)
        //replace all , with . already for the "original" transliteration
        translit = translit.replaceAll(",", ".");
        return translit.trim();
    }
    
    //transcribe the transliteration to the same convention as used in Ramses corpus
    private static String transcribe(String translit) {
        translit = translit.replaceAll("z", "s");
        translit = translit.replaceAll("i", "y");
        translit = translit.replaceAll("j", "i");
        translit = translit.replaceAll("u", "w");
        translit = translit.replaceAll("i̯", "i");
        translit = translit.replaceAll("\\.pl", "");
        if (translit.matches("[1-9]\\..*")) {
            translit = translit.replaceFirst("\\.", "-");
        }
        if (separated) {
            translit = translit.replaceAll("", " ").trim();
        }
        return translit.trim();
    }
    
    private static String transcribeLemma(String translit) {
        String newTrans = "";
        translit = translit.replaceAll("i̯", "i");
        for (char letter : translit.toCharArray()) {
            if (letters.containsKey(letter)) {
                letter = letters.get(letter);
            }
            newTrans += letter;
        }
        translit = newTrans;
        translit = translit.replaceFirst("\\.[i]$", "i");
        translit = translit.replaceFirst("\\.[y]$", "y");
        translit = translit.replaceAll("\\.pl", "");
        if (translit.matches("[1-9]\\..*")) {
            translit = translit.replaceFirst("\\.", "-");
        }
        if (separated) {
            translit = translit.replaceAll("", " ").trim();
        }
        return translit.trim();
    }
    
    private static String editNumber(String translit, String hieros) {
        if (translit.matches(".*\\.\\.[n0-9].*")) {
            translit = hieros;
        }
        return translit;
    }
    
    //clean extra characters from the encoding
    private static String editMdc(String mdc) {
        mdc = mdc.replaceAll(" ?\\. ?", "");
        mdc = mdc.replaceAll("-", " ");
        mdc = mdc.replaceAll("!", "");
        return mdc.trim();
    }
    
    //Add the word, its transliteration and update their frequency to the treeamp
    // also add original forms of hieros and lemma_form
    private static void addWord(String mdc, String translit, String original, String lemma_form) {
        TreeMap<String, TreeMap<Integer, ArrayList<String>>> translits = new TreeMap<>();
        TreeMap<Integer, ArrayList<String>> counts = new TreeMap<>();
        ArrayList<String> originals = new ArrayList<>();
        
         if (words.containsKey(mdc)) {
             translits = words.get(mdc);
         }
         int count = 0;
         if (translits.containsKey(translit)) {
             counts = translits.get(translit);
             count = counts.firstKey();
             originals = counts.get(count);
             counts.remove(count);
         }
         if (!originals.contains(original)) {
             originals.add(0, original);
         }
         if (!lemma_form.equals("") && !originals.contains("lemma:"+lemma_form)) {
             originals.add("lemma:"+lemma_form);
         }
         counts.put(count+1, originals);
         translits.put(translit, counts);
         words.put(mdc, translits);
    }
    
    
    private static void printWords(String folder) throws IOException { 
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(folder+"aesWords"));
            String toFile;
            for (Map.Entry<String, TreeMap<String, TreeMap<Integer, ArrayList<String>>>> entry : words.entrySet()) {
                //toFile = "";
                TreeMap<String, TreeMap<Integer, ArrayList<String>>> translits = entry.getValue();
                for (Map.Entry<String, TreeMap<Integer, ArrayList<String>>> entry2 : translits.entrySet()) {
                    toFile = "";
                    TreeMap<Integer, ArrayList<String>> counts = entry2.getValue();
                    toFile += entry.getKey()+"\t"+entry2.getKey()+"\t"+counts.firstKey()+"\t";
                    for (String original : counts.get(counts.firstKey())) {
                        toFile += original+"\t";
                    }
                    writer.write(toFile+"\n");
                }
                
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            writer.close();
        }
    }
    
    private static void readLetters(String filename) throws IOException {
        BufferedReader reader = null;
        letters = new TreeMap<>();
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineArray = line.split("\t");
                letters.put(lineArray[0].charAt(0), lineArray[1].charAt(0));
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
        }
    }
}
