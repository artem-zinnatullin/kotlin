package Jet86

class A {
  <!REDECLARATION!>class object<!> {
    val x = 1
  }
  <!MANY_CLASS_OBJECTS, REDECLARATION!>class object<!> {
    val x = 1
  }
}

class AA {
  class object {
    val x = 1
  }
  <!MANY_CLASS_OBJECTS!>class object A<!> {
    val x = 1
  }
    <!MANY_CLASS_OBJECTS!>class object AA<!> {
    val x = 1
  }
}

class B() {
  val x = 12
}

object b {
  <!CLASS_OBJECT_NOT_ALLOWED!>class object<!> {
    val x = 1
  } // error
}

val a = A.x
val c = B.<!UNRESOLVED_REFERENCE!>x<!>
val d = b.<!UNRESOLVED_REFERENCE!>x<!>

val s = <!NO_CLASS_OBJECT!>System<!>  // error
fun test() {
  System.out.println()
  java.lang.System.out.println()
}