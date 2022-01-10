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
package org.sonar.php.tree.impl.expression;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class NameIdentifierTreeImpl extends PHPTree implements NameIdentifierTree {

  private final InternalSyntaxToken nameToken;
  private static final Kind KIND = Kind.NAME_IDENTIFIER;

  public NameIdentifierTreeImpl(InternalSyntaxToken nameToken) {
    this.nameToken = Preconditions.checkNotNull(nameToken);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }


  @Override
  public SyntaxToken token() {
    return nameToken;
  }

  @Override
  public String text() {
    return token().text();
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(nameToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitNameIdentifier(this);
  }
}
