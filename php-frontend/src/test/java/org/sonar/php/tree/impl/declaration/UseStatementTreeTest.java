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
package org.sonar.php.tree.impl.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class UseStatementTreeTest extends PHPTreeModelTest {

  @Test
  void singleDeclaration() {
    UseStatementTree tree = parse("use \\ns1\\ns2\\name;", PHPLexicalGrammar.USE_STATEMENT);
    assertThat(tree.is(Kind.USE_STATEMENT)).isTrue();
    assertThat(tree.useTypeToken()).isNull();
    assertThat(tree.prefix()).isNull();
    assertThat(tree.nsSeparatorToken()).isNull();
    assertThat(tree.openCurlyBraceToken()).isNull();
    assertThat(tree.closeCurlyBraceToken()).isNull();
    assertThat(tree.clauses()).hasSize(1);
  }

  @Test
  void multipleDeclarations() {
    UseStatementTree tree = parse("use \\ns1\\ns2\\name, \\ns1\\ns2\\name2;", PHPLexicalGrammar.USE_STATEMENT);
    assertThat(tree.is(Kind.USE_STATEMENT)).isTrue();
    assertThat(tree.useTypeToken()).isNull();
    assertThat(tree.clauses()).hasSize(2);
  }

  @Test
  void constToken() {
    UseStatementTree tree = parse("use const \\ns1\\ns2\\name;", PHPLexicalGrammar.USE_STATEMENT);

    assertThat(tree.is(Kind.USE_STATEMENT)).isTrue();
    assertThat(tree.useTypeToken().text()).isEqualTo("const");
  }

  @Test
  void functionToken() {
    UseStatementTree tree = parse("use function \\ns1\\ns2\\name;", PHPLexicalGrammar.USE_STATEMENT);

    assertThat(tree.is(Kind.USE_STATEMENT)).isTrue();
    assertThat(tree.useTypeToken().text()).isEqualTo("function");
  }

  @Test
  void groupUseStatement() {
    UseStatementTree tree = parse("use ns1\\ns2\\{name1 as a1, function name2};", Kind.GROUP_USE_STATEMENT);
    assertThat(tree.is(Kind.GROUP_USE_STATEMENT)).isTrue();
    assertThat(tree.prefix()).isNotNull();
    assertThat(expressionToString(tree.prefix())).isEqualTo("ns1\\ns2");
    assertThat(tree.nsSeparatorToken()).isNotNull();
    assertThat(tree.openCurlyBraceToken()).isNotNull();
    assertThat(tree.closeCurlyBraceToken()).isNotNull();
    assertThat(tree.clauses()).hasSize(2);
  }

  @Test
  void withTrailingComma() {
    UseStatementTree tree = parse("use ns1\\{A, B, C,};", Kind.GROUP_USE_STATEMENT);
    assertThat(tree.is(Kind.GROUP_USE_STATEMENT)).isTrue();
    assertThat(tree.prefix()).isNotNull();
    assertThat(tree.clauses()).hasSize(3);
  }
}
