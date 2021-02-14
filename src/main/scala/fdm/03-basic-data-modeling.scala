package fdm

import java.io.File
import java.time.Instant

import scala.annotation.tailrec

/**
 * The following exercises test your ability to model various entities using case classes.
 */
object product_modeling {

  /**
   * EXERCISE 1
   *
   * Using a case class, create a model of a product, which has a name, description, and a price.
   *
   */
  final case class Product(name: String, description: String, price: BigDecimal)

  /**
   * EXERCISE 2
   *
   * Using a case class, create a model of a a user profile, which has a picture URL, and text-
   * based location (indicating the geographic area where the user is from).
   */
  final case class UserProfile(pictureUrl: String, location: String)

  /**
   * EXERCISE 3
   *
   * Using a case class, create a model of an item that can be posted on LinkedIn's feed. This
   * item contains a subject and some text.
   */
  final case class FeedItem(subject: String, text: String)

  /**
   * EXERCISE 4
   *
   * Using a case class, create a model of an event, which has an event id, a timestamp, and a
   * map of properties (String/String).
   */
  final case class Event(id: String, eventId: String, timeStamp: Instant, properties: Map[String, String])
}

/**
 * The following exercises test your ability to model various entities using enums.
 */
object sum_modeling {

  /**
   * EXERCISE 1
   *
   * Using an enum, create a model of a color, which could be `Red`, `Green`, `Blue`, or `Custom`,
   * and if `Custom`, then it should store `red`, `green`, and `blue` components individually, as
   * an integer (`Int`) value.
   */
  sealed trait Color
  object Color {
    case object Red                                 extends Color
    case object Green                               extends Color
    case object Blue                                extends Color
    final case class Custom(r: Int, g: Int, b: Int) extends Color
  }

  /**
   * EXERCISE 2
   *
   * Using an enum, create a model of an web event, which could be either a page load for a certain
   * URL, a click on a particular button, or a click to a specific URL.
   */
  sealed trait WebEvent
  object WebEvent {
    final case class PageLoad(url: String) extends WebEvent
    final object ButtonClick               extends WebEvent
    final case class UrlClick(url: String) extends WebEvent
  }

  /**
   * EXERCISE 3
   *
   * Using an enum, create a model of an age bracket, which could be baby, child, young adult,
   * teenager, adult, mature adult, or senior adult.
   */
  sealed trait AgeBracket
  object AgeBracket {
    case object Baby        extends AgeBracket
    case object Child       extends AgeBracket
    case object Teenager    extends AgeBracket
    case object YoungAdult  extends AgeBracket
    case object Adult       extends AgeBracket
    case object MatureAdult extends AgeBracket
    case object SeniorAdult extends AgeBracket
  }

  /**
   * EXERCISE 4
   *
   * Using an enum, create a model of a step in a JSON pipeline, which could be transform,
   * aggregate, or save to file.
   * aggregate.
   */
  type Json
  sealed trait JsonPipelineStep
  object JsonPipeline {
    final case class Transform(fn: Json => Json)                        extends JsonPipelineStep
    final case class Aggregate(initial: Json, fn: (Json, Json) => Json) extends JsonPipelineStep
    final case class SaveTofile(file: String)                           extends JsonPipelineStep
  }
}

/**
 * The following exercises test your ability to model various entities using both case classes and
 * enums.
 */
object mixed_modeling {

  /**
   * EXERCISE 1
   *
   * Using only case classes and enums, create a model of an order for an e-commerce platform, which
   * would consist of a number of items, each with a certain price, and an overall price, including
   * shipping and handling charges.
   */
  final case class Order(
    items: List[Item],
    overallPrice: BigDecimal,
    shippingCharge: BigDecimal,
    handlingCharge: BigDecimal
  )
  final case class Item(price: BigDecimal)

  object RandomGuySolution {
    type Currency
    case class Amount(value: Int, currency: Currency)
    case class OrderItem(price: Int, description: String)
    case class Order(items: Seq[OrderItem], totalPrice: Amount, charges: Seq[OrderCharge])

    sealed trait OrderCharge
    object OrderCharge {
      final case class Shipping(cost: Amount) extends OrderCharge
      final case class Handling(cost: Amount) extends OrderCharge
    }
  }

  /**
   * EXERCISE 2
   *
   * Using only case classes and enums, create a model of an `Email`, which contains a subject,
   * a body, a recipient, and a from address.
   */
  final case class Email(subject: String, body: String, recipient: List[Recipient], from: Address)
  final case class Address(email: String)
  final case class Recipient(address: Address)

  /**
   * EXERCISE 3
   *
   * Using only case classes and enums, create a model of a page layout for a content-management
   * system, which could consist of predefined elements, such as a news feed, a photo gallery,
   * and other elements, arranged in some well-defined way relative to each other.
   */
  object MySolution {
    sealed trait LayoutElement
    case object PredefinedElement extends LayoutElement
    case object NewsFeed          extends LayoutElement
    case object PhotoGallery      extends LayoutElement
  }

  object SomeSolution {
    sealed trait Element
    object Element {
      final case class NewsFeed()     extends Element
      final case class PhotoGallery() extends Element
    }
    final case class Placement(element: Element, vertical: Range, horizontal: Range)
    final case class PageLayout(elements: List[Placement])
  }

  object OtherPersonSolution {
    final case class PageLayout(elem: PageElement)

    sealed trait PageElement
    object PageElement {
      case object Empty                                 extends PageElement
      final case class Gallery()                        extends PageElement
      final case class NewsFeed()                       extends PageElement
      final case class Article()                        extends PageElement
      final case class Row(elements: List[PageElement]) extends PageElement
      final case class Col(elements: List[PageElement]) extends PageElement
    }

