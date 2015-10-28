package org.sonar.java.se.dhb;

import org.sonar.java.cfg.CFG;
import org.sonar.java.cfg.CFG.Block;
import org.sonar.plugins.java.api.JavaCheck;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.MethodTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolicExecutor extends BaseTreeVisitor {

  private static final int DEFAULT_MAXIMUM_COMPLEXITY = 2000;

  private static JavaCheck JAVA_CHECK = new JavaCheck() {
  };

  private final JavaFileScannerContext context;
  private final int maximumComplexity;
  private final Map<Block, List<ConstraintsSet>> processedBlocks = new HashMap<>();
  private int complexity = 0;

  public SymbolicExecutor(JavaFileScannerContext context) {
    this(context, DEFAULT_MAXIMUM_COMPLEXITY);
  }

  public SymbolicExecutor(JavaFileScannerContext context, int maximumComplexity) {
    this.context = context;
    this.maximumComplexity = maximumComplexity;
  }

  @Override
  public void visitMethod(MethodTree tree) {
    super.visitMethod(tree);
    BlockTree body = tree.block();
    if (body != null) {
      CFG cfg = CFG.build(tree);
      execute(cfg);
    }
  }

  public void execute(CFG cfg) {
    try {
      SymbolicProcessor processor = new SymbolicProcessor(this);
      processor.process(cfg.entry());
    } catch (AbstractSymbolicException e) {
      report(e);
    }
  }

  public boolean hasNotProcessed(Block block, ConstraintsSet constraints) throws AbstractSymbolicException {
    List<ConstraintsSet> processedconstraints = processedBlocks.get(block);
    if (processedconstraints == null) {
      processedconstraints = new ArrayList<>();
      processedBlocks.put(block, processedconstraints);
      processedconstraints.add(constraints);
      complexity += 1;
      if (complexity > maximumComplexity) {
        throw new ComplexityExceededException(block.elements().get(0), "Algorithmic complexity limit reached (" + maximumComplexity + ")");
      }
      return true;
    }
    for (ConstraintsSet constraintsSet : processedconstraints) {
      if (!constraintsSet.implies(constraints)) {
        processedconstraints.add(constraints);
        return true;
      }
    }
    return false;
  }

  public void report(AbstractSymbolicException exception) {
    exception.report(context, JAVA_CHECK);
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    List<Block> blocks = new ArrayList<>(processedBlocks.keySet());
    Collections.sort(blocks, new Comparator<Block>() {
      @Override
      public int compare(Block o1, Block o2) {
        return Double.compare(o2.id(), o1.id());
      }
    });
    for (Block block : blocks) {
      if (buffer.length() > 0) {
        buffer.append("\n\n");
      }
      buffer.append('B');
      buffer.append(block.id());
      buffer.append(" processed with ");
      buffer.append(processedBlocks.get(block));
    }
    return buffer.toString();
  }
}
