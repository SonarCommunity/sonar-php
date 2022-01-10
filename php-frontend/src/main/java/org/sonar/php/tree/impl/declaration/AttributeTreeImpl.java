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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class AttributeTreeImpl extends PHPTree implements AttributeTree {
  private final NamespaceNameTree name;
  @Nullable
  private final SyntaxToken openParenthesisToken;
  private final SeparatedList<CallArgumentTree> arguments;
  @Nullable
  private final SyntaxToken closeParenthesisToken;

  public AttributeTreeImpl(NamespaceNameTree name,
                           @Nullable SyntaxToken openParenthesisToken,
                           SeparatedList<CallArgumentTree> arguments,
                           @Nullable SyntaxToken closeParenthesisToken) {
    this.name = name;
    this.openParenthesisToken = openParenthesisToken;
    this.arguments = arguments;
    this.closeParenthesisToken = closeParenthesisToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(name, openParenthesisToken),
      arguments.elementsAndSeparators(),
      IteratorUtils.iteratorOf(closeParenthesisToken)
    );
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitAttribute(this);
  }

  @Override
  public Kind getKind() {
    return Kind.ATTRIBUTE;
  }

  @Override
  public NamespaceNameTree name() {
    return name;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public SeparatedList<CallArgumentTree> arguments() {
    return arguments;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesisToken;
  }
}
