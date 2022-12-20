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
package org.sonar.php;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.php.compat.PhpFileImpl;
import org.sonar.plugins.php.api.visitors.PhpFile;

public class FileTestUtils {

  private FileTestUtils() {
  }

  public static PhpFile getFile(File file, String contents) {
    try {
      Files.write(file.toPath(), contents.getBytes(Charset.defaultCharset()));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write test file: " + file.getAbsolutePath());
    }
    return getFile(file);
  }

  public static PhpFile getFile(File file) {
    return PhpFileImpl.create(getInputFile(file));
  }

  public static InputFile getInputFile(File file, String content) {
    try {
      Files.write(file.toPath(), content.getBytes(Charset.defaultCharset()));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write test file: " + file.getAbsolutePath());
    }
    return getInputFile(file);
  }

  public static InputFile getInputFile(File file) {
    try {
      DefaultInputFile inputFile = TestInputFileBuilder.create("moduleKey", file.getName())
        .setModuleBaseDir(file.getParentFile().toPath())
        .setCharset(Charset.defaultCharset())
        .initMetadata(new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset())).build();
      return inputFile;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create test file from: " + file.getAbsolutePath());
    }
  }
}
