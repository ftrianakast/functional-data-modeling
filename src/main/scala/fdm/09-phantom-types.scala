package fdm

object phantom_types {

  /**
   * EXERCISE 1
   *
   * Add a phantom type parameter to `Socket`, which can keep track of the state of the socket:
   * either `Created` or `Connected`. Use this type parameter in the methods below to improve their
   * type safety.
   */
  type Created
  type Connected
  trait Socket

  def createSocket(): Socket                                 = ???
  def connectSocket(address: String, socket: Socket): Socket = ???
  def readSocket(socket: Socket): Array[Byte]                = ???

  object Solution {
    type Created
    type Connected
    trait Socket[State]

    def createSocket(): Socket[Created]                                            = ???
    def connectSocket(address: String, socket: Socket[Created]): Socket[Connected] = ???
    def readSocket(socket: Socket[Connected]): Array[Byte]                         = ???
  }

  /**
   * EXERCISE 2
   *
   * Introduce a type parameter to this data type to model whether a `Path` is a file, a directory,
   * or either a file or a directory. Use this to improve the type safety of the `readFile` and
   * `listDirectory` methods.
   *
   * Note: In order to ensure safety, you will have to make the constructors of `Path` private, so
   * that outside code cannot call those constructors with just any type parameter. This is a
   * requirement of using phantom types properly.
   */
  type File
  type Directory
  sealed trait Path
  object Path {
    case object Root                                   extends Path
    final case class ChildOf(path: Path, name: String) extends Path
  }
  def readFile(path: Path): String          = ???
  def listDirectory(path: Path): List[Path] = ???

  object PathSolution {
    type File
    type Directory
    sealed trait Path[PathType] { self =>
      def dir(name: String)(implicit ev: PathType <:< Directory): Path[Either[File, Directory]] =
        Path.ChildOf(self, name)

      def file(name: String)(implicit ev: PathType <:< Directory) = Path.ChildOf(self, name)
    }
    object Path {
      case object Root                                      extends Path[Directory]
      final case class ChildOf(path: Path[_], name: String) extends Path[Either[File, Directory]]
    }

    def readFile(path: Path[File]): String                                        = ???
    def listDirectory(path: Path[Directory]): List[Path[Either[File, Directory]]] = ???

    val x                   = Path.Root.dir("sas")
    val child: Path.ChildOf = ???
  }

  /**
   * EXERCISE 3
   *
   * Phantom types work well with intersection types (`with` in Scala 2.x). They have many
   * wide-ranging applications, including making builder patterns safer.
   *
   * Introduce a phantom type parameter for `PersonBuilder`, and arrange such that the setters
   * add a new type into a type intersection, and that the build method requires both age and name
   * to be set in order to build the person.
   *
   * Note: As before, you must make the constructors of the data type with a phantom type parameter
   * private, so they cannot be called from outside code.
   */
  type SetAge
  type SetName
  case class PersonBuilder(age: Option[Int], name: Option[String]) {
    def age(v: Int): PersonBuilder = copy(age = Some(v))

    def name(s: String): PersonBuilder = copy(name = Some(s))
  }
  final case class Person(name: String, age: Int)
  def build(personBuilder: PersonBuilder): Person =
    Person(personBuilder.name.get, personBuilder.age.get)

  object PersonBuilderPhantom {
    type SetAge
    type SetName
    class PersonBuilder[+SoFar] private (private val age: Option[Int], private val name: Option[String]) {
      def age(v: Int): PersonBuilder[SoFar with SetAge] = new PersonBuilder(age = Some(v), name = name)

      def name(s: String): PersonBuilder[SoFar with SetName] = new PersonBuilder(age = age, name = Some(s))
    }
    object PersonBuilder {
      val personBuilder: PersonBuilder[Any] = new PersonBuilder(None, None)

      def build(personBuilder: PersonBuilder[SetAge with SetName]): Person =
        Person(personBuilder.name.get, personBuilder.age.get)
    }
    final case class Person(name: String, age: Int)

    import PersonBuilder.personBuilder

    PersonBuilder.build(personBuilder.age(42).name("Sherlock Holmes"))
  }

}
