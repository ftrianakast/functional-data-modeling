package fdm

/**
 * Some anti-patterns emerge when doing data modeling in Scala. Learning to spot and refactor such
 * anti-patterns is a very valuable skill that will keep data models precise and easy to maintain
 * in the face of changing business requirements.
 *
 */
/**
 * One anti-pattern common in object-oriented data modeling technique is to define different
 * interfaces for different aspects of a data model, relying on inheritance (which is a form of
 * type intersection) to combine multiple aspects into a new type. While this approach is flexible,
 * it's often _too_ flexible, allowing combinations that don't make sense, and making it hard to
 * deal with combinations in a principled way.
 *
 * In this case, refactoring to enumerations (sealed traits _without_ overlaps) and case classes
 * can not only disallow combinations that don't make sense, but give us a principled way to
 * handle all the combinations that do make sense.
 */
object eliminate_intersection {
  trait Event {
    def eventId: String
  }
  trait UserEvent extends Event {
    def userId: String
  }
  trait TimestampedEvent extends Event {
    def timestamp: java.time.Instant
  }
  trait DeviceEvent extends Event {
    def deviceId: String
  }

  object adt {

    /**
     * EXERCISE 1
     *
     * Create a pure case class / enumeration model of `Event`, which permits events which are
     * user events OR device events (but NOT both), and which permits events that have timestamps
     * or lack timestamps; but which always have event ids.
     */
    object MySolution {
      sealed trait Event
      object Event {
        final case class UserEvent(id: String, userId: String, timedNotion: TimedNotion)     extends Event
        final case class DeviceEvent(id: String, deviceId: String, timedNotion: TimedNotion) extends Event
      }

      sealed trait TimedNotion
      object TimedNotion {
        final case class Timed(time: java.time.Instant) extends TimedNotion
        object NonTimed                                 extends TimedNotion
      }
    }

    object AnotherSolution {
      sealed trait Event {
        val userId: String
        val timestamp: Option[java.time.Instant]
      }

      object Event {
        case class UserEvent(userId: String, timestamp: Option[java.time.Instant])   extends Event
        case class DeviceEvent(userId: String, timestamp: Option[java.time.Instant]) extends Event
      }
    }
  }

}

/**
 * Another anti-pattern is when all cases of an enumeration share the same field, with the same
 * type and the same meaning. This duplication makes maintenance of the hierarchy more difficult,
 * and also makes it difficult to take advantage of built-in functionality like the `copy` method
 * that comes for free with all case classes.
 *
 * In this case, we can apply an "extract product" refactoring, which turns the enumeration into a
 * case class, and pushes the differences between the cases deeper, into a field of the case class
 * (modeled with a new enumeration).
 */
object extract_product {

  /**
   * EXERCISE 1
   *
   * The following cases of the `AdvertisingEvent` enum all share the `pageUrl` and `data` fields,
   * which have the same type and meaning in each case. Apply the _extract product_ refactoring so
   * that  `AdvertisingEvent` becomes a case class, which stores `pageUrl` and `data`, and
   * introduce a new field called `eventType`, which captures event-specific details for the
   * different event types.
   */
  sealed trait AdvertisingEvent
  object AdvertisingEvent {
    case object None                                                  extends AdvertisingEvent
    final case class Impression(eventType: EventType)                 extends AdvertisingEvent
    final case class Click(elementId: String, eventType: EventType)   extends AdvertisingEvent
    final case class Action(actionName: String, eventType: EventType) extends AdvertisingEvent
    final case class EventType(pageUrl: String, data: String)
  }

  /**
   * EXERCISE 2
   *
   * The following cases of `Card` enum all share the `points` field, and this field has the same
   * type and meaning in each case. Apply the _extract product_ refactoring so that `Card` becomes a
   * case class, and introduce a new field called `cardType`, which captures card-specific details
   * for the different event types.
   */
  final case class Card(points: Int, cardType: CardType)

  sealed trait CardType
  object CardType {
    final object Clubs    extends CardType
    final object Diamonds extends CardType
    final object Spades   extends CardType
    final object Hearts   extends CardType
  }

  /**
   * EXERCISE 3
   *
   * Apply the _extract product_ refactoring to `Event`.
   */
  sealed trait Event
  object Event {
    final case class UserPurchase(userId: String, amount: Double, timestamp: java.time.Instant)        extends Event
    final case class UserReturn(userId: String, itemId: String, timestamp: java.time.Instant)          extends Event
    final case class SystemRefund(orderId: String, refundAmount: Double, timestamp: java.time.Instant) extends Event
  }

  object Exercise3Solution {
    final case class Event(timestamp: java.time.Instant, eventType: EventType)
    sealed trait EventType
    object EventType {
      final case class UserPurchase(userId: String, amount: Double)        extends EventType
      final case class UserReturn(userId: String, itemId: String)          extends EventType
      final case class SystemRefund(orderId: String, refundAmount: Double) extends EventType
    }
  }
}

