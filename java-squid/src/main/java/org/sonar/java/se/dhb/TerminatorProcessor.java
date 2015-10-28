package org.sonar.java.se.dhb;

import org.sonar.java.cfg.CFG.Block;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.ConditionalExpressionTree;
import org.sonar.plugins.java.api.tree.DoWhileStatementTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.ForEachStatement;
import org.sonar.plugins.java.api.tree.ForStatementTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.ReturnStatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.WhileStatementTree;

import java.util.HashSet;
import java.util.Set;

public class TerminatorProcessor extends AbstractTreeVisitor {

  private final Tree terminator;
  private final SymbolicProcessor parentProcessor;
  private final Set<Block> successors;
  private final Block trueBlock;
  private final Block falseBlock;
  private AbstractSymbolicException exception = null;

  public TerminatorProcessor(SymbolicProcessor processor, Block block) {
    parentProcessor = processor;
    terminator = block.terminator();
    successors = new HashSet<>(block.successors());
    trueBlock = block.trueBlock();
    falseBlock = block.falseBlock();
  }

  public Set<Block> successors() {
    return successors;
  }

  public void process() throws AbstractSymbolicException {
    if (terminator != null) {
      terminator.accept(this);
      if (exception != null) {
        throw exception;
      }
    }
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    IfStatementTree ifNode = (IfStatementTree) terminator;
    processCondition(ifNode, trueBlock, true);
    if (exception == null) {
      processCondition(ifNode, falseBlock, false);
    }
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    BinaryExpressionTree expression = (BinaryExpressionTree) terminator;
    try {
      processCondition(trueBlock, expression.leftOperand(), true);
      processCondition(falseBlock, expression.leftOperand(), false);
    } catch (AbstractSymbolicException e) {
      exception = e;
    }
  }

  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
    // We are done
    successors.clear();
  }

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
    throw new IllegalStateException("Handling of " + terminator.kind() + " not yet implemented");
  }

  @Override
  public void visitWhileStatement(WhileStatementTree tree) {
    throw new IllegalStateException("Handling of " + terminator.kind() + " not yet implemented");
  }

  @Override
  public void visitDoWhileStatement(DoWhileStatementTree tree) {
    throw new IllegalStateException("Handling of " + terminator.kind() + " not yet implemented");
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    throw new IllegalStateException("Handling of " + terminator.kind() + " not yet implemented");
  }

  @Override
  public void visitForEachStatement(ForEachStatement tree) {
    throw new IllegalStateException("Handling of " + terminator.kind() + " not yet implemented");
  }

  private void processCondition(IfStatementTree syntaxNode, Block block, boolean condition) {
    if (block != null) {
      try {
        processCondition(block, syntaxNode.condition(), condition);
      } catch (AbstractSymbolicException e) {
        exception = e;
      }
    }
  }

  private void processCondition(Block block, ExpressionTree conditionExpression, boolean state) throws AbstractSymbolicException {
    successors.remove(block);
    try {
      SymbolicProcessor processor = parentProcessor.next();
      processor.setConditionConstraints(conditionExpression, state);
      processor.process(block);
    } catch (IncompatibleConstraintsException e) {
      parentProcessor.report(e);
    }
  }
}
