<?php
$x = new class1();

  new class1();               // NOK {{Either remove this useless object instantiation of class "class1" or use it}}
//^^^^^^^^^^^^

new class2;               // NOK {{Either remove this useless object instantiation of class "class2" or use it}}

new class1().method1();

$x;

class1();
