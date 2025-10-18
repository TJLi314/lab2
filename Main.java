import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length == 2 && args[0].matches("\\d+")) {
            int k = Integer.parseInt(args[0]);
            String filename = args[1];
            runAllocMode(k, filename);
            return;
        }

        boolean hasH = false;
        boolean hasR = false;
        boolean hasP = false;
        boolean hasS = false;
        String filename = null;

        for (String arg : args) {
            switch (arg) {
                case "-h" -> hasH = true;
                case "-x" -> hasR = true;
                case "-p" -> hasP = true;
                case "-s" -> hasS = true;
                default -> {
                    if (filename == null) {
                        filename = arg;
                    } else {
                        System.err.printf("ERROR: Command line argument %s not recognized\n\n", arg);
                        printHelp();
                        return;
                    }
                }
            }
        }

        int flagCount = (hasH ? 1 : 0) + (hasR ? 1 : 0) + (hasP ? 1 : 0) + (hasS ? 1 : 0);
        if (flagCount > 1) {
            System.err.println("ERROR: Multiple command-line flags found. Using highest priority. Try '-h' for information on command-line syntax.\n");
        }

        if (hasH) {
            printHelp();
            return;
        } else if (hasR) {
            if (!checkFile(filename)) return;
            runIRMode(filename);
            return;
        } else if (hasP) {
            if (!checkFile(filename)) return;
            runParseMode(filename);
            return;
        } else if (hasS) {
            if (!checkFile(filename)) return;
            runScanMode(filename);
            return;
        }

        // Default to -p if no flags, but filename is given
        if (!checkFile(filename)) return;
        runParseMode(filename);
    }

    private static boolean checkFile(String filename) {
        if (filename == null) {
            System.err.println("ERROR: Missing filename.");
            printHelp();
            return false;
        }
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            System.err.printf("ERROR: Could not open file '%s' as the input file.\n\n", filename);
            printHelp();
            return false;
        }
        return true;
    }

    private static void printHelp() {
        System.out.println("Command Syntax:");
        System.out.println("\t./412alloc [flags] filename\n");

        System.out.println("Required Arguments:");
        System.out.println("\tk \tspecifies the number of registers available to the allocator");
        System.out.println("\tfilename is the pathname (absolute or relative) to the input file\n");

        System.out.println("Optional flags:");
        System.out.println("\t-h \tprints this message\n");
        System.out.println("\t-x \t perform renaming on the code in the input block and print the results");
    }

    private static void runScanMode(String filename) {
        try {
            Scanner scanner = new Scanner(filename);
            Token token = scanner.getNextToken();
            while (token.getType() != TokenType.ENDFILE) {
                System.out.println(token);
                token = scanner.getNextToken();
            }
            System.out.println(token); // Print EOF token
            scanner.close();
        } catch (Exception e) {
            System.err.println("Error scanning file: " + e.getMessage());
        }
    }

    private static void runParseMode(String filename) {
        try {
            ParserResult result = Parser.parse(filename);
            if (result != null) {
                if (result.isSuccess()) {
                    System.out.println("Parsing succeeded. Processed " + result.getOpCount() + " operations.");
                } else {
                    System.out.println("Parse found errors.");
                }
            } else {
                System.out.println("Parsing failed.");
            }
        } catch (Exception e) {
            System.err.println("Error parsing file: " + e.getMessage());
        }
    }

    private static void runIRMode(String filename) {
        try {
            ParserResult result = Parser.parse(filename);
            InterRep head = result.getHead();
            if (result != null) {
                if (result.isSuccess()) {
                    // printIR(result);
                    int maxLive = Renaming.RenamingAlg(head);
                    printIR(result);
                    // System.out.println("MaxLive: " + maxLive);
                } else {
                    System.out.println("\nDue to syntax errors, run terminates.");
                }
            } else {
                System.out.println("Parsing failed.");
            }

            System.out.println("Allocating registers");
            Allocator.AllocateRegisters(head, 4, 3);
            printIR(result);

        } catch (Exception e) {
            System.err.println("Error parsing file: " + e.getMessage());
        }
    }

    private static void runAllocMode(int k, String filename) {
        if (k < 3 || k > 64) {
            System.err.printf("ERROR: Invalid number of registers (%d). Must be between 3 and 64.\n", k);
            return;
        }
        if (!checkFile(filename)) return;

        try {
            ParserResult result = Parser.parse(filename);
            if (result == null || !result.isSuccess()) {
                System.err.println("ERROR: Parsing failed. Cannot allocate registers.");
                return;
            }

            InterRep head = result.getHead();
            int maxLive = Renaming.RenamingAlg(head);
            Allocator.AllocateRegisters(head, maxLive, k); // example: 3 spill registers
            printIR(result);

        } catch (Exception e) {
            System.err.println("ERROR: Failed during allocation: " + e.getMessage());
        }
    }


    private static void printIR(ParserResult result) {
        InterRep current = result.getHead();
        while (current != null) {
            System.out.println(current);
            current = current.getNext();
        }
    }
}
