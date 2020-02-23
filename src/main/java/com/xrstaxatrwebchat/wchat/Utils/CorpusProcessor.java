package com.xrstaxatrwebchat.wchat.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.naming.Context;
import javax.naming.NamingException;

public class CorpusProcessor {

    private Set<String> dictSet = new HashSet<>();
    private Map<String, Double> freq = new HashMap<>();
    private Map<String, Double> dict = new HashMap<>();
    private Map<Double, String> revDict = new HashMap<>();
    private boolean countFreq;
    private InputStream is;
    private int rowSize;
    private int corpusSize;

    public CorpusProcessor(String filename, int rowSize, boolean countFreq) throws FileNotFoundException {
        this(new FileInputStream(filename), rowSize, countFreq);
    }

    public CorpusProcessor(InputStream is, int rowSize, boolean countFreq) {
        this.is = is;
        this.rowSize = rowSize;
        this.countFreq = countFreq;
    }

    public void start(Context ctx) throws IOException {

        int num = -1;

        try{
            num = (Integer) ctx.lookup("java:comp/env/ROW_SIZE");
        }catch (NamingException ne){
            ne.printStackTrace();
        }


        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line = "";
            while ((line = br.readLine()) != null) {

                String[] lns = line.split(" ");

                ArrayList<String> ls = new ArrayList<String>(Arrays.asList(lns));

                String[] outi = null;
                if(ls.size() < num){
                    outi = new String[ls.size()];
                    for(int i = 0; i < ls.size(); i++) {
                        outi[i] = ls.get(i);
                    }
                }else {
                    outi = new String[num];
                    for(int i = 0; i < num; i++) {
                        outi[i] = ls.get(i);
                    }

                }

                String str ="";
                for(int j=0;j<outi.length; j++){
                    str = str.concat(outi[j].toLowerCase() +" ");
                }

//	                String formatLine = StringFmtr.get(str);
                processLine(str, ctx);
//	                processLine(StringFmtr.truncateLine(str));
            }

        }

//	        is.close();


    }

    protected void processLine(String lastLine, Context context) {
        tokenizeLine(lastLine, dictSet, false, context);
    }

    // here we not only split the words but also store punctuation marks
    public void tokenizeLine(String lastLine,
                             Collection<String> resultCollection,
                             boolean addSpecials,
                             Context ctx) {

        String specials = "!\"#$;%^:?*()[]{}<>«»,.–—=+…";

//			try{
//				specials = (String) ctx.lookup("java:comp/env/SPECIALS");
//			}catch (NamingException ne){
//				ne.printStackTrace();
//			}


        String[] words = lastLine.split(" ");

        for (String word : words) {
            if (!word.isEmpty()) {
                boolean specialFound = true;
                while (specialFound && !word.isEmpty()) {
                    for (int i = 0; i < word.length(); ++i) {


                        int idx = specials.indexOf(word.charAt(i));
                        specialFound = false;
                        if (idx >= 0) {
                            String word1 = word.substring(0, i);
                            if (!word1.isEmpty()) {
                                addWord(resultCollection, word1);
                            }
                            if (addSpecials) {
                                addWord(resultCollection, String.valueOf(word.charAt(i)));
                            }
                            word = word.substring(i + 1);
                            specialFound = true;
                            break;
                        }
                    }
                }
                if (!word.isEmpty()) {
                    addWord(resultCollection, word);
                }
            }
        }
    }

    protected void setCorpusSize(int corpusSizeIn) {
        this.corpusSize = corpusSizeIn;
    }

//	    public int getCorpusSize() {
//	    	return this.corpusSize;
//	    }

    private void addWord(Collection<String> coll, String word) {
        if (coll != null) {
            coll.add(word);
        }
        if (countFreq) {
            Double count = freq.get(word);
            if (count == null) {
                freq.put(word, 1.0);
            } else {
                freq.put(word, count + 1);
            }
        }
    }

//	    public Set<String> getDictSet() {
//	        return dictSet;
//	    }

    public Map<String, Double> getFreq() {
        return freq;
    }

    public void setDict(Map<String, Double> dict) {
        this.dict = dict;
    }
    public Map<String, Double> getDict(){return this.dict; }
    public void setRevDict(Map<Double, String> revDict){ this.revDict = revDict; }
    public Map<Double, String> getRevDict(){ return this.revDict; }

    public final List<Double> wordsToIndexes(final Iterable<String> words) {
        int i = rowSize;

        final List<Double> wordIdxs = new LinkedList<>();
        for (final String word : words) {
            if (--i == 0) {
                break;
            }
            final Double wordIdx = this.dict.get(word);
            if (wordIdx != null) {
                wordIdxs.add(wordIdx);
            } else {
                wordIdxs.add(0.0);
            }
        }
        return wordIdxs;
    }

}
