package org.sonar.java.se.dhb;

import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.SwitchStatementTree;
import org.sonar.plugins.java.api.tree.Tree;

public class NameGenerator extends AbstractTreeVisitor {

  private String name;

  private NameGenerator() {
  }

  public static String name(Tree syntaxNode) {
    NameGenerator generator = new NameGenerator();
    syntaxNode.accept(generator);
    return generator.name == null ? syntaxNode.toString() : generator.name;
  }

  @Override
  public void visitIdentifier(IdentifierTree tree) {
    name = tree.name();
  }

  @Override
  public void visitMethodInvocation(MethodInvocationTree tree) {
    name = tree.symbol().name();
  }

  @Override
  public void visitMemberSelectExpression(MemberSelectExpressionTree tree) {
    tree.expression().accept(this);
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    tree.expression().accept(this);
  }
}
