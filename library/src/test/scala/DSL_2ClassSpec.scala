import org.specs2._

class DSL_2ClassSpec extends DSLSpec { def is =                               s2"""
  This is a specification to check Treehugger DSL

  Class definitions are written as
    `CLASSDEF(sym|"C")`, or                                                   $class1
    with the class body as `CLASSDEF(sym|"C") := BLOCK(stat, ...)`.           $class2
    `CLASSDEF(sym|"C") withParams(PARAM("x", typ1), VAL("y", typ2), VAR("z": typ3), ...)`
where `PARAM(...)` declares a parameter while 
`VAL(...)` and `VAR(...)` declare parameters with an accessor.                $class3
  
  Polymorphic classes are written as
    `CLASSDEF(sym|"C") withTypeParams(TYPEVAR(typ|"C"))`.                     $class4
  
  Classes with base classes are written as
    `CLASSDEF(sym|"C") withParents(typ|"B", ...)`.                            $class5
  
  Using `withFlags(flag, ...)`, classes with access modifier can be written as
    `CLASSDEF(sym|"C") withFlags(Flags.PRIVATE)`.                             $class6
  Other uses of `withFlags(flag)` are abstract classes withFlags(Flags.ABSTRACT)`,
final classes `withFlags(Flags.FINAL)`,
sealed classes `withFlags(Flags.SEALED)`.                                     $class7
  
  Private constructors are written as
    `CLASSDEF(sym|"C") withCtorFlags(Flags.PRIVATE)`.                         $class8
  
  Self type annotations are written as
    `CLASSDEF(sym|"C") withSelf(sym|"self",typ)`.                             $class9

  Constructor definitions are written as
    `DEFTHIS withParams(PARAM("x", typ1), ...) := tree`                       $constructor1

  Case class definitions are written as
    `CASECLASSDEF(sym|"C")`, or with the class body, parameters, and parents as
`CASECLASSDEF(sym|"C")` withParams(PARAM("x", typ1), ...) withParents(typ, ...) := BLOCK(stat, ...). $caseclass1

  Trait definitions are written as
    `TRAITDEF(sym|"D")`.                                                      $trait1

  Object definitions are written as
    `OBJECTDEF(sym|"E")`.                                                     $object1

  Case object definitions are written as
    `CASEOBJECTDEF(sym|"E")`.                                                 $object2

  Class members can
    be defined by placing value defitions and function definitions within the class body as
`CLASSDEF(sym|"C") := BLOCK(DEF(sym|"get", typ) := rhs, ...)`.                $member1
  
  Class members with access modifier can be written as
    `DEF(sym|"get", typ|"Int") withFlags(Flags.PROTECTED) := rhs`,            $member2
    `DEF(sym|"get", typ|"Int") withFlags(Flags.OVERRIDE) := rhs`,             $member3
    `DEF(sym|"get", typ|"Int") withFlags(PRIVATEWITHIN("this")) := rhs`.      $member4

  Early definitions can be written as
    `CLASSDEF(sym|"C") withEarlyDefs(stat, ...) withParents(typ, ...) := BLOCK(stat, ...)`. $early1
                                                                              """
  
  import treehugger.forest._
  import definitions._
  import treehuggerDSL._
                                                                             
  def class1 = (CLASSDEF("C"): Tree) must print_as("class C")
  
  def class2 =
    (CLASSDEF("C") := BLOCK(
      VAL("x") := LIT(0)
    )) must print_as(
      """class C {""",
      """  val x = 0""",
      """}"""
    )
  
  def class3 = {
    val tree: Tree = CLASSDEF("C") withParams(PARAM("x", IntClass),
      VAL("y", StringClass), VAR("z", TYPE_LIST(StringClass)))
    tree must print_as("class C(x: Int, val y: String, var z: List[String])")
  }
  
  def class4 =
    ((CLASSDEF("Queue") withTypeParams(TYPEVAR("T"))
      withParams(VAL("leading", TYPE_LIST("T")), VAL("trailing", TYPE_LIST("T"))): Tree) must print_as("class Queue[T](val leading: List[T], val trailing: List[T])")) and
    ((CLASSDEF("C") withTypeParams(TYPEVAR("A") :: TYPEVAR("B") :: Nil)).tree must print_as("class C[A, B]"))
  
