/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.tree.impl.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;

import static org.assertj.core.api.Assertions.assertThat;

class ArrayAccessTreeTest extends PHPTreeModelTest {

  @Test
  void bracketOffset() {
    ArrayAccessTree tree = parse("$a[$offset]", PHPLexicalGrammar.EXPRESSION);

    assertThat(tree.is(Kind.ARRAY_ACCESS)).isTrue();

    assertThat(tree.object().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.object())).isEqualTo("$a");
    assertThat(tree.openBraceToken().text()).isEqualTo("[");
    assertThat(expressionToString(tree.offset())).isEqualTo("$offset");
    assertThat(tree.closeBraceToken().text()).isEqualTo("]");
  }

  @Test
  void curlyBraceOffset() {
    ArrayAccessTree tree = parse("$a{$offset}", PHPLexicalGrammar.EXPRESSION);

    assertThat(tree.is(Kind.ARRAY_ACCESS)).isTrue();

    assertThat(tree.object().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.object())).isEqualTo("$a");
    assertThat(tree.openBraceToken().text()).isEqualTo("{");
    assertThat(expressionToString(tree.offset())).isEqualTo("$offset");
    assertThat(tree.closeBraceToken().text()).isEqualTo("}");
  }

  @Test
  void fieldArrayAccess() {
    ArrayAccessTree tree = parse("$o->f[$offset]", PHPLexicalGrammar.EXPRESSION);

    assertThat(tree.is(Kind.ARRAY_ACCESS)).isTrue();

    assertThat(tree.object().is(Kind.OBJECT_MEMBER_ACCESS)).isTrue();
    assertThat(expressionToString(tree.object())).isEqualTo("$o->f");
    assertThat(tree.openBraceToken().text()).isEqualTo("[");
    assertThat(expressionToString(tree.offset())).isEqualTo("$offset");
    assertThat(tree.closeBraceToken().text()).isEqualTo("]");
  }

  @Test
  void fieldArrayAccessStatic() {
    ArrayAccessTree tree = parse("SomeClass::$f[$offset]", PHPLexicalGrammar.EXPRESSION);

    assertThat(tree.is(Kind.ARRAY_ACCESS)).isTrue();

    assertThat(tree.object().is(Kind.CLASS_MEMBER_ACCESS)).isTrue();
    assertThat(expressionToString(tree.object())).isEqualTo("SomeClass::$f");
    assertThat(expressionToString(tree.offset())).isEqualTo("$offset");
  }
}
