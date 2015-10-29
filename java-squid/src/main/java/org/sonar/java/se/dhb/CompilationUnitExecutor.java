package org.sonar.java.se.dhb;

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

public class CompilationUnitExecutor extends AbstractTreeVisitor {

  private static final int DEFAULT_MAXIMUM_COMPLEXITY = 2000;

  private final JavaFileScannerContext context;
  private final int maximumComplexity;
  private final Map<String, CompiledClass> classes = new HashMap<>();
  private CompiledClass currentClass;
  private CompiledMethod currentMethod;
  private ConstraintsSet currentConstraints;

  public CompilationUnitExecutor(CompilationUnitTree compilationTree, JavaFileScannerContext context) {
    this(compilationTree, context, DEFAULT_MAXIMUM_COMPLEXITY);
  }

  public CompilationUnitExecutor(CompilationUnitTree compilationTree, JavaFileScannerContext context, int maximumComplexity) {
    for (Tree tree : compilationTree.types()) {
      tree.accept(this);
    }
    this.context = context;
    this.maximumComplexity = maximumComplexity;
  }

  public void executeAll() {
    for (CompiledClass clazz : classes.values()) {
      clazz.executeAll(context, maximumComplexity);
    }
  }

  public void execute(String className) {
    classes.get(className).executeAll(context, maximumComplexity);
  }

  public void execute(String className, String methodName) {
    classes.get(className).execute(methodName, context, maximumComplexity);
    }

  @Override
  public void visitClass(ClassTree tree) {
    String name = tree.simpleName().name();
    currentClass = new CompiledClass();
    for (Tree member : tree.members()) {
      member.accept(this);
    }
    classes.put(name, currentClass);
    currentClass = null;
  }

  @Override
  public void visitMethod(MethodTree tree) {
    String name = tree.simpleName().name();
    currentMethod = new CompiledMethod(currentClass.constraints(), tree);
    currentClass.putMethod(name, currentMethod);
    currentMethod = null;
  }

  @Override
  public void visitVariable(VariableTree variableTree) {
    if (currentClass != null) {
      ConstraintsSet constraints = currentMethod == null ? currentClass.constraints() : currentClass.constraints();
      final String name = variableTree.simpleName().name();
      ExpressionTree initializer = variableTree.initializer();
      if (initializer == null) {
        constraints.setConstraints(name, new NonNullSymbolicValue());
        for (AnnotationTree annotation : variableTree.modifiers().annotations()) {
          final String type = annotation.annotationType().toString();
          if ("Nullable".equals(type)) {
            constraints.setConstraints(name, new AnySymbolicValue());
          }
        }
      } else {
        constraints.setConstraints(variableTree.simpleName().name(), ValueGenerator.value(initializer, constraints));
      }
    }
  }
}
