# Game Streaming

Reads a stream of basketball scoring events encoded as 32-bit integers, decodes them into typed events, and keeps
only the ones consistent with the game so far. Written in Scala 2.13 in 2020, with a small fix in 2024.

## Running the tests

Requires [sbt](https://www.scala-sbt.org/).

```
sbt test
```

## Running the example

```
sbt "runMain GameStreaming.ExampleApp"
```

It reads the events in `src/main/resources/example.txt` and prints the ones it kept:

```
TeamScored(TwoPointer,Team1,GameState(2,0,15))
TeamScored(TwoPointer,Team2,GameState(2,2,28))
TeamScored(ThreePointer,Team2,GameState(2,5,60))
...
```

## The event format

Each event is a 32-bit integer, written in hex (for example `0x781002`). Version 1 of the format packs these fields,
from the lowest bit:

| Bits | Field |
|---|---|
| 0 to 1 | points scored: 1, 2 or 3 |
| 2 | scoring team: 0 for team 1, 1 for team 2 |
| 3 to 10 | team 2's total score |
| 11 to 18 | team 1's total score |
| 19 to 30 | match time in seconds |

An event is kept only if its match time is later than the previous event's and neither team's score has gone down.
Anything else is logged and discarded.

## Design

### Event reader

Where the events come from isn't fixed: it could be a file being appended to, a message bus or a web service. So the
reader is a trait over an effect type:

```scala
trait EventReader[F[_]] {
  case class NonConsistentEvent(msg: String) extends BaskBallEventError

  def add(event: String): F[Either[BaskBallEventError, Boolean]]
  def hydrateBuffer(): F[Unit]
  def last: F[Option[BasketballEvent]]
  def lastN(n: Int): F[Seq[BasketballEvent]]
  def all: F[Seq[BasketballEvent]]
}
```

A reader over a web service could use `Future` as its effect, for example. The implementation here uses `Id` and reads
from a file. That makes it synchronous, which wouldn't suit a real system, but it keeps the tests simple.

### Event parser

Parsing is separate from the reader so it can be tested on its own. It converts the hex string to an integer and then
masks out each field. Each kind of failure has its own error type.

### Versioned format

The events come from an external provider, so their layout may change. The bit offsets live in an
`EventFormatSpecification`, and `EventFormatV1` is the current one. If the provider moved a field, a new format object
with the new offsets could be passed to the parser without changing anything else.

### The game model

The game's rules live in `BasketballGame`. For now the only event is `TeamScored`; fouls or free throws would be new
event types.

Sealed type hierarchies rule out impossible values: there is no value for a nine-point basket. Scores and match time
use [refined](https://github.com/fthomas/refined)'s `NonNegInt`, so they cannot be negative.

The consistency check is on the event itself. An inconsistent event is dropped, which is the simplest choice. A real
system might keep it, since later processing could put events back in order.

### Testing

A type class converts a `TeamScored` event back to its hex form. Combined with ScalaCheck, that lets the tests
generate events across the whole range of valid values and check that each one parses back to itself.

## Ideas not done

- A refined type for the maximum score and the maximum match time.
- Use the same type class to generate load for a tool such as Gatling.
- Support games other than basketball.
