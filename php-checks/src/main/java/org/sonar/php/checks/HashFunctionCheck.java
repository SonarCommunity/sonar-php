/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.SUPERGLOBALS;

@Rule(key = "S2053")
public class HashFunctionCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Use an unpredictable salt value.";
  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.getFunctionName(tree);
    if ("hash_pbkdf2".equals(functionName) && tree.arguments().size() >= 3) {
      ExpressionTree saltArgument = tree.arguments().get(2);
      if (isPredictable(saltArgument)) {
        context().newIssue(this, saltArgument, MESSAGE);
      }
    }
    super.visitFunctionCall(tree);
  }

  private boolean isPredictable(ExpressionTree tree) {
    if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      return true;
    }
    if (tree.is(Tree.Kind.ARRAY_ACCESS)) {
      ExpressionTree array = ((ArrayAccessTree) tree).object();
      return array.is(Tree.Kind.VARIABLE_IDENTIFIER)
        && SUPERGLOBALS.contains(((VariableIdentifierTree) array).text());
    }
    if (tree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      Symbol symbol = context().symbolTable().getSymbol(tree);
      Optional<ExpressionTree> uniqueAssignedValue = assignmentExpressionVisitor.getUniqueAssignedValue(symbol);
      if (uniqueAssignedValue.isPresent()) {
        return isPredictable(uniqueAssignedValue.get());
      }
    }
    return false;
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    this.assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }
}
