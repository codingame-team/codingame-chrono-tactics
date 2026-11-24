Viewer
* The turn number (displayed below the game title), with its light grey text against the maize background, is difficult to read.

League
* There doesn't seem to be a way to configure league names in a community puzzle, meaning we're limited to levels or divisions within the Wood league. In that case, I'd suggest updating the names (Wood 2, Wood 1, Bronze+) in the statement and the stub to avoid confusion.

* "… unlock the BOMB/PUSH tx ty ability": For simplicity, please remove "tx ty" from the league introduction section for Wood 2 and Wood 1.

* `leagueLevel`: To avoid confusion, consider using 3 for Wood 3, 2 for Wood 2, and 1 for Wood 1, rather than 1, 2 and 3 respectively.

All sections
* Please consistently use either "cell" or "tile".

The Goal
* Does the grid arena crumble in Wood 3? If not, please remove the word "crumbling" from the statement for that league, and clarify whether tile integrity values have any effect in that league. I assume that units cannot voluntarily move into abyss tiles.

Rules
* Please change "symmetric" to "rotational symmetric".

* Please explain the coordinate system: <var>x</var> increases to the right, <var>y</var> increases downwards, and <const>(0, 0)</const> is the top-left corner.

Turn Resolution Order
* PUSH

  - Please add "to the acting unit" after "adjacent".

  - "any unit": Does that mean a unit can push another unit in the same team? If so, a unit can be effectively moved two tiles in the same turn; is that correct?

  - Please replace "pushed 1 tile away in the same direction" with "pushed 1 tile away in the pushing direction".

  - Can two units push each other in the same turn? If so, could both units' cooldowns increase to 4? Separately, if two units push each other, does the first unit's cooldown increase to 2 before the second unit's action is processed, potentially preventing the second push? Please clarify the resolution order in the statement.

* BOMB

  - Please add "acting unit's" before "Manhattan distance".

  - What happens if multiple units BOMB the same tile in the same turn? Are the integrity reductions cumulative?

* Chrono-Storm Ring Collapse:

  - Please define "outermost active ring".

  - "loses 1 integrity" / "turn into integrity 1 floor tiles": These describe different behaviours. If the former refers to floor tiles and the latter to pillars, please clearly distinguish between the two rules and remove the brackets, as the brackets suggest that it is a less important detail.

* Does a unit have to remain standing on a captured Chrono-Beacon to retain ownership, or does ownership remain unchanged once captured? Can ownership change if an opponent's unit subsequently stands on the beacon? Please clarify.

Time Resolution Order / Output
* Please format `x`, `y`, `tx`, `ty` and `text` using the <var> tag in the action syntaxes (e.g. "<action>MOVE</action> <var>x</var> <var>y</var>").

* What happens when a unit outputs an action that cannot be executed? Examples:
  - MOVE to an adjacent abyss or pillar tile.
  - MOVE to an adjacent tile occupied by a friendly or opposing unit.
  - MOVE outside the grid.
  - PUSH a unit into a pillar or another unit.
  - BOMB a pillar or void tile, or target a tile outside the action's permitted range.
  - PUSH or BOMB while `cooldown` > 0.
  If an unexecutable action is simply ignored, please state this in the Turn Resolution Order section. If it is considered an invalid command and results in a defeat, please add that to the Defeat Conditions section.

Input for One Game Turn
* `cooldown`: Please specify that special actions are unavailable in the statement for Wood 3.

* (Optional) Consider providing turn number as an input.

Output for One Game Turn
* Please remove "(or WAIT)", and then add a separate bullet point for "WAIT".

* Please remove "optionally followed by a special action" from the statement for Wood 3.

* Please remove "optionally followed by:" from the statements for all the leagues. For Wood 2 and Wood 1, insert a new line below it stating: "A <action>MOVE</action> or <action>WAIT</action> command is optionally followed by a special action:".

* Please move the example (MOVE 5 5 / MOVE 5 5 BOMB 6 5) before the explanation of MSG. In its current position, it reads as though the example illustrates MSG.

* Please add a brief description of the actions, like this:
  • MOVE x y to move the unit towards the integer target coordinates (x, y).
  • WAIT to keep the unit stationary.
  • BOMB tx ty to bomb the tile at the integer target coordinates (tx, ty).
  • (similarly for PUSH)

  The detailed restrictions (e.g. Manhattan distance and cooldown requirements) need not be repeated here.

Constraints
* If `unitCount` is fixed in each league, please replace the range (1 to 6) in each league's statement to the fixed value used in that league.

* Can `beaconCount` be 0? If not, please update the range (0 to 7) in the statement for all leagues. I don't know what the game objective is with no beacons available, at least in Wood 3 😆

* Just curious: Any plans to introduce variable grid sizes rather than fixing the dimensions at 11 × 11?

Source Code
* The GitHub site shows a link to your personal profile. Provide a new link if that's unintended.

* Will the league bosses' source code remain publicly available after the game is approved?

New section at the end, if needed
* If the game concept is based on an existing game or another source, please acknowledge and credit the original creator or source.

Stub
* The comment describing possible output pattern puts WAIT in the same category as BOMB and PUSH. Should it be revised like this?

  [MOVE <x> <y> | WAIT] [BOMB <tx> <ty> | PUSH <tx> <ty>] [MSG <text>]
