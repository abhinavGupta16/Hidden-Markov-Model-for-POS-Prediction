package Assignment4;

import java.io.File;
import java.util.*;

import static Assignment4.TrainHmm.START;
import static Assignment4.TrainHmm.END;

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
        TrainHmm.processFile(stateState, wordState, stateIndexMap, wordIndexMap, fileTest, filePos, stateMax);
    }

    public static String[] viterbi(double[][] stateState, double[][] wordState, Map<String,Integer> stateIndexMap, Map<String,Integer> wordIndexMap, String[] obs, double[] stateMax){
        Pair[][] v = new Pair[stateState.length][obs.length+2];
        for(int i = 0; i<v.length; i++){
            for(int j = 0; j < v[0].length;j++){
                v[i][j] = new Pair(0,null);
            }
        }

        v[1][0].value = 1;
        for(int wordIndex = 1; wordIndex <= obs.length; wordIndex++){
            String word = obs[wordIndex-1];
            for(int stateIndex = 1; stateIndex < stateState[0].length; stateIndex++){
                double maxProb = -1;
                Integer maxStateIndex = -1;
                for(int prevStateIndex = 1; prevStateIndex < stateState[0].length; prevStateIndex++){
                    double wordProb;
                    if(!wordIndexMap.containsKey(word)){
                        wordProb = wordNotFoundState(stateState, stateIndex, word);//stateMax[stateIndex]; //wordNotFound(wordState, stateIndex);
                    } else {
                        wordProb = wordState[wordIndexMap.get(word)][stateIndex];
                    }
                    double prob = stateState[prevStateIndex][stateIndex] * wordProb
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

        int endStateIndex = stateIndexMap.get("End");
        double maxProb = -1;
        Integer maxStateIndex = -1;
        for (int prevStateIndex = 1; prevStateIndex < stateState[0].length; prevStateIndex++) {
            double prob = stateState[prevStateIndex][endStateIndex] * v[prevStateIndex][obs.length].value;
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
        Pair current = v[stateIndexMap.get("End")][obs.length+1];
        int n = obs.length+1;
        while(true){
            if(val>=0)
                tags[val] = TrainHmm.getKeyByValue(stateIndexMap, current.prev);
            val--;
            n--;
            if(current.prev==null){
                break;
            }
            current = v[current.prev][n];
        }
        for(int i = 0; i < obs.length;i++){
            if(!wordIndexMap.containsKey(obs[i]) && Character.isUpperCase(obs[i].charAt(0))){
                tags[i] = "NNP";
            }
        }
//        System.out.println(Arrays.toString(tags));
        return tags;
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

    public static double wordNotFoundState(double[][] stateState, int stateIndexFrom, String word){
        double maxProb = -1.0;
        for(int stateIndexTo = 0; stateIndexTo<stateState[0].length; stateIndexTo++){
            if(stateState[stateIndexFrom][stateIndexTo] > maxProb){
                maxProb = stateState[stateIndexFrom][stateIndexTo];
            }
        }
        return maxProb;
    }
}