/**
 * Another anti-pattern occurs when many fields of a case class are optional (or `null`). This
 * indicates there is a "missing enumeration" that can be extracted from the case class, which
 * bundles fields that always appear together.
 *
 * In this case, the "extract sum" refactoring involves identifying patterns of optional fields
 * that always appear together, and pulling them out into the cases of a new enumeration.
 */
object extract_sum {
  final case class Job(title: String, salary: Double)

  final case class Enrollment(university: String, credits: Int, year: java.time.YearMonth)

  /**
   * EXERCISE 1
   *
   * Extract out a missing enumeration from the following data type.
   */
  final case class Person(
    name: String,
    job: Option[Job],                                // employed
    employmentDate: Option[java.time.LocalDateTime], // if employed
    school: Option[Enrollment]                       // full-time student
  )

  object MeineSolution {
    final case class Person(name: String, employment: Employment)
    sealed trait Employment
    object Employment {
      final case class Employed(job: Job, employmentData: java.time.LocalDateTime) extends Employment
      final case class Student(school: Enrollment)                                 extends Employment
    }
  }

  /**
   * EXERCISE 2
   *
   * Extract out a missing enumeration from the following data type.
   */
  final case class Event(
    deviceId: Option[String], // sensor initiated event
    userId: Option[String],   // user initiated event
    timestamp: java.time.Instant,
    reading: Option[Double], // for sensors
    click: Option[String],   // for user click events
    purchase: Option[String] // for user order events
  )

  object EventSolution {
    final case class Event(timestamp: java.time.Instant, eventType: EventType)
    sealed trait EventType
    object EventType {
      final case class UserEvent(userId: String, eventSource: EventSource) extends EventType
      final case class SensoredEvent(reading: Double)                      extends EventType
    }

    sealed trait EventSource
    object EventSource {
      final case class OrderedEvent(event: String) extends EventSource
      final case class SourcedEvent(event: String) extends EventSource
    }
  }

  object EventSolutionAnother {
    final case class Event[A](
      eventSource: EventSource,
      timestamp: java.time.Instant,
      eventType: EventType[A]
    )

    sealed trait EventSource
    object EventSource {
      final case class Device(id: String) extends EventSource
      final case class User(id: String)   extends EventSource
    }

    sealed trait EventType[A]
    object EventType {
      final case class Reading(value: Double)  extends EventType[EventSource.Device]
      final case class Click(value: String)    extends EventType[EventSource.User]
      final case class Purchase(value: String) extends EventType[EventSource.User]
    }
  }

  /**
   * EXERCISE 3
   *
   * Extract out a missing enumeration from the following data type.
   */
  final case class CreditCard(
    digit16: Option[Digit16],      // For VISA
    digit15: Option[Digit15],      // For AMEX
    securityCode4: Option[Digit4], // For VISA
    securityCode3: Option[Digit3]
  )

  final case class Digit16(group1: Digit4, group2: Digit4, group3: Digit4, group4: Digit4)
  final case class Digit15(group1: Digit4, group2: Digit4, group3: Digit4, group4: Digit3)
  final case class Digit4(v1: Digit, v2: Digit, v3: Digit, v4: Digit)
  final case class Digit3(v1: Digit, v2: Digit, v3: Digit)

  object CreditCardSolution {
    final case class CreditCard(creditCardType: CreditCardType)
    sealed trait CreditCardType
    object CreditCardType {
      final case class Visa(digit16: Digit16, securityCode: Digit4) extends CreditCardType
      final case class Amex(digit15: Digit15, securityCode: Digit3) extends CreditCardType
    }
  }

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
}

/**
 * Another anti-pattern is when a code base has (unsealed) traits that are used for storing data.
 * Such code that encourage runtime type checking using `isInstanceOf` or equivalent, which is
 * neither safe, nor easy to maintain as new subtypes are introduced into the code base.
 *
 * In this case, we can apply a "seal trait" refactoring, which involves sealing the data trait,
 * and relocating all classes that implement the trait into the same file, together with the trait.
 * This will ensure we match against all subtypes in a pattern match, making our code safer and
 * giving us compiler assistance to track down places we need to update when we add a new subtype.
 */
object seal_data_trait {

  /**
   * EXERCISE 1
   *
   * Apply the seal trait refactoring to the following `Document` data model, so the compiler will
   * know about and enforce
   */
  trait Document {
    def documentId: String
  }
  trait OwnedDocument extends Document {
    def ownerId: String
  }
  trait AnonymousDocument extends Document
  trait GroupOwnedDocument extends Document {
    def ownerIds: List[String]
  }

