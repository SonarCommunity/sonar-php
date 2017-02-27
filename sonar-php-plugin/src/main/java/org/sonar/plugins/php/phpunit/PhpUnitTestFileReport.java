/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.phpunit.xml.TestCase;

public class PhpUnitTestFileReport {

  private static final double PERCENT = 100d;
  private static final Logger LOGGER = LoggerFactory.getLogger(PhpUnitTestResultImporter.class);
  private int errors = 0;
  private int failures = 0;
  private String file;
  private int skipped = 0;
  private int tests = 0;
  private double testDuration = 0;

  /**
   * The PhpUnitTestFileReport contains all the results of test cases appearing in a given test file.
   * The reason why the report is file-based (as opposed to class-based) is that the SonarQube measures
   * are stored per file.
   *
   * @param file the test file name
   * @param testDuration the test file overall execution time
   */
  public PhpUnitTestFileReport(String file, double testDuration) {
    this.file = file;
    this.testDuration = testDuration;
  }

  public void saveTestMeasures(SensorContext context) {
    InputFile unitTestFile = getUnitTestInputFile(context.fileSystem());
    if (unitTestFile != null) {
      context.<Integer>newMeasure().on(unitTestFile).withValue(skipped).forMetric(CoreMetrics.SKIPPED_TESTS).save();

      context.<Long>newMeasure().on(unitTestFile).withValue((long) testDurationSeconds()).forMetric(CoreMetrics.TEST_EXECUTION_TIME).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue((int) liveTests()).forMetric(CoreMetrics.TESTS).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue(errors).forMetric(CoreMetrics.TEST_ERRORS).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue(failures).forMetric(CoreMetrics.TEST_FAILURES).save();
      if (liveTests() > 0) {
        context.<Double>newMeasure().on(unitTestFile).withValue(ParsingUtils.scaleValue(successPercentage())).forMetric(CoreMetrics.TEST_SUCCESS_DENSITY).save();
      }

    } else {
      LOGGER.debug("Following file is not located in the test folder specified in the Sonar configuration: " + file
        + ". The test results won't be reported in Sonar.");
    }
  }

  private double liveTests() {
    return (double) tests - skipped;
  }

  private double successPercentage() {
    double passedTests = liveTests() - errors - failures;
    return passedTests * PERCENT / liveTests();
  }

  private double testDurationSeconds() {
    return testDuration * 1000d;
  }

  private InputFile getUnitTestInputFile(FileSystem fileSystem) {
    FilePredicates predicates = fileSystem.predicates();
    return fileSystem.inputFile(predicates.and(
      predicates.hasPath(file),
      predicates.hasType(InputFile.Type.TEST),
      predicates.hasLanguage(Php.KEY)));
  }

  public void addTestCase(TestCase testCase) {
    if (TestCase.STATUS_SKIPPED.equals(testCase.getStatus())) {
      this.skipped++;
    } else if (TestCase.STATUS_FAILURE.equals(testCase.getStatus())) {
      this.failures++;
    } else if (TestCase.STATUS_ERROR.equals(testCase.getStatus())) {
      this.errors++;
    }
    this.tests++;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("errors", errors);
    builder.append("failures", failures);
    builder.append("file", file);
    builder.append("skipped", skipped);
    builder.append("tests", tests);
    builder.append("testDuration", testDuration);
    return builder.toString();
  }
}
