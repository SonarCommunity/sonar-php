<?php

function fun1($a, $c) {
}
function fun2($a, $c) {
}
function fun3($a, $c) {
//       ^^^^> {{original implementation}}
  echo $a.$c;
  echo $a.$c;
}
function fun4($a, $c) { // Noncompliant {{Update this method so that its implementation is not identical to "fun3" on line 7.}}
//       ^^^^
  echo $a.$c;
  echo $a.$c;
}
function fun5($a, $c) {
  echo $a;
}
function fun6($a, $c) { // OK, not raising if there is less than 2 statements
  echo $a;
}

class A {
    public $var = 'a default value';

    public function displayVar() {
        echo $this->var;
    }
}

class B {
    public $var = 'a default value';

    public function displayVar() {
    //              ^^^^^^^^^^>
        echo $this->var;
        echo $this->var;
    }

    public function displayVar2() { // Noncompliant {{Update this method so that its implementation is not identical to "displayVar" on line 35.}}
    //              ^^^^^^^^^^^
        echo $this->var;
        echo $this->var;
    }

    public function displayVarSmall() {
        echo $this->var;
    }

    public function displayVarSmall2() { // OK, not raising if there is less than 2 statements
        echo $this->var;
    }

    abstract function toto();
    function nesting() {
       $this->profile = new class {
                   public function displayVar() {
                       echo $this->var;
                       echo $this->var;
                   }
                   public function displayVar2() { // Noncompliant {{Update this method so that its implementation is not identical to "displayVar" on line 58.}}
                       echo $this->var;
                       echo $this->var;
                   }
                   public function displayVar3() { // Noncompliant {{Update this method so that its implementation is not identical to "displayVar" on line 58.}}
                      echo $this->var;
                      echo $this->var;
                   }
                   public function displayVarSmall() {
                       echo $this->var;
                   }
                   public function displayVarSmall2() { // OK, not raising if there is less than 2 statements
                       echo $this->var;
                   }
                };
    }

}
class C {
   private $x;
   private $y;

 public function __call($method, $args)
    {
        throw new \LogicException('Unexpected method call');
    }
    public static function __callStatic($method, $args)
    {
        throw new \LogicException('Unexpected method call');
    }

    public function getXprop() {
       return $x;
    }
    public function getYprop() { // Noncompliant
       return $x;
    }
    public function setXprop($x) {
       $this->x = $x;
    }
    public function setYprop($x) { // Noncompliant
       $this->x = $x;
    }
    public function isXprop() {
       return $x == 0;
    }
    public function isYprop() { // Noncompliant
       return $x == 0;
    }

    public function emptyMethod() {
    }
    public function isEmptyMethod() {
    }
}

function myFunction() : A
{
    something();
    return null;
}

function myFunction() : array
{
    something();
    return null;
}

function myFunction($a, $lot, $of, $params) : array
{
    something();
    return null;
}

function myFunction(int $a, bool $lot, $of, $params) : array // Noncompliant
{
    something();
    return null;
}

function myFunction(int $a, bool $lot, $of) : array
{
    something();
    return null;
}

function myFunctionSmall($a, $lot, $of, $params, $again) : array
{
    return null;
}

function myFunctionSmall($a, $lot, $of, $params, $again) : array // OK, not raising if there is less than 2 statements
{
    return null;
}

class D {
    public function getAprop() {
       return null;
    }
    public function getA2prop() {
       return null;
    }

    public function getXprop() {
       return [];
    }
    public function getX2prop() {
       return [];
    }

    public function getYprop() {
       throw new \LogicException("Not implemented");
    }
    public function getY2prop() {
       throw new \LogicException("Not implemented");
    }

    public function getZprop() {
       return new C();
    }
    public function getZ2prop() {
       return new C();
    }
}
