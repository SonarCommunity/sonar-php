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
package org.sonar.plugins.php.api.tests;

import java.io.File;
import java.nio.file.Paths;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PhpTestFileTest {

  @Test
  public void load_file() throws Exception {
    File physicalFile = new File("src/test/resources/tests/testfile.php");
    PhpTestFile file = new PhpTestFile(physicalFile);
    assertThat(file.contents()).isEqualTo("<?php echo \"Hello\";\n");
    assertThat(file.filename()).isEqualTo("testfile.php");
    String expectedPath = Paths.get("src", "test", "resources", "tests", "testfile.php").toString();
    assertThat(file.toString()).isEqualTo(expectedPath);
    assertThat(file.uri()).isEqualTo(physicalFile.toURI());
  }

  @Test
  public void load_invalid_show_filename() {
    File file = new File("invalid.php");
    assertThatThrownBy(() -> new PhpTestFile(file))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("invalid.php");
  }

}
