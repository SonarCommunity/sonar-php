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
package org.sonar.samples.php;

import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PHPCustomRulesPluginTest {

  @Test
  void shouldTestContext() {
    PHPCustomRulesPlugin plugin = new PHPCustomRulesPlugin();
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(9, 9), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
    Plugin.Context context = new Plugin.Context(runtime);

    plugin.define(context);

    assertEquals(1, context.getExtensions().size());
  }
}
