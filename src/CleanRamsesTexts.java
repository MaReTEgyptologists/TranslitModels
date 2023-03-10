/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package cleanramsestexts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Heidi Jauhiainen
 * University of Helsinki, department of Digital Humanities
 * Machine-readable Texts for Egyptologists project
 * Funded by Kone Foundation
 * 
 * This tool cleans the Ramses Translitteration Software corpus files
 *      - add _ after lacuna and missing and turn shaded* to shaded
	- turn transliterations to mdc
	- remove empty "words"
 * 
 */
public class CleanRamsesTexts {

    /**
     * @param args the command line arguments
     * 0 = name of the file to be cleaned
     * if filename does not start with src or tgt 
     *      args 1 needs to be added indicating whether the file contains encoding (src) or transliteration (tgt)
     * 
     * writes to filename(-.txt)_cleaned
     */
    public static void main(String[] args) throws IOException {
        String file = args[0];
        String type;
        String[] fileArray = file.split("/");
        type = fileArray[fileArray.length-1].replaceFirst("\\-.*", "");
        String filename = fileArray[fileArray.length-1];
        filename = filename.replaceFirst("\\..*", "");
        if (!type.equals("src") && !type.equals("tgt")) {
            if (args.length > 1) {
                type = args[1];
            }
            else {
                System.out.println("Add type 'src' or 'tgt' after the filename (src = encoding and tgt = transliteration)");
            }
        }
        if (type.equals("src")) {
            cleanSource(file, filename);
        }
        else if (type.equals("tgt")) {
            cleanTarget(file, filename);
        }
    }
    
    private static void cleanSource(String file, String filename) throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            writer = new BufferedWriter(new FileWriter(filename+"_cleaned"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("LACUNA", "LACUNA _");
                line = line.replaceAll("MISSING", "MISSING _");
                line = line.replaceAll("SHADED[^ ]", "SHADED");
                line = line.replaceAll(" n ", " N35 ");
                line = line.replaceAll(" A ", " G1 ");
                line = line.replaceAll(" f ", " I9 ");
                line = line.replaceAll(" t ", " X1 ");
                line = line.replaceAll(" i i ", " M17 M17 ");
                line = line.replaceAll(" i ", " M17 ");
                line = line.replaceAll(" nTrw ", " R8A ");
                line = line.replaceAll(" nn ", " M22 M22 ");
                line = line.replaceAll("^n ", "N35 ");
                line = line.replaceAll("^A ", "G1 ");
                line = line.replaceAll("^f ", "I9 ");
                line = line.replaceAll("^t ", "X1 ");
                line = line.replaceAll("^i ", "M17 ");
                line = line.replaceAll("^nTrw ", "R8A ");
                line = line.replaceAll("^nn ", "M22 M22 ");
                line = line.replaceAll("^_", "- _");
                line = line.replaceAll("_ _", "_ - _");
                line = line.replaceAll("_ _", "_ - _");
                writer.write(line+"\n");
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
            writer.close();
        }
    }
    
    private static void cleanTarget(String file, String filename) throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            writer = new BufferedWriter(new FileWriter(filename+"_cleaned"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("b A - n - r a _ m r y - i m n", "b A - n - r a - m r y - i m n");
                line = line.replaceAll("n b - m A a . t - r a _ m r y - i m n", "n b - m A a . t - r a - m r y - i m n");
                line = line.replaceAll("r a - m s - s w _ m r y - i m n _ n T r", "r a - m s - s w - m r y - i m n - n T r");
                line = line.replaceAll("r a - m s - s w _ m A a . t - p t H", "r a - m s - s w - m A a . t - p t H");
                line = line.replaceAll("\\[ _ \\]", "[ ]");
                line = line.replaceAll("^_", "- _");
                line = line.replaceAll("_ _", "_ - _");
                line = line.replaceAll("_ _", "_ - _");
                writer.write(line+"\n");
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
            writer.close();
        }
    }
}
