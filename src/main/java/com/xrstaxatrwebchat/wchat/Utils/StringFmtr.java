package com.xrstaxatrwebchat.wchat.Utils;

public class StringFmtr {


    public static String get(String input){

        String newStr = input
                .replaceAll("\\s+(?=\\p{Punct})", "")
                .replaceAll(" i ", " I ")
                .replaceAll(" i'"," I'")
                .replaceAll("<unk>","") // possible replacements if that's the only response: Not sure, Don't know, Come again?
                .replaceAll("<eos>","")
                .replaceAll("\"","")
                .replaceAll("<","")
                .replaceAll("</","")
                .replaceAll(">","")
                .replaceAll("\"[;()\\\\\\\\/:*?\\\"<>|&']\"","")
                .replaceAll("god", "God")
                .replaceAll("jesus","Jesus")
                .replaceAll("christ", "Christ")
                .replaceAll("jehovah","Jehovah")
                .trim();

        int pos = 0;
        boolean capitalize = true;
        StringBuilder sb = new StringBuilder(newStr);
        while (pos < sb.length()) {
            if ((sb.charAt(pos) == '.')|| (sb.charAt(pos) == '?')|| (sb.charAt(pos) == '?') || (sb.charAt(pos) == '!')) {
                capitalize = true;
            } else if (capitalize && !Character.isWhitespace(sb.charAt(pos))) {
                sb.setCharAt(pos, Character.toUpperCase(sb.charAt(pos)));
                capitalize = false;
            }
            pos++;
        }

        return sb.toString();
    }


    public static String inputSanitizer(String input){

        String newStr = input
                .replaceAll("\\s+(?=\\p{Punct})", "")
                .replaceAll("<unk>","").replaceAll("\"","").replaceAll("/","").replaceAll("< u","")
                .replaceAll("<","").replaceAll("</","").replaceAll(">","")
                .replaceAll("\\$"," dollar ")
                .replaceAll("\"[;\\\\\\\\/:*?\\\"<>|&']\"","")
                .trim();

        return  newStr;
    }


    public static String truncateLine(String line){



        String [] endChars = {".", ";",":", ",", "?","!"};
        String passLine = "";

        for(String item: endChars) {
            double len = (double) line.length();
            int lastPeriodIndex = line.lastIndexOf(item);

            if(lastPeriodIndex/len >= 0.7){
                passLine = line.substring(0, lastPeriodIndex+1);

            }else{
                passLine = line;
            }

        }

        return passLine;
    }

}
