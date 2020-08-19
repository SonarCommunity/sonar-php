/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.symbols.MethodSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.php.symbols.Visibility;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = UnusedFunctionParametersCheck.KEY)
public class UnusedFunctionParametersCheck extends PHPVisitorCheck {

  public static final String KEY = "S1172";
  private static final String MESSAGE = "Remove the unused function parameter \"%s\".";

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    checkParameters(tree);
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    checkParameters(tree);
    super.visitFunctionExpression(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!isExcluded(tree)) {
      checkParameters(tree);
    }
    super.visitMethodDeclaration(tree);
  }

  private void checkParameters(FunctionTree tree) {
    Scope scope = context().symbolTable().getScopeFor(tree);
    if (scope != null && !scope.hasUnresolvedCompact()) {
      List<IdentifierTree> unused = new ArrayList<>();

      for (Symbol symbol : scope.getSymbols(Symbol.Kind.PARAMETER)) {
        if (symbol.usages().isEmpty()) {
          unused.add(symbol.declaration());
        }
      }

      for (IdentifierTree unusedParameter : unused) {
        context().newIssue(this, unusedParameter, String.format(MESSAGE, unusedParameter.text()));
      }
    }
  }

  /**
   * Exclude methods from the check that are a declaration in an interface
   * or that overwrite/implement a method and are not private.
   */
  public boolean isExcluded(MethodDeclarationTree tree) {
    MethodSymbol methodSymbol = Symbols.get(tree);
    boolean isPrivate = methodSymbol.visibility().equals(Visibility.PRIVATE);
    return !tree.body().is(Tree.Kind.BLOCK)
      || !(methodSymbol.isOverriding().isFalse() || isPrivate)
      || !(methodSymbol.isImplementing().isFalse() || isPrivate);
  }

}
