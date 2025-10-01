
import java.util.Arrays;
import java.util.HashSet;

public class TransitionTable {
    private static final int NUM_STATES = 41;
    private static final int ALPHABET_SIZE = 128;

    public static final int[][] transitionTable = new int[NUM_STATES][ALPHABET_SIZE];
    public static final HashSet<Integer> acceptingStates = new HashSet<>(Arrays.asList(5, 7, 11, 12, 18, 24, 27, 33, 35, 36, 37, 38));

    static {
        // Initialize all transitions to -1 (The error state)
        for (int i = 0; i < NUM_STATES; i++) {
            for (int j = 0; j < ALPHABET_SIZE; j++) {
                transitionTable[i][j] = -1;
            }
        }

        transitionTable[0]['s'] = 1;
        transitionTable[0]['l'] = 8;
        transitionTable[0]['r'] = 13;
        transitionTable[0]['m'] = 19;
        transitionTable[0]['a'] = 22; 
        transitionTable[0]['n'] = 25;
        transitionTable[0]['o'] = 28;
        transitionTable[0]['='] = 34;
        transitionTable[0][','] = 36;
        transitionTable[0]['\n'] = 37;
        transitionTable[0]['/'] = 39;
        transitionTable[0][' '] = 0;
        transitionTable[0]['\t'] = 0;
        transitionTable[0]['\r'] = 0;
        for (char c = '0'; c <= '9'; c++) {
            transitionTable[0][c] = 38;
            transitionTable[38][c] = 38;
            transitionTable[13][c] = 38;
        }

        transitionTable[1]['t'] = 2;
        transitionTable[1]['u'] = 6;

        transitionTable[2]['o'] = 3;
        transitionTable[3]['r'] = 4;
        transitionTable[4]['e'] = 5;

        transitionTable[6]['b'] = 7;
        
        transitionTable[8]['o'] = 9;
        transitionTable[8]['s'] = 14;

        transitionTable[9]['a'] = 10;
        transitionTable[10]['d'] = 11;
        transitionTable[11]['I'] = 12;

        transitionTable[13]['s'] = 14;
        transitionTable[14]['h'] = 15;
        transitionTable[15]['i'] = 16;
        transitionTable[16]['f'] = 17;
        transitionTable[17]['t'] = 18;

        transitionTable[19]['u'] = 20;
        transitionTable[20]['l'] = 21;
        transitionTable[21]['t'] = 18;  

        transitionTable[22]['d'] = 23;
        transitionTable[23]['d'] = 24;

        transitionTable[25]['o'] = 26;
        transitionTable[26]['p'] = 27;

        transitionTable[28]['u'] = 29;
        transitionTable[29]['t'] = 30;
        transitionTable[30]['p'] = 31;
        transitionTable[31]['u'] = 32;
        transitionTable[32]['t'] = 33;  

        transitionTable[34]['>'] = 35;        
        transitionTable[39]['/'] = 40;
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            transitionTable[40][i] = 40;
        }
        transitionTable[40]['\n'] = 37;
    }
}