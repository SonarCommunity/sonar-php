<?php
if ($x == 1) {
// ^^^^^^^^^> {{This branch duplicates the one on line 5.}}
} elseif ($x == 2) {
} elseif ($x == 1) { // Noncompliant {{This branch duplicates the one on line 2.}}
//       ^^^^^^^^^
} elseif ($x == 1) { // Noncompliant {{This branch duplicates the one on line 2.}}
}
if ($x == 1) {
} else if ($x == 2) {
} else if ($x == 2) { // Noncompliant {{This branch duplicates the one on line 10.}}
}

switch($i) {
  case 1:
//     ^> {{This case duplicates the one on line 20.}}
    break;
  case 3:
    break;
  case 1:  // Noncompliant {{This case duplicates the one on line 15.}}
//     ^
    break;
  default:
    break;
}
