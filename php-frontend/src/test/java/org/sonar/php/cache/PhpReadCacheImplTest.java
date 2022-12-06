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
import java.io.InputStream;
import org.junit.Test;
import org.sonar.api.batch.sensor.cache.ReadCache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PhpReadCacheImplTest {

  @Test
  public void shouldReadBytesFromReadCache() {
    ReadCache readCache = mock(ReadCache.class);
    when(readCache.contains("key")).thenReturn(true);
    InputStream inputStream = mock(InputStream.class);
    when(readCache.read("key")).thenReturn(inputStream);
    PhpReadCacheImpl phpReadCache = new PhpReadCacheImpl(readCache);

    phpReadCache.readBytes("key");

    verify(readCache).contains("key");
    verify(readCache).read("key");
  }

  @Test
  public void shouldReturnNullWhenIoException() throws IOException {
    ReadCache readCache = mock(ReadCache.class);
    when(readCache.contains("key")).thenReturn(true);
    InputStream inputStream = mock(InputStream.class);
    when(inputStream.readAllBytes()).thenThrow(new IOException());
    when(readCache.read("key")).thenReturn(inputStream);

    PhpReadCacheImpl phpReadCache = new PhpReadCacheImpl(readCache);

    byte[] actual = phpReadCache.readBytes("key");

    assertThat(actual).isNull();
  }

  @Test
  public void shouldReturnNullWhenDoesntContainsKey() throws IOException {
    ReadCache readCache = mock(ReadCache.class);
    when(readCache.contains("key")).thenReturn(false);

    PhpReadCacheImpl phpReadCache = new PhpReadCacheImpl(readCache);

    byte[] actual = phpReadCache.readBytes("key");

    assertThat(actual).isNull();
  }

  @Test
  public void shouldCheckContainsInReadCache() {
    ReadCache readCache = mock(ReadCache.class);
    PhpReadCacheImpl phpReadCache = new PhpReadCacheImpl(readCache);

    phpReadCache.contains("key");

    verify(readCache).contains("key");
  }
}
