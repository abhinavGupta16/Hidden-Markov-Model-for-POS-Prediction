package Assignment4;

import Assignment4.Files.Patterns;

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




    public Map<String,Integer> stateIndexMap;
    public Map<String,Integer> wordIndexMap;
    public Map<String,Integer> suffixIndexMap;
    public double[][] wordState;
    public double[][] stateState;
    public double[][] suffixState;

    public TrainHmm(){
        this.stateIndexMap = new LinkedHashMap<>();
        this.wordIndexMap = new LinkedHashMap<>();
        this.suffixIndexMap = new LinkedHashMap<>();
        this.stateIndexMap.put(START, 1);
        this.stateIndexMap.put(END, 2);

        Patterns.getPrefixIndex(this.suffixIndexMap);
     }

    public static void main(String[] args) throws Exception{
        TrainHmm trainHmm = new TrainHmm();
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
                if(!trainHmm.wordIndexMap.containsKey(word)){
                    trainHmm.wordIndexMap.put(word, wordIndex++);
                }
                if(!trainHmm.stateIndexMap.containsKey(tag)){
                    trainHmm.stateIndexMap.put(tag, stateIndex++);
                }
            }
        }

        trainHmm.wordState = new double[trainHmm.wordIndexMap.size()][trainHmm.stateIndexMap.size()+1];
        trainHmm.stateState = new double[trainHmm.stateIndexMap.size()+1][trainHmm.stateIndexMap.size()+1];
        trainHmm.suffixState = new double[trainHmm.wordIndexMap.size()][trainHmm.stateIndexMap.size()+1];

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
                if(trainHmm.wordIndexMap.containsKey(word)){
                    wordIndex = trainHmm.wordIndexMap.get(word);
                }
                if(trainHmm.stateIndexMap.containsKey(tag)) {
                    stateIndexFrom = trainHmm.stateIndexMap.get(prevState);
                    stateIndexTo = trainHmm.stateIndexMap.get(tag);
                    prevState = tag;
                }
                int suffixIndex = trainHmm.suffixIndexMap.get(Patterns.getPrefix(word));
                trainHmm.suffixState[suffixIndex][stateIndexTo]++;

                trainHmm.stateState[stateIndexFrom][0]++;
                trainHmm.wordState[wordIndex][stateIndexTo]++;
                trainHmm.stateState[stateIndexFrom][stateIndexTo]++;
            } else if(prevState!=START){
                stateIndexFrom = trainHmm.stateIndexMap.get(prevState);
                stateIndexTo = trainHmm.stateIndexMap.get(END);
                prevState = START;
                trainHmm.stateState[stateIndexFrom][0]++;
                trainHmm.stateState[stateIndexFrom][stateIndexTo]++;
            }
        }

//        System.out.println("wordState");
//        print2DArray(wordState, trainHmm.wordIndexMap, trainHmm.stateIndexMap);
//        System.out.println("trainHmm.stateState");
//        print2DArray(trainHmm.stateState, trainHmm.stateIndexMap, trainHmm.stateIndexMap);
//        System.out.println(trainHmm.wordIndexMap);
//        System.out.println(trainHmm.stateIndexMap);

        trainHmm.calculateProbabilityState();
        trainHmm.calculateProbabilityWord();

//        System.out.println(trainHmm.stateState[trainHmm.stateIndexMap.get(".")][2]);
//        System.out.println("wordState");
//        print2DArray(wordState, trainHmm.wordIndexMap, trainHmm.stateIndexMap);
//        System.out.println("trainHmm.stateState");
//        print2DArray(trainHmm.stateState, trainHmm.stateIndexMap, trainHmm.stateIndexMap);

        File fileTest = new File("D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\WSJ_23.words");
        File filePos = new File("D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\wsj_23.pos");
        trainHmm.processFile(fileTest, filePos);

        System.out.println("Score");
        Score.main(new String[]{"D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\WSJ_24.pos", filePos.getPath()});
        System.out.println("\nScore Custom");
        ScoreCustom.main(new String[]{"D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\WSJ_24.pos", filePos.getPath()});
    }

    public void processFile(File fileKey, File filePos) throws Exception{

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
                String[] tags = Veterbi.viterbi(this, obsArr);
                for(int i = 0; i < tags.length; i++){
                    brw.write(obsArr[i] + "\t" + tags[i] + "\n");
                }
                brw.write("\n");
            }
        }
        brw.close();
    }

    public void calculateProbabilityState(){
        for(int i = 1; i < stateState.length; i++){
            for(int j = 1; j < stateState[0].length; j++){
                stateState[i][j] = stateState[i][j]/stateState[i][0];
            }
        }
    }

    public void calculateProbabilityWord(){
        for(Map.Entry<String, Integer> entry: stateIndexMap.entrySet()){
            int stateIndex = entry.getValue();
            for(int i = 0; i < wordState.length; i++){
                wordState[i][stateIndex] = wordState[i][stateIndex]/stateState[stateIndex][0];
            }
            for(int i = 0; i < suffixState.length; i++){
                suffixState[i][stateIndex] = suffixState[i][stateIndex]/stateState[stateIndex][0];
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

