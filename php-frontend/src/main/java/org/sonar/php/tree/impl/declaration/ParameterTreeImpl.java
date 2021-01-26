/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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

import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.UnionTypeTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ParameterTreeImpl extends PHPTree implements ParameterTree {

  private static final Kind KIND = Kind.PARAMETER;

  private final List<AttributeGroupTree> attributeGroups;
  private final SyntaxToken visibility;
  private final DeclaredTypeTree type;
  private final InternalSyntaxToken referenceToken;
  private final InternalSyntaxToken ellipsisToken;
  private final VariableIdentifierTree variableIdentifier;
  private final InternalSyntaxToken equalToken;
  private final ExpressionTree initValue;

  public ParameterTreeImpl(
    List<AttributeGroupTree> attributeGroups,
    @Nullable SyntaxToken visibility,
    @Nullable DeclaredTypeTree type,
    @Nullable InternalSyntaxToken referenceToken,
    @Nullable InternalSyntaxToken ellipsisToken,
    VariableIdentifierTree variableIdentifier,
    @Nullable InternalSyntaxToken equalToken,
    @Nullable ExpressionTree initValue
   ) {
    this.attributeGroups = attributeGroups;
    this.visibility = visibility;
    this.type = type;
    this.referenceToken = referenceToken;
    this.ellipsisToken = ellipsisToken;
    this.variableIdentifier = variableIdentifier;
    this.equalToken = equalToken;
    this.initValue = initValue;
  }

  @Override
  public List<AttributeGroupTree> attributeGroups() {
    return attributeGroups;
  }

  @Nullable
  @Override
  public SyntaxToken visibility() {
    return visibility;
  }

  /**
   * @deprecated since 3.11 - Use {@link #declaredType()} instead.
   */
  @Nullable
  @Override
  @Deprecated
  public TypeTree type() {
    if (type == null) {
      return null;
    }

    if (type.is(Kind.TYPE)) {
      return (TypeTree) type;
    } else {
      return ((UnionTypeTree) type).types().get(0);
    }
  }

  public DeclaredTypeTree declaredType() {
    return type;
  }

  @Nullable
  @Override
  public SyntaxToken referenceToken() {
    return referenceToken;
  }

  @Nullable
  @Override
  public SyntaxToken ellipsisToken() {
    return ellipsisToken;
  }

  @Override
  public VariableIdentifierTree variableIdentifier() {
    return variableIdentifier;
  }

  @Nullable
  @Override
  public SyntaxToken equalToken() {
    return equalToken;
  }

  @Nullable
  @Override
  public ExpressionTree initValue() {
    return initValue;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      attributeGroups.iterator(),
      Iterators.forArray(visibility, type, referenceToken, ellipsisToken, variableIdentifier, equalToken, initValue)
    );
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitParameter(this);
  }

}
