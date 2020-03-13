package Assignment4;

import Assignment4.Files.Patterns;

import java.io.File;
import java.util.*;

import static Assignment4.TrainHmm.*;

class Pair{
    double value;
    Integer prev;
    Pair(double value, Integer prev){
        this.value = value;
        this.prev = prev;
    }
    @Override
    public String toString() {
        return this.value + " " + this.prev;
    }
}

public class Veterbi {
    public static void main(String[] args) throws Exception{
        Map<String, Integer> stateIndexMap = new LinkedHashMap<>();
        stateIndexMap.put(START, 1);
        stateIndexMap.put(END, 2);
        stateIndexMap.put("noun", 3);
        stateIndexMap.put("verb", 4);
        double[][] stateState = new double[][]{
                {0.0,0.0,0.0,0.0,0.0},
                {0.0,0.0,0.0,0.8,0.2},
                {0.0,0.0,0.0,0.0,0.0},
                {0.0,0.0,0.1,0.1,0.8},
                {0.0,0.0,0.7,0.2,0.1}};
        Map<String, Integer> wordIndexMap = new LinkedHashMap<>();
        wordIndexMap.put("fish", 0);
        wordIndexMap.put("sleep", 1);
        double[][] wordState = new double[][]{
                {0.0,0.0,0.0,0.8,0.5},
                {0.0,0.0,0.0,0.2,0.5}};

        double[] stateMax = new double[]{0.0,0.0,0.0,0.8,0.5};

        File fileTest = new File("D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\test.words");
        File filePos = new File("D:\\NYU_assignment\\Spring_2020\\NLP\\NLP\\src\\Assignment4\\Files\\test_generate.pos");
//        TrainHmm.processFile(stateState, wordState, stateIndexMap, wordIndexMap, fileTest, filePos);
    }

    public static String[] viterbi(TrainHmm trainHmm, String[] obs){
        Pair[][] v = new Pair[trainHmm.stateState.length][obs.length+2];
        for(int i = 0; i<v.length; i++){
            for(int j = 0; j < v[0].length;j++){
                v[i][j] = new Pair(0,null);
            }
        }

        v[1][0].value = 1;
        for(int wordIndex = 1; wordIndex <= obs.length; wordIndex++){
            String word = getWord(obs[wordIndex-1]);
            for(int stateIndex = 1; stateIndex < trainHmm.stateState[0].length; stateIndex++){
                double maxProb = -1;
                Integer maxStateIndex = -1;
                for(int prevStateIndex = 1; prevStateIndex < trainHmm.stateState[0].length; prevStateIndex++){
                    double wordProb;
                    if(!trainHmm.wordIndexMap.containsKey(word)){
                        wordProb = wordNotFoundState(trainHmm, prevStateIndex, stateIndex, word);//stateMax[stateIndex]; //wordNotFound(wordState, stateIndex);
                    } else {
                        wordProb = trainHmm.wordState[trainHmm.wordIndexMap.get(word)][stateIndex];
                    }
                    double prob = trainHmm.stateState[prevStateIndex][stateIndex] * wordProb
                            * v[prevStateIndex][wordIndex-1].value;

                    if(prob>maxProb){
                        maxProb = prob;
                        maxStateIndex = prevStateIndex;
                    }
                }
                v[stateIndex][wordIndex].value = maxProb;
                v[stateIndex][wordIndex].prev = maxStateIndex;
            }
        }

        int endStateIndex = trainHmm.stateIndexMap.get("End");
        double maxProb = -1;
        Integer maxStateIndex = -1;
        for (int prevStateIndex = 1; prevStateIndex < trainHmm.stateState[0].length; prevStateIndex++) {
            double prob = trainHmm.stateState[prevStateIndex][endStateIndex] * v[prevStateIndex][obs.length].value;
            if (prob > maxProb) {
                maxProb = prob;
                maxStateIndex = prevStateIndex;
            }
        }
        v[endStateIndex][obs.length+1].value = maxProb;
        v[endStateIndex][obs.length+1].prev = maxStateIndex;

//        for(int i = 0; i < v.length;i++){
//            System.out.println(Arrays.toString(v[i]));
//        }
//        System.out.println();
        String[] tags = new String[obs.length];
        int val = obs.length-1;
        Pair current = v[trainHmm.stateIndexMap.get("End")][obs.length+1];
        int n = obs.length+1;
        while(true){
            if(val>=0)
                tags[val] = TrainHmm.getKeyByValue(trainHmm.stateIndexMap, current.prev);
            val--;
            n--;
            if(current.prev==null){
                break;
            }
            current = v[current.prev][n];
        }
        handleSpecialWords(obs, tags, trainHmm);
//        System.out.println(Arrays.toString(tags));
        return tags;
    }

    public static void handleSpecialWords(String[] obs, String[] tags, TrainHmm trainHmm){
        for(int i = 1; i < obs.length;i++){
                if(!trainHmm.wordIndexMap.containsKey(obs[i]) && Character.isUpperCase(obs[i].charAt(0))){
                        tags[i] = "NNP"; // Unknown word starting with Capital Letter is most likely a Proper Noun
                } else if((!obs[i].equals(",") && tags[i]==",") || (!obs[i].equals("(") && tags[i]=="(")
                        || (!obs[i].equals(")") && tags[i]==")") || (!obs[i].equals("\'\'") && tags[i]=="\'\'")){
                    int stateIndexFrom = trainHmm.stateIndexMap.get(tags[i-1]);
                    double maxProb = -1;
                    for (int stateIndexTo = 1; stateIndexTo < trainHmm.stateState[0].length; stateIndexTo++) {
                        if (trainHmm.stateState[stateIndexFrom][stateIndexTo] > maxProb) {
                            maxProb = trainHmm.stateState[stateIndexFrom][stateIndexTo];
                            tags[i] = TrainHmm.getKeyByValue(trainHmm.stateIndexMap, stateIndexTo);
                        }
                    }
                }
        }
    }

    public static double wordNotFound(double[][] wordState, int stateIndex){
        double maxProb = -1.0;
        for(int wordIndex = 0; wordIndex<wordState.length; wordIndex++){
            if(wordState[wordIndex][stateIndex] > maxProb){
                maxProb = wordState[wordIndex][stateIndex];
            }
        }
        return maxProb;
    }

    public static double wordNotFoundState(TrainHmm trainHmm, int stateIndexFrom, int stateIndex, String word){
        double maxProb = -1.0;
        int suffixIndex = trainHmm.suffixIndexMap.get(Patterns.getPrefix(word));
        if(suffixIndex!=trainHmm.suffixIndexMap.get(".*")){
            for (int stateIndexTo = 0; stateIndexTo < trainHmm.suffixState[0].length; stateIndexTo++) {
                if (trainHmm.suffixState[suffixIndex][stateIndexTo] > maxProb) {
                    maxProb = trainHmm.suffixState[suffixIndex][stateIndexTo];
                }
            }
        } else {
            for (int stateIndexTo = 0; stateIndexTo < trainHmm.stateState[0].length; stateIndexTo++) {
                if (trainHmm.stateState[stateIndexFrom][stateIndexTo] > maxProb) {
                    maxProb = trainHmm.stateState[stateIndexFrom][stateIndexTo];
                }
            }
        }
        return maxProb;
    }
}
