package org.sonar.java.se.dhb;

import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;

public class ValueGenerator extends AbstractTreeVisitor {

  private final ConstraintsSet constraints;
  private SymbolicValue value = new AnySymbolicValue();

  private ValueGenerator(ConstraintsSet constraints) {
    this.constraints = constraints;
  }

  public static SymbolicValue value(ExpressionTree syntaxNode, ConstraintsSet constraints) {
    ValueGenerator generator = new ValueGenerator(constraints);
    syntaxNode.accept(generator);
    return generator.value;
  }

  @Override
  public void visitIdentifier(IdentifierTree tree) {
    value = constraints.getValue(tree.name());
  }

  @Override
  public void visitMethodInvocation(MethodInvocationTree tree) {
    // FIXME If one can infer that the method returns a non-null value, then a non-null value should be assigned
    value = new NonNullSymbolicValue();
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    switch (tree.kind()) {
      case NULL_LITERAL:
        value = SymbolicValue.NULL;
        break;
      case STRING_LITERAL:
        value = new StringConstant(tree.token().text());
        break;
      case BOOLEAN_LITERAL:
        value = "true".equals(tree.token().text()) ? BooleanConstant.TRUE : BooleanConstant.FALSE;
        break;
      case CHAR_LITERAL:
        value = new CharacterConstant(tree.value());
        break;
      case INT_LITERAL:
      case LONG_LITERAL:
      case FLOAT_LITERAL:
      case DOUBLE_LITERAL:
        value = new NumberConstant(tree.value());
        break;
      default:
        throw new IllegalStateException("Unsupported constant type");
    }
  }
}
