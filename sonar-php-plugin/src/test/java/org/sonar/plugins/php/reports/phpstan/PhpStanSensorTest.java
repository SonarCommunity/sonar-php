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
package org.sonar.plugins.php.reports.phpstan;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.rules.RuleType;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.plugins.php.reports.ExternalIssuesSensor;
import org.sonar.plugins.php.reports.ReportSensorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PhpStanSensorTest extends ReportSensorTest {

  private static final String PHPSTAN_PROPERTY = "sonar.php.phpstan.reportPaths";
  private static final Path PROJECT_DIR = Paths.get("src", "test", "resources", "reports", "phpstan");
  protected final PhpStanSensor phpStanSensor = new PhpStanSensor(analysisWarnings);

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @Test
  void testDescriptor() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    phpStanSensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.name()).isEqualTo("Import of PHPStan issues");
    assertThat(sensorDescriptor.languages()).containsOnly("php");
    assertThat(sensorDescriptor.configurationPredicate()).isNotNull();
    assertNoErrorWarnDebugLogs(logTester);

    Path baseDir = PROJECT_DIR.getParent();
    SensorContextTester context = SensorContextTester.create(baseDir);
    context.settings().setProperty(PHPSTAN_PROPERTY, "path/to/report");
    assertThat(sensorDescriptor.configurationPredicate().test(context.config())).isTrue();
  }

  @Test
  void raiseIssueWithUnixPath() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-report.json");
    assertThat(externalIssues).hasSize(3);

    ExternalIssue first = externalIssues.get(0);
    assertThat(first.impacts()).containsOnly(entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
    assertThat(first.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(first.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.MAJOR);
    IssueLocation firstPrimaryLoc = first.primaryLocation();
    assertThat(firstPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:phpstan/file1.php");
    assertThat(firstPrimaryLoc.message())
      .isEqualTo("Parameter #1 $i of function foo expects int, string given.");
    TextRange firstTextRange = firstPrimaryLoc.textRange();
    assertThat(firstTextRange).isNotNull();
    assertThat(firstTextRange.start().line()).isEqualTo(5);

    ExternalIssue second = externalIssues.get(1);
    assertThat(second.impacts()).containsOnly(entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
    assertThat(second.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(second.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.MAJOR);
    IssueLocation secondPrimaryLoc = second.primaryLocation();
    assertThat(secondPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:phpstan/file2.php");
    assertThat(secondPrimaryLoc.message())
      .isEqualTo("Parameter $date of method HelloWorld::sayHello() has invalid typehint type DateTimeImutable.");
    TextRange secondTextRange = secondPrimaryLoc.textRange();
    assertThat(secondTextRange).isNotNull();
    assertThat(secondTextRange.start().line()).isEqualTo(5);

    ExternalIssue third = externalIssues.get(2);
    assertThat(third.impacts()).containsOnly(entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
    assertThat(third.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(third.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.MAJOR);
    IssueLocation thirdPrimaryLoc = third.primaryLocation();
    assertThat(thirdPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:phpstan/file2.php");
    assertThat(thirdPrimaryLoc.message())
      .isEqualTo("Call to method format() on an unknown class DateTimeImutable.");
    TextRange thirdTextRange = thirdPrimaryLoc.textRange();
    assertThat(thirdTextRange).isNotNull();
    assertThat(thirdTextRange.start().line()).isEqualTo(7);

    assertNoErrorWarnDebugLogs(logTester);
  }

  @ParameterizedTest
  @ValueSource(strings = {"phpstan-report_win.json", "phpstan-report-abs.json", "phpstan-report-abs_win.json"})
  void raiseIssueWithPath(String path) throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting(path);
    assertThat(externalIssues).hasSize(3);

    assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  void issuesWhenPhpstanFileHasErrors() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-report-with-error.json");
    assertThat(externalIssues).hasSize(1);

    ExternalIssue first = externalIssues.get(0);
    assertThat(first.impacts()).containsOnly(entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
    assertThat(first.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(first.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.MAJOR);
    IssueLocation firstPrimaryLoc = first.primaryLocation();
    assertThat(firstPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:phpstan/file1.php");
    assertThat(firstPrimaryLoc.message())
      .isEqualTo("Parameter #1 $i of function foo expects int, string given.");
    TextRange firstTextRange = firstPrimaryLoc.textRange();
    assertThat(firstTextRange).isNull();

    assertThat(logTester.logs(Level.ERROR)).isEmpty();
    assertThat(onlyOneLogElement(logTester.logs(Level.WARN))).startsWith("Failed to resolve 22 file path(s) in PHPStan phpstan-report-with-error.json report.");
    assertThat(onlyOneLogElement(logTester.logs(Level.DEBUG)))
      .isEqualTo("Missing information for filePath:'', message:'Parameter $date of method HelloWorld::sayHello() has invalid typehint type DateTimeImutable.'");

    verify(analysisWarnings, times(1))
      .addWarning(startsWith("Failed to resolve 22 file path(s) in PHPStan phpstan-report-with-error.json report."));
  }

  @Test
  void excludedFilesWillNotBeLogged() throws IOException {
    executeSensorImporting("phpstan-report-with-error.json", Map.of("sonar.exclusion", "*/**/notExisting*.php"));

    assertThat(logTester.logs(Level.ERROR)).isEmpty();
    assertThat(logTester.logs(Level.WARN)).isEmpty();
    verify(analysisWarnings, never()).addWarning(anyString());
  }

  @Test
  void issuesWhenPhpstanWithLineAndMessageErrors() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-report-with-line-and-message-error.json");
    assertThat(externalIssues).isEmpty();

    assertThat(onlyOneLogElement(logTester.logs(Level.ERROR)))
      .contains("100 is not a valid line for pointer. File phpstan/file2.php has 10 line(s)");
    assertThat(logTester.logs(Level.WARN)).isEmpty();
    assertThat(logTester.logs(Level.DEBUG)).containsExactly(
      "Missing information for filePath:'phpstan/file2.php', message:'null'",
      "Missing information for filePath:'phpstan/file2.php', message:''");
  }

  @Test
  void noObjectAsRoot() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("no-object-as-root.php");
    assertThat(externalIssues).isEmpty();
    assertThat(onlyOneLogElement(logTester().logs(Level.ERROR)))
      .startsWith("An error occurred when reading report file '")
      .contains("no issue will be imported from this report.\nThe content of the file probably does not have the expected format.");
  }

  @Test
  void reportWithoutIssue() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-report-no-issue.json");
    assertThat(externalIssues).isEmpty();
    assertThat(logTester().logs(Level.ERROR)).isEmpty();
  }

  @Test
  void filePathIsCleanedWhenItContainsAdditionalContext() throws Exception {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-with-context-in-path.json");
    assertThat(externalIssues).hasSize(1);
    assertThat(externalIssues.get(0).primaryLocation().inputComponent().key()).isEqualTo("reports-project:phpstan/file3.php");
  }

  @Override
  protected Path projectDir() {
    return PROJECT_DIR;
  }

  @Override
  protected ExternalIssuesSensor sensor() {
    return phpStanSensor;
  }

  @Override
  protected LogTesterJUnit5 logTester() {
    return logTester;
  }

  private static String loggedFilePaths(boolean overflow, String... filePath) {
    List<String> pathList = Stream.of(filePath).limit(5).map(FilenameUtils::separatorsToSystem).collect(Collectors.toList());
    String log = String.join(";", pathList);
    if (overflow) {
      log += ";...";
    }
    return log;
  }
}
