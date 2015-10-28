package org.sonar.java.se.dhb;

import org.sonar.java.cfg.CFG;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CompilationUnitExecutor extends AbstractTreeVisitor {

  private static final int DEFAULT_MAXIMUM_COMPLEXITY = 2000;

  private final ConstraintsSet classConstraints = new ConstraintsSet();
  private ConstraintsSet contextualConstraints;
  private final Map<String, MethodTree> methods = new HashMap<>();
  private final Map<String, ConstraintsSet> methodConstraints = new HashMap<>();

  public CompilationUnitExecutor(CompilationUnitTree compilationTree) {
    contextualConstraints = classConstraints;
    for (Tree tree : ((ClassTree) compilationTree.types().get(0)).members()) {
      tree.accept(this);
    }
  }

  public void execute(JavaFileScannerContext context) {
    execute(context, DEFAULT_MAXIMUM_COMPLEXITY);
  }

  public void execute(JavaFileScannerContext context, int maximumComplexity) {
    for (Entry<String, MethodTree> entry : methods.entrySet()) {
      final String methodName = entry.getKey();
      final MethodTree method = entry.getValue();
      final ConstraintsSet constraints = methodConstraints.get(methodName);
      if (method.block() != null) {
        CFG cfg = CFG.build(method);
        SymbolicExecutor executor = new SymbolicExecutor(context, maximumComplexity, constraints);
        executor.execute(cfg);
      }
    }
  }

  @Override
  public void visitMethod(MethodTree tree) {
    try {
      String name = tree.simpleName().name();
      contextualConstraints = new ConstraintsSet(classConstraints);
      methods.put(name, tree);
      methodConstraints.put(name, contextualConstraints);
      for (VariableTree parameter : tree.parameters()) {
        parameter.accept(this);
      }
    } finally {
      contextualConstraints = classConstraints;
    }
  }

  @Override
  public void visitVariable(VariableTree variableTree) {
    final String name = variableTree.simpleName().name();
    ExpressionTree initializer = variableTree.initializer();
    if (initializer == null) {
      contextualConstraints.setConstraints(name, new NonNullSymbolicValue());
      for (AnnotationTree annotation : variableTree.modifiers().annotations()) {
        final String type = annotation.annotationType().toString();
        if ("Nullable".equals(type)) {
          contextualConstraints.setConstraints(name, new AnySymbolicValue());
        }
      }
    } else {
      contextualConstraints.setConstraints(variableTree.simpleName().name(), ValueGenerator.value(initializer, contextualConstraints));
    }
  }
}
