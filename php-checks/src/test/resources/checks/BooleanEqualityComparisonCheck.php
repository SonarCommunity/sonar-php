<?php

!true;            // NOK
!false;           // NOK
a == false;       // NOK
a == true;        // NOK
a === false;      // NOK
a === true;       // NOK
a != false;       // NOK
a != true;        // NOK
a !== false;      // NOK
a !== true;       // NOK
false == a;       // NOK
true == a;        // NOK
false != a;       // NOK
true != a;        // NOK
false && foo();   // NOK
foo() || true;    // NOK

a == foo(true);   // OK
true < 0;         // OK
~true;            // OK
++ true;          // OK
!foo;             // OK
foo() && bar();   // OK
a == true == b;   // OK
