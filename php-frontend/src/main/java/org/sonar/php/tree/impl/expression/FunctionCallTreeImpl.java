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
package org.sonar.php.tree.impl.expression;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.symbols.UnknownFunctionSymbol;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class FunctionCallTreeImpl extends PHPTree implements FunctionCallTree {

  private static final Kind KIND = Kind.FUNCTION_CALL;
  private static final QualifiedName UNKNOWN_FUNCTION_NAME = QualifiedName.qualifiedName("<unknown_function>");
  private ExpressionTree callee;
  private final InternalSyntaxToken openParenthesisToken;
  private final SeparatedListImpl<ExpressionTree> arguments;
  private final InternalSyntaxToken closeParenthesisToken;
  private FunctionSymbol symbol = new UnknownFunctionSymbol(UNKNOWN_FUNCTION_NAME);

  public FunctionCallTreeImpl(ExpressionTree callee, InternalSyntaxToken openParenthesisToken, SeparatedListImpl<ExpressionTree> arguments, InternalSyntaxToken closeParenthesisToken) {
    this.callee = callee;
    this.openParenthesisToken = openParenthesisToken;
    this.arguments = arguments;
    this.closeParenthesisToken = closeParenthesisToken;
  }

  public FunctionCallTreeImpl(ExpressionTree callee, SeparatedListImpl<ExpressionTree> arguments) {
    this.callee = callee;
    this.openParenthesisToken = null;
    this.arguments = arguments;
    this.closeParenthesisToken = null;
  }

  public FunctionCallTreeImpl(InternalSyntaxToken openParenthesisToken, SeparatedListImpl<ExpressionTree> arguments, InternalSyntaxToken closeParenthesisToken) {
    this.openParenthesisToken = openParenthesisToken;
    this.arguments = arguments;
    this.closeParenthesisToken = closeParenthesisToken;
  }

  public FunctionCallTreeImpl complete(ExpressionTree callee) {
    this.callee = callee;

    return this;
  }

  @Override
  public ExpressionTree callee() {
    return callee;
  }

  @Nullable
  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public SeparatedListImpl<ExpressionTree> arguments() {
    return arguments;
  }

  @Nullable
  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesisToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.singletonIterator(callee),
      Iterators.singletonIterator(openParenthesisToken),
      arguments.elementsAndSeparators(),
      Iterators.singletonIterator(closeParenthesisToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitFunctionCall(this);
  }

  public FunctionSymbol symbol() {
    return symbol;
  }

  public void setSymbol(FunctionSymbol symbol) {
    this.symbol = symbol;
  }
}
