package Assignment4;

import java.io.*;
import java.util.*;

public class TrainHmm {
    public static String START = "Start";
    public static String END = "End";
    public static String NUMBER_WORD = "NUMBERWORD999";
    public static String NUMBER_WORD_HYPHEN = "NUMBERWORD999-";
    public static String SINGLE_NUMBER_MARKER = "NUMBERSINGLEWORD";
    public static String NUMBER_HYPEN_WORD = "NUMBERHYPENWORD";
    public static String EDWORDS = "WORD999ED";
    public static String SINGLE_NUMBER_MARKER_PATTERN = "[0-9]";
    public static String NUMBER_WORD_PATTERN = "([a-zA-Z]*)?(\\-?)[-+]?[0-9](\\:?)(\\-?)[0-9]*(,[0-9][0-9]*)*?(\\.[0-9]*)*([a-zA-Z]?)";
    public static String NUMBER_HYPEN_WORD_PATTERN = "[0-9]*(\\-)[a-zA-Z]([a-zA-Z]*(\\-)*[a-zA-Z]*)*";
    public static String NUMBER_WORD_HYPHEN_PATTERN = "([a-zA-Z]*)(\\-)[0-9][0-9]*(,[0-9][0-9]*)*?(\\.[0-9]*)*(\\-)?([a-zA-Z])*";
    public static String ED_WORD_PATTERN = ".*ed";

    public static void main(String[] args) throws Exception{
        Map<String,Integer> stateIndexMap = new LinkedHashMap<>();
        Map<String,Integer> wordIndexMap = new LinkedHashMap<>();
        stateIndexMap.put(START, 1);
        stateIndexMap.put(END, 2);
        File file = new File("D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\WSJ_02-21.pos");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        String[] temp;
        int stateIndex = 3;
        int wordIndex = 0;
        while ((line = br.readLine()) != null) {
            if (!line.trim().equals("")) {
                temp = line.split("\t");
                String word = getWord(temp[0]);
                String tag = temp[1];
                if(!wordIndexMap.containsKey(word)){
                    wordIndexMap.put(word, wordIndex++);
                }
                if(!stateIndexMap.containsKey(tag)){
                    stateIndexMap.put(tag, stateIndex++);
                }
            }
        }

        double[][] wordState = new double[wordIndexMap.size()][stateIndexMap.size()+1];
        double[][] stateState = new double[stateIndexMap.size()+1][stateIndexMap.size()+1];
        double[] stateMax = new double[stateIndexMap.size()+1];

        br = new BufferedReader(new FileReader(file));
        int stateIndexFrom = 0;
        int stateIndexTo = 0;
        wordIndex = 0;
        String prevState = START;
        while ((line = br.readLine()) != null) {
            if (!line.trim().equals("")) {
                temp = line.split("\t");
                String word = getWord(temp[0]);
                String tag = temp[1];
                if(wordIndexMap.containsKey(word)){
                    wordIndex = wordIndexMap.get(word);
                }
                if(stateIndexMap.containsKey(tag)) {
                    stateIndexFrom = stateIndexMap.get(prevState);
                    stateIndexTo = stateIndexMap.get(tag);
                    prevState = tag;
                }
                stateState[stateIndexFrom][0]++;
                wordState[wordIndex][stateIndexTo]++;
                stateState[stateIndexFrom][stateIndexTo]++;
            } else if(prevState!=START){
                stateIndexFrom = stateIndexMap.get(prevState);
                stateIndexTo = stateIndexMap.get(END);
                prevState = START;
                stateState[stateIndexFrom][0]++;
                stateState[stateIndexFrom][stateIndexTo]++;
            }
        }

//        System.out.println("wordState");
//        print2DArray(wordState, wordIndexMap, stateIndexMap);
//        System.out.println("stateState");
//        print2DArray(stateState, stateIndexMap, stateIndexMap);
//        System.out.println(wordIndexMap);
//        System.out.println(stateIndexMap);

        calculateProbabilityState(stateState);
        calculateProbabilityWord(stateState, wordState, stateIndexMap, stateMax);
//
//        System.out.println(stateState[stateIndexMap.get(".")][2]);
//        System.out.println("wordState");
//        print2DArray(wordState, wordIndexMap, stateIndexMap);
//        System.out.println("stateState");
//        print2DArray(stateState, stateIndexMap, stateIndexMap);
        File fileTest = new File("D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\WSJ_24.words");
        File filePos = new File("D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\test_generate.pos");
        processFile(stateState, wordState, stateIndexMap, wordIndexMap, fileTest, filePos, stateMax);

        System.out.println("Score");
        Score.main(new String[]{"D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\WSJ_24.pos", filePos.getPath()});
        System.out.println("\nScore Custom");
        ScoreCustom.main(new String[]{"D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\WSJ_24.pos", filePos.getPath()});
    }

    public static void processFile(double[][] stateState, double[][] wordState, Map<String,Integer> stateIndexMap,
                                   Map<String,Integer> wordIndexMap, File fileKey, File filePos, double[] stateMax) throws Exception{

        BufferedReader br = new BufferedReader(new FileReader(fileKey));
        BufferedWriter brw = new BufferedWriter(new FileWriter(filePos));
        String line;
        List<String> obs = new LinkedList<>();
        while ((line = br.readLine()) != null) {
            if (!line.trim().equals("")) {
                obs.add(line);
            } else {
                String[] obsArr = obs.stream().toArray(String[] ::new);
                obs = new LinkedList<>();
                String[] tags = Veterbi.viterbi(stateState, wordState, stateIndexMap, wordIndexMap, obsArr, stateMax);
                for(int i = 0; i < tags.length; i++){
                    brw.write(obsArr[i] + "\t" + tags[i] + "\n");
                }
                brw.write("\n");
            }
        }
        brw.close();
    }

    public static void calculateProbabilityState(double[][] stateState){
        for(int i = 1; i < stateState.length; i++){
            for(int j = 1; j < stateState[0].length; j++){
                stateState[i][j] = stateState[i][j]/stateState[i][0];
            }
        }
    }

    public static void calculateProbabilityWord(double[][] stateState, double[][] wordState, Map<String,Integer> stateIndexMap, double[] stateMax){
        for(Map.Entry<String, Integer> entry: stateIndexMap.entrySet()){
            int stateIndex = entry.getValue();
            for(int i = 0; i < wordState.length; i++){
                wordState[i][stateIndex] = wordState[i][stateIndex]/stateState[stateIndex][0];
            }
            if(stateMax[stateIndex] < wordState[stateIndex][stateIndex]){
                stateMax[stateIndex] = wordState[stateIndex][stateIndex];
            }
        }
    }

    public static void print2DArray(double[][] arr, Map<String,Integer> map, Map<String,Integer> state){
        System.out.println(state);
        for(int i = 0; i< arr.length; i++){
            System.out.println(getKeyByValue(map, i) + " " + Arrays.toString(arr[i]));
        }
        System.out.println();
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String getWord(String word) {
        if (word.matches(SINGLE_NUMBER_MARKER_PATTERN)){
            return SINGLE_NUMBER_MARKER;
        } else if(word.matches(NUMBER_WORD_PATTERN)){
            return NUMBER_WORD;
        } else if (word.matches(NUMBER_HYPEN_WORD_PATTERN)){
            return NUMBER_HYPEN_WORD;
        } else if (word.matches(NUMBER_WORD_HYPHEN_PATTERN)){
            return NUMBER_WORD_HYPHEN;
        }
        return word;
    }
}

