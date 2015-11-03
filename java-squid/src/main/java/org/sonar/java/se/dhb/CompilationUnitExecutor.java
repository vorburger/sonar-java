package org.sonar.java.se.dhb;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CompilationUnitExecutor extends AbstractTreeVisitor {

  private static final int DEFAULT_MAXIMUM_COMPLEXITY = 2000;

  private final JavaFileScannerContext context;
  private final int maximumComplexity;
  private final Map<String, CompiledClass> classes = new HashMap<>();
  private Deque<CompiledClass> processedClasses = new LinkedList<>();
  private Deque<CompiledMethod> processedMethods = new LinkedList<>();

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
    processedClasses.push(new CompiledClass());
    for (Tree member : tree.members()) {
      member.accept(this);
    }
    classes.put(name, processedClasses.pop());
  }

  @Override
  public void visitMethod(MethodTree tree) {
    String name = tree.simpleName().name();
    final CompiledClass currentClass = processedClasses.peek();
    final CompiledMethod method = new CompiledMethod(currentClass.constraints(), tree);
    processedMethods.push(method);
    for (VariableTree variableTree : tree.parameters()) {
      loadConstraints(variableTree, method.constraints());
    }
    currentClass.putMethod(name, processedMethods.pop());
  }

  @Override
  public void visitVariable(VariableTree variableTree) {
    final CompiledClass currentClass = processedClasses.peek();
    loadConstraints(variableTree, currentClass.constraints());
  }

  private void loadConstraints(VariableTree variableTree, ConstraintsSet constraints) {
    final String name = variableTree.simpleName().name();
    ExpressionTree initializer = variableTree.initializer();
    if (initializer == null) {
      constraints.setConstraints(name, new NonNullSymbolicValue());
      for (AnnotationTree annotation : variableTree.modifiers().annotations()) {
        final String type = annotation.annotationType().toString();
        if ("Nullable".equals(type) || "CheckForNull".equals(type)) {
          constraints.setConstraints(name, new AnySymbolicValue());
        }
      }
    } else {
      constraints.setConstraints(variableTree.simpleName().name(), ValueGenerator.value(initializer, constraints));
    }
  }
}
