package org.sonar.java.se.dhb;

import org.sonar.java.cfg.CFG.Block;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import java.util.Set;

public class SymbolicProcessor extends AbstractTreeVisitor {

  private final SymbolicExecutor executor;
  private final ConstraintsSet constraints;
  private AbstractSymbolicException exception;

  public SymbolicProcessor(SymbolicExecutor symbolicExecutor, ConstraintsSet initialConstaints) {
    executor = symbolicExecutor;
    constraints = new ConstraintsSet(initialConstaints);
  }

  public SymbolicProcessor(SymbolicExecutor symbolicExecutor, SymbolicProcessor spawner) {
    executor = symbolicExecutor;
    constraints = new ConstraintsSet(spawner.constraints);
  }

  public SymbolicProcessor next() {
    return new SymbolicProcessor(executor, this);
  }

  public void process(Block block) throws AbstractSymbolicException {
    try {
      if (executor.hasNotProcessed(block, constraints.clone())) {
        for (Tree element : block.elements()) {
          process(element);
        }
        TerminatorProcessor terminator = new TerminatorProcessor(this, block);
        terminator.process();
        final Set<Block> successors = terminator.successors();
        SymbolicProcessor processor = this;
        for (Block successor : successors) {
          processor.process(successor);
          processor = new SymbolicProcessor(executor, processor);
        }
      }
    } catch (AbstractSymbolicException e) {
      executor.report(e);
    }
  }

  private void process(Tree element) throws AbstractSymbolicException {
    exception = null;
    element.accept(this);
    if (exception != null) {
      throw exception;
    }
  }

  @Override
  public void visitMemberSelectExpression(MemberSelectExpressionTree element) {
    ExpressionTree target = element.expression();
    SymbolicValue value = ValueGenerator.value(target, constraints);
    if (value != null && value.maybeNull()) {
      exception = new SymbolicNullPointerException(target, "NullPointerException might be thrown as '" + NameGenerator.name(target) + "' is nullable here");
    }
    if (target.is(Tree.Kind.IDENTIFIER)) {
      constraints.setConstraints(((IdentifierTree) target).name(), new NonNullSymbolicValue());
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree element) {
    IdentifierTree variable = (IdentifierTree) element.variable();
    ExpressionTree expression = element.expression();
    constraints.setConstraints(variable.name(), ValueGenerator.value(expression, constraints));
  }

  @Override
  public void visitVariable(VariableTree element) {
    ExpressionTree initializer = element.initializer();
    if (initializer != null) {
      constraints.setConstraints(element.simpleName().name(), ValueGenerator.value(initializer, constraints));
    }
  }


  void setConditionConstraints(ExpressionTree syntaxNode, boolean condition) throws IncompatibleConstraintsException {
    constraints.setConditionConstraints(syntaxNode, condition);
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder("Processor\n");
    buffer.append(constraints);
    return buffer.toString();
  }

  public void report(AbstractSymbolicException exception) {
    executor.report(exception);
  }
}
