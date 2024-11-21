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
package org.sonar.php.parser.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class ParameterTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.PARAMETER)
      .matches("callable $a")
      .matches("array $a")
      .matches("int $a")
      .matches("object $a")
      .matches("Foo $a")
      .matches("?int $a")
      .matches("&$a")
      .matches("...$a")
      .matches("$a = \"foo\"")
      .matches("int|array|Foo $a")
      .matches("int $a { get; set => 123; }")
      .matches("int $a { final set($value) => $value - 1; }")
      .matches("int $a { get { return $this->a+1; } }");
  }
}
