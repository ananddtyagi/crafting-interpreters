package jlox;

abstract class Statement {
    interface Visitor<R> {
        R visitExpressionStatement(Expression statement);

        R visitPrintStatement(Print statement);

        R visitVarStatement(Var statement);
    }

    static class Expression extends Statement {
        Expression(jlox.Expression expression) {
            this.expression = expression;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStatement(this);
        }

        final jlox.Expression expression;
    }

    static class Print extends Statement {
        Print(jlox.Expression expression) {
            this.expression = expression;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStatement(this);
        }

        final jlox.Expression expression;
    }

    static class Var extends Statement {
        Var(Token name, jlox.Expression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStatement(this);
        }

        final Token name;
        final jlox.Expression initializer;
    }

    abstract <R> R accept(Visitor<R> visitor);
}