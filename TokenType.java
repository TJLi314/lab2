// TokenType.java
import java.util.HashMap;
import java.util.Map;

public enum TokenType {
    LOAD(11, "load"),
    STORE(5, "store"),
    LOADI(12, "loadI"),
    ADD(24, "add"),
    SUB(7, "sub"),
    MULT(18, "mult"),
    LSHIFT(19, "lshift"),
    RSHIFT(20, "rshift"),
    OUTPUT(33, "output"),
    NOP(27, "nop"),
    COMMA(36, ","),
    INTO(35, "=>"),
    NEWLINE(37, "\\n"),
    CONST(38, "CONST"),
    REG(39,"REG"),
    ENDFILE(99, "");

    private final int code;
    private final String lexeme;
    private static final Map<Integer, TokenType> codeToToken = new HashMap<>();

    static {
        for (TokenType t : TokenType.values()) {
            codeToToken.put(t.code, t);
        }
    }

    TokenType(int code, String lexeme) {
        this.code = code;
        this.lexeme = lexeme;
    }

    public int getCode() {
        return code;
    }

    public String getLexeme() {
        return lexeme;
    }

    public static TokenType fromCode(int code) {
        return codeToToken.get(code);
    }
}