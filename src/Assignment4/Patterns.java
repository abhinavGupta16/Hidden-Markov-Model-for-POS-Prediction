package Assignment4;

import java.util.Map;

public enum Patterns {
    ED_WORD_PATTERN(".*ed"),
    AL_WORD_PATTERN(".*al"),
    IAL_WORD_PATTERN(".*ial"),
    TIAL_WORD_PATTERN(".*tial"),
    ABLE_WORD_PATTERN(".*able"),
    S_WORD_PATTERN(".*s"),
    ING_WORD_PATTERN(".*ing"),
    ER_WORD_PATTERN(".*er"),
    ION_WORD_PATTERN(".*ion"),
    ON_WORD_PATTERN(".*on"),
    LOGY_WORD_PATTERN(".*logy"),
    EST_WORD_PATTERN(".*est"),
    ITY_WORD_PATTERN(".*ity"),
    ISM_WORD_PATTERN(".*ism"),
    AGE_WORD_PATTERN(".*age"),
    ATE_WORD_PATTERN(".*ate"),
    ARION_WORD_PATTERN(".*arion"),
    TION_WORD_PATTERN(".*tion"),
    IVE_WORD_PATTERN(".*ive"),
    ISH_WORD_PATTERN(".*ish"),
    NO_SUFFIX_PREFIX(".*");

    private final String patternVal;
    Patterns(String s) {
        this.patternVal = s;
    }

    public static String getPrefix(String word) {
        for (Patterns pattern : Patterns.values()) {
            if(word.matches(pattern.patternVal)){
                return pattern.patternVal;
            }
        }
        return NO_SUFFIX_PREFIX.patternVal;
    }

    public static void getPrefixIndex(Map<String,Integer> suffixIndexMap) {
        int i = 0;
        for (Patterns pattern : Patterns.values()) {
            suffixIndexMap.put(pattern.patternVal, i);
            i++;
        }
    }
}
