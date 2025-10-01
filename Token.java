public class Token {
    private final TokenType type;
    private final Integer value;
    private final int lineNumber;
    private final boolean isSuccess;

    public Token(TokenType type, int lineNumber) {
        this.type = type;
        this.value = null;
        this.lineNumber = lineNumber;
        this.isSuccess = true;
    }

    public Token(TokenType type, Integer value, int lineNumber) {
        this.type = type;
        this.value = value;
        this.lineNumber = lineNumber;
        this.isSuccess = true;
    }

    public Token(TokenType type, Integer value, int lineNumber, boolean isSuccess) {
        this.type = type;
        this.value = value;
        this.lineNumber = lineNumber;
        this.isSuccess = isSuccess;
    }

    public TokenType getType() {
        return type;
    }

    public Integer getValue() {
        return value;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public boolean getSuccess() {
        return this.isSuccess;
    }

    @Override
    public String toString() {
        String s = Integer.toString(this.lineNumber) + ": < " + this.type.toString() + ", \"";
        if (this.type == TokenType.CONST) {
            s += this.value;
        } else if (this.type == TokenType.REG) {
            s += "r" + this.value;
        } else {
            s += this.type.getLexeme();
        }
        s += "\" >";
        return s;
    }
}
