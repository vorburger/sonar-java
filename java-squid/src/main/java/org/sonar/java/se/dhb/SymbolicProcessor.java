package org.sonar.java.se.dhb;

import org.sonar.java.cfg.CFG.Block;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.SwitchStatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import java.util.HashSet;
import java.util.Set;

public class SymbolicProcessor {

  private final SymbolicExecutor executor;
  private final SymbolicProcessor parent;
  private final ConstraintsSet constraints;

  public SymbolicProcessor(SymbolicExecutor symbolicExecutor) {
    executor = symbolicExecutor;
    parent = null;
    constraints = new ConstraintsSet();
  }

  public SymbolicProcessor(SymbolicExecutor symbolicExecutor, SymbolicProcessor spawner) {
    executor = symbolicExecutor;
    parent = spawner;
    constraints = new ConstraintsSet(spawner.constraints);
  }

  private SymbolicValue getExpressionValue(ExpressionTree syntaxNode) {
    switch (syntaxNode.kind()) {
      case IDENTIFIER:
        return constraints.getValue(((IdentifierTree) syntaxNode).name());
      case NULL_LITERAL:
        return SymbolicValue.NULL;
      case STRING_LITERAL:
        return new StringConstant(((LiteralTree) syntaxNode).token().text());
      case BOOLEAN_LITERAL:
        return "true".equals(((LiteralTree) syntaxNode).token().text()) ? BooleanConstant.TRUE : BooleanConstant.FALSE;
      case CHAR_LITERAL:
        return new CharacterConstant(((LiteralTree) syntaxNode).value());
      case INT_LITERAL:
      case LONG_LITERAL:
      case FLOAT_LITERAL:
      case DOUBLE_LITERAL:
        return new NumberConstant(((LiteralTree) syntaxNode).value());
      case METHOD_INVOCATION:
        // FIXME If one can infer that the method returns a non-null value, then a non-null value should be assigned
      default:
        return new AnySymbolicValue();
    }
  }

  public void process(Block block) throws AbstractSymbolicException {
    try {
      if (executor.hasNotProcessed(block, constraints)) {
        for (Tree element : block.elements()) {
          process(element);
        }
        final Set<Block> successors = new HashSet<>(block.successors());
        final Tree terminator = block.terminator();
        if (terminator != null) {
          processTerminator(block, terminator, successors);
        }
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
    if (element.is(Tree.Kind.MEMBER_SELECT)) {
      processMemberSelect((MemberSelectExpressionTree) element);
    } else if (element.is(Tree.Kind.ASSIGNMENT)) {
      processAssignment((AssignmentExpressionTree) element);
    } else if (element.is(Tree.Kind.VARIABLE)) {
      processAssignment((VariableTree) element);
    }
  }

  private void processMemberSelect(MemberSelectExpressionTree element) throws SymbolicNullPointerException {
    ExpressionTree target = element.expression();
    SymbolicValue value = getExpressionValue(target);
    if (value != null && value.maybeNull()) {
      throw new SymbolicNullPointerException(target, "NullPointerException might be thrown as '" + getName(target) + "' is nullable here");
    }
    if (target.is(Tree.Kind.IDENTIFIER)) {
      constraints.setConstraints(((IdentifierTree) target).name(), new NonNullSymbolicValue());
    }
  }

  private void processAssignment(AssignmentExpressionTree element) {
    IdentifierTree variable = (IdentifierTree) element.variable();
    ExpressionTree expression = element.expression();
    constraints.setConstraints(variable.name(), getExpressionValue(expression));
  }

  private void processAssignment(VariableTree element) {
    ExpressionTree initializer = element.initializer();
    if (initializer != null) {
      constraints.setConstraints(element.simpleName().name(), getExpressionValue(initializer));
    }
  }

  private void processTerminator(Block block, Tree terminator, Set<Block> successors) throws AbstractSymbolicException {
    Block trueBlock = block.trueBlock();
    successors.remove(trueBlock);
    Block falseBlock = block.falseBlock();
    successors.remove(falseBlock);
    switch (terminator.kind()) {
      case IF_STATEMENT:
        IfStatementTree ifNode = (IfStatementTree) terminator;
        processCondition(ifNode, trueBlock, true);
        processCondition(ifNode, falseBlock, false);
        break;
      case CONDITIONAL_OR:
      case CONDITIONAL_AND:
        BinaryExpressionTree expression = (BinaryExpressionTree) terminator;
        processCondition(trueBlock, expression.leftOperand(), true);
        processCondition(falseBlock, expression.leftOperand(), false);
        break;
      case RETURN_STATEMENT:
        // We are done
        successors.clear();
        break;
      case CONDITIONAL_EXPRESSION:
      case FOR_STATEMENT:
      default:
        throw new IllegalStateException("Handling of " + terminator.kind() + " not yet implemented");
    }
  }

  private void processCondition(IfStatementTree syntaxNode, Block block, boolean condition) throws AbstractSymbolicException {
    if (block != null) {
      processCondition(block, syntaxNode.condition(), condition);
    }
  }

  private void processCondition(Block block, ExpressionTree conditionExpression, boolean state) throws AbstractSymbolicException {
    try {
      SymbolicProcessor processor = new SymbolicProcessor(executor, this);
      processor.setConditionConstraints(conditionExpression, state);
      processor.process(block);
    } catch (IncompatibleConstraintsException e) {
      executor.report(e);
    }
  }

  private void setConditionConstraints(ExpressionTree syntaxNode, boolean condition) throws IncompatibleConstraintsException {
    constraints.setConditionConstraints(syntaxNode, condition);
  }

  private static String getName(Tree syntaxNode) {
    if (syntaxNode.is(Tree.Kind.MEMBER_SELECT)) {
      return getName(((MemberSelectExpressionTree) syntaxNode).expression());
    } else if (syntaxNode.is(Tree.Kind.SWITCH_STATEMENT)) {
      return getName(((SwitchStatementTree) syntaxNode).expression());
    } else if (syntaxNode.is(Tree.Kind.IDENTIFIER)) {
      return ((IdentifierTree) syntaxNode).name();
    } else if (syntaxNode.is(Tree.Kind.METHOD_INVOCATION)) {
      return ((MethodInvocationTree) syntaxNode).symbol().name();
    } else {
      return syntaxNode.toString();
    }
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder("Processor\n");
    buffer.append(constraints);
    return buffer.toString();
  }
}
