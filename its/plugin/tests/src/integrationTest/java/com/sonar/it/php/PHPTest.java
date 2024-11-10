/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import com.sonar.orchestrator.build.SonarScanner;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonarqube.ws.Issues;

import static org.assertj.core.api.Assertions.assertThat;

class PHPTest extends OrchestratorTest {

  private static final String MULTI_MODULE_PROJECT_KEY = "multimodule-php";
  private static final String EMPTY_FILE_PROJECT_KEY = "empty_file_project_key";
  private static final String PROJECT_WITH_MAIN_AND_TEST_KEY = "project-with-main-and-test";
  private static final String SEVERAL_EXTENSIONS_PROJECT_KEY = "project-with-several-extensions";
  private static final String PROJECT_WITH_VENDOR_KEY = "project-with-vendor";
  private static final String SRC_DIR_NAME = "src";

  /**
   * SONARPLUGINS-1657
   */
  @Test
  void shouldImportSourcesWithUserDefinedFileSuffixes() {
    provisionProject(SEVERAL_EXTENSIONS_PROJECT_KEY, "Project with several extensions", "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectDir(projectDirectoryFor("project-with-several-extensions"))
      .setProperty("sonar.php.file.suffixes", "php,php3,php4,myphp,html");
    executeBuildWithExpectedWarnings(ORCHESTRATOR, build);

    assertThat(getMeasureAsInt(SEVERAL_EXTENSIONS_PROJECT_KEY, "files")).isEqualTo(3);
    assertThat(getMeasureAsInt(getResourceKey(SEVERAL_EXTENSIONS_PROJECT_KEY, "Math2.myphp"), "lines")).isGreaterThan(1);
    assertThat(getComponent(SEVERAL_EXTENSIONS_PROJECT_KEY, getResourceKey(SEVERAL_EXTENSIONS_PROJECT_KEY, "Math3.pgp"))).isNull();
  }

  @Test
  void shouldExcludeVendorDir() {
    provisionProject(PROJECT_WITH_VENDOR_KEY, "Project with vendor dir", "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectDir(projectDirectoryFor("project-with-vendor"));
    executeBuildWithExpectedWarnings(ORCHESTRATOR, build);

    assertThat(getMeasureAsInt(PROJECT_WITH_VENDOR_KEY, "files")).isEqualTo(1);
  }

  /**
   * SONARPLUGINS-943
   */
  @Test
  void shouldSupportMultimoduleProjects() {
    provisionProject(MULTI_MODULE_PROJECT_KEY, "Multimodule PHP Project", "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectDir(projectDirectoryFor("multimodule"));
    executeBuildWithExpectedWarnings(ORCHESTRATOR, build);

    String componentKey1 = MULTI_MODULE_PROJECT_KEY + ":module1/src";
    String componentKey2 = MULTI_MODULE_PROJECT_KEY + ":module2/src";

    assertThat(getMeasureAsInt(componentKey1, "files")).isEqualTo(4);
    assertThat(getMeasureAsInt(componentKey2, "files")).isEqualTo(2);
    assertThat(getMeasureAsInt(MULTI_MODULE_PROJECT_KEY, "files")).isEqualTo(4 + 2);
  }

  /**
   * SONARPHP-667
   */
  @Test
  void shouldNotFailOnEmptyFile() {
    provisionProject(EMPTY_FILE_PROJECT_KEY, "Empty file test project", "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectKey(EMPTY_FILE_PROJECT_KEY)
      .setProjectName("Empty file test project")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(projectDirectoryFor("empty_file"));
    executeBuildWithExpectedWarnings(ORCHESTRATOR, build);

    assertThat(getMeasureAsInt(EMPTY_FILE_PROJECT_KEY, "files")).isEqualTo(3);
  }

  @Test
  void shouldNotFailOnDeeplyNestedTrees() {
    provisionProject("big_concat_key", "Big Concat", "php", "sleep-profile");
    SonarScanner build = createScanner()
      .setProjectKey("big_concat_key")
      .setProjectName("Big Concat")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(projectDirectoryFor("big_concat"));
    executeBuildWithExpectedWarnings(ORCHESTRATOR, build);

    List<Issues.Issue> issues = issuesForComponent("big_concat_key");
    // The file actually contains two calls to sleep(), but only one is visited due to the depth limit of the visitor.
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).getLine()).isEqualTo(105);
  }

  @Test
  void shouldHandleProjectWithOnlyTestFiles() {
    provisionProject(PROJECT_WITH_MAIN_AND_TEST_KEY, "project main and test files", "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectKey(PROJECT_WITH_MAIN_AND_TEST_KEY)
      .setProjectName("Test project")
      .setSourceEncoding("UTF-8")
      .setTestDirs("tests")
      .setSourceDirs("")
      .setProjectDir(projectDirectoryFor("project-with-main-and-test"));
    executeBuildWithExpectedWarnings(ORCHESTRATOR, build);

    List<Issues.Issue> issues = issuesForComponent(PROJECT_WITH_MAIN_AND_TEST_KEY);
    assertThat(issues).hasSize(1);
  }

  private static String getResourceKey(String projectKey, String fileName) {
    return projectKey + ":" + SRC_DIR_NAME + "/" + fileName;
  }

}
