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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ReferenceVariableTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S4824")
public class UnsetForeachReferenceVariableCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Make sure that the referenced value variable is unset after the loop.";

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    super.visitForEachStatement(tree);

    if (!tree.value().is(Tree.Kind.REFERENCE_VARIABLE)) {
      return;
    }

    ReferenceVariableTree referenceVariable = (ReferenceVariableTree) tree.value();

    Symbol symbol = context().symbolTable().getSymbol(referenceVariable.variableExpression());
    if (symbol == null) {
      return;
    }

    boolean usedBeforeUnset = false;
    boolean wasUnset = false;
    for (SyntaxToken usage : symbol.usages()) {
      boolean usageIsOutsideForEach = usageIsOutsideForEach(usage, tree);
      if (usageIsOutsideForEach && !usageIsInUnset(usage)) {
        usedBeforeUnset = true;
      } else if(usageIsOutsideForEach) {
        // unset() found
        wasUnset = true;
        break;
      }
    }

    if (!wasUnset && usedBeforeUnset) {
      context().newIssue(this, referenceVariable, MESSAGE);
    }
  }

  private static boolean usageIsInUnset(SyntaxToken usage) {
    return usage.getParent().getParent().is(Tree.Kind.UNSET_VARIABLE_STATEMENT);
  }

  private static boolean usageIsOutsideForEach(SyntaxToken usage, ForEachStatementTree tree) {
    SyntaxToken forEachLastToken = ((PHPTree) tree).getLastToken();
    return usage.line() > forEachLastToken.line() ||
      (usage.line() == forEachLastToken.line() && usage.column() > forEachLastToken.endColumn());
  }
}
