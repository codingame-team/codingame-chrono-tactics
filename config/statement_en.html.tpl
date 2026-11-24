<!-- LEAGUES level1 level2 level3 -->
<div id="statement_back" class="statement_back" style="display:none"></div>
<div class="statement-body">
  <!-- LEAGUE ALERT -->
  <!-- BEGIN level1 -->
  <div style="color: #7cc576; background-color: rgba(124, 197, 118,.1); padding: 20px; margin-bottom: 15px; text-align: left">
    <p style="margin-bottom: 0"><b>Wood 3 League:</b> Command <b>1 unit</b> to capture Chrono-Beacons across the arena. Special tactical abilities will unlock in higher divisions.</p>
  </div>
  <!-- END -->
  <!-- BEGIN level2 -->
  <div style="color: #7cc576; background-color: rgba(124, 197, 118,.1); padding: 20px; margin-bottom: 15px; text-align: left">
    <p style="margin-bottom: 0"><b>Wood 2 League:</b> You now command <b>2 units</b> and unlock the <action>BOMB</action> ability (range &le; <const>2</const>, cooldown <const>2</const>) to degrade floor tiles and collapse bridges!</p>
  </div>
  <!-- END -->
  <!-- BEGIN level3 -->
  <div style="color: #7cc576; background-color: rgba(124, 197, 118,.1); padding: 20px; margin-bottom: 15px; text-align: left">
    <p style="margin-bottom: 0"><b>Wood 1 League:</b> You now command <b>3 units</b>, unlock the <action>PUSH</action> ability (range <const>1</const>, cooldown <const>2</const>) to knock adjacent units into the abyss, and the outer ring of the arena degrades every <const>20</const> turns!</p>
  </div>
  <!-- END -->

  <!-- GOAL -->
  <div class="statement-section statement-goal">
    <h1><span class="icon icon-goal">&nbsp;</span><span>The Goal</span></h1>
    <div class="statement-goal-content">
      <!-- BEGIN level1 -->
      Outscore your opponent over <const>120</const> turns by capturing <b>Chrono-Beacons</b> on a grid arena.
      <!-- END -->
      <!-- BEGIN level2 level3 -->
      Outscore your opponent over <const>120</const> turns by capturing <b>Chrono-Beacons</b> and keeping your tactical squad alive on a crumbling grid arena.
      <!-- END -->
    </div>
  </div>

  <!-- RULES -->
  <div class="statement-section statement-rules">
    <h1><span class="icon icon-rules">&nbsp;</span><span>Rules</span></h1>
    <div class="statement-rules-content">
      <p>The game is played on a rotationally symmetric <const>11</const>&times;<const>11</const> grid. Coordinates follow the standard grid convention: <var>x</var> increases to the right, <var>y</var> increases downwards, and <const>(0, 0)</const> is the top-left corner. Each tile is one of:</p>
      <ul>
        <!-- BEGIN level1 -->
        <li><const>0</const>: Void abyss (impassable; units cannot voluntarily move onto a <const>0</const> tile).</li>
        <li><const>1</const>, <const>2</const>, <const>3</const>: Walkable floor tile (tile integrity values have no effect in Wood 3, as tiles do not crumble in this division).</li>
        <!-- END -->
        <!-- BEGIN level2 level3 -->
        <li><const>0</const>: Void abyss (impassable; units cannot voluntarily move onto a <const>0</const> tile, and any unit ending up on a <const>0</const> tile is eliminated).</li>
        <li><const>1</const>, <const>2</const>, <const>3</const>: Walkable floor tile with its current structural integrity.</li>
        <!-- END -->
        <li><const>#</const>: Indestructible pillar (impassable obstacle).</li>
      </ul>

      <h3 style="font-size: 16px; font-weight: 700; padding-top: 10px">Turn Resolution Order</h3>
      <ol>
        <li><b>Simultaneous Movement (<action>MOVE</action> <var>x</var> <var>y</var> or <action>WAIT</action>):</b> Each unit takes <const>1</const> orthogonal step along the shortest walkable path toward (<var>x</var>, <var>y</var>), never voluntarily stepping onto void (<const>0</const>) or pillar (<const>#</const>) tiles. If two units attempt to enter the same tile or swap positions, or if a unit's step is blocked by a stationary unit, the blocked units stay in place.</li>
        <!-- BEGIN level3 -->
        <li><b>Tactical Push (<action>PUSH</action> <var>tx</var> <var>ty</var>):</b> All <action>PUSH</action> commands are first validated against post-movement positions (checking <var>cooldown</var> = <const>0</const> and that (<var>tx</var>, <var>ty</var>) is orthogonally adjacent to the acting unit, i.e. Manhattan distance <const>1</const>), and every valid pusher immediately has its <var>cooldown</var> set to <const>2</const>. Recorded pushes are then executed in unit ID order (Player 0's units, then Player 1's units). Any unit at (<var>tx</var>, <var>ty</var>) &mdash; friendly or opposing (pushing a friendly unit can move it an extra tile in the same turn) &mdash; is pushed <const>1</const> tile away in the pushing direction:
          <ul>
            <li>If pushed into the void (<const>0</const>) or outside the grid, the pushed unit is <b>eliminated</b>. If it belonged to the opponent, the pusher gains <b>+<const>15</const> bonus points</b>!</li>
            <li>If pushed into a pillar (<const>#</const>) or another unit, the target stays in place and its <var>cooldown</var> increases by <const>2</const> (stunned).</li>
          </ul>
          Because all pushes are validated before effects are applied, if two adjacent units push each other on the same turn, both pushes are executed (and if either is slammed into an obstacle, its <var>cooldown</var> increases from <const>2</const> to <const>4</const>).
        </li>
        <!-- END -->
        <!-- BEGIN level2 level3 -->
        <li><b>Tile Demolition (<action>BOMB</action> <var>tx</var> <var>ty</var>):</b> If <var>cooldown</var> is <const>0</const> and the acting unit's Manhattan distance to (<var>tx</var>, <var>ty</var>) is <const>1</const> or <const>2</const>, reduces the integrity of floor tile (<var>tx</var>, <var>ty</var>) by <const>1</const> and sets the acting unit's <var>cooldown</var> to <const>2</const>. If multiple units bomb the same tile on the same turn, the integrity reductions are cumulative. If integrity reaches <const>0</const>, the tile collapses into void: any beacon on it is destroyed and any unit standing on it is <b>eliminated</b>!</li>
        <!-- END -->
        <!-- BEGIN level3 -->
        <li><b>Chrono-Storm Ring Collapse:</b> Every <const>20</const> turns (turns <const>20</const>, <const>40</const>, <const>60</const>, and <const>80</const>), the outermost active ring of the arena degrades. A ring at radius <var>r</var> consists of all tiles whose Chebyshev distance from the center (<const>5</const>, <const>5</const>) is <const>max(|x - 5|, |y - 5|) = r</const>, starting at <var>r</var> = <const>5</const> on turn <const>20</const> and shrinking inward by <const>1</const> every <const>20</const> turns (<var>r</var> = <const>5, 4, 3, 2</const>). On that ring:
          <ul>
            <li>Each floor tile (<const>1</const>&ndash;<const>3</const>) loses <const>1</const> integrity, collapsing into void (<const>0</const>) if it reaches <const>0</const>.</li>
            <li>Each pillar (<const>#</const>) is cracked by the storm and turns into a walkable floor tile of integrity <const>1</const>.</li>
          </ul>
        </li>
        <!-- END -->
        <!-- BEGIN level1 -->
        <li><b>Beacon Capture &amp; Scoring:</b> A unit standing on an active Chrono-Beacon captures it for its owner. Once captured, the beacon remains under that player's control even after the unit leaves the tile, until an opposing unit stands on it (transferring ownership). At the end of each turn, each player gains:
        <!-- END -->
        <!-- BEGIN level2 level3 -->
        <li><b>Beacon Capture &amp; Scoring:</b> A unit standing on an active Chrono-Beacon captures it for its owner. Once captured, the beacon remains under that player's control even after the unit leaves the tile, until an opposing unit stands on it (transferring ownership) or its tile collapses into void. At the end of each turn, each player gains:
        <!-- END -->
          <ul>
            <li><b>+<const>2</const> points</b> per controlled Chrono-Beacon,</li>
            <li><b>+<const>1</const> point</b> per alive unit.</li>
          </ul>
          <!-- BEGIN level2 level3 -->
          If all units of a player are eliminated, the match ends immediately and the surviving player receives <b>+<const>20</const> bonus points</b> per remaining alive unit.
          <!-- END -->
        </li>
      </ol>
      <!-- BEGIN level1 -->
      <p><b>Unexecutable Actions:</b> Syntactically valid actions that cannot be executed (such as <action>MOVE</action> outside the grid or toward an obstacle or occupied tile) are simply ignored and do not cause defeat.</p>
      <!-- END -->
      <!-- BEGIN level2 -->
      <p><b>Unexecutable Actions:</b> Syntactically valid actions that cannot be executed (such as <action>MOVE</action> outside the grid or toward an obstacle or occupied tile, or <action>BOMB</action> while <var>cooldown</var> &gt; <const>0</const> or out of range) are simply ignored and do not cause defeat. Targeting a pillar or void tile with <action>BOMB</action> within range while <var>cooldown</var> = <const>0</const> has no effect on the tile but still triggers the <const>2</const>-turn cooldown.</p>
      <!-- END -->
      <!-- BEGIN level3 -->
      <p><b>Unexecutable Actions:</b> Syntactically valid actions that cannot be executed (such as <action>MOVE</action> outside the grid or toward an obstacle or occupied tile, or <action>BOMB</action>/<action>PUSH</action> while <var>cooldown</var> &gt; <const>0</const> or out of range) are simply ignored and do not cause defeat. Targeting a pillar or void tile with <action>BOMB</action> (within range) or an empty tile with <action>PUSH</action> (at Manhattan distance <const>1</const>) while <var>cooldown</var> = <const>0</const> has no board effect, but still triggers the <const>2</const>-turn cooldown.</p>
      <!-- END -->

      <!-- VICTORY CONDITIONS -->
      <div class="statement-victory-conditions">
        <div class="icon victory"></div>
        <div class="blk">
          <div class="title">Victory Conditions</div>
          <div class="text">
            <ul style="padding-bottom:0">
              <!-- BEGIN level1 -->
              <li>You have more points than your opponent at the end of the game (after <const>120</const> turns).</li>
              <!-- END -->
              <!-- BEGIN level2 level3 -->
              <li>You have more points than your opponent at the end of the game (after <const>120</const> turns or when one squad is completely eliminated).</li>
              <!-- END -->
            </ul>
          </div>
        </div>
      </div>

      <!-- DEFEAT CONDITIONS -->
      <div class="statement-lose-conditions">
        <div class="icon lose"></div>
        <div class="blk">
          <div class="title">Defeat Conditions</div>
          <div class="text">
            <ul style="padding-bottom:0">
              <li>You have fewer points than your opponent at the end of the game.</li>
              <li>Your program does not respond within the allotted time or outputs a malformed/unrecognized command.</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- PROTOCOL -->
  <div class="statement-section statement-protocol">
    <h1><span class="icon icon-protocol">&nbsp;</span><span>Game Input &amp; Output</span></h1>
    <div class="blk">
      <div class="title">Initialization Input</div>
      <div class="text">
        <p><span class="statement-lineno">Line 1:</span> 3 space-separated integers <var>width</var>, <var>height</var> and <var>leagueLevel</var>, the width and height of the grid (<const>11</const>&times;<const>11</const>) and the current Wood division (<const>3</const> for Wood 3, <const>2</const> for Wood 2, <const>1</const> for Wood 1).</p>
      </div>
    </div>
    <div class="blk">
      <div class="title">Input for One Game Turn</div>
      <div class="text">
        <p><span class="statement-lineno">Line 1:</span> an integer <var>turn</var>, the current turn number (<const>1</const> to <const>120</const>).</p>
        <p><span class="statement-lineno">Next <var>height</var> lines:</span> a string <var>row</var> of <var>width</var> characters representing one row of the grid (<const>0</const> for void, <const>1</const>&ndash;<const>3</const> for floor tile integrity, or <const>#</const> for an indestructible pillar).</p>
        <p><span class="statement-lineno">Next line:</span> an integer <var>unitCount</var>, the total number of alive units on the grid.</p>
        <p><span class="statement-lineno">Next <var>unitCount</var> lines:</span> 5 space-separated integers <var>unitId</var>, <var>owner</var>, <var>x</var>, <var>y</var> and <var>cooldown</var>:</p>
        <ul>
          <li><var>unitId</var>: unique identifier of the unit.</li>
          <li><var>owner</var>: <const>0</const> if you own this unit (your units are always listed first), <const>1</const> if owned by your opponent.</li>
          <li><var>x</var>, <var>y</var>: grid coordinates of the unit.</li>
          <!-- BEGIN level1 -->
          <li><var>cooldown</var>: always <const>0</const> in Wood 3, as special actions are unavailable in this division.</li>
          <!-- END -->
          <!-- BEGIN level2 level3 -->
          <li><var>cooldown</var>: turns remaining before this unit can use a special action.</li>
          <!-- END -->
        </ul>
        <p><span class="statement-lineno">Next line:</span> an integer <var>beaconCount</var>, the number of active Chrono-Beacons.</p>
        <p><span class="statement-lineno">Next <var>beaconCount</var> lines:</span> 3 space-separated integers <var>bx</var>, <var>by</var> and <var>beaconOwner</var>, the coordinates of the beacon and its owner (<const>-1</const> for neutral, <const>0</const> for you, <const>1</const> for your opponent).</p>
      </div>
    </div>
    <div class="blk">
      <div class="title">Output for One Game Turn</div>
      <div class="text">
        <p><span class="statement-lineno">One line per alive unit you own</span> (in the exact order they appear in the input), containing one movement command:</p>
        <ul>
          <li><action>MOVE</action> <var>x</var> <var>y</var> to move the unit toward the integer target coordinates (<var>x</var>, <var>y</var>).</li>
          <li><action>WAIT</action> to keep the unit stationary.</li>
        </ul>
        <!-- BEGIN level2 -->
        <p>A <action>MOVE</action> or <action>WAIT</action> command is optionally followed by a special action:</p>
        <ul>
          <li><action>BOMB</action> <var>tx</var> <var>ty</var> to bomb the tile at the integer target coordinates (<var>tx</var>, <var>ty</var>).</li>
        </ul>
        <!-- END -->
        <!-- BEGIN level3 -->
        <p>A <action>MOVE</action> or <action>WAIT</action> command is optionally followed by a special action:</p>
        <ul>
          <li><action>BOMB</action> <var>tx</var> <var>ty</var> to bomb the tile at the integer target coordinates (<var>tx</var>, <var>ty</var>).</li>
          <li><action>PUSH</action> <var>tx</var> <var>ty</var> to push a unit at the integer target coordinates (<var>tx</var>, <var>ty</var>).</li>
        </ul>
        <!-- END -->
        <!-- BEGIN level1 -->
        <p>Example: <action>MOVE 5 5</action></p>
        <!-- END -->
        <!-- BEGIN level2 level3 -->
        <p>Example: <action>MOVE 5 5 BOMB 6 5</action></p>
        <!-- END -->
        <p>You may optionally append <action>MSG</action> <var>text</var> at the end of a line to display a message in the viewer.</p>
      </div>
    </div>
    <div class="blk">
      <div class="title">Constraints</div>
      <div class="text">
        <var>width</var> = <const>11</const>, <var>height</var> = <const>11</const><br>
        <!-- BEGIN level1 -->
        <var>unitCount</var> = <const>2</const><br>
        <var>beaconCount</var> = <const>7</const><br>
        <!-- END -->
        <!-- BEGIN level2 -->
        <const>2</const> &le; <var>unitCount</var> &le; <const>4</const><br>
        <const>0</const> &le; <var>beaconCount</var> &le; <const>7</const><br>
        <!-- END -->
        <!-- BEGIN level3 -->
        <const>2</const> &le; <var>unitCount</var> &le; <const>6</const><br>
        <const>0</const> &le; <var>beaconCount</var> &le; <const>7</const><br>
        <!-- END -->
        Response time for first turn &le; <const>1000</const> ms<br>
        Response time for one turn &le; <const>50</const> ms<br>
        Maximum number of turns: <const>120</const>
      </div>
    </div>
  </div>

  <!-- SOURCE CODE & CREDITS -->
  <div class="statement-section statement-examples">
    <h1><span class="icon icon-example">&nbsp;</span><span>Source Code &amp; Credits</span></h1>
    <div class="statement-examples-text">
      <p>The referee, viewer, and starter bots source code is available on GitHub:
        <a href="https://github.com/codingame-team/codingame-chrono-tactics" target="_blank" rel="noopener noreferrer">https://github.com/codingame-team/codingame-chrono-tactics</a>
      </p>
      <p><b>Credits &amp; Inspiration:</b> Original puzzle design inspired by classic turn-based tactical grid games (notably <i>Into the Breach</i> for positional push tactics and shrinking-arena mechanics). Built with the official CodinGame Game Engine SDK.</p>
    </div>
  </div>
</div>
