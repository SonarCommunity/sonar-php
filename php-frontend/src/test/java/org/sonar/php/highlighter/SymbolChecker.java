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
package org.sonar.php.highlighter;

import java.util.Collection;
import java.util.List;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * To test the symbols and the symbol references.
 */
public class SymbolChecker {

  private String componentKey;

  public SymbolChecker(String componentKey) {
    this.componentKey = componentKey;
  }

  /**
   * Checks the existence of a symbol at the specified location.
   * @param line the line of the symbol
   * @param column any column of the symbol
   */
  public void checkSymbolExistence(SensorContextTester context, int line, int column) {
    Collection<TextRange> foundReferences = context.referencesForSymbolAt(componentKey, line, column);
    String message = String.format("a symbol is expected to exist and have references at line %d and column %d", line, column);
    assertThat(foundReferences).as(message).isNotNull();
  }

  public void checkNoSymbolExists(SensorContextTester context, int line, int column) {
    Collection<TextRange> foundReferences = context.referencesForSymbolAt(componentKey, line, column);
    String message = String.format("no symbol is expected to exist at line %d and column %d", line, column);
    assertThat(foundReferences).as(message).isNull();
  }

  /**
   * Checks that the specified symbol references match with the symbol references of the specified symbol.
   * @param line the line of the symbol
   * @param column any column of the symbol
   * @param referenceRanges all references to the symbol
   */
  public void checkSymbolReferences(SensorContextTester context, int line, int column, List<? extends TextRange> referenceRanges) {
    Collection<TextRange> foundReferences = context.referencesForSymbolAt(componentKey, line, column);
    assertThat(foundReferences).isNotNull();
    String message = String.format("number of found references to the symbol located at line %d and column %d", line, column);
    assertThat(foundReferences).as(message).hasSameSizeAs(referenceRanges);
    for (TextRange referenceRange : referenceRanges) {
      assertThat(foundReferences).extracting("start", "end").contains(tuple(referenceRange.start(), referenceRange.end()));
    }
  }

}
