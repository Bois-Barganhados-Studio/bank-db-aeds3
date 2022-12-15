package estruturas.PatternSearch;

import java.util.Hashtable;

/**
 * @author Vinicius G Santos
 */
public class BoyerMoore {

    /**
     * Boyer Moore string pattern search algorithm implementation.
     * @param pattern The pattern to be searched for.
     * @param text The string to execute the search on.
     * @return A PatternSearchInfo which contains the amount
     * found in the text and the amount of comparisons.
     */
    public static PatternSearchInfo find(String pattern, String text) {
        if (pattern == null || text == null) throw new IllegalArgumentException("Parameters must not be null.");
        // Generating the bad character and good suffix tables
        Hashtable<Character, Integer> badCharTable = genBadCharTable(pattern);
        int[] goodSuffixTable = genGoodSuffixTable(pattern);
        PatternSearchInfo info = new PatternSearchInfo();
        // Main loop algorithm
        for (int i = pattern.length() - 1, j = i, k = i; i < text.length();) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i--; j--;
                if (j == -1) {
                    // The pattern was found -> resets indexes
                    j = pattern.length() - 1;
                    info.addFoundCount(1);
                    i = ++k;
                }
            } else {
                // Mismatch -> shift
                Integer badCharShift = badCharTable.get(text.charAt(i));
                badCharShift = badCharShift == null ? j + 1 : j - badCharShift;
                k += badCharShift > goodSuffixTable[j] ? badCharShift : goodSuffixTable[j];
                i = k;
                j = pattern.length() - 1;
            }
            info.addNumComp(2);
        }
        return  info;
    }

    private static Hashtable<Character, Integer> genBadCharTable(String pattern) {
        Hashtable<Character, Integer> badCharTable = new Hashtable<>();
        for (int i = pattern.length() - 2; i > -1; i--)
            if (!badCharTable.containsKey(pattern.charAt(i)))
                badCharTable.put(pattern.charAt(i), i);
        return badCharTable;
    }

    private static int[] genGoodSuffixTable(String pattern) {
        int[] goodSuffixTable = new int[pattern.length()];
        goodSuffixTable[goodSuffixTable.length - 1] = 1;
        for (int i = goodSuffixTable.length - 2, j = 1; i > -1;) {
            String suffix = pattern.substring(i + j);
            int k = pattern.lastIndexOf(suffix, i);
            if (k != -1 && k - 1 != i) {
                goodSuffixTable[i] = i - k + j;
                i--;
                j = 1;
            } else {
                j++;
            }
        }
        return goodSuffixTable;
    }

}
