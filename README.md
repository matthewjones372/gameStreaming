# Game Streaming

### Prerequisites

* [SBT](https://www.scala-sbt.org/) - Interactive build tool, installation instructions can be found [here](https://www.scala-sbt.org/1.x/docs/Setup.html)

### Building

```
$sbt compile
```

## Running the tests

```
$sbt test
```
Example:
```
[info] IdEventReaderTest:
[info] IdEventReader
[info] - should return none when there are no events
[info] - should return the last valid event from a file with valid events
[info] - should return the last valid event from a file with an invalid event
[info] - should return the last n events
[info] - should does not return inconsistent values
[info] TeamScoredTest:
[info] TeamScored
[info] - should flag new event as consistent when team1's  score is increased
[info] - should flag new event as consistent when team2's score is increased
[info] - should flag new event as consistent when the game time is increased
[info] - should flag new event as inconsistent when team1's score is decreased
[info] - should flag new event as inconsistent when team2's score is decreased
[info] - should flag new event as inconsistent when game time is decreased
[info] FileHydrationSourceTest:
[info] FileHydrationSource
[info] - should return a list of strings from a valid line separated file
```

### Design Decisions

#### Event Reader
The spec states that the basketball events are streamed into the system but this could mean any number of things such as
a file which is appened to, a message bus, a web service etc. As it's not really specified I kept a trait with a higher kinded type. 

```
trait EventReader[F[_]] {
  case class NonConsistentEvent(msg: String) extends BaskBallEventError

  def add(event: String): F[Either[BaskBallEventError, Boolean]]
  def hydrateBuffer(): F[Unit]
  def last: F[Option[BasketballEvent]]
  def lastN(n: Int): F[Seq[BasketballEvent]]
  def all: F[Seq[BasketballEvent]]
}
```

* The benefit of this is it allows the event reader to be implemented in the most suitable way for the source. 
For example, if the events were streamed from a web service the effect could be a Future and would be a case of implementing the methods wrapped in that effect.

* For this example, I kept things simple and just used an Id type and a file as the source. Not ideal for a real world example as this would mean syncnronus code but at least kept the tests simple and allowed me demonstrate it's intented usage.  

#### Event Parser
* The logic for parsing events was kept separate as it allowed for eaiser testing. I was hoping to avoid the conversion to string but couldn't think of an easier way without resorting to hard to read code or an external library. 
The over-head seems small and there are rich error types. 

### Event Specification
* As it is an external provider I think it would be poor practice to assume that there format would be stable forever so I included an event specification as code. 
For example, If in the future the provider changed their offsets for team1 scoring it would be case of creating new event format and then injecting that into the event parser.

#### BasketBall Game
* This is where all the business logic of the game is kept, at the momoment there is only a TeamScored event but I would presume there would be more such as a foul play or a penalty throw etc.

* Type hierarchies are used where possible. This helps prevent invalid states such as a team scoring 9 points - a value for that doesn't exist.  

* I chose to break the no library rule for refined types. It allowed me encode the types as Non-Negtive without having to write any additional logic but puts a constraint on future programmer at the type level. 

* The logic for consistency checking is on the events, I again kept it simple, if an event is not consistent with the last it is simply not added to the event bus. But this may change depending on the requriment, the users may want to keep invalid events as post processing may allow them to sort the events.
Events may just be sent out of order.

#### Testing
* To test the system using a large range of values I created a type class which allows the conversion of TeamScored event into a hex representation. 
This allows the usage of scalacheck which allows the creation of generative tests. As I've also used refined types and type hierarcies the system is constrained to only accept a possible range of values.

### TODO
* Ideally I would create my own refined type to encode the max and min value of the score and game time and have this as part of the Format spec.
* I would also like to use this type class for Load testing and use a system such as Gatling. 