# Phase 1 — Interface and Test Design

## Assumptions

The assignment tells us to make reasonable assumptions where the requirements are
ambiguous and to document them. These are ours:

1. **Positions are metre indices 0–495.** Index `i` covers the metre from `i` to `i+1`.
   The street is 500 m and the car is 5 m long, so the furthest the car's rear can
   stand is metre 495 (the car then occupies 495–500).
2. **The sensors sit at the rear of the car and face right.** A metre is therefore
   detected when the car *drives across it*: moving forward from position `i`
   records metre `i`.
3. **A metre is detected once and never re-measured.** The first reading for a metre
   is final; later passes over the same ground do not overwrite it.
4. **A metre counts as free at 100 cm or more.** The sensor range is 0–200 cm, so
   100 cm is half the range: enough clearance that the car is confident the space
   beside it is genuinely open rather than a shallow gap between parked cars.
5. **`Park` searches the whole street from metre 0, not only forwards.** The
   requirement says the car "moves forwards towards the end of the street until such
   a stretch is detected", but if the car has already detected a free stretch behind
   it, reversing to that known stretch is better than driving on to look for a new
   one. We therefore check the already-detected metres from the start of the street
   first, and only drive forward when nothing known qualifies.
6. **When no sensor can be trusted, `isEmpty` returns 0 cm.** 0 means "an object is
   right next to the car", which is read as BLOCKED. This is the safe failure mode:
   a car with two broken sensors refuses to park rather than parking blind.
7. **The actuators are not modelled.** A move is just a change to the stored position.

## Data structures and internal state

The `AutomaticParking` class remembers the following between method calls:

- **Car position** (`int`, 0–495): the car's current metre on the street. Changed by
  `MoveForward` and `MoveBackwards`, read by `whereIs`, `Park` and `UnPark`.
- **Parked status** (`boolean`): whether the car is currently parked. Set by `Park`
  and `UnPark`, read by `whereIs`, both move methods and `Park`.
- **Parking map** (`ParkingMap`, one `SpotStatus` of UNKNOWN / FREE / BLOCKED per
  metre): what the car has detected so far. Written by `MoveForward` and
  `MoveBackwards` through `isEmpty`, read by `Park` when searching for a 5 m free
  stretch. A copy is returned to the caller after every move.
- **Two sensor objects** implementing the `Sensor` interface (`int read()`): the
  source of the distance readings used by `isEmpty`.
- **An accumulated error per sensor** (`double`): how noisy each sensor has been
  across all `isEmpty` calls so far. `isEmpty` updates it, and a sensor whose
  accumulated error reaches the retirement limit is ignored permanently.

Design values:

| Value | Setting | Named constant in code |
| :- | :- | :- |
| Street length | 500 m | `STREET_LENGTH` |
| Car length | 5 m | `CAR_LENGTH` |
| Last legal position | 495 (`STREET_LENGTH - CAR_LENGTH`) | — |
| Readings per sensor per `isEmpty` call | 5 | `SENSOR_SIM_AMOUNT` |
| Distance at or above which a metre is FREE | 100 cm | — |
| A call's error counts as noise above | 0.075 | — |
| Accumulated error at which a sensor is ignored | 1.0 | — |

Data structures returned by methods:

- **`CarInfo`** (position and parked status): returned by `whereIs`. A *copy* is
  returned, so a caller cannot reach in and change the car's state.
- **`ParkingRecord`** (position and a copy of the parking map): returned by
  `MoveForward` and `MoveBackwards`.

## The noise filter

`isEmpty` takes 5 readings `r₁..r₅` from each sensor and computes that sensor's
mean. The **error of one call** is the mean relative deviation of the readings from
their own mean:

```
error = (1/n) · Σ | rᵢ / mean − 1 |          , and error = 0 when mean = 0
```

Readings are never negative, so a mean of 0 means every reading was 0 — a perfectly
steady sensor, not a noisy one. Defining `error = 0` in that case also avoids a
0/0 division that would otherwise produce `NaN`.

A call's error is added to the sensor's accumulated error **only when it exceeds
0.075**, so a steady sensor never drifts towards retirement. A sensor is trusted
while its accumulated error is **below 1.0**; once it reaches 1.0 it is disregarded
permanently. This is what "continuously returns very noisy output" means in our
implementation: a single bad call is tolerated, four in a row are not.

Worked values used by the tests:

