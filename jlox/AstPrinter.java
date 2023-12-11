package jlox;

import jlox.Expression.*;

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
        if (expression.value == null) return "nil";
        return expression.value.toString();
    }

    @Override
    public String visitUnaryExpression(Unary expression) {
        return parenthesize(expression.operator.lexeme, expression.right);
    };


    @Override
    public String visitVariableExpression(Variable expression) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitVariableExpression'");
    }
    

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
        System.out.println(new AstPrinter().print(example));
    }

    private static Expression example = new Binary(
            new Unary(
                new Token(TokenType.MINUS, "-", null, 1, 0),
                new Literal(123)
            ),
            new Token(TokenType.STAR, "*", null, 1, 1),
            new Grouping(
                new Literal(45.67)
            )
        );

    @Override
    public String visitAssignExpression(Assign expression) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAssignExpression'");
    }

    @Override
    public String visitLogicalExpression(Logical expression) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLogicalExpression'");
    }
}
