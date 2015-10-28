package org.sonar.java.se.dhb;

import com.google.common.collect.ImmutableMap;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ConstraintsSet implements Cloneable {

  private final ConstraintsSet parent;
  private final Map<String, SymbolicValue> constraints;

  public ConstraintsSet() {
    this((ConstraintsSet) null);
  }

  public ConstraintsSet(ConstraintsSet parent) {
    this.parent = parent;
    constraints = new HashMap<>();
  }

  private ConstraintsSet(Map<String, SymbolicValue> collectConstraints) {
    parent = null;
    constraints = ImmutableMap.copyOf(collectConstraints);
  }

  public void setConstraints(String syntaxNode, SymbolicValue value) {
    if (value != null) {
      constraints.put(syntaxNode, value);
    }
  }

  public SymbolicValue getValue(String syntaxNode) {
    SymbolicValue value = constraints.get(syntaxNode);
    if (value == null && parent != null) {
      return parent.getValue(syntaxNode);
    }
    return value == null ? new AnySymbolicValue() : value;
  }

  public boolean implies(ConstraintsSet constraintsSet) {
    for (Entry<String, SymbolicValue> entry : constraints.entrySet()) {
      SymbolicValue value = constraintsSet.constraints.get(entry.getKey());
      if (value == null || !entry.getValue().implies(value)) {
        return false;
      }
    }
    return true;
  }

  private Map<String, SymbolicValue> collectConstraints() {
    HashMap<String, SymbolicValue> map = new HashMap<>();
    if (parent != null) {
      map.putAll(parent.collectConstraints());
    }
    map.putAll(constraints);
    return map;
  }

  @Override
  public ConstraintsSet clone() {
    return new ConstraintsSet(collectConstraints());
  }

  @Override
  public String toString() {
    Map<String, SymbolicValue> collectedConstraints = collectConstraints();
    StringBuilder buffer = new StringBuilder();
    for (Entry<String, SymbolicValue> entry : collectedConstraints.entrySet()) {
      if (buffer.length() > 0) {
        buffer.append('\n');
      }
      buffer.append(entry.getKey());
      buffer.append(" = ");
      buffer.append(entry.getValue());
    }
    return buffer.toString();
  }

  private SymbolicValue getExpressionValue(ExpressionTree syntaxNode) {
    switch (syntaxNode.kind()) {
      case IDENTIFIER:
        return constraints.get(((IdentifierTree) syntaxNode).name());
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
        return new NonNullSymbolicValue();
      default:
        return new AnySymbolicValue();
    }
  }

  void setConditionConstraints(ExpressionTree syntaxNode, boolean condition) throws IncompatibleConstraintsException {
    switch (syntaxNode.kind()) {
      case EQUAL_TO:
        setEqualConstraints((BinaryExpressionTree) syntaxNode, condition, true);
        break;
      case NOT_EQUAL_TO:
        setEqualConstraints((BinaryExpressionTree) syntaxNode, !condition, false);
        break;
      case CONDITIONAL_AND:
      case CONDITIONAL_OR:
        BinaryExpressionTree conditionExpression = (BinaryExpressionTree) syntaxNode;
        setConditionConstraints(conditionExpression.rightOperand(), condition);
        break;
      default:
        break;
    }
  }

  private void setEqualConstraints(BinaryExpressionTree syntaxNode, boolean condition, boolean inverted) throws IncompatibleConstraintsException {
    ExpressionTree leftOperand = syntaxNode.leftOperand();
    ExpressionTree rightOperand = syntaxNode.rightOperand();
    if (leftOperand.is(Tree.Kind.IDENTIFIER)) {
      setEqualConstraints(syntaxNode, ((IdentifierTree) leftOperand).name(), getExpressionValue(rightOperand), condition, inverted);
    } else if (rightOperand.is(Tree.Kind.IDENTIFIER)) {
      setEqualConstraints(syntaxNode, ((IdentifierTree) rightOperand).name(), getExpressionValue(leftOperand), condition, inverted);
    }
  }

  private void setEqualConstraints(BinaryExpressionTree syntaxNode, String name, SymbolicValue expressionValue, boolean condition, boolean inverted)
    throws IncompatibleConstraintsException {
    SymbolicValue actualValue = getValue(name);
    if (condition) {
      if (actualValue.implies(expressionValue)) {
        throw new IncompatibleConstraintsException(syntaxNode, "Change this condition so that it does not always evaluate to \"" + inverted + "\"");
      }
      setConstraints(name, expressionValue);
    } else {
      SymbolicValue constrainedValue = expressionValue.nonEqualValue();
      if (actualValue.implies(constrainedValue)) {
        throw new IncompatibleConstraintsException(syntaxNode, "Change this condition so that it does not always evaluate to \"" + !inverted + "\"");
      }
      setConstraints(name, constrainedValue);
    }
  }
}
