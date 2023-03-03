import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.Navigator;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static String ALL_TYPES = "src/test/java";
    private static String CONCRETE_TEST = "src/test/java/test2";
    private static String OUTPUT = "./out/";


    private static final List<GraphNode> graphNodes = new ArrayList<>();

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
        List<File> javaFiles = findJavaFiles(new File(CONCRETE_TEST));

        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
        combinedSolver.add(new ReflectionTypeSolver());
        combinedSolver.add(new JavaParserTypeSolver(new File(ALL_TYPES)));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
        StaticJavaParser
                .getParserConfiguration()
                .setSymbolResolver(symbolSolver);

        for (File javaFile : javaFiles) {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            List<ConstructorDeclaration> constructors = new ArrayList<>();
            new ConstructorCollector().visit(cu, constructors);
            constructors.forEach(method -> {
                graphNodes.add(new GraphNode(getNameOfClass(method.resolve().getQualifiedSignature()), method.resolve().getQualifiedSignature(), method.getBody()));
            });

            List<MethodDeclaration> methods = new ArrayList<>();
            new MethodCollector().visit(cu, methods);
            methods.forEach(method -> {
                graphNodes.add(new GraphNode(getNameOfClass(method.resolve().getQualifiedSignature()), method.resolve().getQualifiedSignature(), method.getBody().orElse(null)));
            });
        }

        graphNodes.forEach(Main::fillRoutes);

        graphNodes.forEach(g -> {
            if (g.getSequence().size() > 0) {
                var diagram = makeSequenceDiagram(g);
//                System.out.println(diagram);

                SourceStringReader reader = new SourceStringReader(diagram);
                File png = new File(OUTPUT + (g.getMethodName()).replace(".", "_") + ".png");
                try {
                    reader.generateImage(png);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static String makeSequenceDiagram(GraphNode g) {
        StringBuilder sb = new StringBuilder();
        Map<String, Integer> participants = new HashMap<>();

        sb.append("title ").append(g.getClassName()).append(".").append(g.getMethodName()).append("\n");
        g.getSequence().add(0, "activate " + g.getClassName());
        g.getSequence().add(g.getSequence().size() - 1, "deactivate " + g.getClassName());
        g.getSequence().forEach(s -> {
            if (s.contains("->")) {
                var left = s.split("->")[1].split(":")[0].trim();
                if (!participants.containsKey(left)) {
                    participants.put(left, 1);
                } else {
                    participants.put(left, participants.get(left) + 1);
                }
            }
            sb.append(s).append("\n");
        });
        sb.append("@enduml\n");
        //order participants base on integer
        var orderedParticipants = participants.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toList());
        Collections.reverse(orderedParticipants);
//        orderedParticipants.forEach(p -> sb.insert(0, "participant " + p.getKey() + "\n"));
//        sb.insert(0, "participant " + g.getClassName() + "\n");
        sb.insert(0, "@startuml\n");
        return sb.toString();
    }

    private static String getNameOfClass(String methodSignature) {
        var left = methodSignature.split("\\(")[0];
        return left.substring(0, left.lastIndexOf("."));
    }

    private static GraphNode findGraphNode(String className, String signature) {
        return graphNodes.stream().filter(g -> g.getClassName().equals(className) && g.getMethodName().equals(signature)).findFirst().orElse(null);
    }

    private static void parseNode(GraphNode graphNode, Node node, List<String> result) {
        if (node instanceof ObjectCreationExpr) {
            var className = ((ObjectCreationExpr) node).getType().resolve().describe();
            var signature = ((ObjectCreationExpr) node).toString().replace("new ", "");
            var route = findGraphNode(className, className + "." + signature);
            if (route != null && route.getSequence().size() == 0) {
                fillRoutes(route);
            }
            if (route != null) {
                graphNode.addRoute(route);

                result.add(graphNode.getClassName() + " -> " + className + ": " + signature);
                result.add("activate " + className);
                result.addAll(route.getSequence());
                result.add(graphNode.getClassName() + " <-- " + className);
                result.add("deactivate " + className);
            }
            return;
        }
        if (node instanceof MethodCallExpr) {
            var signature = ((MethodCallExpr) node).resolve().getQualifiedSignature();
            var className = ((MethodCallExpr) node).resolve().declaringType().getQualifiedName();
            var route = findGraphNode(className, signature);
            if (route != null)
                graphNode.addRoute(route);

            //recursive call
            if (className.equals(graphNode.getClassName()) && signature.equals(graphNode.getMethodName())) {

                result.add(graphNode.getClassName() + " -> " + graphNode.getClassName() + ": " + signature);
                result.add("activate " + graphNode.getClassName());
                result.add("deactivate " + graphNode.getClassName());
                return;
            }

            //own call
            if (className.equals(graphNode.getClassName())) {
                result.add(graphNode.getClassName() + " -> " + graphNode.getClassName() + ": " + signature);
                if (route != null && route.getSequence().size() == 0) {
                    fillRoutes(route);
                }
                if (route != null)
                    result.addAll(route.getSequence());
                return;
            }

            result.add(graphNode.getClassName() + " -> " + className + ": " + signature);
            result.add("activate " + className);
            if (route != null && route.getSequence().size() == 0) {
                fillRoutes(route);
            }
            if (route != null)
                result.addAll(route.getSequence());
            else
                if (node.findAll(MethodCallExpr.class).size() > 0)
                    node.getChildNodes().forEach(child -> parseNode(graphNode, child, result));
            result.add(graphNode.getClassName() + " <-- " + className);
            result.add("deactivate " + className);
            return;
        }
        if (node instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) node;
            if (ifStmt.getThenStmt() != null) {
                result.add("alt " + ifStmt.getCondition().toString());
                parseNode(graphNode, ifStmt.getThenStmt(), result);
            }
            if (ifStmt.getElseStmt().isPresent()) {
                result.add("else");
                parseNode(graphNode, ifStmt.getElseStmt().orElse(null), result);
            }
            result.add("end");
            return;
        }
        if (node instanceof ForStmt) {
            ForStmt forStmt = (ForStmt) node;
            result.add("loop " + forStmt.getInitialization().get(0).toString() + ";" + forStmt.getCompare().get().toString() + ";" + forStmt.getUpdate().get(0).toString());
            parseNode(graphNode, forStmt.getBody(), result);
            result.add("end");
            return;
        }
        if (node instanceof ForEachStmt) {
            ForEachStmt forStmt = (ForEachStmt) node;
            result.add("loop " + forStmt.getIterable().toString());
            parseNode(graphNode, forStmt.getBody(), result);
            result.add("end");
            return;
        }
        if (node instanceof WhileStmt) {
            WhileStmt whileStmt = (WhileStmt) node;
            result.add("loop " + whileStmt.getCondition().toString());
            parseNode(graphNode, whileStmt.getBody(), result);
            result.add("end");
            return;
        }
        node.getChildNodes().forEach(child -> parseNode(graphNode, child, result));
    }


    private static void fillRoutes(GraphNode graphNode) {
        if (graphNode.getSequence().size() > 0)
            return;
        List<String> methodCalls = new ArrayList<>();
        if (graphNode.getBlockStmt() == null)
            return;
        graphNode.getBlockStmt().getChildNodes().forEach(node -> {
            parseNode(graphNode, node, methodCalls);
        });
        graphNode.setSequence(methodCalls);
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