| Readings | Mean | Error per call | Calls until retired |
| :- | :- | :- | :- |
| `{150, 150, 150, 150, 150}` | 150 | 0 (below 0.075, never accumulates) | never |
| `{125, 100, 90, 110, 195}` | 124 | 0.232 | 5th |
| `{40, 100, 65, 40, 70}` | 63 | 0.292 | 4th |
| `{0, 0, 0, 0, 0}` | 0 | 0 (by definition above) | never |

`isEmpty` returns the **smaller** of the two trusted means, because the requirement
asks for the distance to the *nearest* object.

> **Known coverage gap.** Every sensor script in the test suite produces a per-call
> error of either 0 or at least 0.232. No test therefore distinguishes the 0.075
> threshold from any other value between 0 and 0.232 — the suite would behave
> identically at 0.1. Closing this needs a script whose per-call error falls inside
> that band.

## Method signatures

- **`public ParkingRecord MoveForward()`** — no parameters, since it always moves 1 m
  and uses the car's stored state. Returns the current position and the parking map
  so far.
- **`public ParkingRecord MoveBackwards()`** — as `MoveForward`, but moves the car 1 m
  backwards.
- **`public CarInfo whereIs()`** — no parameters. Returns a `CarInfo` holding the
  current position and whether the car is parked.
- **`public void Park()`** — first checks the already-detected metres, from the start
  of the street, for a 5 m free stretch. If none is found there, it drives forward
  towards the end of the street until one is detected. The car then reverses into the
  stretch and `isParked` becomes true. If no stretch is found before the end of the
  street, the car is left at 495 and is not parked.
- **`public void UnPark()`** — if the car is parked, it moves the car 5 m forward out
  of the spot and sets `isParked` to false. If the car is not parked, nothing happens.
- **`public int isEmpty()`** — reads both sensors 5 times each and updates each
  sensor's accumulated error. Returns the filtered distance in cm (0–200) to the
  nearest object on the right. A retired sensor is ignored, and 0 is returned if both
  are retired.

`getValidParkingPosition()` is a private helper of `Park`, not part of the interface:
it drives the car forward as it searches, so it is deliberately not exposed.

## Decision tables

### whereIs

| Car state | Expected position | Expected isParked | Test case |
| :- | :- | :- | :- |
| New car | 0 | false | WhereIsTestStart |
| After 3 moves forward | 3 | false | WhereIsTestAfterMoveThreeSteps |
| After 1000 moves forward (end of street) | 495 | false | WhereIsTestAfterMove1000Steps |
| After `Park` on an empty street | 0 | true | WhereIsAfterPark |

### MoveForward

| Parked | Position before | Sensor reading | Expected position | Expected map | Test case |
| :- | :- | :- | :- | :- | :- |
| no | 0 | any | 1 | metre 0 recorded | MoveForwardTestOneStepFromStart |
| no | 495 | any | 495 | unchanged | MoveForwardMoreThanStreetLength |
| yes | any | any | unchanged | unchanged | MoveForwardWhileParkedDoesNothing |
| no | 0 | 100 | 1 | metre 0 FREE | MoveForwardRecordsFreeAt100 |
| no | 0 | 99 | 1 | metre 0 BLOCKED | MoveForwardRecordsBlockedAt99 |
| no | 0, three moves | 150 | 3 | metres 0–2 FREE, 3 UNKNOWN | MoveForwardAccumulatesMap |

### MoveBackwards

| Parked | Position before | Metre behind | Expected position | Expected map | Test case |
| :- | :- | :- | :- | :- | :- |
| no | 0 (start) | — | 0 | unchanged | MoveBackwardsFromStart |
| no | 5 (middle) | already known | 4 | unchanged | MoveBackwardsFromPositionFive |
| no | 495 (end) | already known | 494 | unchanged | MoveBackwardsFromEnd |
| yes | any | — | unchanged | unchanged | MoveBackwardsWhileParkedDoesNothing |
| no | 3, sensors now report the opposite | already BLOCKED | 2 | metre 2 stays BLOCKED | MoveBackwardsKeepsAlreadyRecordedSpot |

Note: because the car can only reach position `p` by driving forward across every
metre below `p`, the metre behind the car is always already recorded. Recording on
the way back is therefore a no-op in practice — `MoveBackwardsKeepsAlreadyRecordedSpot`
is the test that pins that behaviour down.

### isEmpty

| Sensor A trusted | Sensor B trusted | Expected return | Test case |
| :- | :- | :- | :- |
| yes | yes | smaller of the two means | isEmptyTest |
| yes, retires on 4th call | yes | B's mean from the 4th call on | isEmptyNoisySensorA |
| yes | yes, retires on 4th call | A's mean from the 4th call on | isEmptyNoisySensorB |
| no | no | 0 | isEmptyBothSensorsNoisy |

