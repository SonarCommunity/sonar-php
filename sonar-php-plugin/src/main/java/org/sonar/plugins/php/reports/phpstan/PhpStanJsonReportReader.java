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
package org.sonar.plugins.php.reports.phpstan;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.plugins.php.reports.JsonReportReader;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PhpStanJsonReportReader extends JsonReportReader {

  private final Consumer<Issue> consumer;
  private static final Pattern POSSIBLE_PATH_CONTEXT_PATTERN = Pattern.compile("\\s\\(in context of.*$");

  private PhpStanJsonReportReader(Consumer<Issue> consumer) {
    this.consumer = consumer;
  }

  static void read(InputStream in, Consumer<Issue> consumer) throws IOException, ParseException {
    new PhpStanJsonReportReader(consumer).read(in);
  }

  private void read(InputStream in) throws IOException, ParseException {
    JSONObject rootObject = (JSONObject) jsonParser.parse(new InputStreamReader(in, UTF_8));
    JSONObject files = (JSONObject) rootObject.get("files");
    if (files != null) {
      files.forEach((file, records) -> onFile(cleanFilePath((String) file), (JSONObject) records));
    }
  }

  private void onFile(String file, JSONObject records) {
    JSONArray messages = (JSONArray) records.get("messages");
    if (messages != null) {
      ((Stream<JSONObject>) messages.stream()).forEach(m -> onMessage(file, m));
    }
  }

  private void onMessage(String file, JSONObject message) {
    Issue issue = new Issue();
    issue.filePath = file;
    issue.startLine = toInteger(message.get("line"));
    issue.message = (String) message.get("message");
    consumer.accept(issue);
  }

  /**
   * The key containing the file path might contain additional context information when issues are related to traits. Example:
   * <pre>phpstan/file3.php (in context of class Bar)</pre>. We do remove this additional information here.
   * See SONARPHP-1262
   */
  private static String cleanFilePath(String file) {
    return POSSIBLE_PATH_CONTEXT_PATTERN.matcher(file).replaceAll("");
  }
}
