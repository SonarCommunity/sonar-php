/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = CollapsibleIfStatementCheck.KEY)
public class CollapsibleIfStatementCheck extends PHPVisitorCheck {

  public static final String KEY = "S1066";
  private static final String MESSAGE = "Merge this if statement with the enclosing one.";

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    super.visitIfStatement(tree);

    if (!hasElseOrElseIf(tree)) {
      StatementTree singleStatement = getSingleNestedStatement(tree);

      if (singleStatement != null && isIfStatementWithoutElse(singleStatement)) {
        context().newIssue(this, ((IfStatementTree) singleStatement).ifToken(), MESSAGE);
      }
    }
  }

  private static boolean isIfStatementWithoutElse(StatementTree statement) {
    return statement.is(Kind.IF_STATEMENT, Kind.ALTERNATIVE_IF_STATEMENT) && !hasElseOrElseIf((IfStatementTree) statement);
  }

  @Nullable
  private static StatementTree getSingleNestedStatement(IfStatementTree ifStatement) {
    if (ifStatement.statements().size() == 1) {
      StatementTree statement = ifStatement.statements().get(0);

      if (statement.is(Kind.BLOCK)) {
        BlockTree blockTree = (BlockTree) statement;

        if (blockTree.statements().size() == 1) {
          return blockTree.statements().get(0);
        }

      } else {
        return statement;
      }
    }

    return null;
  }

  private static boolean hasElseOrElseIf(IfStatementTree ifStatement) {
    return ifStatement.elseClause() != null || !ifStatement.elseifClauses().isEmpty();
  }

}
