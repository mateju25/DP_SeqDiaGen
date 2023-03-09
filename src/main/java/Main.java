import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final String LIBS = "./libs";
    private static final String CONCRETE_TEST = "src/test2/java";
    private static final String OUTPUT = "./out2/";

    private static final Boolean ONLY_PROJECT = true;


    private static final List<GraphNode> graphNodes = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        List<File> javaFiles = FileUtils.findJavaFiles(new File(CONCRETE_TEST));
        List<File> jarFiles = FileUtils.findJarFiles(new File(LIBS));

        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
        combinedSolver.add(new ReflectionTypeSolver());
        combinedSolver.add(new JavaParserTypeSolver(new File(CONCRETE_TEST)));
        jarFiles.forEach(jarFile -> {
            try {
                combinedSolver.add(new JarTypeSolver(jarFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
        StaticJavaParser
                .getParserConfiguration()
                .setSymbolResolver(symbolSolver);

        for (File javaFile : javaFiles) {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            List<FieldDeclaration> fields = new ArrayList<>();
            List<ConstructorDeclaration> constructors = new ArrayList<>();
            List<MethodDeclaration> methods = new ArrayList<>();

            var classType = cu.getClassByName(javaFile.getName().replace(".java", ""));
            if (classType.isPresent()) {
                fields = classType.get().findAll(FieldDeclaration.class);
                constructors = classType.get().findAll(ConstructorDeclaration.class);
                methods = classType.get().findAll(MethodDeclaration.class);
            }

            List<FieldDeclaration> finalFields = fields;
            constructors.forEach(method -> {
                var params = new ArrayList<>(method.getParameters());
                var declarations = method.findAll(VariableDeclarationExpr.class);
                graphNodes.add(new GraphNode(getNameOfClass(method.resolve().getQualifiedSignature()), method.resolve().getQualifiedSignature(), method.getBody(), finalFields, params, declarations));
            });

            List<FieldDeclaration> finalFields1 = fields;
            methods.forEach(method -> {
                var params = new ArrayList<>(method.getParameters());
                var declarations = method.findAll(VariableDeclarationExpr.class);
                graphNodes.add(new GraphNode(getNameOfClass(method.resolve().getQualifiedSignature()), method.resolve().getQualifiedSignature(), method.getBody().orElse(null), finalFields1, params, declarations));
            });
        }
        for (int i = 0; i < graphNodes.size(); i++) {
            generateNode(graphNodes.get(i));
        }
//        graphNodes.forEach(Main::generateNode);

        graphNodes.forEach(g -> {
            if (g.getSequence().size() > 0) {
                var diagram = makeSequenceDiagram(g);
                if (diagram == null)
                    return;

                SourceStringReader reader = new SourceStringReader(diagram);
                File png = new File(OUTPUT + (g.getClassName()).replaceAll("[.<>() ?]", "_") + "_" + graphNodes.indexOf(g) + ".png");
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

        sb.append("title ").append(g.getMethodName()).append("\n");
        g.getSequence().add(0, "activate " + g.getClassName());
        g.getSequence().forEach(s -> {
            if (s.contains("activate") && !s.contains("deactivate")) {
                var participant = s.replace("activate ", "");
                if (!participants.containsKey(participant)) {
                    participants.put(participant, 1);
                } else {
                    participants.put(participant, participants.get(participant) + 1);
                }
            }
            sb.append(s).append("\n");
        });
        if (participants.values().size() <= 1)
            return null;
        sb.append("deactivate ").append(g.getClassName()).append("\n");
        sb.append("@enduml\n");
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

    private static void fillDiagramFromNode(GraphNode from, String to, String signature, GraphNode inside, List<String> result) {
        generateNode(inside);
        from.addRoute(inside);

        if (ONLY_PROJECT && findGraphNode(to, signature) == null)
            return;

        result.add(from.getClassName() + " -> " + to + ": " + signature);
        result.add("activate " + to);
        result.addAll(inside.getSequence());
        result.add(from.getClassName() + " <-- " + to + ": " + signature);
        result.add("deactivate " + to);
    }

    private static void parseNode(GraphNode graphNode, Node node, List<String> result) {
        if (!(node instanceof ObjectCreationExpr || node instanceof ExplicitConstructorInvocationStmt ||
                node instanceof MethodCallExpr || node instanceof IfStmt || node instanceof ForStmt ||
                node instanceof ForEachStmt || node instanceof WhileStmt || node instanceof DoStmt)) {
            node.getChildNodes().forEach(child -> parseNode(graphNode, child, result));
            return;
        }

        //constructors
        if (node instanceof ObjectCreationExpr) {
            String className = ((ObjectCreationExpr) node).getType().resolve().describe();
            String signature = node.toString().replace("new ", "");
            var route = findGraphNode(className, className + "." + signature);
            if (route != null) {
                fillDiagramFromNode(graphNode, className, signature, route, result);
            }
            return;
        }
        if (node instanceof ExplicitConstructorInvocationStmt) {
            String signature = ((ExplicitConstructorInvocationStmt) node).resolve().getQualifiedSignature();
            String className = getNameOfClass(signature);
            var route = findGraphNode(className, signature);
            if (route != null) {
                fillDiagramFromNode(graphNode, className, signature, route, result);
            }
            return;
        }

        if (node instanceof MethodCallExpr) {
            String signature = ((MethodCallExpr) node).resolve().getQualifiedSignature();
            String className = ((MethodCallExpr) node).resolve().declaringType().getQualifiedName();

            if (((MethodCallExpr) node).getScope().isPresent()) {
                var name = ((MethodCallExpr) node).getScope().get().toString();

                var newType = graphNode.getMembersMap().get(name);
                if (newType != null) {
                    signature = signature.replace(className, newType);
                    className = newType;
                }
            }

            String changedSignature = null;
            if (((MethodCallExpr) node).getArguments().size() > 0) {
                var classNameAndMethod = signature.split("\\(")[0];
                var args = new ArrayList<String>();
                ((MethodCallExpr) node).getArguments().forEach(expression -> {
                    String newParamType;
                    if (expression instanceof ObjectCreationExpr)
                        args.add(((ObjectCreationExpr) expression).getType().resolve().describe());
                    if (expression instanceof NameExpr)
                        if (graphNode.getMembersMap().containsKey(((NameExpr) expression).getNameAsString()))
                            args.add(graphNode.getMembersMap().get(((NameExpr) expression).getNameAsString()));
                });
                if (args.size() > 0) {
                    changedSignature = signature;
                    signature = classNameAndMethod + "(" + String.join(", ", args) + ")";
                }
            }

            var route = findGraphNode(className, signature);
            if (changedSignature != null) {
                var possible = findGraphNode(className, changedSignature);
                if (possible != null) {
                    graphNodes.add(new GraphNode(className, signature, possible.getBlockStmt(), possible.getClassMembers(), possible.getParametersMembers(), possible.getVariableMembers()));
                    route = findGraphNode(className, signature);
                    var args = signature.split("\\)")[0].split("\\(")[1].split(", ");
                    for (int i = 0; i < args.length; i++) {
                        var param = route.getParametersMembers().get(i);
                        if (!param.getType().resolve().describe().equals(args[i]))
                            route.getMembersMap().put(param.getNameAsString(), args[i]);
                    }
                }
            }
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
                    generateNode(route);
                }
                if (route != null)
                    result.addAll(route.getSequence());
                return;
            }

            if (ONLY_PROJECT && findGraphNode(className, signature) == null)
                return;

            result.add(graphNode.getClassName() + " -> " + className + ": " + signature);
            result.add("activate " + className);
            if (route != null) {
                generateNode(route);
                result.addAll(route.getSequence());
            } else {
                if (node.findAll(LambdaExpr.class).size() > 0) {
                    //TODO lambda
                    var newNode = new GraphNode(className, signature, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                    node.getChildNodes().forEach(child -> parseNode(newNode, child, result));
                } else {
                    if (node.findAll(MethodCallExpr.class).size() > 0)
                        node.getChildNodes().forEach(child -> parseNode(graphNode, child, result));
                }
            }
            result.add(graphNode.getClassName() + " <-- " + className);
            result.add("deactivate " + className);
            return;
        }
        //condition
        if (node instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) node;
            if (ifStmt.getThenStmt() != null) {
                result.add("alt " + ifStmt.getCondition().toString().replaceAll("[\r\n]", ""));
                parseNode(graphNode, ifStmt.getThenStmt(), result);
            }
            if (ifStmt.getElseStmt().isPresent()) {
                result.add("else");
                parseNode(graphNode, ifStmt.getElseStmt().orElse(null), result);
            }
            if (ONLY_PROJECT && result.get(result.size() - 1).contains("alt")) {
                result.remove(result.get(result.size() - 1));
                return;
            }
            result.add("end");
            return;
        }
        //loops
        if (node instanceof ForStmt) {
            ForStmt forStmt = (ForStmt) node;
            result.add("loop " + forStmt.getInitialization().stream().map(Node::toString).collect(Collectors.joining(",")) + ";" + (forStmt.getCompare().isPresent() ? forStmt.getCompare().get().toString() : "") + ";" + forStmt.getUpdate().stream().map(Node::toString).collect(Collectors.joining(",")));
            parseNode(graphNode, forStmt.getBody(), result);
            if (ONLY_PROJECT && result.get(result.size() - 1).contains("loop")) {
                result.remove(result.get(result.size() - 1));
                return;
            }
            result.add("end");
            return;
        }
        if (node instanceof ForEachStmt) {
            ForEachStmt forStmt = (ForEachStmt) node;
            result.add("loop " + forStmt.getIterable().toString());
            parseNode(graphNode, forStmt.getBody(), result);
            if (ONLY_PROJECT && result.get(result.size() - 1).contains("loop")) {
                result.remove(result.get(result.size() - 1));
                return;
            }
            result.add("end");
            return;
        }
        if (node instanceof WhileStmt) {
            WhileStmt whileStmt = (WhileStmt) node;
            result.add("loop " + whileStmt.getCondition().toString());
            parseNode(graphNode, whileStmt.getBody(), result);
            if (ONLY_PROJECT && result.get(result.size() - 1).contains("loop")) {
                result.remove(result.get(result.size() - 1));
                return;
            }
            result.add("end");
            return;
        }
        if (node instanceof DoStmt) {
            DoStmt whileStmt = (DoStmt) node;
            result.add("loop " + whileStmt.getCondition().toString());
            parseNode(graphNode, whileStmt.getBody(), result);
            if (ONLY_PROJECT && result.get(result.size() - 1).contains("loop")) {
                result.remove(result.get(result.size() - 1));
                return;
            }
            result.add("end");
            return;
        }
    }


    private static void generateNode(GraphNode graphNode) {
        if (graphNode.getSequence().size() > 0)
            return;
        List<String> umlStrings = new ArrayList<>();
        if (graphNode.getBlockStmt() == null)
            return;
        graphNode.getBlockStmt().getChildNodes().forEach(node -> {
            parseNode(graphNode, node, umlStrings);
        });
        graphNode.setSequence(umlStrings);
    }


}
