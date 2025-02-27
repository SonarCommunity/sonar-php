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
package org.sonar.php.checks.utils.argumentmatching;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArgumentMatcherValueContainmentTest {

  @Test
  void argumentIndicatorWithString() {
    ArgumentMatcherValueContainment argumentMatcher = ArgumentMatcherValueContainment.builder()
      .values("VALUE")
      .position(1)
      .name(null)
      .build();

    assertThat(argumentMatcher.getValues()).isEqualTo(Set.of("value"));
    assertThat(argumentMatcher.getPosition()).isEqualTo(1);
    assertThat(argumentMatcher.getName()).isNull();
  }

  @Test
  void argumentIndicatorWithSet() {
    ArgumentMatcherValueContainment argumentMatcher = ArgumentMatcherValueContainment.builder()
      .values(Set.of(
        "VALUE"))
      .position(1)
      .name("argumentName")
      .build();

    assertThat(argumentMatcher.getValues()).isEqualTo(Set.of("value"));
    assertThat(argumentMatcher.getName()).isEqualTo("argumentName");
  }
}
