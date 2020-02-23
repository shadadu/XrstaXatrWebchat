package com.xrstaxatrwebchat.wchat.Utils;

import javax.naming.Context;
import java.util.*;



public class Seq2SeqModel {

    private final Map<String, Double> dict = new HashMap<>();
    private final Map<Double, String> revDict = new HashMap<>();
    private final List<List<Double>> corpus = new ArrayList<>();
    private FileLoads fileLoads = new FileLoads();

    public CorpusProcessor createDictionary(Context ctx) throws Exception {

        String specials = "!\"#$;%^:?*()[]{}<>«»,.–—=+…";   // (String) ctx.lookup("java:comp/env/SPECIALS");
        String CHARS = "-\\/_&" + specials;
        String corpusFileName = (String) ctx.lookup("java:comp/env/CORPUS_FILENAME");
        int rowSize = (int) ctx.lookup("java:comp/env/ROW_SIZE");
        int maxDict = (int) ctx.lookup("java:comp/env/MAX_DICT");


        double idx = 3.0;
        dict.put("<unk>", 0.0);
        revDict.put(0.0, "<unk>");
        dict.put("<eos>", 1.0);
        revDict.put(1.0, "<eos>");
        dict.put("<go>", 2.0);
        revDict.put(2.0, "<go>");
        for (char c : CHARS.toCharArray()) {
            if (!dict.containsKey(String.valueOf(c))) {
                dict.put(String.valueOf(c), idx);
                revDict.put(idx, String.valueOf(c));
                ++idx;
            }
        }
        System.out.println("Load corpus and building the dictionary...");

        CorpusProcessor corpusProcessor = new CorpusProcessor(fileLoads.toTempPath(corpusFileName, ctx), rowSize, true);

        corpusProcessor.start(ctx);
        Map<String, Double> freqs = corpusProcessor.getFreq();
        Set<String> dictSet = new TreeSet<>();
        Map<Double, Set<String>> freqMap = new TreeMap<>(new Comparator<Double>() {

            @Override
            public int compare(Double o1, Double o2) {
                return (int) (o2 - o1);
            }
        });
        for (Map.Entry<String, Double> entry : freqs.entrySet()) {
            Set<String> set = freqMap.get(entry.getValue());

            if (set == null) {
                set = new TreeSet<>(); // tokens of the same frequency would be sorted alphabetically
                freqMap.put(entry.getValue(), set);
            }
            set.add(entry.getKey());
        }
        int cnt = 0;
        dictSet.addAll(dict.keySet());

        for (Map.Entry<Double, Set<String>> entry : freqMap.entrySet()) {
            for (String val : entry.getValue()) {
                if (dictSet.add(val) && ++cnt >= maxDict) {
                    break;
                }
            }
            if (cnt >= maxDict) {
                break;
            }
        }

        System.out.println("Dictionary is ready, size is " + dictSet.size());
        // index the dictionary and build the reverse dictionary for lookups
        for (String word : dictSet) {
            if (!dict.containsKey(word)) {
                dict.put(word, idx);
                revDict.put(idx, word);
                ++idx;
            }
        }
        System.out.println("Total dictionary size is " + dict.size() + ". Load corpus and processing the corpus dataset...");
        corpusProcessor = new CorpusProcessor(fileLoads.toTempPath(corpusFileName, ctx), rowSize, false) {
            @Override
            protected void processLine(String lastLine, Context cntxt) {
                List<String> words = new ArrayList<>();
                tokenizeLine(lastLine, words, true, cntxt);
                corpus.add(wordsToIndexes(words));
            }
        };
        corpusProcessor.setDict(dict);
        corpusProcessor.setRevDict(revDict);
        corpusProcessor.start(ctx);
        corpusProcessor.setCorpusSize(corpus.size());
        System.out.println("Done. Corpus size is " + corpus.size());
        return corpusProcessor;
    }


}