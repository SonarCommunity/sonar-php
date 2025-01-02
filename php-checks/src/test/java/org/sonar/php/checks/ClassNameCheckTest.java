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
package org.sonar.php.checks;

import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class ClassNameCheckTest {

  private ClassNameCheck check = new ClassNameCheck();
  private String fileName = "ClassNameCheck.php";

  @Test
  void defaultValue() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(fileName));
  }

  @Test
  void custom() throws Exception {
    check.format = "^[a-z][a-zA-Z0-9]*$";
    List<PhpIssue> expectedIssues = new LinkedList<>();
    expectedIssues.add(new LineIssue(check, 7, "Rename class \"MyClass\" to match the regular expression " + check.format + "."));
    PHPCheckTest.check(check, TestUtils.getCheckFile(fileName), expectedIssues);
  }
}
