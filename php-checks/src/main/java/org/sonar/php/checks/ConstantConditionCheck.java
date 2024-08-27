/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks;

import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.symbols.SymbolImpl;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ConstantConditionCheck.KEY)
public class ConstantConditionCheck extends PHPVisitorCheck {

  public static final String KEY = "S5797";
  private static final String MESSAGE = "Replace this expression; used as a condition it will always be constant.";
  private static final String SECONDARY_MESSAGE = "Last assignment.";
  private static final Tree.Kind[] CONDITIONAL_KINDS = {
    Tree.Kind.CONDITIONAL_AND,
    Tree.Kind.CONDITIONAL_OR,
    Tree.Kind.ALTERNATIVE_CONDITIONAL_AND,
    Tree.Kind.ALTERNATIVE_CONDITIONAL_OR,
    Tree.Kind.ALTERNATIVE_CONDITIONAL_XOR,
  };
  private static final Tree.Kind[] BOOLEAN_CONSTANT_KINDS = {
    Tree.Kind.BOOLEAN_LITERAL,
    Tree.Kind.NUMERIC_LITERAL,
    Tree.Kind.REGULAR_STRING_LITERAL,
    Tree.Kind.NULL_LITERAL,
    Tree.Kind.HEREDOC_LITERAL,
    Tree.Kind.NOWDOC_LITERAL,
    Tree.Kind.MAGIC_CONSTANT,
    Tree.Kind.ARRAY_INITIALIZER_FUNCTION,
    Tree.Kind.ARRAY_INITIALIZER_BRACKET,
    Tree.Kind.NEW_EXPRESSION,
    Tree.Kind.FUNCTION_EXPRESSION,
  };

  public static <T> Optional<T> getLastValue(List<T> list) {
    if (list.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(list.get(list.size() - 1));
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    if (tree.is(CONDITIONAL_KINDS)) {
      checkConstant(tree.leftOperand());
      checkConstant(tree.rightOperand());
    }
    super.visitBinaryExpression(tree);
  }

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    if (tree.is(Tree.Kind.LOGICAL_COMPLEMENT)) {
      checkConstant(tree.expression());
    }
    super.visitPrefixExpression(tree);
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    ExpressionTree conditionExpression = tree.condition().expression();
    checkConstant(conditionExpression);
    super.visitIfStatement(tree);
  }

  @Override
  public void visitElseifClause(ElseifClauseTree tree) {
    ExpressionTree conditionExpression = tree.condition().expression();
    checkConstant(conditionExpression);
    super.visitElseifClause(tree);
  }

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
    ExpressionTree conditionExpression = tree.condition();
    checkConstant(conditionExpression);
    super.visitConditionalExpression(tree);
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    ExpressionTree conditionExpression = tree.expression().expression();
    checkConstant(conditionExpression);
    super.visitSwitchStatement(tree);
  }

  private void checkConstant(ExpressionTree conditionExpression) {
    if (conditionExpression.is(BOOLEAN_CONSTANT_KINDS)) {
      newIssue(conditionExpression, MESSAGE);
      return;
    }
    if (conditionExpression.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      VariableIdentifierTreeImpl variableIdentifier = (VariableIdentifierTreeImpl) conditionExpression;
      SymbolImpl variableSymbol = variableIdentifier.symbol();
      Optional<ExpressionTree> variableLastValue = getLastValue(variableSymbol.assignedValues());
      if (variableLastValue.isPresent() && variableLastValue.get().is(BOOLEAN_CONSTANT_KINDS)) {
        newIssue(conditionExpression, MESSAGE).secondary(variableLastValue.get(), SECONDARY_MESSAGE);
      }
    }
  }
}
