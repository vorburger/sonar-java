package org.sonar.java.se.dhb;

import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.VariableTree;

public abstract class AbstractSyntaxVisitor extends AbstractTreeVisitor {

  private final ConstraintsSet constraints;

  protected AbstractSyntaxVisitor(ConstraintsSet constraints) {
    this.constraints = constraints;
  }

  public ConstraintsSet constraints() {
    return constraints;
  }

  @Override
  public void visitVariable(VariableTree variableTree) {
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
