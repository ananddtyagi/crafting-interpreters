package jlox;

import jlox.TokenType;
import jlox.Expression.*;
import jlox.Token;

class AstPrinter implements Expression.Visitor<String> {
    String print(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visitBinaryExpression(Binary expression) {
        return parenthesize(expression.operator.lexeme, expression.left, expression.right);
    }

    @Override
    public String visitGroupingExpression(Grouping expression) {
        return parenthesize("group", expression.expression);
    }

    @Override
    public String visitLiteralExpression(Literal expression) {
        if (expression.value == null) return "null";
        return expression.value.toString();
    }

    @Override
    public String visitUnaryExpression(Unary expression) {
        return parenthesize(expression.operator.lexeme, expression.right);
    };

    private String parenthesize(String name, Expression... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expression expression : expressions) {
            builder.append(" ");
            builder.append(expression.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        Expression expression = new Binary(
            new Unary(
                new Token(TokenType.MINUS, "-", null, 1, 0),
                new Literal(123)
            ),
            new Token(TokenType.STAR, "*", null, 1, 1),
            new Grouping(
                new Literal(45.67)
            )
        );
    
        System.out.println(new AstPrinter().print(expression));
    }
}