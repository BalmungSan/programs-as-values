package lambda

object Program {
  private final case class Continue[+A](value: A) extends Program[A]
  private final case object Halt extends Program[Nothing]

  def continue[A](value: A): Program[A] = Continue(value)
  def halt: Program[Nothing] = Halt
}

sealed trait Program[+A] {
  import Program.{Continue, Halt}

  def transformOutput[B](f: A => B): Program[B] = this match {
    case Continue(value) => Continue(f(value))
    case Halt => Halt
  }

  def zip[B](that: Program[B]): Program[(A, B)] = this match {
    case Continue(a) => that.transformOutput(b => (a, b))
    case Halt => Halt
  }

  def chainProgram[B](f: A => Program[B]): Program[B] = this match {
    case Continue(value) => f(value)
    case Halt => Halt
  }

  def recover[B >: A](fallback: Program[B]): Program[B] = this match {
    case Continue(value) => Continue(value)
    case Halt => fallback
  }

  def resultOr[B >: A](default: B): B = this match {
    case Continue(value) => value
    case Halt => default
  }
}

object Example {
  def num(n: Int): Program[Int] =
    Program.continue(n)

  def duplicate(p: Program[Int]): Program[Int] =
    p.transformOutput(n => n * 2)

  def divide(p1: Program[Int], p2: Program[Int]): Program[Int] =
    p1.zip(p2).chainProgram {
      case (n1, n2) =>
        if (n2 != 0) Program.continue(n1 / n2)
        else Program.halt
    }

  def main(args: Array[String]): Unit = {
    // ((x / y) / z) * 2
    def equation(x: Int, y: Int, z: Int): Program[Int] =
      duplicate(divide(
        divide(num(x), num(y)),
        num(z)
      ))

    def run(x: Int, y: Int, z: Int): Int =
      equation(x, y, z).resultOr(-1)

    println(s"The result is: ${run(100, 10, 5)}")
  }
}
