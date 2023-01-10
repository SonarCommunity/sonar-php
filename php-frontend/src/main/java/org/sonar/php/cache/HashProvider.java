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
package org.sonar.php.cache;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.annotation.CheckForNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.php.api.visitors.PhpFile;

public class HashProvider {
  private static final Logger LOG = Loggers.get(HashProvider.class);
  private static final String ERROR_MESSAGE = "Error calculation hash for: ";

  private HashProvider() {
  }

  @CheckForNull
  public static String hash(InputFile file) {
    try {
      return hash(file.contents(), file.filename());
    } catch (IOException e) {
      LOG.error(ERROR_MESSAGE + file.filename(), e);
    }
    return null;
  }

  @CheckForNull
  public static String hash(PhpFile file) {
    try {
      return hash(file.contents(), file.filename());
    } catch (RuntimeException e) {
      LOG.error(ERROR_MESSAGE + file.filename(), e);
    }
    return null;
  }

  private static String hash(String content, String filename) {
    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      byte[] digest = sha256.digest(content.getBytes(StandardCharsets.UTF_8));
      return new BigInteger(digest).toString();
    } catch (NoSuchAlgorithmException e) {
      LOG.error(ERROR_MESSAGE + filename, e);
    }
    return null;
  }
}
