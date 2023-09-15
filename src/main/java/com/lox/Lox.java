package com.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author peiyou
 * @version 1.0
 * @className Lox
 * @date 2023/9/8 09:14
 **/
public class Lox {

    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    // 交互式
    static boolean repl = true;

    private static final Interpreter interpreter = new Interpreter();
    private static final ReplInterpreter replInterpreter = new ReplInterpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("使用：jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            repl = false;
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * 运行path的脚本
     * @author Peiyou
     * @date 2023/9/8 09:20
     * @param path
     * @return void
     */
    private static void runFile(String path) throws IOException {

        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    /**
     * 交互式运行代码
     * @author Peiyou
     * @date 2023/9/8 09:22
     * @return void
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            // Control-D 退出
            if (line == null) break;

            Scanner scanner = new Scanner(line);
            List<Token> tokens = scanner.scanTokens();
            StringBuilder sb = new StringBuilder(line);
            int missRightBraceCount = 0;
            while (!isInputEnd(tokens) || ((missRightBraceCount = countMissRightBrace(tokens)) > 0)) {
                System.out.print("> ");
                for (int i = 0; i < missRightBraceCount; i++) {
                    System.out.print("\t");
                }
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
                scanner = new Scanner(sb.toString());
                tokens = scanner.scanTokens();
            }
            run(sb.toString());
            hadError = false;
        }
    }

    private static boolean isInputEnd(List<Token> tokens) {
        if (tokens.size() == 1) return true;
        return (tokens.size() > 1 && isEnclosing(tokens.get(tokens.size() - 2)));
    }

    private static boolean isEnclosing(Token token) {
         switch (token.type) {
             case TERNARY_QUESTION_MARK:
             case TERNARY_COLON:
             case COMMA:
             case DOT:
             case MINUS:
             case PLUS:
             case SLASH:
             case STAR:
             case LOGIC_AND:
             case LOGIC_OR:
             case BANG_EQUAL:
             case EQUAL:
             case EQUAL_EQUAL:
             case GREATER:
             case GREATER_EQUAL:
             case LESS:
             case LESS_EQUAL:
             case AND:
             case CLASS:
             case ELSE:
             case FUN:
             case FOR:
             case IF:
             case OR:
             case PRINT:
             case RETURN:
             case VAR:
             case WHILE:
                 return false;
             default: return true;
         }

    }

    private static int countMissRightBrace(List<Token> tokens) {
        int count = 0;
        for (Token token: tokens) {
            if (token.type == TokenType.LEFT_BRACE) {
                count++;
            } else if (token.type == TokenType.RIGHT_BRACE) {
                count--;
            }
        }
        return count;
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hadError) return;
        Resolver resolver = new Resolver(interpreter);
        if (hadError) return;
        resolver.resolve(statements);
        interpreter.interpret(statements);
//        System.out.println(new AstPrinter().print(expression));
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where,
                               String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
