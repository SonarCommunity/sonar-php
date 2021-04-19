<?php

// IPv4-like strings

const serverAddress = "1.20.33.145";  // Noncompliant {{Make sure using this hardcoded IP address is safe here.}}
//                    ^^^^^^^^^^^^^
const nonRelatedValue = 42;

class A {
  const constFieldNameWithPasswordInIt = "8.8.8.8"; // Noncompliant
  const otherConstFieldName = "http://1.2.3.4:8080/foo"; // Noncompliant
}

function foo($socket) {
  static $otherStaticVariableName = "xxx";
  socket_connect($socket, '1.1.1.1', 23); // Noncompliant
}

const localhost = "127.0.0.1"; // Compliant - Loopback address
const localhostUrl = "ftp://127.0.0.1:22/bla"; // Compliant - exception for localhost
const notIPAddress = "1.20.33.345"; // Compliant - segment > 255
const notRouteable = "0.0.0.0"; // Compliant


// IPv6 like strings

$serverAddress = "[2001:db8:a0b:12f0::1]:21";                   // Noncompliant
//               ^^^^^^^^^^^^^^^^^^^^^^^^^^^
$serverAddress = "2001:db8:a0b:12f0::1";                        // Noncompliant
$serverAddress = "2001:db8:a0b:12f0:12:1";                      // Compliant
$serverAddress = "2001:0db8:0a0b:12f0:0000:0000:0000:0001";     // Noncompliant
$serverAddress = "::1";                                         // Compliant - Loopback address
$serverAddress = "[::1]";                                       // Compliant - Loopback address
$serverAddress = "0:0:0:0:0:0:0:1";                             // Compliant - Loopback address
$serverAddress = "::2";                                         // Noncompliant
$serverAddress = "2001:db8:3:4::192.0.2.33";                    // Noncompliant
$serverAddress = "1234:::1";                                    // Compliant - no IPv6
$serverAddress = '0:1234:dc0:41:216:3eff:fe67:3e01';            // Noncompliant


$url = "http://[2001:db8:a0b:12f0::1]/index.html";              // Noncompliant
//     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

$randomText = "sdfsdfsdf asda sd::2";
$randomText = "sdfsdfsdf asda 8.8.8.8";
$class = $string . '::' . $string;
$class = self::class.'::controller';
