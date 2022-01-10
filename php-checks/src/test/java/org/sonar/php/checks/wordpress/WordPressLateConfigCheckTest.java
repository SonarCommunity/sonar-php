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
package org.sonar.php.checks.wordpress;

import java.io.IOException;
import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.api.visitors.PHPCheck;

public class WordPressLateConfigCheckTest extends WordPressConfigCheckTest {

  PHPCheck check = new WordPressLateConfigCheck();

  @Test
  public void test_config_file_with_parenthesis() throws IOException {
    wordPressVerifier.verify(check, "wordpress/WordPressLateConfigCheck/with-parenthesis.php");
  }

  @Test
  public void test_config_file_without_parenthesis() throws IOException {
    wordPressVerifier.verify(check, "wordpress/WordPressLateConfigCheck/without-parenthesis.php");
  }

  @Test
  public void test_other_file() {
    CheckVerifier.verifyNoIssue(check, "wordpress/WordPressLateConfigCheck/no-wp-config.php");
  }

}
