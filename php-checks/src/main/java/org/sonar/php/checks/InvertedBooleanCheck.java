/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.HashMap;
import java.util.Map;

@Rule(key = "S1940")
public class InvertedBooleanCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Use the opposite operator '%s' instead and remove complement operator.";

  private static final Kind[] BINARY_EXPRESSION = {
    Kind.GREATER_THAN,
    Kind.GREATER_THAN_OR_EQUAL_TO,
    Kind.LESS_THAN,
    Kind.LESS_THAN_OR_EQUAL_TO,
    Kind.EQUAL_TO,
    Kind.STRICT_EQUAL_TO,
    Kind.NOT_EQUAL_TO,
    Kind.STRICT_NOT_EQUAL_TO};

  private final Map<String, String> operatorReplacements = new HashMap<>();

  public InvertedBooleanCheck() {
    operatorReplacements.put("<", ">=");
    operatorReplacements.put(">", "<=");
    operatorReplacements.put("==", "!=");
    operatorReplacements.put("===", "!==");
    operatorReplacements.put("<=", ">");
    operatorReplacements.put(">=", "<");
    operatorReplacements.put("!=", "==");
    operatorReplacements.put("!==", "===");
  }

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    if (tree.is(Kind.LOGICAL_COMPLEMENT)) {
      checkComplementParenthesisedExpression(tree, tree.expression());
    }

    super.visitPrefixExpression(tree);
  }

  private void checkComplementParenthesisedExpression(UnaryExpressionTree tree, ExpressionTree expression) {
    if (expression.is(BINARY_EXPRESSION)) {
      String operator = ((BinaryExpressionTree) expression).operator().text();
      context().newIssue(this, tree, String.format(MESSAGE, operatorReplacements.get(operator)));
    } else if (expression.is(Kind.PARENTHESISED_EXPRESSION)) {
      checkComplementParenthesisedExpression(tree, ((ParenthesisedExpressionTree) expression).expression());
    }
  }

}
