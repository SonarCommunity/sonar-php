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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.symbols.MethodSymbolData;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.symbols.UnknownLocationInFile;
import org.sonar.php.symbols.Visibility;
import org.sonar.php.tree.symbols.SymbolQualifiedName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ProjectSymbolDataSerializerTest {

  public static final String PLUGIN_VERSION = "1.2.3";

  @Test
  public void shouldSerializeAndDeserializeExampleData() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    List<MethodSymbolData> methods = new ArrayList<>();
    methods.add(new MethodSymbolData(
      new LocationInFileImpl("Mail.php", 183, 27, 183, 46),
      "setDefaultTransport",
      List.of(new Parameter("$transport", "Zend_Mail_Transport_Abstract", false, false)),
    new FunctionSymbolData.FunctionSymbolProperties(false, false),
      Visibility.PUBLIC,
      false));
    methods.add(new MethodSymbolData(
      new LocationInFileImpl("Mail.php", 195, 27, 195, 46),
      "getDefaultTransport",
      List.of(),
      new FunctionSymbolData.FunctionSymbolProperties(true, false),
      Visibility.PUBLIC,
      false));

    methods.add(new MethodSymbolData(
      new LocationInFileImpl("Mail.php", 215, 20, 215, 31),
      "__construct",
      List.of(new Parameter("$charset", null, true, false)),
      new FunctionSymbolData.FunctionSymbolProperties(false, false),
      Visibility.PUBLIC,
      false
    ));

    ClassSymbolData classSymbolData = new ClassSymbolData(
      new LocationInFileImpl("Mail.php", 52, 6, 52, 15),
      SymbolQualifiedName.qualifiedName("zend_mail"),
      SymbolQualifiedName.qualifiedName("zend_mime_message"),
      List.of(),
      ClassSymbol.Kind.NORMAL,
      methods);
    projectSymbolData.add(classSymbolData);
    FunctionSymbolData functionSymbolData = new FunctionSymbolData(
      new LocationInFileImpl("file1.php", 2,9,2,12),
      SymbolQualifiedName.qualifiedName("foo"),
      List.of(new Parameter("$i", "int", false, false)),
      new FunctionSymbolData.FunctionSymbolProperties(false, false)
    );
    projectSymbolData.add(functionSymbolData);

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, PLUGIN_VERSION));
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(new DeserializationInput(binary.data(), binary.stringTable(), PLUGIN_VERSION));

    assertThat(actual).isEqualToComparingFieldByFieldRecursively(projectSymbolData);
  }

  @Test
  public void shouldThrowExceptionWhenNotSymbolQualifiedName() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    ClassSymbolData classSymbolData = new ClassSymbolData(
      new LocationInFileImpl("Mail.php", 52, 6, 52, 15),
      () -> "dummy",
      SymbolQualifiedName.qualifiedName("zend_mime_message"),
      List.of(),
      ClassSymbol.Kind.NORMAL,
      List.of());
    projectSymbolData.add(classSymbolData);

    Throwable throwable = catchThrowable(() -> ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, PLUGIN_VERSION)));

    assertThat(throwable)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageStartingWith("The QualifiedName of type ");
  }

  @Test
  public void shouldThrowExceptionWhenNotFunctionSymbolData() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    FunctionSymbolData functionSymbolData = new MethodSymbolData(
      new LocationInFileImpl("file1.php", 2,9,2,12),
      "name",
      List.of(new Parameter("$i", "int", false, false)),
      new FunctionSymbolData.FunctionSymbolProperties(false, false),
      Visibility.PUBLIC
    );
    projectSymbolData.add(functionSymbolData);

    Throwable throwable = catchThrowable(() -> ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, PLUGIN_VERSION)));

    assertThat(throwable)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageStartingWith("The FunctionSymbolData of type ");
  }

  @Test
  public void shouldSerializeAndDeserializeClassWithImplementedInterface() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();

    ClassSymbolData classSymbolData = new ClassSymbolData(
      new LocationInFileImpl("Mail.php", 52, 6, 52, 15),
      SymbolQualifiedName.qualifiedName("zend_mail"),
      SymbolQualifiedName.qualifiedName("zend_mime_message"),
      List.of(SymbolQualifiedName.qualifiedName("some_interface")),
      ClassSymbol.Kind.NORMAL,
      List.of());
    projectSymbolData.add(classSymbolData);

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, PLUGIN_VERSION));
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(new DeserializationInput(binary.data(), binary.stringTable(), PLUGIN_VERSION));

    assertThat(actual).isEqualToComparingFieldByFieldRecursively(projectSymbolData);
  }

  @Test
  public void shouldSerializeAndDeserializeClassWithUnknownLocation() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();

    ClassSymbolData classSymbolData = new ClassSymbolData(
      UnknownLocationInFile.UNKNOWN_LOCATION,
      SymbolQualifiedName.qualifiedName("dummy"),
      SymbolQualifiedName.qualifiedName("dummy"),
      List.of(),
      ClassSymbol.Kind.NORMAL,
      List.of());
    projectSymbolData.add(classSymbolData);

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, PLUGIN_VERSION));
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(new DeserializationInput(binary.data(), binary.stringTable(), PLUGIN_VERSION));

    assertThat(actual).isEqualToComparingFieldByFieldRecursively(projectSymbolData);
  }

  @Test
  public void shouldSerializeAndDeserializeClassWithNullSuperClass() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();

    ClassSymbolData classSymbolData = new ClassSymbolData(
      UnknownLocationInFile.UNKNOWN_LOCATION,
      SymbolQualifiedName.qualifiedName("dummy"),
      null,
      List.of(),
      ClassSymbol.Kind.NORMAL,
      List.of());
    projectSymbolData.add(classSymbolData);

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, PLUGIN_VERSION));
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(new DeserializationInput(binary.data(), binary.stringTable(), PLUGIN_VERSION));

    assertThat(actual).isEqualToComparingFieldByFieldRecursively(projectSymbolData);
  }

  @Test
  public void shouldSerializeAndDeserializeClassWithQualifiedNameContainsBackslash() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();

    ClassSymbolData classSymbolData = new ClassSymbolData(
      UnknownLocationInFile.UNKNOWN_LOCATION,
      SymbolQualifiedName.qualifiedName("symfony\\bridge\\monolog\\handler\\helper"),
      null,
      List.of(),
      ClassSymbol.Kind.NORMAL,
      List.of());
    projectSymbolData.add(classSymbolData);

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, PLUGIN_VERSION));
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(new DeserializationInput(binary.data(), binary.stringTable(), PLUGIN_VERSION));

    assertThat(actual).isEqualToComparingFieldByFieldRecursively(projectSymbolData);
  }

  @Test
  public void shouldThrowExceptionWhenStringTableCorrupted() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    FunctionSymbolData functionSymbolData = new FunctionSymbolData(
      new LocationInFileImpl("file1.php", 2,9,2,12),
      SymbolQualifiedName.qualifiedName("name"),
      List.of(),
      new FunctionSymbolData.FunctionSymbolProperties(false, false)
    );
    projectSymbolData.add(functionSymbolData);

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, PLUGIN_VERSION));
    Throwable throwable = catchThrowable(() -> ProjectSymbolDataDeserializer.fromBinary(
      new DeserializationInput(
        binary.data(),
        corruptBit(binary.stringTable()),
        PLUGIN_VERSION
      )));

    assertThat(throwable)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Can't read data from cache");
  }

  @Test
  public void shouldThrowExceptionWhenProjectSymbolDataCorrupted() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    FunctionSymbolData functionSymbolData = new FunctionSymbolData(
      new LocationInFileImpl("file1.php", 2,9,2,12),
      SymbolQualifiedName.qualifiedName("name"),
      List.of(),
      new FunctionSymbolData.FunctionSymbolProperties(false, false)
    );
    projectSymbolData.add(functionSymbolData);

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, PLUGIN_VERSION));
    Throwable throwable = catchThrowable(() -> ProjectSymbolDataDeserializer.fromBinary(
      new DeserializationInput(
      corruptBit(binary.data()),
      binary.stringTable(),
        PLUGIN_VERSION)));

    assertThat(throwable)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Can't read data from cache");
  }

  @Test
  public void shouldReturnNullWhenWrongPluginVersion() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();

    ClassSymbolData classSymbolData = new ClassSymbolData(
      UnknownLocationInFile.UNKNOWN_LOCATION,
      SymbolQualifiedName.qualifiedName("dummy"),
      SymbolQualifiedName.qualifiedName("dummy"),
      List.of(),
      ClassSymbol.Kind.NORMAL,
      List.of());
    projectSymbolData.add(classSymbolData);

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, PLUGIN_VERSION));
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(new DeserializationInput(binary.data(), binary.stringTable(), "5.5.5"));

    assertThat(actual).isNull();
  }

  private byte[] corruptBit(byte[] input) {
    input[input.length - 1] = (byte) (input[input.length - 1] << 1);
    return input;
  }
}
