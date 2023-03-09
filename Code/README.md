### The Scripts used for creating the transliteration models

The jar-files were used as follows:

1. Get wordlist from Aes data with MaReTEAesVocabularyMaker.jar
   * `java -jar MaReTEAesVocabularyMaker.jar <AesFiles folder>`
   * \> AesWords
   * uses LetterConversion.txt (see folder ../Files)
2. Clean the Ramses data with CleanRamsesTexts.jar
   - add _ after lacuna and missing and turn shaded* to shaded
	- turn transliterations to mdc
	- remove empty "words"
	- correct some repeated misalignments (e.g. U5 D36 X1 H6 Y1 Z2 Q3 X1 V28 A1 is always transliterated with 2 words r a - m s - s w _ m A a . t - p t H)
   * `java -jar CleanRamsesTexts.jar <filename>`
       - add type (src or tgt) if the filename does not start with it
   * \> src-sep-train_cleaned
3. Get wordlist from the lines with even number of lines in the Ramses data with MaReTELexiconMaker.jar
   * `java -jar MaReTELexiconMaker.jar src-sep-train_cleaned tgt-train_cleaned <name>`
   * \> words_\<name\>
4. Get lines with uneven number of words from Ramses data with MaReTEGetUnevenLines.jar
   * `java -jar MaReTEGetUnevenLines.jar src-sep-train_cleaned tgt-train_cleaned <name>`
   * \> src_\<name\>
   * \> tgt_\<name\>
5. Align src_ramsesUneven and tgt_ramsesUneven with MaReTENeedlemanWunsch.jar
   * `java -jar MaReTENeedlemanWunsch.jar src_ramsesUneven tgt_ramsesUneven words_ramsesEven,AesWords`
   * \> alignedLines
6. Get wordlist from the aligned lines
   * `java -jar MaReTELexiconMaker.jar alignedLines ramsesAligned`
7. Make transliteration models
   * `java -jar MaReTEModelMaker <list;of;wordlists;to;use> <name of model> <source of model> > Stdout
