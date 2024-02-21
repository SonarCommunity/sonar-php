/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks.utils.type;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.sonar.php.checks.utils.type.StaticFunctionCall.staticFunctionCall;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

class StaticFunctionCallTest {

  private static final String A_B_C_DEF = "A\\B\\C::def";

  @Test
  void illegalStaticFunctionCall() {
    assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> staticFunctionCall("A\\B\\C\\def"));
  }

  @Test
  void matches() {
    assertThat(staticFunctionCall(A_B_C_DEF).matches(qualifiedName("A\\B\\C"), "def")).isTrue();
    assertThat(staticFunctionCall(A_B_C_DEF).matches(qualifiedName("a\\b\\c"), "DEF")).isTrue();
    assertThat(staticFunctionCall(A_B_C_DEF).matches(qualifiedName("A\\B\\C"), "ghi")).isFalse();
    assertThat(staticFunctionCall(A_B_C_DEF).matches(qualifiedName("A\\B\\D"), "def")).isFalse();
  }

}
