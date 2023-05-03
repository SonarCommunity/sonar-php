/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2023 SonarSource SA
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
package com.sonar.it.php;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.Issues;

import static org.assertj.core.api.Assertions.assertThat;

public class SuppressWarningsTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final String PROJECT_KEY = "suppress_warnings";

  @Test
  public void shouldSuppressIssueWithAnnotation() {
    Tests.provisionProject(PROJECT_KEY, "SuppressWarningsTest", "php", "nosonar-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor(PROJECT_KEY));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
    List<Issues.Issue> issues = Tests.issuesForComponent(PROJECT_KEY);
    assertThat(issues).isEmpty();
  }

}