  def class5 = (CLASSDEF("C") withParents(sym.Addressable): Tree) must print_as("class C extends Addressable")
    
  def class6 = (CLASSDEF("C") withFlags(Flags.PRIVATE): Tree) must print_as("private class C")
    
  def class7 =
    ((CLASSDEF("C") withFlags(Flags.ABSTRACT): Tree) must print_as("abstract class C")) and
    ((CLASSDEF("C") withFlags(Flags.FINAL): Tree) must print_as("final class C")) and
    ((CLASSDEF("C") withFlags(Flags.SEALED): Tree) must print_as("sealed class C"))
  
  def class8 =
    (CLASSDEF("C") withCtorFlags(Flags.PRIVATE)
      withParams(PARAM("x", IntClass)): Tree) must print_as("class C private (x: Int)")

  def class9 =
    (CLASSDEF("C") withSelf("self", "T1", "T2") := BLOCK(
      VAL("x") := REF("self")
    )) must print_as(
      "class C { self: T1 with T2 => ",
      "  val x = self",
      "}"
    )

  def constructor1 =
    (CLASSDEF("C")
      withParams(PARAM("s", StringClass)) := BLOCK(
      DEFTHIS withParams(PARAM("x", IntClass)) := BLOCK(
        THIS APPLY(REF("x") TOSTRING)
      )
    )) must print_as(
      "class C(s: String) {",
      "  def this(x: Int) = {",
      "    this(x.toString)",
      "  }",
      "}"
    )

  def caseclass1 =
    ((CASECLASSDEF("C"): Tree) must print_as("case class C()")) and
    ((CASECLASSDEF("C") withParams(VAL("x", IntClass) withFlags(Flags.OVERRIDE)) withParents(sym.Addressable APPLY(REF("x"))) := BLOCK(
      DEF("y") := LIT(0)
    ))
      must print_as(
        """case class C(override val x: Int) extends Addressable(x) {""",
        """  def y = 0""",
        """}"""
      ))
  
  def trait1 = (TRAITDEF("D"): Tree) must print_as("trait D")
  
  def object1 =
    ((OBJECTDEF("E"): Tree) must print_as("object E")) and
    ((OBJECTDEF("E") withParents("T"): Tree) must print_as("object E extends T"))

  def object2 =
    ((CASEOBJECTDEF("E"): Tree) must print_as("case object E")) and
    ((CASEOBJECTDEF("E") withParents("T"): Tree) must print_as("case object E extends T"))

  def member1 =
    (CLASSDEF("C") := BLOCK(
      DEF("get") := LIT(0)
    )) must print_as(
      """class C {""",
      """  def get = 0""",
      """}"""
    )
  
  def member2 =
    (CLASSDEF("C") := BLOCK(
      DEF("get") withFlags(Flags.PROTECTED) := LIT(0)
    )) must print_as(
      """class C {""",
      """  protected def get = 0""",
      """}"""
    )
  
  def member3 =
    (CLASSDEF("C") := BLOCK(
      DEF("get") withFlags(Flags.OVERRIDE, Flags.FINAL) := LIT(0)
    )) must print_as(
      """class C {""",
      """  final override def get = 0""",
      """}"""
    )
  
  def member4 =
    (CLASSDEF("C") := BLOCK(
      DEF("get") withFlags(PRIVATEWITHIN("this")) := LIT(0)
    )) must print_as(
      """class C {""",
      """  private[this] def get = 0""",
      """}"""
    )
    
  def early1 =
    (CLASSDEF("C") withEarlyDefs(
      VAL("name") := LIT("Bob")
    ) withParents("B") := BLOCK(
      Predef_print APPLY REF("msg")
    )) must print_as(
    "class C extends {",
    "  val name = \"Bob\"",
    "} with B {",
    "  print(msg)",
    "}"
  )
}
