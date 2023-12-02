package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import jlox.RecursiveParser;
public class Lox {
    private static final Interpreter interpreter = new Interpreter();

    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    public static void main(String[] args) throws IOException {
        if(args.length > 1){
            System.out.println("Usage jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while(true) {
            System.out.println("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        RecursiveParser recursiveParser = new RecursiveParser(tokens);
        Expression expression = recursiveParser.parse();
        if (hadError) return;

        interpreter.interpret(expression);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, String.format(" at '%s'", token.lexeme), message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println(String.format("[line %2d] Error%s: %s", line, where, message));
        hadError = true;
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(String.format(error.getMessage(),"\n[line %2d]", error.token.line));
        hadRuntimeError = true;
    }
}