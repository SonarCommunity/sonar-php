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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestResultImporterTest {

  private static final File BASE_DIR = new File("src/test/resources/org/sonar/plugins/php/phpunit/sensor/src/");
  private final AnalysisWarningsWrapper analysisWarnings = spy(AnalysisWarningsWrapper.class);
  private TestResultImporter importer;
  private SensorContextTester context;
  private DefaultFileSystem fs;

  @Rule
  public LogTester logTester = new LogTester();

  @Before
  public void setUp() {
    context = SensorContextTester.create(new File("src/test/resources"));
    importer = new TestResultImporter(analysisWarnings);
    fs = new DefaultFileSystem(BASE_DIR.getAbsoluteFile());
  }

  @Test
  public void should_add_waring_and_log_when_report_not_found() throws IOException {
    executeSensorImporting(new File("notfound.txt"));
    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(1);
    assertThat((logTester.logs(LoggerLevel.ERROR).get(0)))
      .startsWith("An error occurred when reading report file '")
      .contains("notfound.txt', nothing will be imported from this report.");

    verify(analysisWarnings, times(1))
      .addWarning(startsWith("An error occurred when reading report file '"));
  }

  @Test()
  public void shouldGenerateTestsMeasures() throws IOException {
    DefaultInputFile appTestFile = TestInputFileBuilder.create("moduleKey", "src/AppTest.php").setType(InputFile.Type.TEST).setLanguage(Php.KEY).build();
    DefaultInputFile appSkippedTestFile = TestInputFileBuilder.create("moduleKey", "src/AppSkipTest.php").setType(InputFile.Type.TEST).setLanguage(Php.KEY).build();

    fs.add(appTestFile);
    fs.add(appSkippedTestFile);
    context.setFileSystem(fs);

    String appTestFileKey = appTestFile.key();
    String appSkipTestFileKey = appSkippedTestFile.key();

    executeSensorImporting(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_NAME));

    PhpTestUtils.assertMeasure(context, appTestFileKey, CoreMetrics.TESTS, 1);
    PhpTestUtils.assertMeasure(context, appTestFileKey, CoreMetrics.TEST_FAILURES, 0);
    PhpTestUtils.assertMeasure(context, appTestFileKey, CoreMetrics.TEST_ERRORS, 0);
    PhpTestUtils.assertMeasure(context, appTestFileKey, CoreMetrics.SKIPPED_TESTS, 0);
    PhpTestUtils.assertMeasure(context, appTestFileKey, CoreMetrics.TEST_EXECUTION_TIME, 0L);

    PhpTestUtils.assertMeasure(context, appSkipTestFileKey, CoreMetrics.TESTS, 0);
    PhpTestUtils.assertMeasure(context, appSkipTestFileKey, CoreMetrics.TEST_FAILURES, 0);
    PhpTestUtils.assertMeasure(context, appSkipTestFileKey, CoreMetrics.TEST_ERRORS, 0);
    PhpTestUtils.assertMeasure(context, appSkipTestFileKey, CoreMetrics.SKIPPED_TESTS, 1);
    PhpTestUtils.assertMeasure(context, appSkipTestFileKey, CoreMetrics.TEST_EXECUTION_TIME, 0L);

    assertThat(logTester.logs(LoggerLevel.WARN)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.WARN).get(0))
      .startsWith("Failed to resolve 6 file path(s) in PHPUnit tests")
      .endsWith("Nothing is imported related to file(s): MegaAppTest.php;" +
        "src/App2Test.php;src/App3Test.php;src/AppErrorTest.php;src/AppFailTest.php;...");

    verify(analysisWarnings, times(1))
      .addWarning(startsWith("Failed to resolve 6 file path(s) in PHPUnit tests"));
  }

  private void executeSensorImporting(File fileName) {
    context.settings().setProperty(importer.reportPathKey(), fileName.getAbsolutePath());
    importer.execute(context);
  }

}
