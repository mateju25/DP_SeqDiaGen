import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GraphNode {
    private String className;
    private String methodName;

    private Map<String, String> membersMap = new HashMap<>();

    private List<FieldDeclaration> classMembers = new ArrayList<>();
    private List<Parameter> parametersMembers = new ArrayList<>();
    private List<VariableDeclarationExpr> variableMembers = new ArrayList<>();

    private BlockStmt blockStmt;
    private List<String> sequence = new ArrayList<>();

    private List<GraphNode> routes = new ArrayList<>();

    public GraphNode(String className, String methodName, BlockStmt blockStmt, List<FieldDeclaration> classMembers, List<Parameter> parametersMembers, List<VariableDeclarationExpr> variableMembers) {
        this.className = className;
        this.methodName = methodName;
        this.blockStmt = blockStmt;
        this.classMembers = classMembers;
        this.parametersMembers = parametersMembers;
        this.variableMembers = variableMembers;

        parametersMembers.forEach(parameter -> {
            if (parameter.findAll(ClassOrInterfaceType.class).size() > 0 && parameter.findAll(SimpleName.class).size() > 0) {
                var type = parameter.getType().resolve().describe();
                var name = parameter.getNameAsString();
                membersMap.put(name, type);
            }
        });

        variableMembers.forEach(parameter -> {
            if (parameter.findAll(VariableDeclarator.class).size() > 0) {
                var type = parameter.findAll(VariableDeclarator.class).get(0).getType().resolve().describe();
                if (parameter.findAll(ObjectCreationExpr.class).size() > 0) {
                    type = parameter.findAll(ObjectCreationExpr.class).get(0).getType().resolve().describe();
                }
                var name = parameter.findAll(VariableDeclarator.class).get(0).getNameAsString();
                membersMap.put(name, type);
            }
        });

        classMembers.forEach(parameter -> {
            if (parameter.findAll(VariableDeclarator.class).size() > 0) {
                var type = parameter.findAll(VariableDeclarator.class).get(0).getType().resolve().describe();
                if (parameter.findAll(ObjectCreationExpr.class).size() > 0) {
                    type = parameter.findAll(ObjectCreationExpr.class).get(0).getType().resolve().describe();
                }
                var name = parameter.findAll(VariableDeclarator.class).get(0).getNameAsString();
                membersMap.put(name, type);
            }
        });
    }

    public void addRoute(GraphNode graphNode) {
        routes.add(graphNode);
    }

    public FieldDeclaration findByName(String name) {
        for (FieldDeclaration fieldDeclaration : classMembers) {
            if (fieldDeclaration.getVariables().get(0).getNameAsString().equals(name)) {
                return fieldDeclaration;
            }
        }
        return null;
    }

    public Parameter findParameterByName(String name) {
        for (Parameter parameter : parametersMembers) {
            if (parameter.getNameAsString().equals(name)) {
                return parameter;
            }
        }
        return null;
    }

    public VariableDeclarationExpr findVariableByName(String name) {
        for (VariableDeclarationExpr variableDeclarationExpr : variableMembers) {
            if (variableDeclarationExpr.getVariables().get(0).getNameAsString().equals(name)) {
                return variableDeclarationExpr;
            }
        }
        return null;
    }
}
