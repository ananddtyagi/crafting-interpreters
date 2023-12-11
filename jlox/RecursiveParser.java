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
        if (match(PRINT)) return printStatement();
        if (match(IF)) return ifStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Statement.Block(block());
        return expressionStatement();
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
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

    private Statement ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after if");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if conditional statement.");

        consume(LEFT_BRACE, "Expect '{' for beginning of if block");
        Statement thenBranch = new Statement.Block(block());
        Statement elseBranch = null;
        if (match(ELSE)){
            consume(LEFT_BRACE, "Expect '{' for beginning of if block");
            elseBranch = new Statement.Block(block());
        }

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement whileStatement() {
        consume(LEFT_PAREN, "'(' expected for while conditional");
        Expression condition = expression();
        consume(RIGHT_PAREN, "')' expected after while condition");

        consume(LEFT_BRACE, "Expect '{' for beginning of while block");
        Statement body = new Statement.Block(block());

        return new Statement.While(condition, body);
    }


    private Expression expression() {
        return assignment();
    }

    private Expression or() {
        Expression expression = and();

        while (match(OR)) {
            Token operator = previous();
            Expression right = and();
            expression = new Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression and() {
        Expression expression = equality();

        while (match(AND)) {
            Token operator = previous();
            Expression right = equality();
            expression = new Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression assignment() {
        Expression expression = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if(expression instanceof Variable) {
                Token name = ((Variable)expression).name;
                return new Assign(name, value);
            }

            error(equals, "Invalid assignment target");
        }

        return expression;
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
