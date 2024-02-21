/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.metrics.CpdVisitor;

public class CpdSerializer {

  private final ByteArrayOutputStream stream;
  private final VarLengthOutputStream out;
  private final StringTable stringTable;

  private CpdSerializer() {
    stream = new ByteArrayOutputStream();
    out = new VarLengthOutputStream(stream);
    stringTable = new StringTable();
  }

  public static SerializationResult toBinary(CpdSerializationInput serializationInput) {
    CpdSerializer serializer = new CpdSerializer();
    return serializer.convert(serializationInput);
  }

  private SerializationResult convert(CpdSerializationInput serializationInput) {
    try (out; stream) {
      String pluginVersion = serializationInput.pluginVersion();
      writeText(pluginVersion);

      List<CpdVisitor.CpdToken> cpdTokens = serializationInput.cpdTokens();
      writeInt(cpdTokens.size());
      for (CpdVisitor.CpdToken cpdToken : cpdTokens) {
        write(cpdToken);
      }

      out.writeUTF("END");

      return new SerializationResult(stream.toByteArray(), writeStringTable());
    } catch (IOException e) {
      throw new IllegalStateException("Can't store data in cache", e);
    }
  }

  private void write(CpdVisitor.CpdToken cpdToken) throws IOException {
    writeInt(cpdToken.line());
    writeInt(cpdToken.column());
    writeInt(cpdToken.endLine());
    writeInt(cpdToken.endColumn());
    writeText(cpdToken.text());
  }

  private void writeText(@Nullable String text) throws IOException {
    out.writeInt(stringTable.getIndex(text));
  }

  private void writeInt(int number) throws IOException {
    out.writeInt(number);
  }

  private byte[] writeStringTable() throws IOException {
    ByteArrayOutputStream stringTableStream = new ByteArrayOutputStream();
    VarLengthOutputStream output = new VarLengthOutputStream(stringTableStream);
    List<String> byIndex = stringTable.getStringList();
    output.writeInt(byIndex.size());
    for (String string : byIndex) {
      output.writeUTF(string);
    }

    output.writeUTF("END");
    return stringTableStream.toByteArray();
  }
}
