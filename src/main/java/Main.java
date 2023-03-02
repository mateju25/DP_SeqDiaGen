import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.Navigator;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static List<File> findJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    javaFiles.addAll(findJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }

    public static void main(String[] args) throws IOException {
        List<File> javaFiles = findJavaFiles(new File("src/test/java/test2"));

        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File("src/test/java"));

        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
        combinedSolver.add(reflectionTypeSolver);
        combinedSolver.add(javaParserTypeSolver);

//        for (File javaFile : javaFiles) {
//            showReferenceTypeDeclaration(combinedSolver.solveType(javaFile.getName().replace(".java", "")));
//        }

        for (File javaFile : javaFiles) {
            var className = javaFile.getName().replace(".java", "");
            if (!className.equals("Main"))
                continue;
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
            StaticJavaParser
                    .getParserConfiguration()
                    .setSymbolResolver(symbolSolver);

            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            System.out.println(cu.getClassByName(className).isPresent() ? cu.getClassByName(className).get().resolve().getQualifiedName() : cu.getInterfaceByName(className).get().resolve().getQualifiedName());
            List<ConstructorDeclaration> constructors = new ArrayList<>();
            new ConstructorCollector().visit(cu, constructors);
            constructors.forEach(method -> {
                System.out.println("----" + method.resolve().getQualifiedSignature());
                var methodCalls = getContructorCalls(method);
                printMethods(className, cu, methodCalls);
            });

            List<MethodDeclaration> methods = new ArrayList<>();
            new MethodCollector().visit(cu, methods);
            methods.forEach(method -> {
                System.out.println("----" + method.resolve().getQualifiedSignature());
                var methodCalls = getMethodCalls(method);
                printMethods(className, cu, methodCalls);
            });
        }
    }

    private static void printMethods(String className, CompilationUnit cu, List<String> methodCalls) {
        if (!methodCalls.isEmpty()) {
            List<FieldDeclaration> fields = new ArrayList<>();
            new FieldCollector().visit(cu, fields);

            methodCalls.forEach(methodCall -> {
                if (methodCall.startsWith("super(")) {
                    System.out.println("--------" + methodCall.replace("super", cu.getClassByName(className).get().getExtendedTypes().get(0).resolve().asReferenceType().getQualifiedName()));
                    return;
                }
                System.out.println("--------" + methodCall);
            });
        }
    }


    private static List<String> getMethodCalls(MethodDeclaration method) {
        List<String> methodCalls = new ArrayList<>();
        method.findAll(MethodCallExpr.class).forEach(methodCall -> {
            methodCalls.add(methodCall.resolve().getQualifiedSignature());
        });
        return methodCalls;
    }

    private static List<String> getContructorCalls(ConstructorDeclaration constructor) {
        List<String> methodCalls = new ArrayList<>();
        constructor.findAll(ConstructorDeclaration.class).forEach(methodCall -> {
            if (methodCall.findAll(BlockStmt.class).get(0).toString().contains("super")) {
                methodCalls.add("super(" + methodCall.getParameters().stream().map(p -> p.getType().asString()).collect(Collectors.joining(",")) + ")");
                return;
            }
            if (methodCall.findAll(BlockStmt.class).get(0).toString().contains("this")) {
                methodCalls.add("this(" + methodCall.getParameters().stream().map(p -> p.getType().asString()).collect(Collectors.joining(",")) + ")");
                return;
            }
            methodCalls.add(methodCall.resolve().getQualifiedSignature());
        });
        constructor.findAll(MethodCallExpr.class).forEach(methodCall -> {
            methodCalls.add(methodCall.resolve().getQualifiedSignature());
        });
        return methodCalls;
    }

    private static class FieldCollector extends VoidVisitorAdapter<List<FieldDeclaration>> {

        @Override
        public void visit(FieldDeclaration md, List<FieldDeclaration> collector) {
            super.visit(md, collector);
            collector.add(md);
        }
    }

    private static class MethodCollector extends VoidVisitorAdapter<List<MethodDeclaration>> {

        @Override
        public void visit(MethodDeclaration md, List<MethodDeclaration> collector) {
            super.visit(md, collector);
            collector.add(md);
        }
    }

    private static class ConstructorCollector extends VoidVisitorAdapter<List<ConstructorDeclaration>> {

        @Override
        public void visit(ConstructorDeclaration md, List<ConstructorDeclaration> collector) {
            super.visit(md, collector);
            collector.add(md);
        }
    }

    private static void showReferenceTypeDeclaration(ResolvedReferenceTypeDeclaration resolvedReferenceTypeDeclaration) {
        System.out.printf("== %s ==%n",
                resolvedReferenceTypeDeclaration.getQualifiedName());
        System.out.println(" fields:");
        resolvedReferenceTypeDeclaration.getAllFields().forEach(f ->
                System.out.printf(" %s %s%n", f.getType(), f.getName()));
        System.out.println(" methods:");
        resolvedReferenceTypeDeclaration.getAllMethods().forEach(m ->
                System.out.printf(" %s%n", m.getQualifiedSignature()));
        System.out.println();
    }
}
