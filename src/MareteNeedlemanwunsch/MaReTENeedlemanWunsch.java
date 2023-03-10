/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package mareteneedlemanwunsch;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Heidi Jauhiainen
 * University of Helsinki, department of Digital Humanities
 * Machine-readable Texts for Egyptologists project
 * Funded by Kone Foundation
 * 
 * This tool aligns Manuel de Codage lines with the respective lines of transliteration.
 * Developed with the Ramses Translitteration software corpus and optimised for it.
 * 
 */
public class MaReTENeedlemanWunsch {

    /**
     * @param args the command line arguments
     * 0 = name of the file with the Manuel de Codage lines, where words have been segmented
     * 1 = name of the file with the respective transliteration lines with spaces between characters
     *      (if no spaces, change variable spaces to be false)
     * 2 = name of a vocabulary (with mdc-code and transliteration pairs separated by a tab), can be produced with MaReTELexiconMaker
     *      several vocabularies can be added by separating the names by comma (no spaces)
     * 
     * writes to a file called aligned_<args3> 
     * 
     * A file called ignore.txt with lines to be ignored (either mdc or transliteration or mixed) can be added to the folder from where the script is run
     * Also ignores lines with more than 3 words marked as MISSING
     * 
     * Writes the aligned lines to alignedLines
     */

    private static TreeMap<String, TreeSet<String>> keyset;
    private static ArrayList<String> ignored;
    private static TreeMap<String, TreeSet<String>> vocab;
    private static int penaltyScore;
    private static boolean spaces;
    private static BufferedWriter writer;
    
    public static void main(String[] args) throws IOException {
        //Name of the Manuel de Codage file
        String mdcFile = args[0];
        //Name of the transliteration file
        String transFile = args[1];
        //change to false, if the transliteration file does not have spaces between each character
        spaces = true;
        //the vocabulary for the words read from the vocabulary file(s)
        //several vocabularies can be added by separating the names by comma (no spaces)
        vocab = new TreeMap<>();
        String[] lists = args[2].split(",");
        for (String list : lists) {
            readWordlist(list);
        }
        writer = new BufferedWriter(new FileWriter("aligned_"+args[3]));
        //the penalty score used by the Needleman-Wunsch algorithm can be changed
        //-1 works best with the Ramses Translitteration Software corpus
        penaltyScore = -1;
        makeKeyset();
        //A file called ignore.txt with lines to be ignored (either mdc or transliteration or mixed) 
        //can be added to the folder from where the script is run
        readIgnored("ignore.txt");
        
        readLines(mdcFile, transFile);

    }
    
