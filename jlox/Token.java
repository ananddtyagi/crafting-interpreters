package jlox;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;
    final int tokenPosition;

    Token(TokenType type, String lexeme, Object literal, int line, int tokenPosition) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.tokenPosition = tokenPosition;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
