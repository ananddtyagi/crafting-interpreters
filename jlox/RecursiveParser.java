package jlox;

import static jlox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

import jlox.Expression.*;

public class RecursiveParser {
    private final List<Token> tokens;
    private int current = 0;

    private static class RecursiveParseError extends RuntimeException {}

    RecursiveParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    Expression parseExpression() {
        Expression expression = expression();
        return expression;
    }

    private Statement declaration() {
        try {
            if(match(VAR)) return varDeclaration();

            return statement();
        }
        catch (RecursiveParseError recursiveParseError) {
            synchronize();
            return null;
        }
    }

    private Statement varDeclaration() {
        Token name = consume(IDENTIFIER, "Expecting variable name when declaring variable.");

        Expression initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Var(name, initializer);
    }


    private Statement statement() {
        return match(PRINT) ? printStatement() : expressionStatement();
    }

    private Statement expressionStatement() {
        Expression expression = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expression(expression);
    }

    private Statement printStatement() {
        Expression value = expression();
        consume(SEMICOLON, "Expect ';' after print statement.");
        return new Statement.Print(value);
    }

    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression expression = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression comparison() {
        Expression expression = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expression = new Binary(expression, operator, right);
        }

        return expression;
    }


    private Expression term() {
        Expression expression = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expression right = factor();
            expression = new Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression factor() {
        Expression expression = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Binary(expression, operator, right);
        }

        return expression;
    }

    
    private Expression unary() {
        if(match(BANG, MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Unary(operator, right);
        }

        return primary();
    }

    private Expression primary() {
        if (match(FALSE)) return new Literal(false);
        if (match(TRUE)) return new Literal(true);
        if (match(NIL)) return new Literal(null);
        if (match(NUMBER, STRING)) {
            return new Literal(previous().literal);
        }
        if (match(IDENTIFIER)) return new Variable(previous());

        if (match(LEFT_PAREN)) {
            Expression expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Grouping(expression);
        }

        throw error(peek(), "Expect expression.");
    }


    private Token consume(TokenType tokenType, String message) {
        if (check(tokenType)) return advance();

        throw error(peek(), message);
    }

    private RecursiveParseError error(Token currentToken, String message) {
        Lox.error(currentToken.line, message);
        synchronize();
        return new RecursiveParseError();
    }

    private void synchronize() {
        advance();

        while(!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
                default:
                    break;
            }
            advance();
        }
    }

    private boolean match(TokenType... tokenTypes) {;
        for (TokenType tokenType : tokenTypes) {
            if (check(tokenType)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType tokenType) {
        if (isAtEnd()) return false;
        return (peek().type == tokenType);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token previous() {
        return tokens.get(current-1);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }
}