    //read the Manuel de Codage and transliteration files concurrently line by line
    //Split the lines into arrays of words and send them to Needleman-Wunsch algorithm
    private static void readLines(String mdcFile, String transFile) throws IOException {
        BufferedReader mdcReader = null;
        BufferedReader transReader = null;
        String mdcLine = "", transLine;
        String[] mdcArray, transArray;
        try {
            mdcReader = new BufferedReader(new FileReader(mdcFile));
            transReader = new BufferedReader(new FileReader(transFile));
            int count = 0;
            while ((mdcLine = mdcReader.readLine()) != null && (transLine = transReader.readLine()) != null) {
                
                mdcArray = mdcLine.split("_");
                //correcting some recurring inconsistant translations in the Ramses Corpus
                //these are corrected to correspond to the encoded words
                /*transLine = transLine.replaceAll("b A - n - r a _ m r y - i m n", "b A - n - r a - m r y - i m n");
                transLine = transLine.replaceAll("n b - m A a . t - r a _ m r y - i m n", "n b - m A a . t - r a - m r y - i m n");
                transLine = transLine.replaceAll("r a - m s - s w _ m r y - i m n _ n T r", "r a - m s - s w - m r y - i m n - n T r");
                transLine = transLine.replaceAll("r a - m s - s w _ m A a . t - p t H", "r a - m s - s w - m A a . t - p t H");
                */
                transArray = transLine.split("_");
                //Ignoring lines that have more than two words marked as missing
                count = 0;
                if (mdcLine.contains("MISSING")) {
                    for (String word : mdcArray) {
                        if (word.trim().equals("MISSING")) {
                            count++;
                        }
                    }
                }
                if (!ignored.contains(mdcLine.trim()) && !ignored.contains(transLine.trim()) && count < 4) {
                    needlemanWunch(mdcArray, transArray);
                }
                else {
                    //System.out.println(mdcLine);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e+"\t"+mdcLine);
        }
        finally {
            mdcReader.close();
            transReader.close();
            writer.close();
        }
    }
    
    //The algorithm to align the two lines passed to the method
    private static void needlemanWunch(String[] mdcArray, String[] transArray) throws IOException {
        String mdc, translit, direction;
        TreeSet<String> translits = new TreeSet<>();
        int top, left, topLeft;
        int points, highestScore;
        int[][] scores = new int[mdcArray.length+1][transArray.length+1];
        String[][] directions = new String[mdcArray.length+1][transArray.length+1];
        String transliteration = "", manuel = "";
        TreeMap<Integer, ArrayList<String>> scored = new TreeMap<>();
        //Build a table of scores and directions
        //column represent the words in the mdc-file and rows in the transliteration-file
        //First initiate the first row and first column 
        //scores: to numbers decending from 0
        //directions to T (for top) for first row and L (for left)
        int score = -1;
        scores[0][0] = 0;
        directions[0][0] = "done";
        for (int i=1; i<=mdcArray.length; i++) {
            scores[i][0] = score--;
            directions[i][0] = "T";
        }
        score = -1;
        for (int j=1; j<=transArray.length; j++) {
            scores[0][j] = score--;
            directions[0][j] = "L";
        }
        //Then fill the rest fo the table 
        //Check each mdc in the mdc-line
        for (int i=1; i<=mdcArray.length; i++) {
            mdc = mdcArray[i-1].trim();
            //get possible transliterations for this mdc from the vocabulary
            translits = getTranslitSet(mdc);
            //check each transliteration in the transliteration-line against the possible transliteration of the mdc
            for (int j=1; j<=transArray.length; j++) {
                translit = transArray[j-1].trim();
                //get the points for this spesific transliteration-mdc pair
                points = getPoints(translit, translits, mdc);
                int penalty = penaltyScore;
                //get the score from topleft cell and add points to it
                topLeft = scores[i-1][j-1] + points;
                //get the score from the cell above and add penalty to it
                top = scores[i-1][j] + penalty;
                //get the score from the cell to left and add penalty to it
                left = scores[i][j-1] + penalty;
                //get the highest score and the directions from where it was received
                highestScore = 0;
                direction = "";
                scored = new TreeMap<>();
                ArrayList<String> DirectionList = new ArrayList<>();
                //first add the score from top
                DirectionList.add("T");
                scored.put(top, DirectionList);
                //if score from the left is not the same as from top left
                //make a new DirectionList
                if (!scored.containsKey(left)) {
                    DirectionList = new ArrayList<>();
                }
                DirectionList.add("L");
                scored.put(left, DirectionList);
                //if the score from top left (i.e. diagonally) is the same as from top or the left
                //get the list from treemap scored
                if (scored.containsKey(topLeft)) {
                    DirectionList = scored.get(topLeft);
                }
                //otherwise make a new list
                else {
                    DirectionList = new ArrayList<>();
                }
                DirectionList.add("D");
                scored.put(topLeft, DirectionList);
                //get the directions with the highest score and add them to the tables scores and directions
                highestScore = scored.lastKey();
                DirectionList = scored.get(highestScore);
                for (String word : DirectionList) {
                    direction += ";"+word;
                }
                scores[i][j] = highestScore;
                directions[i][j] = direction;
            }
        }
        //to get the best alignment, start from the last words
        int j = transArray.length-1;
        int i = mdcArray.length-1;
        String tempMdc = "", tempTrans ="";
        String codage = "", trans = "";
        Stack<Node> stack = new Stack<>();
        int nextI = 0, nextJ = 0;
        TreeMap<Integer, ArrayList<String>> scoredLines = new TreeMap<>();
        direction = directions[i][j];
        //each Node-object knows its coordinates in the tables and one of the directions where the highest score came from
        Node newNode;
        direction = direction.replaceFirst(";", "");
        String[] directionArray = direction.split(";");
        //put the Nodes for each of the directions from the last words to a stack
        //if several directions, top left will be the fist to come out of the stack, 
        //      first of the alignments with the highest points is chosen
        for (String dir : directionArray) {
            newNode = new Node(i, j, dir);
            newNode.setPreviousString("", "");
            stack.push(newNode);
        }
        //check the way to the first words of the sentence for each of the directions
        //when several directions are encountered for one word pair, each of them gets its own Node
        //after reaching the first word, the method backs off to the latest junction
        //      each node knows the alignment and points so far
        while (!stack.empty()) {
            Node node = stack.peek();
            stack.remove(node);
            i = node.getI();
            if (i>=0) {
                tempMdc = node.getPreviousMdc();
                tempTrans = node.getPreviousTrans();
                points = node.getPreviousPoints();

                j = node.getJ();
                if (i>0) {
                    codage = mdcArray[i-1].trim();
                    translits = getTranslitSet(codage);
                }
                if (j>0) {
                    trans = transArray[j-1].trim();
                }
                //score the words again, points for each node on the way to the first words are added up!
                score = getPoints(trans, translits, codage);

                String thisDirection = node.getDirection();
                //depending on the direction, the words are aligned with each other or with '-'
                if (thisDirection.equals("D")) {
                    tempTrans = trans+" _ "+tempTrans;
                    //System.out.println(transliteration);
                    tempMdc = codage+" _ "+tempMdc;
                    nextI = i-1;
                    nextJ = j-1;
                }
                else if (thisDirection.equals("T")) {
                    tempTrans = "- _ "+tempTrans;
                    tempMdc = codage+" _ "+tempMdc;
                    nextI = i-1;
                    nextJ = j;
                }
                else if (thisDirection.equals("L")) {
                    tempTrans = trans+" _ "+tempTrans;
                    tempMdc = "- _ "+tempMdc;
                    nextI = i;
                    nextJ = j-1;
                }
                //get the direction of the next cell (according to the direction in this cell/node)
                direction = directions[nextI][nextJ];
                //when first words reached, save the possible alignment of the line with the points
                if (direction.equals("done")) {
                    transliteration = tempTrans;
                    manuel = tempMdc;
                    //only the first occurence for each points, are saved
                    if (!scoredLines.containsKey(points)) {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(manuel);
                        list.add(transliteration);
                        scoredLines.put(points, list);
                    }
                }
                //if not firsts words yet, save new Node to the stack for each of the directions of the following cell
                //as indicated by the direction of this Node
                else {
                    directionArray = direction.replaceFirst(";", "").split(";");
                    for (String dir : directionArray) {
                        newNode = new Node(nextI, nextJ, dir);
                        newNode.setPreviousString(tempMdc, tempTrans);
                        newNode.setPreviousPoints(points+score);
                        stack.push(newNode);
                    }
                }
            }
        }
        //After all possible routes are saved, get the aligned lines with the highest score
        int winningScore = scoredLines.lastKey();
        ArrayList<String> list = scoredLines.get(winningScore);
        //write the aligned lines to file <alingedLines>
        writer.write(list.get(0).trim()+" \n");
        writer.write(list.get(1).trim()+" \n");
        writer.write("\n");
    }
    
    //get the possible transliterations for the words from the vocabulary
    private static TreeSet<String> getTranslitSet(String mdc) {
        TreeSet<String> translits = new TreeSet<>();
        
        if (vocab.containsKey(mdc)) {
            translits = vocab.get(mdc);
            //System.out.println(mdc);
        }
        return translits;
    }
    
    //get the possible transliterations for some of the words that start with the same sign
    private static TreeSet<String> getMoreTranslits(String mdc, TreeSet<String> translits) {
        TreeSet<String> mdcs;
        String[] array = mdc.split(" ");
        if (array.length > 1) {
            String firstMdc = array[0];
            //get all words starting with the first mdc
            if (keyset.containsKey(firstMdc)) {
                mdcs = keyset.get(firstMdc);
                if (!mdcs.isEmpty()) {
                    for (String codeString : mdcs) {
                        //if the mdc is only 2 signs long or the 3 first signs are the same as with the word
                        //get the transliterations for this word (starting with the first sign)
                        String beginning = ""; 
                        if (array.length > 2) {
                            beginning = array[0]+" "+array[1]+" "+array[2];
                        }
                        //beginning = array[0]+" "+array[1];
                        if (codeString.startsWith(beginning)) {
                            //add the transliterations to the allready existing possible transliterations
                            translits.addAll(vocab.get(codeString));
                        }
                    }
                }

            }
            

        }
        return translits;
    }
    
    //Get score for the transliteration-mdc pair given as input
    private static Integer getPoints(String translit, TreeSet<String> translits, String mdc) {
        //basic score is the penalty score is set in the main-method
        int points = penaltyScore;
        //Ramses specific:
        //if transliteration is an insertion by the editor (e.g. '(Hr)' or '= <f>')
        //the penalty score is lower
        if (
                translit.matches("=? ?<.*") || 
                translit.matches("=? ?\\([^\\)]* \\)")
                ) {
            points = -5;
        }
        //if the transliteration is found in the possible transliterations for the mdc
        // score is the highest
        else if (translits.contains(translit)) {
            points = 5;
        }
        //if not found, make additional checks on the transliteration
        else if (checkTranslit(translit, translits)) {
                points = 4;
            }
        else {
            //get more possible transliterations
            translits = getMoreTranslits(mdc, translits);
            //if transliteration is found in the enlarged set of possible transliterations
            //score is marginally lower than the highest
            if (translits.contains(translit)) {
                points = 4;
            }
            //if not found, make additional checks on the transliteration 
            //or if mdc with SHADED 
            else if (checkTranslit(translit, translits) || (mdc.contains("SHADED") && translit.contains("["))) {
                points = 3;
            }
            //else if mdc has more than one sign, make further checks
            else if (mdc.split(" ").length > 1) {
                //if (checkReverse(translit, mdc)) {
                //if (checkFirstSign(translit, mdc)) {
                if (checkReverse(translit, mdc) || checkFirstSign(translit, mdc)) {
                    points = 3;
                }
            }
        }
        
        return points;
    }
    
    //Additional checks for the transliteration
    private static boolean checkTranslit(String translit, TreeSet<String> translits) {
        if (!translits.isEmpty()) {
            for (String trans : translits) {
                //check if this transliteration starts with one of the possible ones
                //or if one of the possible transliterations starts with this one
                if (trans.startsWith(translit) || translit.startsWith(trans)) {
                    return true;
                }
                //check if the first 3 characters are the same
                if (trans.length() > 2 && translit.length() > 2) {
                    if (trans.substring(0, 3).equals(translit.substring(0, 3))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    //check if words starting with the first signs reversed (e.g. A1 B1 Z2 > B1 A Z2) have the wanted transliteration
    private static boolean checkReverse(String translit, String mdc) {
        TreeSet<String> translits = new TreeSet<>();
        String[] array = mdc.split(" ");
        String newMdc = array[1]+" "+array[0];
        if (mdc.length() > 2) {
                for (int i=2; i<array.length; i++) {
                newMdc += " "+array[i];
            }
        }
        translits = getTranslitSet(newMdc);
        if (translits.contains(translit)) {
            return true;
        }
        return false;
    }
    
    private static String getMdc(String mdc) {
        if (!spaces) {
            mdc = mdc.replaceAll(" ", "");
        }
        return mdc;
    }
    
    //check if the transliteration starts with any of the transliterations for all words starting with the same sign 
    private static boolean checkFirstSign(String translit, String mdc) {
        TreeSet<String> translits = new TreeSet<>();
        String[] array = mdc.split(" ");
        translits = vocab.get(array[0]);
        if (translits != null) {
            for (String trans : translits) {
                if (translit.startsWith(trans)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    //reads a list of word pairs from a file and adds them to the local vocabulary
    //File format: mdc<tab>transliteration
    private static void readWordlist(String filename) throws IOException {
        BufferedReader reader = null;
        String[] lineArray;
        String mdc, translit;
        TreeSet<String> translits;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                lineArray = line.split("\\t");
                mdc = lineArray[0];
                translit = lineArray[1];
                String tempTranslit = "";
                if (spaces) {
                    for (int i=0; i<translit.length(); i++) {
                        tempTranslit += Character.toString(translit.charAt(i))+" ";
                    }
                }
                translit = tempTranslit;
                translits = new TreeSet<>();
                if (vocab.containsKey(mdc)) {
                    translits = vocab.get(mdc);
                }
                if (!translits.contains(translit)) {
                    translits.add(translit);
                }
                vocab.put(mdc, translits);
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    //Make a list of all sign-mdcs that appear in the first position of a word in the vocabulary
    //with all the possible word-mdcs starting with that sign
    private static void makeKeyset() {
        keyset = new TreeMap<>();
        TreeSet<String> mdcs;
        for (String mdc : vocab.keySet()) {
            //System.out.println(mdc);
            if (mdc.length() > 1) {
                String[] array = mdc.split(" ");
                String firstCode = array[0];
                mdcs = new TreeSet<>();
                if (keyset.containsKey(firstCode)) {
                    mdcs = keyset.get(firstCode);
                }
                mdcs.add(mdc);
                keyset.put(firstCode, mdcs);
            }
        }
    }
    
    //if files contain lines that should not be aligned (e.g. with mistakes) because it will not be possible
    //they can be given as a DirectionList in a file called ignore.txt
    //either mdc or transliteration line can be put in the file
    private static void readIgnored(String filename) throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        File file = new File(filename);
        ignored = new ArrayList<>();
        if (file.exists()) {
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!ignored.contains(line.trim())) {
                        ignored.add(line.trim());
                    }
                }
            }
            catch (Exception e) {
                System.out.println(e);
            }
            finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
    }
}