### Park

| Parked | Free stretch (behind) | Free stretch (ahead) | Exp. pos. | Exp. parked | Test case |
| :- | :- | :- | :- | :- | :- |
| no | no | yes at 10 | 10 | true | ParkTest |
| no | yes at 0, car at 200 | yes | 0 | true | ParkUsesFreeStretchBehindCar |
| no | no | no | 495 | false | ParkNoFreeStretch |
| yes | — | — | unchanged | true | ParkWhenAlreadyParked |

### UnPark

| Parked | Pos. before | Exp. pos. | Exp. parked | Test case |
| :- | :- | :- | :- | :- |
| yes | 0 | 5 | false | UnParkMovesOut |
| no | 0 | 0 | false | UnParkWhenNotParked |

## Test suite

| # | Test case | Input | Expected output |
| :- | :- | :- | :- |
| 1 | WhereIsTestStart | New car | pos 0, not parked |
| 2 | WhereIsTestAfterMoveThreeSteps | 3× forward | pos 3, not parked |
| 3 | WhereIsTestAfterMove1000Steps | 1000× forward | pos 495, not parked |
| 4 | WhereIsAfterPark | Sensors 150, Park | pos 0, parked |
| 5 | MoveForwardTestOneStepFromStart | 1× forward | pos 1 |
| 6 | MoveForwardMoreThanStreetLength | 1001× forward | pos 495 |
| 7 | MoveForwardWhileParkedDoesNothing | Sensors 150, Park, 1× forward | pos 0, parked |
| 8 | MoveForwardRecordsFreeAt100 | Sensors 100, 1× forward | metre 0 FREE |
| 9 | MoveForwardRecordsBlockedAt99 | Sensors 99, 1× forward | metre 0 BLOCKED |
| 10 | MoveBackwardsFromStart | 1× backward | pos 0 |
| 11 | MoveBackwardsFromPositionFive | 5× forward, 1× backward | pos 4 |
| 12 | MoveBackwardsFromEnd | 1000× forward, 1× backward | pos 494 |
| 13 | MoveBackwardsWhileParkedDoesNothing | Park at 10, 1× backward | pos 10, parked |
| 14 | isEmptyTest | A `{125,100,90,110,195}`, B `{100}×5` | 100 |
| 15 | isEmptyNoisySensorA | A `{40,100,65,40,70}`, B `{150}×5` | 63, 63, 63, 150, 150 |
| 16 | isEmptyNoisySensorB | A `{150}×5`, B `{40,100,65,40,70}` | 63, 63, 63, 150, 150 |
| 17 | isEmptyBothSensorsNoisy | A and B `{40,100,65,40,70}` | 63, 63, 63, 0, 0 |
| 18 | ParkTest | Metres 0–9 blocked, then free (100), Park | pos 10, parked |
| 19 | ParkUsesFreeStretchBehindCar | Metres 0–9 free, 10–199 blocked, car at 200, Park | pos 0, parked |
| 20 | ParkNoFreeStretch | Sensors 0, Park | pos 495, not parked |
| 21 | ParkWhenAlreadyParked | Park at 10, Park again | pos 10, parked |
| 22 | UnParkMovesOut | Sensors 150, Park, UnPark | pos 5, not parked |
| 23 | UnParkWhenNotParked | New car, UnPark | pos 0, not parked |
| 24 | MoveForwardAccumulatesMap | Sensors 150, 3× forward | pos 3; metres 0–2 FREE, metre 3 UNKNOWN |
| 25 | MoveBackwardsKeepsAlreadyRecordedSpot | Sensors 0, 3× forward, sensors 150, 1× backward | pos 2; metre 2 still BLOCKED |
| 26 | SimulatedSensorStaysInRange | 1000 reads from SimulatedSensor | every reading within 0–200 cm |

Cases 1–23 come from the decision tables above. Cases 24–26 were added to close
coverage gaps: 24 checks that the map *accumulates* rather than holding only the
last metre, 25 pins down the "detected once, never overwritten" rule, and 26 checks
that the random stand-in sensor respects the 0–200 cm range stated in the
requirements.

Cases 8, 9 and 18 sit on the 100 cm free/blocked boundary: 8 and 9 test the two
sides of it directly, and 18 uses a reading of exactly 100 so that `Park` is driven
by a metre that is free by the narrowest margin.
