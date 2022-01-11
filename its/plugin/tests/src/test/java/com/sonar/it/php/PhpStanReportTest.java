/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2022 SonarSource SA
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
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;

import static org.assertj.core.api.Assertions.assertThat;

public class PhpStanReportTest {

  private static final String PROJECT = "phpstan_project";

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = Tests.ORCHESTRATOR;

  @Test
  public void import_report() {
    Tests.provisionProject(PROJECT, PROJECT, "php", "no_rules");
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor("phpstan_project"));
    Tests.executeBuildWithExpectedWarnings(ORCHESTRATOR, build);

    List<Issues.Issue> issues = Tests.issuesForComponent("phpstan_project");
    assertThat(issues).hasSize(2);
    Issues.Issue first = issues.get(0);
    assertThat(first.getComponent()).isEqualTo("phpstan_project:src/test.php");
    assertThat(first.getRule()).isEqualTo("external_phpstan:phpstan.finding");
    assertThat(first.getMessage()).isEqualTo("Message for issue without line.");
    assertThat(first.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
    assertThat(first.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    assertThat(first.getEffort()).isEqualTo("5min");
    assertThat(first.getLine()).isZero();

    Issues.Issue second = issues.get(1);
    assertThat(second.getComponent()).isEqualTo("phpstan_project:src/test.php");
    assertThat(second.getRule()).isEqualTo("external_phpstan:phpstan.finding");
    assertThat(second.getMessage()).isEqualTo("Parameter #1 $i of function foo expects int, string given.");
    assertThat(second.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
    assertThat(second.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    assertThat(second.getEffort()).isEqualTo("5min");
    assertThat(second.getLine()).isEqualTo(5);
  }
}
