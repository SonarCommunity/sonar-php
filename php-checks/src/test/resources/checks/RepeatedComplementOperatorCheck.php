<?php

$c = !!$a; // Noncompliant
$d = ~~$b; // Noncompliant

$c = !(!$a); // Noncompliant
$d = ~(~$b); // Noncompliant

$c = !((!$a)); // Noncompliant
$d = ~((~$b)); // Noncompliant
$c = !(((!$a))); // Noncompliant

$c = !$a; // Compliant
$d = ~$b; // Compliant

$c = !(!$a && $e); // Compliant
$d = ~(~$b && $e); // Compliant
$d = ~($b && ~$e); // Compliant
$c = !!$a && $b; // Noncompliant

$c = !$a <=> $b; // Compliant
$c = !$a & $b; // Compliant
$c = !$a ?? $b; // Compliant

$c = --$b; // Compliant
$c = -(-$b); // Noncompliant
$c = -(--$b); // Compliant
$c = -(-$b && $c); // Compliant
