<?php
session_set_cookie_params(0);
session_set_cookie_params(42, "/path"); // Noncompliant {{Set "lifetime" parameter to "0".}}
//                        ^^
session_set_cookie_params(42); // Noncompliant
session_set_cookie_params($someValue);
session_set_cookie_params();
session_set_cookie_params(path:42);
session_set_cookie_params(path:42, lifetime:42); // Noncompliant
$x->session_set_cookie_params(42);
unrelated_function(42);
