package jlox;

import java.util.List;

abstract class Statement {
    interface Visitor<R> {
        R visitExpressionStatement(Expression statement);

        R visitPrintStatement(Print statement);

        R visitVarStatement(Var statement);

        R visitBlockStatement(Block statement);

        R visitIfStatement(If statement);

        R visitWhileStatement(While statement);
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

    static class Block extends Statement {
        Block(List<Statement> statements) {
            this.statements = statements;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }

        final List<Statement> statements;
    }

    static class If extends Statement {
        If(jlox.Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStatement(this);
        }

        final jlox.Expression condition;
        final Statement thenBranch;
        final Statement elseBranch;
    }

    static class While extends Statement {
        While(jlox.Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }

        final jlox.Expression condition;
        final Statement body;
    }

    abstract <R> R accept(Visitor<R> visitor);
}