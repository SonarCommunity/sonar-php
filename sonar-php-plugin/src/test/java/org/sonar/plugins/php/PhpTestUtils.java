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
/**
 *
 */
package org.sonar.plugins.php;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.php.api.Php;

import static org.assertj.core.api.Assertions.assertThat;

public class PhpTestUtils {

  public static final String PHPUNIT_REPORT_DIR = "org/sonar/plugins/php/phpunit/sensor/";
  public static final String PHPUNIT_REPORT_NAME = PHPUNIT_REPORT_DIR + "phpunit-junit-report.xml";
  public static final String PHPUNIT_COVERAGE_REPORT = PHPUNIT_REPORT_DIR + "phpunit.coverage.xml";

  public static final String SENSOR_TEST_PHPUNIT_REPORT_DIR = "phpunit-reports/";
  public static final String UT_COVERAGE_REPORT_RELATIVE_PATH = SENSOR_TEST_PHPUNIT_REPORT_DIR + "coverage/ut-coverage.xml";
  public static final String IT_COVERAGE_REPORT_RELATIVE_PATH = SENSOR_TEST_PHPUNIT_REPORT_DIR +  "coverage/it-coverage.xml";
  public static final String OVERALL_COVERAGE_REPORT_RELATIVE_PATH = SENSOR_TEST_PHPUNIT_REPORT_DIR +  "coverage/overall-coverage.xml";

  public static final String GENERATED_UT_COVERAGE_REPORT_RELATIVE_PATH = SENSOR_TEST_PHPUNIT_REPORT_DIR + "coverage/generated-ut-coverage.xml";
  public static final String GENERATED_IT_COVERAGE_REPORT_RELATIVE_PATH = SENSOR_TEST_PHPUNIT_REPORT_DIR + "coverage/generated-it-coverage.xml";
  public static final String GENERATED_OVERALL_COVERAGE_REPORT_RELATIVE_PATH = SENSOR_TEST_PHPUNIT_REPORT_DIR + "coverage/generated-overall-coverage.xml";

  private PhpTestUtils() {
  }
  
  public static File getModuleBaseDir() {
    return new File("src/test/resources");
  }

  public static <T extends Serializable> void assertMeasure(SensorContextTester context, String componentKey, org.sonar.api.measures.Metric<T> metric, T expected) {
    assertThat(context.measure(componentKey, metric).value()).as("metric for: " + metric.getKey()).isEqualTo(expected);
  }

  public static <T extends Serializable> void assertNoMeasure(SensorContextTester context, String componentKey, org.sonar.api.measures.Metric<T> metric) {
    assertThat(context.measure(componentKey, metric)).as("metric for: " + metric.getKey()).isNull();
  }

  public static DefaultInputFile inputFile(String fileName, InputFile.Type type) {
    try {
      return TestInputFileBuilder.create("moduleKey", fileName)
        .setModuleBaseDir(PhpTestUtils.getModuleBaseDir().toPath())
        .setType(type)
        .setCharset(Charset.defaultCharset())
        .setLanguage(Php.KEY)
        .initMetadata(new String(java.nio.file.Files.readAllBytes(new File("src/test/resources/" + fileName).toPath()), StandardCharsets.UTF_8)).build();
    } catch (IOException e) {
      throw new IllegalStateException("File not found", e);
    }
  }

  public static DefaultInputFile inputFile(String fileName) {
    return inputFile(fileName, InputFile.Type.MAIN);
  }
}
