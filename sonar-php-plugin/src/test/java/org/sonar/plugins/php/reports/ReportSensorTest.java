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
package org.sonar.plugins.php.reports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public abstract class ReportSensorTest {
  protected final AnalysisWarningsWrapper analysisWarnings = spy(AnalysisWarningsWrapper.class);


  protected static String language(Path file) {
    String path = file.toString();
    return path.substring(path.lastIndexOf('.') + 1);
  }

  protected static String onlyOneLogElement(List<String> elements) {
    assertThat(elements).hasSize(1);
    return elements.get(0);
  }

  protected static void assertNoErrorWarnDebugLogs(LogTester logTester) {
    org.assertj.core.api.Assertions.assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
    org.assertj.core.api.Assertions.assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
    org.assertj.core.api.Assertions.assertThat(logTester.logs(LoggerLevel.DEBUG)).isEmpty();
  }

  protected List<ExternalIssue> executeSensorImporting(@Nullable String fileName) throws IOException {
    Path projectDir = projectDir();
    Path baseDir = projectDir.getParent();
    SensorContextTester context = SensorContextTester.create(baseDir);
    try (Stream<Path> fileStream = Files.list(projectDir)) {
      fileStream.forEach(file -> addFileToContext(context, baseDir, file));
      context.setRuntime(SonarRuntimeImpl.forSonarQube(Version.create(8, 9), SonarQubeSide.SERVER, SonarEdition.DEVELOPER));
      if (fileName != null) {
        String path = projectDir.resolve(fileName).toAbsolutePath().toString();
        context.settings().setProperty("sonar.php." + sensor().reportKey() + ".reportPaths", path);
      }
      sensor().execute(context);
      return new ArrayList<>(context.allExternalIssues());
    }
  }

  private static void addFileToContext(SensorContextTester context, Path projectDir, Path file) {
    try {
      String projectId = projectDir.getFileName().toString() + "-project";
      context.fileSystem().add(TestInputFileBuilder.create(projectId, projectDir.toFile(), file.toFile())
        .setCharset(UTF_8)
        .setLanguage(language(file))
        .setContents(new String(Files.readAllBytes(file), UTF_8))
        .setType(InputFile.Type.MAIN)
        .build());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void no_issues_without_report_paths_property() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting(null);
    assertThat(externalIssues).isEmpty();
    assertNoErrorWarnDebugLogs(logTester());
  }

  @Test
  public void no_issues_with_invalid_report_path() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("invalid-path.txt");
    assertThat(externalIssues).isEmpty();
    assertThat(onlyOneLogElement(logTester().logs(LoggerLevel.ERROR)))
      .startsWith("An error occurred when reading report file '")
      .contains("invalid-path.txt', no issue will be imported from this report.\nThe file was not found.");

    verify(analysisWarnings, times(1))
      .addWarning(startsWith("An error occurred when reading report file '"));
  }

  @Test
  public void no_issues_with_invalid_file() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("not-" + sensor().reportKey() + "-report.json");
    assertThat(externalIssues).isEmpty();
    assertThat(onlyOneLogElement(logTester().logs(LoggerLevel.ERROR)))
      .startsWith("An error occurred when reading report file '")
      .contains("no issue will be imported from this report.\nThe content of the file probably does not have the expected format.");

    verify(analysisWarnings, times(1))
      .addWarning(startsWith("An error occurred when reading report file '"));
  }

  @Test
  public void no_issues_with_empty_file() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting(sensor().reportKey() + "-report-empty.json");
    assertThat(externalIssues).isEmpty();
    assertNoErrorWarnDebugLogs(logTester());
  }

  protected abstract Path projectDir();

  protected abstract ExternalIssuesSensor sensor();

  protected abstract LogTester logTester();

}
