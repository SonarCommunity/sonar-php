<?php

namespace checks\regex;

class DuplicatesInCharacterClassCheck
{

  function noncompliant($input)
  {
    preg_match("/[0-99]/", $input); // Noncompliant {{Remove duplicates in this character class.}}
    preg_match("/[90-9]/", $input); // Noncompliant
    preg_match("/[0-73-9]/", $input); // Noncompliant
    preg_match("/[0-93-57]/", $input); // Noncompliant
    preg_match("/[4-92-68]/", $input); // Noncompliant
    preg_match("/[0-33-9]/", $input); // Noncompliant
    preg_match("/[0-70-9]/", $input); // Noncompliant
    preg_match("/[3-90-7]/", $input); // Noncompliant
    preg_match("/[3-50-9]/", $input); // Noncompliant
    preg_match("/[xxx]/", $input); // Noncompliant
    preg_match("/[A-z_]/", $input); // Noncompliant
    preg_match("/(?i)[A-Za-z]/", $input); // Noncompliant
    preg_match("/(?i)[A-_d]/", $input); // Noncompliant
    preg_match("/(?iu)[Ä-Üä]/", $input); // Noncompliant
    preg_match("/(?iu)[a-Öö]/", $input);// Noncompliant
    preg_match("/[  ]/", $input); // Noncompliant
    preg_match("/(?i)[  ]/", $input); // Noncompliant
    preg_match("/(?iu)[  ]/", $input); // Noncompliant
    preg_match("/(?i)[A-_D]/", $input); // Noncompliant
    preg_match("/(?iu)[A-_D]/", $input); // Noncompliant
    preg_match("/(?i)[xX]/", $input); // Noncompliant
    preg_match("/(?iu)[äÄ]/", $input); // Noncompliant
    preg_match("/(?iU)[äÄ]/", $input); // Noncompliant
    preg_match("/(?iu)[xX]/", $input); // Noncompliant
    preg_match("/[\"\".]/", $input); // Noncompliant
    preg_match("/[\x{F600}-\x{F637}\x{F608}]/", $input); // Noncompliant
    preg_match("/[\Qxx\E]/", $input); // Noncompliant
    preg_match("/[[a][a]]/", $input); // Noncompliant
    preg_match("/[[abc][b]]/", $input); // Noncompliant
    preg_match("/[[^a]b]/", $input); // Noncompliant
    preg_match("/[[^a]z]/", $input); // Noncompliant
    preg_match("/[a[^z]]/", $input); // Noncompliant
    preg_match("/[z[^a]]/", $input); // Noncompliant
    preg_match("/[\s\Sx]/", $input); // Noncompliant
    preg_match("/(?U)[\s\Sx]/", $input); // Noncompliant
    preg_match("/[\w\d]/", $input); // Noncompliant
    preg_match("/[\wa]/", $input); // Noncompliant
    preg_match("/[\d1]/", $input); // Noncompliant
    preg_match("/[\d1-3]/", $input); // Noncompliant
    preg_match("/(?U)[\wa]/", $input); // Noncompliant
    preg_match("/[A-Za-z]/i", $input); // Noncompliant
    preg_match("/[0-9\d]/", $input); // Noncompliant
    preg_match("/[0-9\\d]/", $input); // Noncompliant
    preg_match("/[0-9\\\\\d]/", $input); // Noncompliant
  }

  function compliant($input)
  {
    preg_match("/a-z\d/", $input);
    preg_match("/[0-9][0-9]?/", $input);
    preg_match("/[xX]/", $input);
    preg_match("/[\s\S]/", $input);
    preg_match("/[[^\s\S]x]/", $input);
    preg_match("/(?U)[\s\S]/", $input);
    preg_match("/(?U)[\S\u0085\u2028\u2029]/", $input);
    preg_match("/[\d\D]/", $input);
    preg_match("/(?U)[\d\D]/", $input);
    preg_match("/[\w\W]/", $input);
    preg_match("/(?U)[\w\W]/", $input);
    preg_match("/[\wä]/", $input);
    preg_match("/(?i)[äÄ]/", $input);
    preg_match("/(?i)[Ä-Üä]/", $input);
    preg_match("/(?u)[äÄ]/", $input);
    preg_match("/(?u)[xX]/", $input);
    preg_match("/[ab-z]/", $input);
    preg_match("/[[a][b]]/", $input);
    preg_match("/[[^a]a]/", $input);
    preg_match("/(?i)[a-Öö]/", $input);
    preg_match("/[0-9\Q.-_\E]/", $input); // This used to falsely interpret .-_ as a range and complain that it overlaps with 0-9
    preg_match("/[A-Z\Q-_.\E]/", $input);
    preg_match("/[\x00\x01]]/", $input); // This used to falsely complain about x and 0 being duplicates
    preg_match("/[\x00-\x01\x02-\x03]]/", $input);
    preg_match("/[z-a9-0]/", $input); // Illegal character class should not make the check explode
    preg_match("/[aa/", $input); // Check should not run on syntactically invalid regexen
    preg_match("/(?U)[\wä]/", $input); // False negative because we don't support Unicode characters in \w and \W
    preg_match("/(?U)[[^\W]a]/", $input); // False negative because once we negate a character class whose contents we don't
                                  // fully understand, we ignore it to avoid false positives
    preg_match("/[[a-z&&b-e]c]/", $input); // FN because we don't support intersections
    preg_match("/(?i)[A-_d-{]/", $input); // FN because we ignore case insensitivity unless both ends of the ranges are letters
    preg_match("/(?i)[A-z_]/", $input); // FN because A-z gets misinterpreted as A-Za-z due to the way we handle case insensitivity
    preg_match("/[\p{Armenian}x]/", $input); // FN because we don't support \p at the moment
    preg_match("/[\\\\abc]/", $input);
    preg_match('/[ \s \' " \: \{ \} \[ \] , & \* \# \?]/x', $input);
    preg_match("/[0-9\\\d]/", $input); // Compliant
    preg_match("/[[:alnum:]alnum]/", $input);
  }

  function emoji($input) {
    preg_match("/[😂😊]/", $input); // Compliant
    preg_match("/[^\ud800\udc00-\udbff\udfff]/", $input); // Compliant
  }
}