    // A page with a main part of some stuff and a news feed on the right
    lazy val layout = PageLayout(
      PageElement.Row(
        List(
          PageElement.Col(
            List(
              PageElement.Gallery(),
              PageElement.Article(),
              PageElement.Article()
            )
          ),
          PageElement.NewsFeed()
        )
      )
    )
  }

  object DeGoesSolution {
    sealed trait PageLayout
    object PageLayout {
      case object NewsFeed                                             extends PageLayout
      case object PhotoGallery                                         extends PageLayout
      final case class Horizontal(left: PageLayout, right: PageLayout) extends PageLayout
      final case class Vertical(top: PageLayout, bottom: PageLayout)   extends PageLayout
    }
  }

  /**
   * EXERCISE 4
   *
   * Using only case classes and enums, create a model of a rule that describes the conditions for
   * triggering an email to be sent to a shopper on an e-commerce website.
   */
  object MyIncorrectSolution {
    sealed trait EmailTriggerRule
    final case class SubjectContains(string: String)                      extends EmailTriggerRule
    final case class BodyContains(string: String)                         extends EmailTriggerRule
    final case class SenderIn(senders: Set[Address])                      extends EmailTriggerRule
    final case class RecipientIn(recipients: Set[Address])                extends EmailTriggerRule
    final case class And(left: EmailTriggerRule, right: EmailTriggerRule) extends EmailTriggerRule
    final case class Or(left: EmailTriggerRule, right: EmailTriggerRule)  extends EmailTriggerRule
    final case class Negate(emailFilter: EmailTriggerRule)                extends EmailTriggerRule
  }

  object PossibleSolution {
    sealed trait EmailTriggerRule
    object EmailTriggerRule {
      object ShopperAbandons                                                extends EmailTriggerRule
      final case class After(days: Int)                                     extends EmailTriggerRule
      final case class And(left: EmailTriggerRule, right: EmailTriggerRule) extends EmailTriggerRule
      final case class Or(left: EmailTriggerRule, right: EmailTriggerRule)  extends EmailTriggerRule
      final case class Negate(emailFilter: EmailTriggerRule)                extends EmailTriggerRule
    }
  }
}

object basic_dm_graduation {
  sealed trait Command
  object Command {
    case object Look                      extends Command
    case object Quit                      extends Command
    final case class LookAt(what: String) extends Command
    final case class Go(where: String)    extends Command
    final case class Take(item: String)   extends Command
    final case class Drop(item: String)   extends Command
    final case class Fight(who: String)   extends Command

    def fromString(string: String): Option[Command] =
      string.trim.toLowerCase.split("\\s+").toList match {
        case "go" :: where :: Nil          => Some(Go(where))
        case "look" :: Nil                 => Some(Look)
        case "look" :: "at" :: what :: Nil => Some(LookAt(what))
        case "take" :: item :: Nil         => Some(Take(item))
        case "drop" :: item :: Nil         => Some(Drop(item))
        case "fight" :: who :: Nil         => Some(Fight(who))
        case ("quit" | "exit") :: Nil      => Some(Quit)
        case _                             => None
      }
  }

  /**
   * EXERCISE
   *
   * Using case classes and sealed traits (and whatever other data types you like), design a game
   * world that can be used to play a simple text-based role playing game.
   *
   * The data type should model the player, non-player characters, and items available to pick up
   * or drop in the game world.
   */
  object MySolution {
    final case class State(players: List[Character], items: List[Item])

    sealed trait Character
    final case class Player(username: String)  extends Character
    final case class NonPlayable(name: String) extends Character

    sealed trait Item
    final case class DropableItem(data: ItemData) extends Item
    final case class PickableItem(data: ItemData) extends Item
    final case class ItemData(name: String)
  }

  object DGoesSolution {
    final case class State(currentLocation: Location, player: Character, gameMap: GameMap) {
      def items: List[Item] = ???
    }
    final case class Character(
      name: String,
      stats: CharStats,
      clazz: CharClass,
      status: CharStatus,
      equipped: List[Item],
      iventory: List[Item]
    )

    final case class Item(name: String, itemType: ItemType)
    sealed trait ItemType
    object Item {
      final case class Weapon(damage: Int, durability: Int) extends ItemType
      final case class Food(energyBoost: Int)               extends ItemType
      final case class HealingPotion(healthBoost: Int)      extends ItemType
    }

    final case class Location(name: String, items: List[Item], npcs: List[Character])
    final case class GameMap(set: Set[Location], connected: Map[Location, Set[Location]])

    final case class CharStats(health: Int, charClass: CharClass, charStatus: CharStatus)
    sealed trait CharClass
    object CharClass {
      case object Wizard  extends CharClass
      case object Warrior extends CharClass
    }

    sealed trait CharStatus
    object CharStatus {
      case object Normal   extends CharStatus
      case object Poisoned extends CharStatus
      case object Cursed   extends CharStatus
    }
  }

  def describe(state: DGoesSolution.State): String =
    "You are playing this game."

  def process(state: DGoesSolution.State, command: Command): (String, Option[DGoesSolution.State]) =
    if (command == Command.Quit) ("You quitted", None)
    else (s"You did: ${command}, which had no effect.", Some(state))

  def main(args: Array[String]): Unit = {
    @tailrec
    def loop(state: DGoesSolution.State): Unit = {
      println(describe(state))

      val line = scala.io.StdIn.readLine()

      Command.fromString(line) match {
        case None =>
          println("Unrecognized command")
          loop(state)

        case Some(command) =>
          process(state, command) match {
            case (output, next) =>
              println(output)
              next match {
                case Some(value) => loop(value)
                case None        => println("Goodbye!")
              }
          }
      }
    }

    loop(
      ???
    )
  }
}
