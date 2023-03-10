/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maretelexiconmaker;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 *
 * @author Heidi Jauhiainen
 * University of Helsinki, department of Digital Humanities
 * Machine-readable Texts for Egyptologists project
 * Funded by Kone Foundation
 * 
 * This tool extracts encoding-transliteration pairs from two separate files where the lines are aligned
 * (as in the Ramses Translitteration Software Corpus)
 * Only gets words from line-pares that have equally many tokens.
 * The encoded words have to be separated with an underscore ' _ '.
 * The default mode deals with transliteration where characters have been separated with white space and words with underscore ' _ '.
 * 
 * The tool has been developed to deal with the Ramses Translitteration Software corpus. 
 */
public class MaReTELexiconMaker {

    /**
     * @param args the command line arguments
     * 0 = name of the file with the encoded hieroglyphic text lines or aligned hieroglyphic and transliteration lines
     * 1 = optional name of the file with the respective transliteration lines
     * 1 or 2 = name of the file where the extracted words are saved in <filename>
     * 
     * The words are saved in file
     *      words_<filename>
     */
    
    private static TreeMap<String, TreeMap<String, Integer>> words;
    private static Boolean separated;
    
    public static void main(String[] args) throws IOException {
        boolean oneFile = false;
        String transFile = "";
        String mdcFile = args[0];
        if (args.length == 3) {
            transFile = args[1];
        }
        else {
            oneFile = true;
        }
        String filename = args[args.length-1];
        separated = false;
        BufferedReader mdcReader = null;
        BufferedReader transReader = null;
        String mdcLine, transLine;
        String[] mdcArray, transArray;
        words = new TreeMap<>();
        try {
            mdcReader = new BufferedReader(new FileReader(mdcFile));
            if (!transFile.equals("")) {
                transReader = new BufferedReader(new FileReader(transFile));
            }
            mdcLine = mdcReader.readLine();
            if (oneFile) {
                transLine = mdcReader.readLine();
            }
            else {
                transLine = transReader.readLine();
            }
            while (mdcLine != null && transLine != null) {
                mdcArray = mdcLine.split("_");
                //correcting some recurring inconsistant translations in the Ramses Corpus
                //these are corrected to correspond to the encoded words
                /*transLine = transLine.replaceAll("b A - n - r a _ m r y - i m n", "b A - n - r a - m r y - i m n");
                transLine = transLine.replaceAll("n b - m A a . t - r a _ m r y - i m n", "n b - m A a . t - r a - m r y - i m n");
                transLine = transLine.replaceAll("r a - m s - s w _ m r y - i m n _ n T r", "r a - m s - s w - m r y - i m n - n T r");
                transLine = transLine.replaceAll("r a - m s - s w _ m A a . t - p t H", "r a - m s - s w - m A a . t - p t H");
                */
                transArray = transLine.split("_");
                //get words if line pairs are of even length
                if (mdcArray.length == transArray.length) {
                    getWords(mdcLine, transLine);
                    //Uncomment to align the lines to Stdout
                    /*System.out.println(mdcLine);
                    System.out.println(transLine);
                    System.out.println("");*/
                }
                //get next lines
                if (oneFile) {
                    mdcReader.readLine();
                    mdcLine = mdcReader.readLine();
                    transLine = mdcReader.readLine();
                }
                else {
                    mdcLine = mdcReader.readLine();
                    transLine = transReader.readLine();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            mdcReader.close();
            if (transReader != null) {
                transReader.close();
            }
        }
        printWords(words, filename);
    }
    
    private static void getWords(String mdcLine, String transLine) {
        String mdc, translit;
        TreeMap<String, Integer> translits;
        int count;
        //Tested with different kinds of dictionaries from the Ramses Corpus training sets lines of equal number of tokens
            // in order to use with Needleman-Wunch algorithm (MaReTENeedlemanWunch.jar) to align lines of uneven lenght in the same Corpus
        //if (!mdcLine.contains("MISSING") && !mdcLine.contains("SHADED") && !transLine.matches(".*[\\[/?<].*") && !transLine.matches(".*_ \\([^\\)]+\\) _.*") && !mdcLine.contains("LACUNA")) {
        //if  (!mdcLine.contains("MISSING") && !mdcLine.contains("SHADED") && !transLine.matches(".*[\\[/\\?<].*") && !transLine.matches(".*_ \\([^\\)]+\\) _.*") && !transLine.matches("\\([^\\)]+\\) _.*")  && !mdcLine.contains("LACUNA")) {
        //if  (!mdcLine.contains("MISSING") && !transLine.matches(".*[\\[/\\?<].*") && !transLine.matches(".*_ \\([^\\)]+\\) _.*") && !transLine.matches("\\([^\\)]+\\) _.*")) {
        String[] mdcArray = mdcLine.split("_");
        String[] transArray = transLine.split("_");
        //the best method is to take all line pairs of the same lenght but to leave out words that are partly or totally missing
        //and words that have been added to the transliteration by the editors
        for (int i=0; i<mdcArray.length-1; i++) {
            mdc = mdcArray[i].trim();
            translit = transArray[i].trim();
            /*if (translit.equals("p A y . f") && mdc.equals("-")) {
                System.out.println(mdcLine);
                System.out.println(transLine);
            }*/
            //leave out words that are totally or partly missing
            if (!mdc.equals("MISSING") && 
                    !mdc.contains("SHADE") && 
                    !translit.contains("[") && 
                    !translit.equals("/ /") && 
                    !translit.contains("?") && 
                    !translit.contains("/ /") &&
                    !mdc.equals("-") &&
                    !translit.equals("-") &&
                    !mdc.equals("LACUNA")
                    ) {
                translits = new TreeMap<>();
                count = 0;
                if (words.containsKey(mdc)) {
                    translits = words.get(mdc);
                }
                if (translits.containsKey(translit)) {
                    count = translits.get(translit);
                }
                translits.put(translit, count+1);
                //System.out.println(mdc);
                words.put(mdc, translits);
            }
        }
    }
    
    private static void printWords(TreeMap<String, TreeMap<String, Integer>> words, String filename) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("words_"+filename));
        
            for (Map.Entry<String, TreeMap<String, Integer>> entry : words.entrySet()) {
                TreeMap<String, Integer> translits = entry.getValue();
                for (Map.Entry<String, Integer> entry2 : translits.entrySet()) {
                   String translit = entry2.getKey();
                   if (!separated) {
                       translit = translit.replaceAll(" ", "");
                   }
                   writer.write(entry.getKey()+"\t"+translit+"\t"+entry2.getValue()+"\n");
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
    
}
