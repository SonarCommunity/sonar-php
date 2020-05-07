<?php
class A {

  public $fieldNameWithPasswordInIt = retrievePassword();
  public $fieldNameWithPasswordInIt = "xxx"; // Noncompliant {{'password' detected in this variable name, review this potentially hardcoded credential.}}
//       ^^^^^^^^^^^^^^^^^^^^^^^^^^
  public $fieldNameWithPasswordInIt = ""; // OK, empty
  public $fieldNameWithPasswordInIt = ''; // OK, empty
  public $fieldNameWithPasswordInIt = "$password";
  public $fieldNameWithPasswordInIt;
  public $otherFieldName = "";

  // only a single issue even if multiple occurence of forbidden words
  public $myPasswordIsPWD = "something"; // Noncompliant {{'password' detected in this variable name, review this potentially hardcoded credential.}}
  public $myPasswordIsPWD = ""; // OK, empty

  private function a() {
    $variable1 = "blabla";
    $variable2 = "login=a&pwd=xxx"; // Noncompliant {{'pwd' detected in this variable name, review this potentially hardcoded credential.}}
//               ^^^^^^^^^^^^^^^^^
    $variable3 = "login=a&password=";
    $variable4 = "login=a&password=$password";

    $variableNameWithPasswordInIt = "xxx"; // Noncompliant
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    $otherVariableNameWithPasswordInIt;
    $this->fieldNameWithPasswdInIt = "xx"; // Noncompliant
//         ^^^^^^^^^^^^^^^^^^^^^^^
    $this->fieldNameWithPasswordInIt = retrievePassword();
  }

}

const secretPassword = "xxx"; // Noncompliant
const otherConstant = "xxx";
const nonRelatedValue = 42;

class A {
  const constFieldNameWithPasswordInIt = "something"; // Noncompliant
  const constFieldNameWithPasswordInIt = ""; // OK, empty
  const otherConstFieldName = "";
}

function foo() {
  static $staticVariableNameWithPasswordInIt = "xxx"; // Noncompliant
  static $otherStaticVariableName = "xxx";
}

$var1 = "password=?"; // Compliant
$var1 = "password=:password"; // Compliant
$var1 = "password=:param"; // Compliant
$var1 = "password=%s"; // Compliant
$var1 = "password='" . pwd . "'"; // Compliant
$var1 = "password=" . pwd . "'"; // Compliant
$var1 = "password=?&login=a"; // Compliant
$var1 = "password=:password&login=a"; // Compliant
$var1 = "password=:param&login=a"; // Compliant
$var1 = "password=%s&login=a"; // Compliant
$var1 = "password=(secret)"; // Noncompliant

$pwd = "pwd"; // Compliant
$password = "pwd"; // Noncompliant
$password = "password"; // Compliant
$ampq_password = 'amqp-password'; // Compliant
const CONFIG_PATH_QUEUE_AMQP_PASSWORD = 'queue/amqp/password'; // Compliant
const IDENTITY_VERIFICATION_PASSWORD_FIELD = 'current_password'; // Compliant

// The literal string doesn't contain the wordlist item matched on the variable name
const DEFAULT_AMQP_PASSWORD = 'pwd'; // Noncompliant

ldap_bind("a", "b", "p4ssw0rd"); // Noncompliant
$conection = new PDO("a", "b", "p4ssw0rd"); // Noncompliant
mysqli_connect("a", "b", "p4ssw0rd"); // Noncompliant
mysql_connect("a", "b", "p4ssw0rd"); // Noncompliant
ldap_exop_passwd("a", "b", "c", "p4ssw0rd"); // Noncompliant
mssql_connect("a", "b", "p4ssw0rd"); // Noncompliant
odbc_connect("a", "b", "p4ssw0rd"); // Noncompliant
db2_connect("a", "b", "p4ssw0rd"); // Noncompliant
cubrid_connect("a", "b", "c", "d", "p4ssw0rd"); // Noncompliant
maxdb_connect("a", "b", "p4ssw0rd"); // Noncompliant
maxdb_change_user("a", "b", "p4ssw0rd"); // Noncompliant
imap_open("a", "b", "p4ssw0rd"); // Noncompliant
ifx_connect("a", "b", "p4ssw0rd"); // Noncompliant
dbx_connect("a", "b", "c", "d", "p4ssw0rd"); // Noncompliant
fbsql_pconnect("a", "b", "p4ssw0rd"); // Noncompliant

ldap_bind("a", "b"); // Compliant
ldap_bind("a", "b", $foo); // Compliant
