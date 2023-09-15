package com.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * @author peiyou
 * @version 1.0
 * @className GenerateAst
 * @date 2023/9/8 13:19
 **/
public class GenerateAst {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length != 1) {
            System.out.println("使用：generate_ast <输入的目录>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Unary    : Token operator, Expr right",
                "Ternary  : Expr condition, Expr left, Expr right",
                "Variable : Token name",
                "SelfIncOrDecr: Token name, Expr variable",
                "Lambda   : Token name, List<Token> params, List<Stmt> body"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body",
                "Break      : Token token",
                "Continue   : Token token",
                "Return     : Token token, Expr value"
        ));
    }

    /**
     * 生成Ast
     * @author Peiyou
     * @date 2023/9/8 13:24
     * @param outputDir
     * @param baseName
     * @param types 存放着 类型: 字段类型 字段1, 字段类型 字段2, 字段类型 字段3  ， 以这样的形式放的，冒号后面是字段（多个字段间用逗号分开），前面是类型
     * @return void
     */
    private static void defineAst(String outputDir, String baseName, List<String> types) throws FileNotFoundException, UnsupportedEncodingException {
        String path = outputDir + File.separator + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + "{");

        writer.println();
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        defineVisitor(writer, baseName, types);
        for (String type: types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("    static class " + className + " extends " + baseName + "{");
        // 构造函数
        writer.println("        " + className + "(" + fieldList + ") {");

        // 构造函数内的字段赋值
        String[] fields = fieldList.split(",");
        for (String field : fields) {
            field = field.trim();
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }
        writer.println("        }");

        writer.println();

        for (String field: fields) {
            field = field.trim();
            writer.println("        final " + field + ";");
        }

        //  accept 方法
        writer.println();
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" +
                className + baseName + "(this);");
        writer.println("        }");


        writer.println("    }");


    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }
}
