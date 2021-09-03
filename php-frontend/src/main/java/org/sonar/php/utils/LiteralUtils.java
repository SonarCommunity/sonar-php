/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiteralUtils {

  private LiteralUtils() {
    // This class only contains static methods
  }

  public static long longLiteralValue(String literalValue) {
    String value = literalValue.replace("_", "");
    if (value.startsWith("0b") || value.startsWith("0B")) {
      return Long.parseUnsignedLong(value.substring(2), 2);
    }
    return Long.decode(value);
  }

  // https://www.php.net/manual/en/language.types.string.php
  public static String stringLiteralValue(String literalValue) {
    return StringLiteralParser.stringValue(literalValue);
  }

  private abstract static class StringLiteralParser {

    private final String valueWithoutQuotes;

    static String stringValue(String literalValue) {
      String valueWithoutQuotes = literalValue.substring(1, literalValue.length() - 1);
      if (valueWithoutQuotes.indexOf('\\') == -1) {
        return valueWithoutQuotes;
      }
      StringLiteralParser parser = (literalValue.charAt(0) == '\'')
        ? new SingleQuotedStringLiteralParser(valueWithoutQuotes)
        : new DoubleQuotedStringLiteralParser(valueWithoutQuotes);
      return parser.stringValue();
    }

    StringLiteralParser(String valueWithoutQuotes) {
      this.valueWithoutQuotes = valueWithoutQuotes;
    }

    String stringValue() {
      StringBuilder stringValue = new StringBuilder();
      boolean isInEscapeSequence = false;
      int i = 0;
      while (i < valueWithoutQuotes.length()) {
        char c = valueWithoutQuotes.charAt(i);
        if (isInEscapeSequence) {
          String remainder = valueWithoutQuotes.substring(i);
          int escapeSequenceLength = handleEscapeSequence(stringValue, c, remainder);
          isInEscapeSequence = false;
          i += escapeSequenceLength - 1;
        } else {
          if (c == '\\') {
            isInEscapeSequence = true;
          } else {
            stringValue.append(c);
          }
          i++;
        }
      }
      return stringValue.toString();
    }

    /**
     * @return the total number of characters in the handled escape sequence
     */
    abstract int handleEscapeSequence(StringBuilder stringValue, char charAfterBackslash, String remainder);
  }

  private static class SingleQuotedStringLiteralParser extends StringLiteralParser {

    SingleQuotedStringLiteralParser(String valueWithoutDelimiters) {
      super(valueWithoutDelimiters);
    }

    @Override
    int handleEscapeSequence(StringBuilder stringValue, char charAfterBackslash, String remainder) {
      if (charAfterBackslash == '\'') {
        stringValue.append('\'');
      } else if (charAfterBackslash == '\\') {
        stringValue.append('\\');
      } else {
        stringValue.append('\\').append(charAfterBackslash);
      }
      return 2;
    }
  }

  private static class DoubleQuotedStringLiteralParser extends StringLiteralParser {

    DoubleQuotedStringLiteralParser(String valueWithoutDelimiters) {
      super(valueWithoutDelimiters);
    }

    @Override
    int handleEscapeSequence(StringBuilder stringValue, char charAfterBackslash, String remainder) {
      switch (charAfterBackslash) {
        case '\\':
          stringValue.append('\\');
          break;
        case '"':
          stringValue.append('\"');
          break;
        case 'n':
          stringValue.append('\n');
          break;
        case 'r':
          stringValue.append('\r');
          break;
        case 't':
          stringValue.append('\t');
          break;
        case 'f':
          stringValue.append('\f');
          break;
        case 'v':
          stringValue.append('\u000b');
          break;
        case 'e':
          stringValue.append('\u001b');
          break;
        case 'x':
          Matcher matcher = Pattern.compile("^x([0-9A-Fa-f]{1,2})").matcher(remainder);
          if (matcher.find()) {
            String hexValue = matcher.group(1);
            stringValue.append((char) Integer.parseInt(hexValue, 16));
            return hexValue.length() + 2;
          } else {
            stringValue.append("\\x");
          }
          break;
        case 'u':
          Matcher unicodeMatcher = Pattern.compile("^u\\{([0-9A-Fa-f]+)}").matcher(remainder);
          if (unicodeMatcher.find()) {
            String hexValue = unicodeMatcher.group(1);
            stringValue.append((char) Integer.parseInt(hexValue, 16));
            return hexValue.length() + 4;
          } else {
            stringValue.append("\\u");
          }
          break;
        case '$':
          stringValue.append('$');
          break;
        default:
          Matcher octalMatcher = Pattern.compile("^([0-7]{1,3})").matcher(remainder);
          if (octalMatcher.find()) {
            String octalValue = octalMatcher.group(1);
            stringValue.append((char) Integer.parseInt(octalValue, 8));
            return octalValue.length() + 1;
          } else {
            stringValue.append('\\').append(charAfterBackslash);
          }
      }
      return 2;
    }
  }

}
