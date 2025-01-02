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
package org.sonar.php.tree.impl.statement;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ExpressionListStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ExpressionListStatementTreeImpl extends PHPTree implements ExpressionListStatementTree {

  private static final Kind KIND = Kind.EXPRESSION_LIST_STATEMENT;

  private final SeparatedList<ExpressionTree> expressions;
  private final InternalSyntaxToken eosToken;

  public ExpressionListStatementTreeImpl(SeparatedList<ExpressionTree> expressions, InternalSyntaxToken eosToken) {
    this.expressions = expressions;
    this.eosToken = eosToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(expressions.elementsAndSeparators(), IteratorUtils.iteratorOf(eosToken));
  }

  @Override
  public SeparatedList<ExpressionTree> expressions() {
    return expressions;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitExpressionListStatement(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }
}
