import java.util.ArrayList;
import java.util.Arrays;

public class Allocator {
    public static void AllocateRegisters(InterRep head, int maxLive, int regNum) {
        // go through linked list to get max VR
        InterRep current = head;
        int maxVR = -1;
        while (current.getNext() != null) {

            if (current.getArg1().getVR() != null) maxVR = Math.max(maxVR, current.getArg1().getVR());
            if (current.getArg2().getVR() != null) maxVR = Math.max(maxVR, current.getArg2().getVR());
            if (current.getArg3().getVR() != null) maxVR = Math.max(maxVR, current.getArg3().getVR());
            current = current.getNext();
        }
        // System.out.println("MaxVR: " + maxVR);

        // data structures needed to support allocation
        int[] VRToPR = new int[maxVR + 1];
        int[] VRToSpillLoc = new int[maxVR + 1];
        int[] VRToIM = new int[maxVR + 1];
        // int[] PRToVR = new int[regNum];
        // int[] PRNU = new int[regNum];

        int[] PRToVR;
        int[] PRNU;
        if (maxLive <= regNum) {
            PRToVR = new int[regNum];
            PRNU = new int[regNum];
        }
        else {
            PRToVR = new int[regNum - 1];
            PRNU = new int[regNum - 1];
        }
        Arrays.fill(VRToPR, -1);
        Arrays.fill(VRToSpillLoc, -1);
        Arrays.fill(PRToVR, -1);
        Arrays.fill(PRNU, -1);
        Arrays.fill(VRToIM, -1);

        ArrayList<Integer> freeRegs = new ArrayList<>();
        int i = maxLive <= regNum ? regNum - 1 : regNum - 2;
        for (; i >= 0; i--) freeRegs.add(i);
        // printFreeRegisters(freeRegs);

        // printMaps(VRToPR, PRToVR, PRNU, VRToSpillLoc);
        int spillLoc = 32768;
        int spillReg = regNum - 1;
        int firstPR = -1;
        current = head;
        while (current != null) {
            // System.out.println("\nCurrent operation: " + current);
            // Ignore output and nop operations
            // QUESTION: Do we ignore output operations?
            if (current.getOpCode() == TokenType.OUTPUT || current.getOpCode() == TokenType.NOP) {
                current = current.getNext();
                continue;
            }

            // Process use operands
            ArrayList<InterRepBlock> useOperands = new ArrayList<>();
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

            // System.out.println("Assigning VRs to PRs for uses");
            // Assign VRs to PRs
            for (InterRepBlock operand : useOperands) {
                // System.out.println("Current operand to assign for use: " + operand);
                Integer VR = operand.getVR();
                if (VRToPR[VR] != -1) {
                    operand.setPR(VRToPR[VR]);
                    PRNU[VRToPR[VR]] = operand.getNU();
                    firstPR = VRToPR[VR];
                    continue;
                }
                
                // Spill code
                if (freeRegs.isEmpty()) {
                    // System.out.println("No free registers, need to spill");
                    // Check if any VR is rematerializable
                    int PRToFree = -1;
                    int VRToRematerialize = -1;
                    for (int j = 0; j < VRToIM.length; j++) {
                        if (VRToIM[j] != -1 && VRToPR[j] != -1) {
                            if (firstPR != -1 && VRToPR[j] == firstPR) {
                                // System.out.println("Want to rematerialize PR" + VRToPR[j] + ", but was last used PR");
                                continue;
                            }
                            PRToFree = VRToPR[j];
                            VRToRematerialize = j;
                            break;
                        }
                    }

                    if (PRToFree != -1) {
                        // System.out.println("PR " + PRToFree + " is rematerializable");
                        VRToPR[VRToRematerialize] = -1;
                        PRToVR[PRToFree] = -1;
                        PRNU[PRToFree] = -1;
                        // printVRToIM(VRToIM);
                        freeRegs.add(PRToFree);
                    } else {
                        // System.out.println("Did not find Rematerializable VR: ");
                        // printVRToIM(VRToIM);
                        // Create loadI operation
                        InterRep loadiNode = new InterRep(
                            TokenType.LOADI,
                            new InterRepBlock(spillLoc),
                            new InterRepBlock(),
                            new InterRepBlock()
                        );

                        loadiNode.getArg3().setPR(spillReg);
                        loadiNode.setPrev(current.getPrev());
                        current.getPrev().setNext(loadiNode);

                        // Choose PR to spill
                        int furthestNU = -1;
                        int furthestNUPR = -1;
                        for (int j = 0; j < PRNU.length; j++) {
                            // if (lastRestoredPR != -1 && j == lastRestoredPR) continue;
                            if (firstPR != -1 && j == firstPR) {
                                // System.out.println("Want to rematerialize PR" + VRToPR[j] + ", but was last used PR");
                                continue;
                            } 
                            if (PRNU[j] > furthestNU) {
                                furthestNU = PRNU[j];
                                furthestNUPR = j;
                            }
                        }
                        int VRToSpill = PRToVR[furthestNUPR];
                        // System.out.println("Spilling PR " + furthestNUPR + " and VsR " + VRToSpill);

                        // Update VRToSPillLoc, VRToPR, PRToVR, and NU accordinly, and add free register back
                        VRToPR[VRToSpill] = -1;
                        PRToVR[furthestNUPR] = -1;
                        PRNU[furthestNUPR] = -1;
                        VRToSpillLoc[VRToSpill] = spillLoc;
                        spillLoc += 4;
                        freeRegs.add(furthestNUPR);

                        // Create Store operation
                        InterRep storeNode = new InterRep(
                                TokenType.STORE,
                                new InterRepBlock(),
                                new InterRepBlock(),
                                new InterRepBlock()
                            );

                        storeNode.getArg1().setPR(furthestNUPR);
                        storeNode.getArg1().setVR(VRToSpill);
                        storeNode.getArg3().setPR(spillReg);
                        storeNode.setPrev(loadiNode);
                        storeNode.setNext(current);
                        loadiNode.setNext(storeNode);
                        current.setPrev(storeNode);

                        // System.out.println("Added spill operands: " + loadiNode + ", " + storeNode);
                    }
                }
                
                // Restore code
                if (VRToIM[VR] != -1) { 
                    // System.out.println("Rematerializing VR " + VR + " with IM " + VRToIM[VR]);
                    // Create loadI operation
                    InterRep loadiNode = new InterRep(
                        TokenType.LOADI,
                        new InterRepBlock(VRToIM[VR]),
                        new InterRepBlock(),
                        new InterRepBlock()
                    );

                    loadiNode.getArg3().setPR(freeRegs.get(freeRegs.size() - 1));
                    loadiNode.setNext(current);
                    loadiNode.setPrev(current.getPrev());
                    current.getPrev().setNext(loadiNode);
                    current.setPrev(loadiNode);
                    
                    // System.out.println("Added rematerialize operands " + loadiNode + " with last restored VR = " + VR);
                    // lastRestoredVR = VR;

                } else if (VRToSpillLoc[VR] != -1) {
                    // System.out.println("Restoring VR " + VR + " with address " + VRToSpillLoc[VR]);
                    // Create loadI operation
                    InterRep loadiNode = new InterRep(
                        TokenType.LOADI,
                        new InterRepBlock(VRToSpillLoc[VR]),
                        new InterRepBlock(),
                        new InterRepBlock()
                    );

                    loadiNode.getArg3().setPR(spillReg);
                    loadiNode.setPrev(current.getPrev());
                    current.getPrev().setNext(loadiNode);
                    VRToSpillLoc[VR] = -1;

                    // Create load operation
                    InterRep loadNode = new InterRep(
                        TokenType.LOAD,
                        new InterRepBlock(),
                        new InterRepBlock(),
                        new InterRepBlock()
                    );

                    loadNode.getArg1().setPR(spillReg);
                    loadNode.getArg3().setVR(VR);
                    loadNode.getArg3().setPR(freeRegs.get(freeRegs.size() - 1));

                    loadNode.setPrev(loadiNode);
                    loadNode.setNext(current);
                    loadiNode.setNext(loadNode);
                    current.setPrev(loadNode);
                    // lastRestoredVR = VR;
                    // System.out.println("Added restore operands " + loadiNode + " and " + loadNode + " with last restored VR = " + VR);
                }
                // printFreeRegisters(freeRegs);
                int PR = freeRegs.remove(freeRegs.size() - 1);
                // printFreeRegisters(freeRegs);
                Integer NU = operand.getNU();
                VRToPR[VR] = PR;
                PRToVR[PR] = VR;
                PRNU[PR] = NU;
                // System.out.println("Assigning first PR: " + PR);
                firstPR = PR;
                operand.setPR(PR);
                // System.out.println("Adding VR " + VR + " and PR" + PR);
                // printMaps(VRToPR, PRToVR, PRNU, VRToSpillLoc);
                // System.out.println(operand);
            }
            
            firstPR = -1;
            // lastRestoredVR = -1;
            // System.out.println("Freeing PRs that have last used VRs");
            // Free PRs that have last use VRs
            for (InterRepBlock operand : useOperands) {
                // System.out.println(operand);
                // System.out.println(operand.getPR());
                if (VRToPR[operand.getVR()] == -1) continue;
                int PR = VRToPR[operand.getVR()];
                if (PRNU[PR] != -1) continue;
                
                // System.out.println("Freeing " + operand);
                Integer VR = operand.getVR();
                VRToPR[VR] = -1;
                PRToVR[PR] = -1;
                PRNU[PR] = -1;
                VRToIM[VR] = -1;
                freeRegs.add(PR);
                // System.out.println("Freeing VR " + VR + " and PR" + PR);
                // printFreeRegisters(freeRegs);
            }

            // System.out.println("Assigning PRs to VRs for definitions");
            // Process operand definition
            if (current.getOpCode() != TokenType.STORE) {
                InterRepBlock operand = current.getArg3();
                Integer VR = operand.getVR();
                if (VRToPR[VR] != -1) {
                    operand.setPR(VRToPR[VR]);
                    PRNU[VRToPR[VR]] = operand.getNU();
                    current = current.getNext();
                    continue;
                }

                // Spill code
                if (freeRegs.isEmpty()) {
                    // System.out.println("No free registers, need to spill");
                    int VRToRematerialize = -1;
                    int PRToFree = -1;
                    for (int j = 0; j < VRToIM.length; j++) {
                        if (VRToIM[j] != -1 && VRToPR[j] != -1) {
                            PRToFree = VRToPR[j];
                            VRToRematerialize = j;
                            break;
                        }
                    }

                    if (PRToFree != -1) {
                        VRToPR[VRToRematerialize] = -1;
                        PRToVR[PRToFree] = -1;
                        PRNU[PRToFree] = -1;
                        // printVRToIM(VRToIM);
                        // System.out.println("PR " + PRToFree + " is rematerializable");
                        freeRegs.add(PRToFree);
                    } else {
                        // System.out.println("Did not find Rematerializable VR: ");
                        // printVRToIM(VRToIM);
                        // Create loadI operation
                        InterRep loadiNode = new InterRep(
                            TokenType.LOADI,
                            new InterRepBlock(spillLoc),
                            new InterRepBlock(),
                            new InterRepBlock()
                        );

                        loadiNode.getArg3().setPR(spillReg);
                        loadiNode.setPrev(current.getPrev());
                        current.getPrev().setNext(loadiNode);

                        // Choose PR to spill
                        int furthestNU = -1;
                        int furthestNUPR = -1;
                        for (int j = 0; j < PRNU.length; j++) {
                            if (PRNU[j] > furthestNU) {
                                furthestNU = PRNU[j];
                                furthestNUPR = j;
                            }
                        }
                        int VRToSpill = PRToVR[furthestNUPR];
                        // System.out.println("Spilling PR " + furthestNUPR + " and VR " + VRToSpill);

                        // Update VRToSPillLoc, VRToPR, PRToVR, and NU accordinly, and add free register back
                        VRToPR[VRToSpill] = -1;
                        PRToVR[furthestNUPR] = -1;
                        PRNU[furthestNUPR] = -1;
                        VRToSpillLoc[VRToSpill] = spillLoc;
                        spillLoc += 4;
                        freeRegs.add(furthestNUPR);

                        // Create Store operation
                        InterRep storeNode = new InterRep(
                                TokenType.STORE,
                                new InterRepBlock(),
                                new InterRepBlock(),
                                new InterRepBlock()
                            );

                        storeNode.getArg1().setPR(furthestNUPR);
                        storeNode.getArg1().setVR(VRToSpill);
                        storeNode.getArg3().setPR(spillReg);
                        storeNode.setPrev(loadiNode);
                        storeNode.setNext(current);
                        loadiNode.setNext(storeNode);
                        current.setPrev(storeNode);

                        // System.out.println("Added spill operands: " + loadiNode + ", " + storeNode);
                    }
                }
                // printFreeRegisters(freeRegs);

                Integer NU = operand.getNU();
                if (NU == -1) {
                    // System.out.println("This definition doesn't have a next use. No need to allocate");
                    operand.setPR(freeRegs.get(freeRegs.size() - 1));
                    current = current.getNext();
                    continue;
                }

                if (current.getOpCode() == TokenType.LOADI) {
                    VRToIM[VR] = current.getArg1().getSR();
                    // System.out.println("Current operation is LOADI, setting VRToIM[" + VR + "] to " +  current.getArg1().getSR());
                }

                int PR = freeRegs.remove(freeRegs.size() - 1);
                // printFreeRegisters(freeRegs);
                VRToPR[VR] = PR;
                PRToVR[PR] = VR;
                PRNU[PR] = NU;
                operand.setPR(PR);
                // System.out.println("Adding VR " + VR + " and PR" + PR);
            }

            current = current.getNext();
            // checkMaps(VRToPR, PRToVR);
            // printMaps(VRToPR, PRToVR, PRNU, VRToSpillLoc);
            // checkPRNUAligns(PRNU, PRToVR);
        }
    }

