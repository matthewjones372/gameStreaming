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
a file which is appended to, a message bus, a web service etc. As it's not really specified I created a trait with a higher kinded type. 

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

* For this example, I kept things simple and just used an ID type and a file as a source. Not ideal for a real world example as this would mean synchronous code but at least kept the tests simple and allowed me to demonstrate it's intended usage.  

#### Event Parser
* The logic for parsing events was kept separate as it allowed for eaiser testing. I was hoping to avoid the conversion to string but couldn't think of an easier way without resorting to hard to read code or an external library. 
The over-head seems small and there are rich error types. 

### Event Specification
* As it is an external provider I think it would be poor practice to assume that their format would be stable forever. I included an event specification trait which allows the event spec to be versioned in code.  
If in the future, the provider changed their offsets for say team1 scoring it would be a case of creating a new event format with the new offsets and then injecting that into the event parser.

#### Basketball Game
* This is where all the business logic of the game is kept, for now there is only a TeamScored event but I would presume there would be more added such as a foul play event or a penalty throw event etc.

* Type hierarchies are used where possible. This helps prevent invalid states such as a team scoring 9 points (A value for this won't exist).  

* I chose to break the no library rule for refined types. It allowed me encode the types as Non-Negtive without having to write any additional logic but puts a constraint on future programmer at the type level. 

* The logic for consistency checking is on the events, I again kept it simple, if an event is not consistent with the last it is simply not added to the event bus. But this may change depending on the requirement, the users may want to keep invalid events as post processing may allow them to sort the events.

#### Testing
* To test the system using a large range of values I created a type class which allows the conversion of TeamScored event into a hex representation. 
Then used this in combination with scalacheck which allows the creation of generative tests. As I've also used refined types and type hierarcies, the system is constrained to only accept a possible range of values.

#### TODO
* Ideally I would create my own refined type to encode the max and min value of the score, also for game score I could encode the maximum time of a match.
* I would also like to use this type class for Load testing and use a system such as Gatling to check for any possible bottlenecks.
* At the moment the system has Basketball games in mind, but there's really no reason why other types of  games couldn't be implemented and used.