import java.io.*;

public class Scanner {
    private final BufferedReader reader;
    private int lookaheadChar = -1; 
    private int lineNumber = 1;

    public Scanner(String filename) throws IOException {
        this.reader = new BufferedReader(new FileReader(filename));
    }

    private int getNextChar() throws IOException {
        if (this.lookaheadChar != -1) {
            int temp = this.lookaheadChar;
            this.lookaheadChar = -1;  
            return temp;
        }
        return reader.read();
    }

    public void skipLine() throws IOException {
        this.lookaheadChar = -1;
        this.reader.readLine();
        this.lineNumber++;
    }

    public void close() throws IOException {
        this.reader.close();
    }

    public Token getNextToken() throws IOException {
        int prevState = 0;
        int state = 0;
        String curInt = "";
        String curString = "";
        char firstLetter = '\0';
        int ch;

        while ((ch = getNextChar()) != -1) {
            state = TransitionTable.transitionTable[state][ch];
            
            // System.out.println("Char: '" + ch + "' (" + (char) ch + "), State: " + state);
            if (state == -1) {
                // error state, check if previous state was accepting
                if (TransitionTable.acceptingStates.contains(prevState)) {
                    // save offending character for next token
                    this.lookaheadChar = ch;

                    if (prevState == 18) {
                        if (firstLetter == 'l') prevState = 19;
                        else if (firstLetter == 'r') prevState = 20;
                    }

                    if (prevState == 38) {
                        // System.out.println("First letter for CONST state " + firstLetter);
                        if (firstLetter == 'r')
                            prevState = 39;
                    }


                    Token returnToken = new Token(
                        TokenType.fromCode(prevState), 
                        prevState == 38 || prevState == 39 ? Integer.parseInt(curInt) : null,
                        this.lineNumber
                    );

                    if (returnToken.getType() == TokenType.NEWLINE) {
                        this.lineNumber++;
                    }

                    // System.out.println("Current integer: " + curInt);
                    return returnToken;
                } else {
                    char currentChar = (char) ch;
                    String errorString = currentChar != '\n' 
                                        && currentChar != '\t' 
                                        && currentChar != '\r'  
                                        ? curString + (char) ch 
                                        : curString;

                    System.err.println("ERROR " + Integer.toString(this.lineNumber) + ": \t\"" + errorString + "\" is not a valid word.");
                    this.lookaheadChar = -1;
                    if (currentChar != '\n') {
                        this.reader.readLine();
                    }
                    return new Token(TokenType.NEWLINE, null, this.lineNumber++, false);
                }
            }

            if (ch >= '0' && ch <= '9') {
                curInt += (char) ch;
            }

            if ((char) ch != ' ') curString += (char) ch;

            firstLetter = firstLetter == '\0' || firstLetter == ' ' || firstLetter == '\t' || firstLetter == '\r' ? (char) ch : firstLetter;
            // System.out.println("Current first letter: " + firstLetter + "( " + (int) firstLetter +" )");    
            prevState = state;
        }

        // Reached EOF, check if last token was accepting
        if (TransitionTable.acceptingStates.contains(prevState)) {
            if (prevState == 18) {
                if (firstLetter == 'l') prevState = 19;
                else if (firstLetter == 'r') prevState = 20;
            }
            if (prevState == 38) {
                if (firstLetter == 'r') prevState = 39;
            }

            Token returnToken = new Token(
                TokenType.fromCode(prevState),
                prevState == 38 || prevState == 39 ? Integer.parseInt(curInt) : null,
                this.lineNumber
            );

            if (returnToken.getType() == TokenType.NEWLINE) {
                this.lineNumber++;
            }

            return returnToken;
        } else if (state != 0) {
            // EOF reached but last token incomplete
            System.err.println("ERROR " + Integer.toString(this.lineNumber) + ": \t\"" + curString + "\" is not a valid word.");
        }

        return new Token(
            TokenType.ENDFILE,
            null,
            this.lineNumber
        );
    }
}