    public static void printFreeRegisters(ArrayList<Integer> freeRegs) {
            System.out.print("Free registers: ");
            for (Integer i : freeRegs) System.out.print(i + ", ");
            System.out.println();
    }

    public static void printMaps(int[] VRToPR, int[] PRToVR, int[] PRNU, int[] VRToSpillLoc) {
        System.out.print("VRToPR: ");
        for (int i = 0; i < VRToPR.length; i++) System.out.print(i + ": " + VRToPR[i] + ",");

        System.out.print("\nPRToVR: ");
        for (int i = 0; i < PRToVR.length; i++) System.out.print(i + ": " + PRToVR[i] + ",");

        System.out.print("\nPRNU: ");
        for (int i = 0; i < PRNU.length; i++) System.out.print(i + ": " + PRNU[i] + ",");

        System.out.print("\nVRToSpillLoc: ");
        for (int i = 0; i < VRToSpillLoc.length; i++) System.out.print(i + ": " + VRToSpillLoc[i] + ",");
        System.out.println();
    }

    public static void printVRToIM(int[] VRToIM) {
        System.out.print("VRToIM: ");
        for (int i = 0; i < VRToIM.length; i++) System.out.print(i + ": " + VRToIM[i] + ",");
        System.out.println();
    }

    public static void checkPRNUAligns(int[] PRNU, int[] PRToVR) {
        System.out.println("Checking PRNU aligns with PRToVR");
        for (int i = 0; i < PRNU.length; i++) {
            if (PRToVR[i] == -1 && PRNU[i] != -1) {
                System.out.println("FATAL ERROR: A PR WAS FREED TOO QUICKLY");
            }

            if (PRToVR[i] != -1 && PRNU[i] == -1) {
                System.out.println("FATAL ERROR: A PR WITH NU -1 WAS NOT FREED");
                System.out.println("Culprit PR: " + i);
            }
        }
    }

    public static void checkMaps(int[] VRToPR, int[]PRToVR) {
        System.out.println("Checking VRToPR aligns with PRToVR");
        for (int i = 0; i < VRToPR.length; i++) {
            int PR = VRToPR[i];
            if (PR != -1) {
                if (PRToVR[PR] != i) {
                    System.out.println("FATAL ERROR: PR AND VR DON'T MATCH UP ON VR " + i);
                    System.out.println("Supposed PR is " + PR);
                }
            }
        }

        for (int i = 0; i < PRToVR.length; i++) {
            int VR = PRToVR[i];
            if (VR != -1) {
                if (VRToPR[VR] != i) {
                    System.out.println("FATAL ERROR: PR AND VR DON'T MATCH UP ON PR " + i);
                    System.out.println("Supposed VR is " + VR);
                }
            }
        }
    }
}