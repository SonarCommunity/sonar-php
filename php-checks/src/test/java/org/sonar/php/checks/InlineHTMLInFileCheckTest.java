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
package org.sonar.php.checks;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.FileIssue;

class InlineHTMLInFileCheckTest {

  private InlineHTMLInFileCheck check = new InlineHTMLInFileCheck();
  private static final String TEST_DIR = "InlineHTMLInFileCheck/";

  @Test
  void ok() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok.php");
  }

  @Test
  void okAsp() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok_asp.php");
  }

  @Test
  void okExcludedFile() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok.phtml");
  }

  @Test
  void ko() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko.php"), Collections.singletonList(new FileIssue(check, "Remove the inline HTML in this file.")));
  }
}
