package fdm

/**
 * Sometimes we don't want to take the time to model data precisely. For example, we might want to
 * model an email address with a string, even though most strings are not valid email addresses.
 *
 * In such cases, we can save time by using a smart constructor, which lets us ensure we model
 * only valid data, but without complicated data types.
 */
object smart_constructors {

  // Security code: 3 digits
  // Digit: 0 - 9
  case class SecurityCode(digit1: Digit, digit2: Digit, digit3: Digit)
  sealed trait Digit {
    case object _0 extends Digit
    case object _1 extends Digit
    case object _2 extends Digit
    case object _3 extends Digit
    case object _4 extends Digit
    case object _5 extends Digit
    case object _6 extends Digit
    case object _7 extends Digit
    case object _8 extends Digit
    case object _9 extends Digit
  }

  // How to model this in a different way: Refine (smarts constructor library)

  sealed abstract case class Email private (value: String)
  object Email {
    def fromString(email: String): Option[Email] =
      if (email.matches("""/\w+@\w+.com""")) Some(new Email(email) {}) else None

    def processEmail(email: Email): Unit = ???
  }

  /**
   * EXERCISE 1
   *
   * Create a smart constructor for `NonNegative` which ensures the integer is always non-negative.
   */
  sealed abstract case class NonNegative private (value: Int)
  object NonNegative {
    def fromInteger(value: Int): Option[NonNegative] =
      if (value >= 0) new Some(new NonNegative(value) {}) else None
  }

  /**
   * EXERCISE 2
   *
   * Create a smart constructor for `Age` that ensures the integer is between 0 and 120.
   */
  sealed abstract case class Age private (value: Int)
  object Age {
    def fromInteger(value: Integer): Option[Age] =
      if (value >= 0 && value <= 120) Some(new Age(value) {}) else None
  }

  /**
   * EXERCISE 3
   *
   * Create a smart constructor for password that ensures some security considerations are met.
   */
  sealed abstract case class Password private (value: String)

  object Password {
    def fromCondition(value: String, condition: String => Boolean): Option[Password] =
      if (condition(value)) Some(new Password(value) {}) else None
  }

  object SolutionLambon {
    sealed trait PasswordRule {
      def fulfills(s: String): Boolean
    }
    object PasswordRule {
      final case class MinLength(n: Int) extends PasswordRule {
        override def fulfills(s: String): Boolean = s.length() >= n
      }
      final class hasDigit extends PasswordRule {
        override def fulfills(s: String): Boolean = s.matches(".*[0-9].*")
      }
    }

    sealed abstract case class Password private (value: String)
    object Password {
      def checkPwd(candidate: String, rules: PasswordRule*): Option[Password] = {

        val isOk = rules.nonEmpty &&
          rules.foldLeft(true) { case (cur, rule) => cur && rule.fulfills(candidate) }

        if (isOk) Some(new Password(candidate) {}) else None
      }
    }
  }
}

object applied_smart_constructors {

  /**
   * EXERCISE 1
   *
   * Identify the weaknesses in this data type, and use smart constructors (and possibly other
   * techniques) to correct them.
   */
  final case class BankAccount(id: String, name: String, balance: Double, opened: java.time.Instant)

  object MySolution {
    sealed abstract case class SafeBankAccount private (
      id: SafeBankAccount.BankAccountId,
      name: SafeBankAccount.BankAccountName,
      balance: SafeBankAccount.Balance,
      opened: java.time.Instant
    )
    object SafeBankAccount {
      final case class BankAccountId(id: String)
      final case class Balance(balance: Double)
      final case class BankAccountName(name: String)

      // Only if there are cross validations between fields
      // This smart constructor is not necessarily needed
      def buildBankAccount(id: String, name: String, balance: Double, opened: java.time.Instant): Unit =
        if (balance < 0) None
        else if (name == "") None
        else if (id == "") None
        else new SafeBankAccount(BankAccountId(id), BankAccountName(name), Balance(balance), opened) {}
    }
  }

  object DGoesSolution {
    final case class BankAccount(id: BankAccountId, name: String, balance: Double, opened: java.time.Instant)
    abstract case class BankAccountId private (value: String)
    object BankAccountId {
      def fromString(id: String): Option[BankAccountId] = ???
    }
  }
  object AnotherSolution {
    final case class BankAccount(id: BankAccId, name: String, balance: Double, opened: java.time.Instant)

    sealed abstract case class BankAccId private (s: String)
    object BankAccId {
      def fromString(s: String): Option[BankAccId] =
        if (s.length != 10) None else Some(new BankAccId(s) {})
    }
  }

  /**
   * EXERCISE 2
   *
   * Identify the weaknesses in this data type, and use smart constructors (and possibly other
   * techniques) to correct them.
   */
  final case class Person(age: Int, name: String, salary: Double)
  sealed abstract case class SafePerson private (age: SafePerson.Age, name: String, salary: SafePerson.Salary)
  object SafePerson {
    final case class Age(age: Int)
    final case class Salary(salary: Double)
    def buildPerson(age: Int, name: String, salary: Double): Option[SafePerson] =
      if (age < 0 | age > 120) None
      else if (salary <= 0) None
      else Some(new SafePerson(Age(age), name, Salary(salary)) {})
  }

  /**
   * EXERCISE 3
   *
   * Identify the weaknesses in this data type, and use smart constructors (and possibly other
   * techniques) to correct them.
   */
  final case class SecurityEvent(machine: String, timestamp: String, eventType: Int)
  object EventType {
    val PortScanning    = 0
    val DenialOfService = 1
    val InvalidLogin    = 2
  }

  object Answer {

    sealed abstract case class SecurityEvent private (
      machine: Machine,
      timestamp: java.time.Instant,
      eventType: SecurityEvent.EventType
    )
    case class Machine(name: String)

    object SecurityEvent {
      sealed trait EventType
      object PortScanning    extends EventType
      object DenialOfService extends EventType
      object InvalidLogin    extends EventType =

      def buildEvent(machine: String, eventType: EventType): SecurityEvent =
        new SecurityEvent(Machine(machine), java.time.Instant.now(), eventType) {}
    }
  }
}
