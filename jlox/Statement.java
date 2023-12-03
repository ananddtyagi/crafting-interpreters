package jlox;

abstract class Statement {
    interface Visitor<R> {
        R visitExpressionStatement(Expression statement);

        R visitPrintStatement(Print statement);
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

    abstract <R> R accept(Visitor<R> visitor);
}