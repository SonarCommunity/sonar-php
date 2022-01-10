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
package org.sonar.php.symbols;

import org.sonar.php.tree.impl.declaration.ClassDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.php.tree.impl.expression.AnonymousClassTreeImpl;
import org.sonar.php.tree.impl.expression.FunctionCallTreeImpl;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

/**
 * Utility class to retrieve symbols from the AST.
 * We can drop this class as soon as we expose an equivalent API directly on the AST interfaces.
 */
public class Symbols {

  private Symbols() {
  }

  public static ClassSymbol get(ClassDeclarationTree classDeclarationTree) {
    return ((ClassDeclarationTreeImpl) classDeclarationTree).symbol();
  }

  public static FunctionSymbol get(FunctionCallTree functionCallTree) {
    return ((FunctionCallTreeImpl) functionCallTree).symbol();
  }

  public static ClassSymbol getClass(NamespaceNameTree namespaceNameTree) {
    if (namespaceNameTree instanceof ClassNamespaceNameTreeImpl) {
      return ((ClassNamespaceNameTreeImpl) namespaceNameTree).symbol();
    }
    return new UnknownClassSymbol(qualifiedName(namespaceNameTree.qualifiedName()));
  }

  public static ClassSymbol get(AnonymousClassTree anonymousClassTree) {
    return ((AnonymousClassTreeImpl) anonymousClassTree).symbol();
  }

  public static MethodSymbol get(MethodDeclarationTree methodDeclarationTree) {
    return ((MethodDeclarationTreeImpl) methodDeclarationTree).symbol();
  }
}
