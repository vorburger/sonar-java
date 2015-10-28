package org.sonar.java.se.dhb;

import org.sonar.java.model.expression.TypeArgumentListTreeImpl;
import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.ArrayAccessExpressionTree;
import org.sonar.plugins.java.api.tree.ArrayDimensionTree;
import org.sonar.plugins.java.api.tree.ArrayTypeTree;
import org.sonar.plugins.java.api.tree.AssertStatementTree;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.BreakStatementTree;
import org.sonar.plugins.java.api.tree.CaseGroupTree;
import org.sonar.plugins.java.api.tree.CaseLabelTree;
import org.sonar.plugins.java.api.tree.CatchTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ConditionalExpressionTree;
import org.sonar.plugins.java.api.tree.ContinueStatementTree;
import org.sonar.plugins.java.api.tree.DoWhileStatementTree;
import org.sonar.plugins.java.api.tree.EmptyStatementTree;
import org.sonar.plugins.java.api.tree.EnumConstantTree;
import org.sonar.plugins.java.api.tree.ExpressionStatementTree;
import org.sonar.plugins.java.api.tree.ForEachStatement;
import org.sonar.plugins.java.api.tree.ForStatementTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.InstanceOfTree;
import org.sonar.plugins.java.api.tree.LabeledStatementTree;
import org.sonar.plugins.java.api.tree.LambdaExpressionTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodReferenceTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.ModifiersTree;
import org.sonar.plugins.java.api.tree.NewArrayTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.PackageDeclarationTree;
import org.sonar.plugins.java.api.tree.ParameterizedTypeTree;
import org.sonar.plugins.java.api.tree.ParenthesizedTree;
import org.sonar.plugins.java.api.tree.PrimitiveTypeTree;
import org.sonar.plugins.java.api.tree.ReturnStatementTree;
import org.sonar.plugins.java.api.tree.SwitchStatementTree;
import org.sonar.plugins.java.api.tree.SynchronizedStatementTree;
import org.sonar.plugins.java.api.tree.ThrowStatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TreeVisitor;
import org.sonar.plugins.java.api.tree.TryStatementTree;
import org.sonar.plugins.java.api.tree.TypeCastTree;
import org.sonar.plugins.java.api.tree.TypeParameterTree;
import org.sonar.plugins.java.api.tree.TypeParameters;
import org.sonar.plugins.java.api.tree.UnaryExpressionTree;
import org.sonar.plugins.java.api.tree.UnionTypeTree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.plugins.java.api.tree.WhileStatementTree;
import org.sonar.plugins.java.api.tree.WildcardTree;

public abstract class AbstractTreeVisitor implements TreeVisitor {

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
  }

  @Override
  public void visitImport(ImportTree tree) {
  }

  @Override
  public void visitClass(ClassTree tree) {
  }

  @Override
  public void visitMethod(MethodTree tree) {
  }

  @Override
  public void visitBlock(BlockTree tree) {
  }

  @Override
  public void visitEmptyStatement(EmptyStatementTree tree) {
  }

  @Override
  public void visitLabeledStatement(LabeledStatementTree tree) {
  }

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
  }

  @Override
  public void visitAssertStatement(AssertStatementTree tree) {
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
  }

  @Override
  public void visitCaseGroup(CaseGroupTree tree) {
  }

  @Override
  public void visitCaseLabel(CaseLabelTree tree) {
  }

  @Override
  public void visitWhileStatement(WhileStatementTree tree) {
  }

  @Override
  public void visitDoWhileStatement(DoWhileStatementTree tree) {
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
  }

  @Override
  public void visitForEachStatement(ForEachStatement tree) {
  }

  @Override
  public void visitBreakStatement(BreakStatementTree tree) {
  }

  @Override
  public void visitContinueStatement(ContinueStatementTree tree) {
  }

  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
  }

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
  }

  @Override
  public void visitSynchronizedStatement(SynchronizedStatementTree tree) {
  }

  @Override
  public void visitTryStatement(TryStatementTree tree) {
  }

  @Override
  public void visitCatch(CatchTree tree) {
  }

  @Override
  public void visitUnaryExpression(UnaryExpressionTree tree) {
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
  }

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
  }

  @Override
  public void visitArrayAccessExpression(ArrayAccessExpressionTree tree) {
  }

  @Override
  public void visitMemberSelectExpression(MemberSelectExpressionTree tree) {
  }

  @Override
  public void visitNewClass(NewClassTree tree) {
  }

  @Override
  public void visitNewArray(NewArrayTree tree) {
  }

  @Override
  public void visitMethodInvocation(MethodInvocationTree tree) {
  }

  @Override
  public void visitTypeCast(TypeCastTree tree) {
  }

  @Override
  public void visitInstanceOf(InstanceOfTree tree) {
  }

  @Override
  public void visitParenthesized(ParenthesizedTree tree) {
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
  }

  @Override
  public void visitIdentifier(IdentifierTree tree) {
  }

  @Override
  public void visitVariable(VariableTree tree) {
  }

  @Override
  public void visitEnumConstant(EnumConstantTree tree) {
  }

  @Override
  public void visitPrimitiveType(PrimitiveTypeTree tree) {
  }

  @Override
  public void visitArrayType(ArrayTypeTree tree) {
  }

  @Override
  public void visitParameterizedType(ParameterizedTypeTree tree) {
  }

  @Override
  public void visitWildcard(WildcardTree tree) {
  }

  @Override
  public void visitUnionType(UnionTypeTree tree) {
  }

  @Override
  public void visitModifier(ModifiersTree modifiersTree) {
  }

  @Override
  public void visitAnnotation(AnnotationTree annotationTree) {
  }

  @Override
  public void visitLambdaExpression(LambdaExpressionTree lambdaExpressionTree) {
  }

  @Override
  public void visitTypeParameter(TypeParameterTree typeParameter) {
  }

  @Override
  public void visitTypeArguments(TypeArgumentListTreeImpl trees) {
  }

  @Override
  public void visitTypeParameters(TypeParameters trees) {
  }

  @Override
  public void visitOther(Tree tree) {
  }

  @Override
  public void visitMethodReference(MethodReferenceTree methodReferenceTree) {
  }

  @Override
  public void visitPackage(PackageDeclarationTree tree) {
  }

  @Override
  public void visitArrayDimension(ArrayDimensionTree tree) {
  }

}
