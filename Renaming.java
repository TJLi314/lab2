import java.util.ArrayList;
import java.util.Arrays;

public class Renaming {
    public static int RenamingAlg(InterRep head) {

        // Go to end of linked list to get index, tail, and max SR
        int index = 0;
        InterRep current = head;
        int maxSR = -1;
        while (current.getNext() != null) {
            index++;

            if (current.getOpCode() != TokenType.LOADI && current.getOpCode() != TokenType.OUTPUT && current.getArg1().getSR() != null) maxSR = Math.max(maxSR, current.getArg1().getSR());
            if (current.getArg2().getSR() != null) maxSR = Math.max(maxSR, current.getArg2().getSR());
            if (current.getArg3().getSR() != null) maxSR = Math.max(maxSR, current.getArg3().getSR());
            current = current.getNext();
        }
        // System.out.println("Current index: " + index);
        // System.out.println("Current maxSR: " + maxSR);

        int[] SRToVR = new int[maxSR + 1];
        Arrays.fill(SRToVR, -1);
        int[] LU = new int[maxSR + 1];
        Arrays.fill(LU, -1);

        int VRName = 0;
        int maxLive = 0;
        int currentLive = 0;
        while(current != null) {
            // Ignore output and nop operations
            // QUESTION: Do we ignore output operations?
            if (current.getOpCode() == TokenType.OUTPUT || current.getOpCode() == TokenType.NOP) {
                current = current.getPrev();
                index--;
                continue;
            }

            // Process operand definition
            if (current.getOpCode() != TokenType.STORE) {
                InterRepBlock operand = current.getArg3();
                int SR = operand.getSR();

                // QUESTION: For an unused definition, do we increment maxLive?
                if (SRToVR[SR] == -1) { // Unused definition
                    SRToVR[SR] = VRName++;
                    currentLive++;
                }

                current.getArg3().setVR(SRToVR[SR]);
                current.getArg3().setNU(LU[SR]);
                SRToVR[SR] = -1;
                LU[SR] = -1;
                currentLive--;
            }

            // Process use operands
            ArrayList<InterRepBlock> useOperands = new ArrayList<InterRepBlock>();
            switch (current.getOpCode()) {
                case STORE -> {
                    useOperands.add(current.getArg1());
                    useOperands.add(current.getArg3());
                }
                case LOAD -> {
                    useOperands.add(current.getArg1());
                }
                case LOADI -> {

                }
                case ADD, SUB, MULT, LSHIFT, RSHIFT -> {
                    useOperands.add(current.getArg1());
                    useOperands.add(current.getArg2());
                }
                default -> {
                }
            }

            for (InterRepBlock operand : useOperands) {
                Integer SR = operand.getSR();

                if (SRToVR[SR] == -1) { // Last use
                    SRToVR[SR] = VRName++;
                    currentLive++;
                }

                operand.setVR(SRToVR[SR]);
                operand.setNU(LU[SR]);
            }
           
            for (InterRepBlock operand : useOperands) {
                LU[operand.getSR()] = index;
            }

            maxLive = Math.max(maxLive, currentLive);
            index--;
            current = current.getPrev();
        }

        return maxLive;
    }
}