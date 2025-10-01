import java.io.File;

public class Main {
    public static void main(String[] args) {
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
        System.out.println("\t./412fe [flags] filename\n");

        System.out.println("Required Arguments:");
        System.out.println("\tfilename is the pathname (absolute or relative) to the input file\n");

        System.out.println("Optional flags:");
        System.out.println("\t-h \tprints this message\n");

        System.out.println("At most one of the following three flags:");
        System.out.println("\t-s \tprints tokens in token stream");
        System.out.println("\t-p \tinvokes parser and reports on success or failure");
        System.out.println("\t-r \tprints human readable version of parser's IR");
        System.out.println("If none is specified, the default action is '-p'");
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
            if (result != null) {
                if (result.isSuccess()) {
                    // printIR(result);
                    int maxLive = Renaming.RenamingAlg(result.getHead());
                    printIR(result);
                    // System.out.println("MaxLive: " + maxLive);
                } else {
                    System.out.println("\nDue to syntax errors, run terminates.");
                }
            } else {
                System.out.println("Parsing failed.");
            }
        } catch (Exception e) {
            System.err.println("Error parsing file: " + e.getMessage());
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
