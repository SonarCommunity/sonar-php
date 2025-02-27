/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFileFilter;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.plugins.php.api.Php;

public class PhpExclusionsFileFilter implements InputFileFilter {

  private final String[] excludedPatterns;
  private static final Logger LOG = LoggerFactory.getLogger(PhpExclusionsFileFilter.class);
  private static final int DEFAULT_AVERAGE_LINE_LENGTH_THRESHOLD = 220;

  public PhpExclusionsFileFilter(Configuration configuration) {
    excludedPatterns = configuration.getStringArray(PhpPlugin.PHP_EXCLUSIONS_KEY);
  }

  @Override
  public boolean accept(InputFile inputFile) {
    if (!Php.KEY.equals(inputFile.language())) {
      return true;
    }

    URI fileUri = inputFile.uri();
    String relativePath = fileUri.toString();
    if (WildcardPattern.match(WildcardPattern.create(excludedPatterns), relativePath)) {
      LOG.debug("File [{}] is excluded by '{}' property and will not be analyzed", fileUri, PhpPlugin.PHP_EXCLUSIONS_KEY);
      return false;
    }

    if (new AverageLineLengthCalculator(inputFile).getAverageLineLength() > DEFAULT_AVERAGE_LINE_LENGTH_THRESHOLD) {
      LOG.debug("File [{}] is excluded because it is considered generated (average line length is too big).", fileUri);
      return false;
    }

    return true;
  }

  /**
   * An instance of this class computes the average line length of file.
   * Before making the computation, it discards all lines which are part
   * of the header comment.
   * The header comment is a comment which starts on the first line of the file.
   * It may be either a C-like comment (i.e., it starts with <code>"/*"</code>) or a C++-like comment
   * (i.e., it starts with <code>"//"</code>).
   */
  private static class AverageLineLengthCalculator {
    private static final Pattern PHP_OPEN_TAG = Pattern.compile(LexicalConstant.PHP_OPENING_TAG);

    private InputFile file;

    private boolean isAtFirstLine = true;

    private boolean isInHeaderComment = false;

    private boolean isClike = false;

    public AverageLineLengthCalculator(InputFile file) {
      this.file = file;
    }

    public int getAverageLineLength() {
      long nbLines = 0;
      long nbCharacters = 0;

      List<String> lines = fileLines(file);

      for (String line : lines) {
        if (!ignoreLine(line)) {
          nbLines++;
          nbCharacters += line.length();
        }
      }

      return nbLines > 0 ? (int) (nbCharacters / nbLines) : 0;
    }

    private static List<String> fileLines(InputFile inputFile) {
      List<String> lines = new ArrayList<>();
      try (var scanner = new Scanner(inputFile.inputStream(), inputFile.charset())) {
        while (scanner.hasNextLine()) {
          lines.add(scanner.nextLine());
        }
      } catch (IOException e) {
        throw new AnalysisException(String.format("Unable to read file '%s'", inputFile), e);
      }
      return lines;
    }

    private boolean ignoreLine(String line) {
      String trimmedLine = line.trim();
      if (isAtFirstLine) {
        if (trimmedLine.isEmpty()) {
          return true;
        }
        if (PHP_OPEN_TAG.matcher(trimmedLine).find()) {
          return false;
        }
        isAtFirstLine = false;
        return isFirstLineInHeaderComment(trimmedLine);
      } else if (isInHeaderComment) {
        return isSubsequentLineInHeaderComment(trimmedLine);
      }
      return false;
    }

    private boolean isFirstLineInHeaderComment(String line) {
      if (line.startsWith("/*")) {
        isClike = true;
        isInHeaderComment = !line.endsWith("*/");
        return true;
      } else if (line.startsWith("//")) {
        isClike = false;
        isInHeaderComment = true;
        return true;
      }
      return false;
    }

    private boolean isSubsequentLineInHeaderComment(String line) {
      if (isClike) {
        if (line.endsWith("*/")) {
          isInHeaderComment = false;
        } else if (line.contains("*/")) {
          // case of */ followed with something, possibly a long minified line
          isInHeaderComment = false;
          return false;
        }
        return true;
      } else {
        if (line.startsWith("//")) {
          return true;
        } else {
          isInHeaderComment = false;
          return false;
        }
      }
    }

  }
}
