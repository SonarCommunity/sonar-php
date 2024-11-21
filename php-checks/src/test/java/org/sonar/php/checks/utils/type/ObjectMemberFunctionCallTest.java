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
package org.sonar.php.checks.utils.type;

import org.junit.jupiter.api.Test;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.checks.utils.type.TreeValuesTest.expression;

class ObjectMemberFunctionCallTest {

  @Test
  void test() {
    CompilationUnitTree unit = (CompilationUnitTree) TreeValuesTest.PARSER.parse("<?php " +
    /* expr0 */ "$x->m();" +
    /* expr1 */ "m();" +
    /* expr2 */ "NULL;" +
    /* expr3 */ "$x->$var();" +
    /* expr4 */ "AClass::m();");
    SymbolTable symbolTable = SymbolTableImpl.create(unit);
    TreeValues expr0 = TreeValues.of(expression(unit, 0), symbolTable);
    TreeValues expr1 = TreeValues.of(expression(unit, 1), symbolTable);
    TreeValues expr2 = TreeValues.of(expression(unit, 2), symbolTable);
    TreeValues expr3 = TreeValues.of(expression(unit, 3), symbolTable);
    TreeValues expr4 = TreeValues.of(expression(unit, 4), symbolTable);

    assertThat(new ObjectMemberFunctionCall("m", values -> true).test(expr0)).isTrue();
    assertThat(new ObjectMemberFunctionCall("m", values -> false).test(expr0)).isFalse();
    assertThat(new ObjectMemberFunctionCall("M", values -> true).test(expr0)).isTrue();
    assertThat(new ObjectMemberFunctionCall("y", values -> true).test(expr0)).isFalse();
    assertThat(new ObjectMemberFunctionCall("m", values -> true).test(expr1)).isFalse();
    assertThat(new ObjectMemberFunctionCall("m", values -> true).test(expr2)).isFalse();
    assertThat(new ObjectMemberFunctionCall("var", values -> true).test(expr3)).isFalse();
    assertThat(new ObjectMemberFunctionCall("m", values -> true).test(expr4)).isFalse();
  }

}
