package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("xor", XOR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }


    Scanner (String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line, current));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length(); // TODO: when would this be greater than?
    }

    private void scanToken() {
        char c = advance(); // this returns back the current character and forwards the current pointer
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break; 
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            // TODO: Bitwise operators
            default:
                if (isNumber(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, String.format("Unexpected character %c", c));
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER; // if null, it's not a reserved word
        addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isNumber(c);
    }

    private boolean isAlpha(char c) {
        return  (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private void number() {
        boolean isDouble = false;
        while(isNumber(peek())) advance();

        if(peek() == '.' && isNumber(peekNext())) {
            advance(); // this is the decimal
            isDouble = true;
            while(isNumber(peek())) advance();
        }
        
        if (isDouble) {
            addToken(NUMBER, Double.parseDouble(source.substring(start, current))); // TODO: this uses Java's double parser, write my own parser instead
        } else {
            addToken(NUMBER, Integer.parseInt(source.substring(start, current)));
        }
    }

    private boolean isNumber(char c) {
        return c >= '0' && c <= '9';
        // TODO: is it faster to do -> ?
        // return !(c < '0' || c > '9')
    }

    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line ++;
            advance();
        }
        // we have either reached the end of the string or end of file
        
        
        if (isAtEnd()) { // end of file before end of string
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance(); // if we reach here, we are at the closing "

        String value = source.substring(start + 1, current - 1); // does not include the quotes.
        addToken(STRING, value);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, current));
    }
}
