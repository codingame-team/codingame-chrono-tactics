# Chrono-Tactics — CodinGame Multiplayer SDK Project

Un puzzle multijoueur algorithmique complet développé avec le **CodinGame Game Engine (`4.7.7`)**, structuré en **3 ligues progressives** (**Bois 2 → Bois 1 → Bronze+**) et utilisant les assets libres de droits de `codingame-sdk-assets`.

---

## 1. Commandes Rapides

### Lancer le Viewer Web Local (`http://localhost:8888/test.html`)
```bash
cd chrono-tactics-sdk
mvn test-compile exec:java -Dexec.mainClass="Main" -Dexec.classpathScope="test"
```
Puis ouvrez **[http://localhost:8888/test.html](http://localhost:8888/test.html)** dans votre navigateur pour visualiser le replay interactif (avec infobulles `TooltipModule`, bouton d'affichage de grille `ToggleModule`, écran de fin `EndScreenModule`, et bouton **Export** pour générer l'archive `.zip` à téléverser sur [CodinGame Contribute](https://www.codingame.com/contribute/)).

### Lancer le Banc de Simulation Rapide (Sans Serveur Web)
```bash
cd chrono-tactics-sdk
mvn test-compile exec:java -Dexec.mainClass="Main" -Dexec.classpathScope="test" -Dexec.args="--simulate"
```

---

## 2. Structure du Projet

* [`pom.xml`](pom.xml) : Configuration Maven avec `core`, `module-entities`, `module-tooltip`, `module-endscreen`, `module-toggle` et `runner` (`4.7.7`).
* [`config/config.ini`](config/config.ini) : Paramètres du jeu (`type=multi`, `min_players=2`, `max_players=2`).
* [`config/stub.txt`](config/stub.txt) : Générateur de code d'entrée/sortie multi-langages.
* [`config/statement_en.html.tpl`](config/statement_en.html.tpl) & [`config/statement_fr.html`](config/statement_fr.html) : Énoncés HTML par ligue.
* [`config/level1/Boss.java`](config/level1/Boss.java), [`config/level2/Boss.java`](config/level2/Boss.java), [`config/level3/Boss.java`](config/level3/Boss.java) : Boss progressifs pour chaque ligue.
* [`src/main/java/com/codingame/game/Referee.java`](src/main/java/com/codingame/game/Referee.java) : Arbitre complet, gestion des tours simultanés, animations multi-étapes (`commitWorldState`), tooltips et détection de victoire anticipée.
* [`src/main/java/com/codingame/game/Grid.java`](src/main/java/com/codingame/game/Grid.java) : Génération procédurale symétrique d'arène $11 \times 11$ et pathfinding BFS.
* [`src/test/java/Agent1.java`](src/test/java/Agent1.java) : IA tactique avancée (BFS, anticipation à 1 tour des mouvements adverses, esquive de dalles fragiles, `PUSH` et `BOMB` létaux).
