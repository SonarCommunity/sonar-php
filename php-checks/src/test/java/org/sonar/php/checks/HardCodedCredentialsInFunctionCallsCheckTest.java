/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.checks;

import java.io.FileNotFoundException;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTester;
import org.sonar.plugins.php.CheckVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.php.checks.HardCodedCredentialsInFunctionCallsCheck.JsonSensitiveFunctionsReader;

public class HardCodedCredentialsInFunctionCallsCheckTest {

  @Rule
  public LogTester logTester = new LogTester().setLevel(Level.DEBUG);

  @Test
  public void test() throws Exception {
    CheckVerifier.verify(new HardCodedCredentialsInFunctionCallsCheck(), "HardCodedCredentialsInFunctionCallsCheck.php");
  }

  @Test
  public void parseResourceThrowsException() {
    assertThatThrownBy(() -> JsonSensitiveFunctionsReader.parseResource("no_valid_file_location" +
      ".json")).isInstanceOf(FileNotFoundException.class);
  }

  @Test
  public void toIntegerReturnsNull() {
    Integer integer = JsonSensitiveFunctionsReader.toInteger("string");
    assertThat(integer).isNull();
  }

  @Test
  public void shouldLogErrorOnInvalidFile() {
    JsonSensitiveFunctionsReader.parseSensitiveFunctions("invalidLocation", Set.of("invalid_fileName"));

    assertThat(logTester.setLevel(Level.ERROR).logs()).hasSize(1);
  }
}
