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
package org.sonar.plugins.php;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.sonar.api.batch.sensor.cache.ReadCache;
import org.sonar.api.batch.sensor.cache.WriteCache;

public class ReadWriteInMemoryCache implements ReadCache, WriteCache {

  private Map<String, byte[]> storage = new HashMap<>();

  @Override
  public InputStream read(String key) {
    byte[] bytes = storage.get(key);
    return new ByteArrayInputStream(bytes);
  }

  @Override
  public boolean contains(String key) {
    return storage.containsKey(key);
  }

  @Override
  public void write(String key, InputStream data) {
    try {
      storage.put(key, data.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(String key, byte[] data) {
    storage.put(key, data);
  }

  @Override
  public void copyFromPrevious(String key) {

  }
}
