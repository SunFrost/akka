# Flow.fromSinkAndSourceCoupled

Allows coupling termination (cancellation, completion, erroring) of Sinks and Sources while creating a Flow between them.

@ref[Flow stages composed of Sinks and Sources](../index.md#flow-stages-composed-of-sinks-and-sources)

@@@div { .group-scala }

## Signature

@@signature [Flow.scala]($akka$/akka-stream/src/main/scala/akka/stream/scaladsl/Flow.scala) { #fromSinkAndSourceCoupled }

@@@

## Description

Allows coupling termination (cancellation, completion, erroring) of Sinks and Sources while creating a Flow between them.
Similar to `Flow.fromSinkAndSource` however couples the termination of these two stages.

E.g. if the emitted `Flow` gets a cancellation, the `Source` of course is cancelled,
however the Sink will also be completed. The table below illustrates the effects in detail:

| Returned Flow                                   | Sink (in)                   | Source (out)                    |
|-------------------------------------------------|-----------------------------|---------------------------------|
| cause: upstream (sink-side) receives completion | effect: receives completion | effect: receives cancel         |
| cause: upstream (sink-side) receives error      | effect: receives error      | effect: receives cancel         |
| cause: downstream (source-side) receives cancel | effect: completes           | effect: receives cancel         |
| effect: cancels upstream, completes downstream  | effect: completes           | cause: signals complete         |
| effect: cancels upstream, errors downstream     | effect: receives error      | cause: signals error or throws  |
| effect: cancels upstream, completes downstream  | cause: cancels              | effect: receives cancel         |

The order in which the *in* and *out* sides receive their respective completion signals is not defined, do not rely on its ordering.

