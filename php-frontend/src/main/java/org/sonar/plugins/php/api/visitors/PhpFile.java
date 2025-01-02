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
package org.sonar.plugins.php.api.visitors;

import java.net.URI;

/**
 * Class representing a file being analysed by our PHP analyzer.
 */
public interface PhpFile {

  String contents();

  /**
   * @return Filename for this file (including extension). For example: MyFile.php.
   */
  String filename();

  /**
   * @return Unique identifier of the file. It may not be a file:// URI, as it may not exist physically.
   */
  URI uri();

  String key();

}
