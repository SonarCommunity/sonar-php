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
package org.sonar.php.checks;

import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;

public class InheritanceDepthCheckTest {

  @Test
  public void defaults() {
    CheckVerifier.verify(new InheritanceDepthCheck(), "InheritanceDepthCheck/defaults.php");
  }

  @Test
  public void reduce_max_inheritance_depth() {
    InheritanceDepthCheck check = new InheritanceDepthCheck();
    check.max = 3;
    CheckVerifier.verify(check, "InheritanceDepthCheck/reduce_max_inheritance_depth.php");
  }

  @Test
  public void filtered_classes() {
    InheritanceDepthCheck check = new InheritanceDepthCheck();
    check.filteredClasses = "filteredClass1, filteredClass2";
    CheckVerifier.verifyNoIssue(check, "InheritanceDepthCheck/filtered_classes.php");
  }
}
