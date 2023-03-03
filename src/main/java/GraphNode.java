import com.github.javaparser.ast.stmt.BlockStmt;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GraphNode {
    private String className;
    private String methodName;

    private BlockStmt blockStmt;
    private List<String> sequence = new ArrayList<>();

    private List<GraphNode> routes = new ArrayList<>();

    public GraphNode(String className, String methodName, BlockStmt blockStmt) {
        this.className = className;
        this.methodName = methodName;
        this.blockStmt = blockStmt;
    }

    public void addRoute(GraphNode graphNode) {
        routes.add(graphNode);
    }
}
