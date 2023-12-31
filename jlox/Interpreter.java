package jlox;

import java.util.List;

import jlox.Expression.*;
import jlox.Statement.*;

import static jlox.TokenType.*;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    private Environment environment = new Environment();
    private boolean repl;

    void interpret(List<Statement> statements, boolean repl) {
        this.repl = repl;
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    void interpretREPL(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    void interpret(Expression expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitBinaryExpression(Binary expression) {
        Object left = evaluate(expression.left);
        Token operator = expression.operator;
        Object right = evaluate(expression.right);

        switch (operator.type) {
            case MINUS:
                mustBeNumerical(operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                return handleSlash(operator, left, right);
            case STAR:
                return handleStar(operator, left, right);
            case PLUS:
                return handlePlus(operator, left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                mustBeNumerical(operator, left, right);
                return (double) left > (double) right;
            case LESS_EQUAL:
                mustBeNumerical(operator, left, right);
                return (double) left <= (double) right;
            case LESS:
                mustBeNumerical(operator, left, right);
                return (double) left < (double) right;
            case GREATER_EQUAL:
                mustBeNumerical(operator, left, right);
                return (double) left >= (double) right;
            default:
                break;
        }
        return null;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;
        // TODO: add warning mentioning only like-typed objects can be equal
        return left.equals(right);
    }

    private void mustBeNumerical(Token operator, Object... objects) {
        for (Object object : objects) {
            if (!(object instanceof Double)) {
                throw new RuntimeError(operator, "Operator can only be used with numbers.");
            }
        }
    }

    private Object handlePlus(Token operator, Object left, Object right) {
        if ((left instanceof Double) && (right instanceof Double)) {
            return (double) left + (double) right;
        }

        if ((left instanceof String) && (right instanceof String)) {
            return (String) left + (String) right;
        }
        throw new RuntimeError(operator, "Cannot add Objects of different types");
    }

    private Object handleStar(Token operator, Object left, Object right) {
        if ((left instanceof Double) && (right instanceof Double)) {
            return (double) left * (double) right;
        }

        if ((left instanceof String) && (right instanceof Double)) {
            return multiplyString(operator, (String) left, (double) right);
        }

        if ((left instanceof Double) && (right instanceof String)) {
            return multiplyString(operator, (String) right, (double) left);
        }
        throw new RuntimeError(operator, "Cannot multiply given Objects");
    }

    private String multiplyString(Token operator, String str, double multiple) {
        if ((double) multiple % 1 != 0) {
            throw new RuntimeError(operator, "Cannot multiply string with a non-integer");
        }

        StringBuilder multipliedString = new StringBuilder();
        for (int i = 0; i < multiple; i++) {
            multipliedString.append(str);
        }
        return multipliedString.toString();
    }

    private Object handleSlash(Token operator, Object left, Object right) {

        mustBeNumerical(operator, left, right);

        if ((double) right == 0) {
            throw new RuntimeError(operator, "Cannot Divide by 0");
        }

        return (double) left / (double) right;

    }

    @Override
    public Object visitGroupingExpression(Grouping expression) {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitLiteralExpression(Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitUnaryExpression(Unary expression) {
        Object right = expression.right;

        switch (expression.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                return -(double) right;
            default:
                break;
        }

        return null;
    }

    @Override
    public Object visitVariableExpression(Variable expression) {
        return environment.get(expression.name);
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    @Override
    public Void visitExpressionStatement(Statement.Expression statement) {
        Object value = evaluate(statement.expression);
        if (repl) {
            System.out.println(stringify(value));
        }
        return null;
    }

    @Override
    public Void visitPrintStatement(Print statement) {
        Object value = evaluate(statement.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStatement(Var statement) {
        Object value = statement.initializer != null ? evaluate(statement.initializer) : null;
        environment.define(statement.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitAssignExpression(Assign expression) {
        Object value = evaluate(expression.value);
        environment.assign(expression.name, value);
        return value;
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement.statements, new Environment(environment));
        return null;
    }

    private void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitIfStatement(If statement) {
        if (isTruthy(evaluate(statement.condition))) {
            execute(statement.thenBranch);
        } else if (statement.elseBranch != null) {
            execute(statement.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitLogicalExpression(Logical expression) {
        // we actually return the value so you can do things like var x = a == b or nil for ex.
        Object left = evaluate(expression.left);

        if (expression.operator.type == OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }
        return evaluate(expression.right);
    }

    @Override
    public Void visitWhileStatement(While statement) {

        while (isTruthy(evaluate(statement.condition))) {
            execute(statement.body);
        }

        return null;
    }
}
