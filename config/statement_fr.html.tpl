<!-- LEAGUES level1 level2 level3 -->
<div id="statement_back" class="statement_back" style="display:none"></div>
<div class="statement-body">
  <!-- LEAGUE ALERT -->
  <!-- BEGIN level1 -->
  <div style="color: #7cc576; background-color: rgba(124, 197, 118,.1); padding: 20px; margin-bottom: 15px; text-align: left">
    <p style="margin-bottom: 0"><b>Ligue Bois 2 :</b> Contrôlez <b>1 unité</b> pour capturer les Chrono-Balises de l'arène. Les actions spéciales (<code>BOMB</code> et <code>PUSH</code>) seront débloquées dans les ligues supérieures.</p>
  </div>
  <!-- END -->
  <!-- BEGIN level2 -->
  <div style="color: #7cc576; background-color: rgba(124, 197, 118,.1); padding: 20px; margin-bottom: 15px; text-align: left">
    <p style="margin-bottom: 0"><b>Ligue Bois 1 :</b> Vous contrôlez désormais <b>2 unités</b> et débloquez l'action <code>BOMB tx ty</code> (portée &le; 2, recharge 2 tours) pour dégrader les dalles et faire s'effondrer les ponts !</p>
  </div>
  <!-- END -->
  <!-- BEGIN level3 -->
  <div style="color: #7cc576; background-color: rgba(124, 197, 118,.1); padding: 20px; margin-bottom: 15px; text-align: left">
    <p style="margin-bottom: 0"><b>Ligue Bronze+ :</b> Vous contrôlez désormais <b>3 unités</b>, débloquez l'action <code>PUSH tx ty</code> (portée 1, recharge 2 tours) pour pousser les unités adjacentes dans l'abîme, et l'anneau extérieur de l'arène se dégrade tous les 20 tours !</p>
  </div>
  <!-- END -->

  <!-- GOAL -->
  <div class="statement-section statement-goal">
    <h1><span class="icon icon-goal">&nbsp;</span><span>Objectif</span></h1>
    <div class="statement-goal-content">
      Obtenez un score supérieur à votre adversaire en <b>120 tours</b> en capturant les <b>Chrono-Balises</b> et en maintenant votre escouade tactique en vie sur une arène qui s'effondre.
    </div>
  </div>

  <!-- RULES -->
  <div class="statement-section statement-rules">
    <h1><span class="icon icon-rules">&nbsp;</span><span>Règles</span></h1>
    <div class="statement-rules-content">
      <p>La partie se déroule sur une grille symétrique <b>11&times;11</b> où chaque case possède l'un des états suivants :</p>
      <ul>
        <li><const>0</const> : Gouffre / Vide (infranchissable ; toute unité se retrouvant sur une case <const>0</const> est éliminée).</li>
        <li><const>1</const>, <const>2</const>, <const>3</const> : Dalle praticable avec son intégrité structurelle actuelle.</li>
        <li><const>#</const> : Pilier indestructible (obstacle infranchissable).</li>
      </ul>

      <h3 style="font-size: 16px; font-weight: 700; padding-top: 10px">Ordre de Résolution d'un Tour</h3>
      <ol>
        <li><b>Déplacement Simultané (<action>MOVE x y</action>) :</b> Chaque unité avance d'une case orthogonalement le long du plus court chemin praticable vers <var>(x, y)</var>. Si deux unités tentent d'entrer sur la même case ou d'échanger leurs positions, aucune des deux ne bouge.</li>
        <!-- BEGIN level3 -->
        <li><b>Poussée Tactique (<action>PUSH tx ty</action>) :</b> Si <var>cooldown == 0</var> et que <var>(tx, ty)</var> est orthogonalement adjacente (distance de Manhattan de 1), l'unité présente en <var>(tx, ty)</var> est repoussée d'une case dans la même direction :
          <ul>
            <li>Si elle est poussée dans le vide (<const>0</const>) ou hors de la grille, l'unité poussée est <b>éliminée</b> et le pousseur gagne <b>+15 points bonus</b> !</li>
            <li>Si elle est poussée contre un pilier (<const>#</const>) ou une autre unité, la cible reste sur place et son temps de recharge augmente de <const>+2</const> (étourdie).</li>
          </ul>
          Passe le <var>cooldown</var> de l'unité qui pousse à <const>2</const>.
        </li>
        <!-- END -->
        <!-- BEGIN level2 level3 -->
        <li><b>Démolition de Dalle (<action>BOMB tx ty</action>) :</b> Si <var>cooldown == 0</var> et <var>1 &le; DistanceManhattan &le; 2</var>, réduit de <const>1</const> l'intégrité de la dalle <var>(tx, ty)</var>. Si l'intégrité tombe à <const>0</const>, la dalle s'effondre dans le vide : toute balise dessus est détruite et toute unité qui s'y trouve est <b>éliminée</b> ! Passe le <var>cooldown</var> de l'unité à <const>2</const>.</li>
        <!-- END -->
        <!-- BEGIN level3 -->
        <li><b>Tempête Temporelle (Effondrement Annulaire) :</b> Tous les <const>20</const> tours (tours 20, 40, 60, 80, 100), l'anneau extérieur actif de l'arène perd <const>1</const> point d'intégrité (les piliers de cet anneau deviennent des dalles d'intégrité <const>1</const>).</li>
        <!-- END -->
        <li><b>Capture des Balises &amp; Score :</b> Une unité située sur une Chrono-Balise active la capture pour son propriétaire. À la fin de chaque tour, chaque joueur gagne :
          <ul>
            <li><b>+2 points</b> par Chrono-Balise contrôlée,</li>
            <li><b>+1 point</b> par unité en vie.</li>
          </ul>
          Si toutes les unités d'un joueur sont éliminées, la partie s'arrête immédiatement et le survivant reçoit <b>+20 points bonus</b> par unité encore en vie.
        </li>
      </ol>
    </div>
  </div>

  <!-- PROTOCOL -->
  <div class="statement-section statement-protocol">
    <h1><span class="icon icon-protocol">&nbsp;</span><span>Entrées &amp; Sorties du Jeu</span></h1>
    <div class="statement-protocol-content">
      <div class="blk">
        <div class="title">Entrées d'Initialisation</div>
        <div class="text">
          Ligne 1 : <var>width</var> <var>height</var> <var>leagueLevel</var> (<const>11 11</const> et le niveau de la ligue actuelle).
        </div>
      </div>
      <div class="blk">
        <div class="title">Entrées pour un Tour de Jeu</div>
        <div class="text">
          <ul>
            <li>Les <var>height</var> lignes suivantes : chaîne <var>row</var> de longueur <var>width</var> (<const>0</const>, <const>1</const>-<const>3</const>, ou <const>#</const>).</li>
            <li>Ligne suivante : <var>unitCount</var>, le nombre total d'unités en vie.</li>
            <li>Les <var>unitCount</var> lignes suivantes : <var>unitId</var> <var>owner</var> <var>x</var> <var>y</var> <var>cooldown</var> (<var>owner</var> vaut toujours <const>0</const> pour vos unités, listées en premier, et <const>1</const> pour celles de l'adversaire).</li>
            <li>Ligne suivante : <var>beaconCount</var>, le nombre de balises actives.</li>
            <li>Les <var>beaconCount</var> lignes suivantes : <var>bx</var> <var>by</var> <var>beaconOwner</var> (<const>-1</const> = neutre, <const>0</const> = à vous, <const>1</const> = à l'adversaire).</li>
          </ul>
        </div>
      </div>
      <div class="blk">
        <div class="title">Sortie pour un Tour de Jeu</div>
        <div class="text">
          Affichez <b>une ligne par unité en vie que vous possédez</b> (dans l'ordre exact où elles apparaissent en entrée) :
          <ul>
            <li><action>MOVE x y</action> (ou <action>WAIT</action>) éventuellement suivi de :</li>
            <!-- BEGIN level2 level3 -->
            <li><action>BOMB tx ty</action> (distance de Manhattan <const>1</const> ou <const>2</const>, requiert <var>cooldown == 0</var>)</li>
            <!-- END -->
            <!-- BEGIN level3 -->
            <li><action>PUSH tx ty</action> (distance de Manhattan <const>1</const>, requiert <var>cooldown == 0</var>)</li>
            <!-- END -->
          </ul>
          Exemple : <action>MOVE 5 5 BOMB 6 5</action>
        </div>
      </div>
    </div>
  </div>

  <!-- CODE SOURCE -->
  <div class="statement-section statement-examples">
    <h1><span class="icon icon-example">&nbsp;</span><span>Code Source</span></h1>
    <div class="statement-examples-text">
      Le code source complet de l'arbitre (Referee), du viewer et des bots de référence est disponible sur GitHub :
      <a href="https://github.com/codingame-team/codingame-chrono-tactics" target="_blank" rel="noopener noreferrer">https://github.com/codingame-team/codingame-chrono-tactics</a>
    </div>
  </div>
</div>
