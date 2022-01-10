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
package org.sonar.php.tree.symbols;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

class NamespaceNameResolvingVisitor extends PHPVisitorCheck {

  private final SymbolTableImpl symbolTable;
  private final Map<String, SymbolQualifiedName> aliases = new HashMap<>();
  private SymbolQualifiedName currentNamespace = SymbolQualifiedName.GLOBAL_NAMESPACE;

  public NamespaceNameResolvingVisitor(SymbolTableImpl symbolTable) {
    this.symbolTable = symbolTable;
  }

  public SymbolQualifiedName currentNamespace() {
    return currentNamespace;
  }

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    NamespaceNameTree namespaceNameTree = tree.namespaceName();
    currentNamespace = namespaceNameTree != null ? SymbolQualifiedName.create(namespaceNameTree) : SymbolQualifiedName.GLOBAL_NAMESPACE;
    super.visitNamespaceStatement(tree);

    if (isBracketedNamespace(tree)) {
      currentNamespace = SymbolQualifiedName.GLOBAL_NAMESPACE;
    }
  }

  public boolean isBracketedNamespace(NamespaceStatementTree tree) {
    return tree.openCurlyBrace() != null;
  }

  @Override
  public void visitUseStatement(UseStatementTree tree) {
    SymbolQualifiedName namespacePrefix = getPrefix(tree);

    tree.clauses().forEach(useClauseTree -> {
      String alias = getAliasName(useClauseTree);
      SymbolQualifiedName originalName = getOriginalFullyQualifiedName(namespacePrefix, useClauseTree);
      aliases.put(alias.toLowerCase(Locale.ROOT), originalName);
    });
  }

  @Nullable
  private static SymbolQualifiedName getPrefix(UseStatementTree useStatementTree) {
    NamespaceNameTree prefix = useStatementTree.prefix();
    return prefix == null ? null : SymbolQualifiedName.create(prefix);
  }

  private static SymbolQualifiedName getOriginalFullyQualifiedName(@Nullable SymbolQualifiedName namespacePrefix, UseClauseTree useClauseTree) {
    SymbolQualifiedName originalName = SymbolQualifiedName.create(useClauseTree.namespaceName());
    if (namespacePrefix != null) {
      return namespacePrefix.resolve(originalName);
    }
    return originalName;
  }

  private static String getAliasName(UseClauseTree useClauseTree) {
    NameIdentifierTree aliasNameTree = useClauseTree.alias();
    if (aliasNameTree != null) {
      return aliasNameTree.text();
    } else {
      return useClauseTree.namespaceName().name().text();
    }
  }

  /**
   * Resolution rules are defined in http://php.net/manual/en/language.namespaces.rules.php
   * <p>
   * If unqualified name (see above link for definition) is a class symbol we resolve it in the current namespace. If it's a function we check
   * if it's declared in current namespace, and if not we resolve as if it was in global namespace. This is imprecise
   * heuristics, because function could be declared in current namespace, but in another source file, thus we will
   * incorrectly consider such functions to be in the global namespace
   */
  public QualifiedName getFullyQualifiedName(NamespaceNameTree name, Symbol.Kind kind) {
    if (name.isFullyQualified()) {
      return SymbolQualifiedName.create(name);
    }
    SymbolQualifiedName alias = resolveAlias(name);
    if (alias != null) {
      return alias;
    }
    if (name.hasQualifiers()) {
      return currentNamespace.resolve(SymbolQualifiedName.create(name));
    }
    if (kind == Symbol.Kind.CLASS) {
      return currentNamespace.resolve(SymbolQualifiedName.create(name));
    }
    Symbol symbol = symbolTable.getSymbol(currentNamespace.resolve(SymbolQualifiedName.create(name)));
    if (symbol != null) {
      return symbol.qualifiedName();
    }
    return SymbolQualifiedName.create(name);
  }

  @CheckForNull
  private SymbolQualifiedName resolveAlias(NamespaceNameTree namespaceNameTree) {
    if (namespaceNameTree.isFullyQualified()) {
      return null;
    }
    if (namespaceNameTree.namespaces().isEmpty()) {
      return lookupAlias(namespaceNameTree.name());
    }
    // first namespace element is potentially an alias
    NameIdentifierTree potentialAlias = namespaceNameTree.namespaces().iterator().next();
    SymbolQualifiedName aliasedNamespace = lookupAlias(potentialAlias);
    if (aliasedNamespace != null) {
      return aliasedNamespace.resolveAliasedName(namespaceNameTree);
    }
    return null;
  }

  @CheckForNull
  private SymbolQualifiedName lookupAlias(IdentifierTree identifierTree) {
    String alias = identifierTree.text().toLowerCase(Locale.ROOT);
    return aliases.get(alias);
  }

}
