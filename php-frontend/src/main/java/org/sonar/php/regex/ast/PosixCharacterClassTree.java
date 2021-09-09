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
package org.sonar.php.regex.ast;

import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassTree;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.IndexRange;
import org.sonarsource.analyzer.commons.regex.ast.SourceCharacter;

public class PosixCharacterClassTree extends CharacterClassTree {

  public PosixCharacterClassTree(RegexSource source, IndexRange range, SourceCharacter openingBracket,
    boolean negation, String property, FlagSet activeFlags) {
    super(source,
      range,
      openingBracket,
      negation,
      new PosixCharacterClassElementTree(source, new IndexRange(range.getBeginningOffset()+1, range.getEndingOffset()-1), property, activeFlags),
      activeFlags);
  }
  
  public PosixCharacterClassTree(RegexSource source, SourceCharacter openingBracket, SourceCharacter closingBracket,
    boolean negation, String property, FlagSet activeFlags) {
    this(source,
      openingBracket.getRange().merge(closingBracket.getRange()),
      openingBracket,
      negation,
      property,
      activeFlags);
  }
}