  object DocumentSolution {
    final case class Document(id: String, documentType: DocumentType)
    sealed trait DocumentType
    object DocumentType {
      final case class OwnedDocument(ownerId: String)               extends DocumentType
      final object AnonymusDocument                                 extends DocumentType
      final case class GroupedOwnedDocument(ownerIds: List[String]) extends DocumentType
    }
  }
}

/**
 * Wildcard pattern matches on enumerations that change frequently are an anti-pattern, because
 * when new cases are added, there is no way for the compiler to remind developers to update
 * business logic involving the enumeration, which leads to subtle bugs in application logic.
 *
 * In this case, we can apply a "eliminate wildcard" refactoring that involves explicitly matching
 * against all cases of an enumeration.
 */
object eliminate_wildcard {
  trait Phone
  def sendText(phone: Phone, message: String): Unit = println(s"Sending a text to ${phone}: ${message}")

  /**
   * EXERCISE 1
   *
   * Eliminate the wild card pattern match, and instead move to matching each case individually.
   */
  def pageDeveloper(event: Event, onCall: Phone): Unit =
    event match {
      case Event.ServerDown => sendText(onCall, "The server is down, please look into it right away!")
      case _                =>
    }

  object WildCardSolution1 {
    def pageDeveloper(event: Event, onCall: Phone): Unit =
      event match {
        case Event.ServerDown       => sendText(onCall, "The server is down, please look into it right away!")
        case Event.ServiceRestarted =>
        case Event.AnotherObject    =>
      }
  }

  /**
   * EXERCISE 2
   *
   * Add another type of event, such as `BillingOverage`, which might require immediate attention
   * from a devops engineer. The Scala compiler will make you update existing code safely.
   */
  sealed trait Event
  object Event {
    case object ServerDown       extends Event
    case object ServiceRestarted extends Event
    case object AnotherObject    extends Event
  }
}

/**
 * Another anti-pattern is when code uses pattern matching to perform a so-called "type case".
 * Type cases are when the runtime type of a value is checked, rather than deconstructing the
 * value using one of the cases of the enumeration. Matching cases of an enumeration helps ensure
 * that no implementation details leak into pattern matches, discourages using traits in any other
 * way than for enumerations, and helps give developers a chance to update logic when new
 * fields are added to case classes.
 */
object eliminate_typecase {
  sealed trait Event
  sealed trait IdentifiedEvent extends Event {
    def id: String
  }
  final case class Click(href: String)                extends Event
  final case class Purchase(id: String, item: String) extends IdentifiedEvent

  /**
   * EXERCISE 1
   *
   * Refactor this code to eliminate the type case. You may have to refactor the data model too.
   */
  def logIdentifiedEvents(event: Event): Unit =
    event match {
      case identified: IdentifiedEvent => println("User event: " + identified.id)
      case _                           =>
    }

  object MySolution {
    sealed trait Event
    object Event {
      final case class Click(href: String)                extends Event
      final case class Purchase(id: String, item: String) extends Event
    }
    def logIdentifiedEvents(event: Event): Unit =
      event match {
        case MySolution.Event.Click(_)        =>
        case MySolution.Event.Purchase(id, _) => println("User event: " + id)
      }
  }

}

/**
 * Another anti-pattern is creating many variables of the same type in a given scope by pattern
 * matching on recursive data structures and giving values of the same type different names.
 *
 * In this case, we can apply the "shadow variable" refactoring and deliberately choose to
 * shadow variables of the same name and type in outer scopes, reducing the possibility that we
 * accidentally refer to a variable in an outer scope.
 */
object nested_shadowing {

  /**
   * EXERCISE 1
   *
   * Identify the bug caused by too many variables of the same type in the same scope, and fix it
   * by applying the shadow variable refactoring.
   */
  def count[A](list: List[A]): Int =
    list match {
      case Nil       => 0
      case _ :: list => 1 + count(list)
    }

  sealed trait UserBehavior
  object UserBehavior {
    case object Purchase                                                 extends UserBehavior
    case object Return                                                   extends UserBehavior
    case object Anything                                                 extends UserBehavior
    final case class Sequence(first: UserBehavior, second: UserBehavior) extends UserBehavior
    final case class Not(behavior: UserBehavior)                         extends UserBehavior
  }

  import UserBehavior._

  /**
   * EXERCISE 2
   *
   * Identify the bug caused by too many variables of the same type in the same scope, and fix it
   * by applying the shadow variable refactoring.
   */
  def analyzePattern(b: UserBehavior): Boolean =
    b match {
      case Purchase => true
      case Return   => true
      case Anything => false
      case Sequence(first, second) =>
        analyzePattern(first) || analyzePattern(second)
      case Not(b) =>
        analyzePattern(b)
    }
}
