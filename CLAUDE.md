# Jdr Assistant

Compagnon de table pour le jeu de rôle **en présentiel** : il s'ajoute à la partie, il ne la remplace pas. Le PC du MJ fait serveur local, les mobiles des joueurs s'y connectent par QR code. Fiches synchronisées en temps réel, ping d'un élément (objet, sort…) vers les autres, messages privés.

Volontairement **sans règles** : pas de jets de dés, pas de calculs, pas de valeurs dérivées. Le core stocke, valide la structure, synchronise et affiche.

Deux couches :

- **Core** : modèle générique en Kotlin commun. Trois niveaux `Kind` (schéma fourni par un module) → `Blueprint` (définition réutilisable, compendium) → `Element` (instance vivante dans la campagne). Valeurs riches typées (catalogue fermé : `Text`, `Number`, `Flag`, `Choice`, `Resource`…), opérations typées (`Damage(5)` plutôt que `Set(12)`), validées et journalisées par le serveur.
- **Modules de système de jeu** (D&D en premier) : **données déclaratives** en JSON, sans code. Kinds, blueprints, layouts, thème, i18n. Téléchargés depuis le serveur du MJ, interprétés par les apps. Un module *cite* le vocabulaire du core (types, opérations, primitives de layout), il ne le définit jamais.

## Documentation

- [README.md](README.md) : vision produit, périmètre et hors périmètre.
- [docs/conception-core.md](docs/conception-core.md) : conception du core. Décisions avec statut (**Décidé** / **Proposé** / **Ouvert**), vocabulaire, modèle de classes, catalogue des types de valeurs, flux d'une opération, format des modules, questions ouvertes.

Ce doc est la **source de vérité du modèle**. Avant d'implémenter quelque chose qui touche au modèle, vérifier le statut du point concerné. Quand une discussion tranche un point *Proposé* ou *Ouvert*, reporter la décision dans le doc dans le même temps.

## Stack et commandes

- **Kotlin Toolchain** (ex-Amper), pas Gradle. Wrapper `./kotlin` à la racine, version épinglée dans le script. `project.yaml` liste les modules, chaque module a son `module.yaml`. Doc : <https://kotlin-toolchain.org/>.
- Layout d'un module : `src/` (commun), `src@jvm/`, `src@android/`, `src@ios/` (spécifique), `test/`, `resources/`. `kotlin.test` est fourni automatiquement aux tests.
- Modules : `core` (kmp/lib, cibles jvm, android, iosArm64, iosSimulatorArm64), `app/shared` (Compose Multiplatform), `app/androidApp`, `app/desktopApp`, `app/iosApp` (points d'entrée), `server` (Ktor, bibliothèque JVM démarrée par `desktopApp`).
- Plateformes visées : **desktop** (PC du MJ), **Android** et **iOS** (joueurs). Pas de version web. Le serveur tourne dans l'app desktop du MJ : il démarre avec elle et s'arrête à la fermeture de la fenêtre.
- Package racine : `com.estebandacosta.jdrassistant`. Le core vit sous `com.estebandacosta.jdrassistant.core`.
- Suivi des tickets : projet YouTrack **RPG**, accessible via le serveur MCP `youtrack` (déclaré dans `.mcp.json`, lit `YOUTRACK_URL` et `YOUTRACK_TOKEN` dans l'environnement).

```bash
./kotlin build -m core            # compile le core pour toutes ses plateformes
./kotlin test -m core -p jvm      # tests du core, JVM seulement (boucle rapide)
./kotlin test -m core             # tests du core sur toutes les plateformes
./kotlin show modules             # liste des modules (-m prend le nom court : shared, desktopApp…)
```

Le core doit rester compilable sur **toutes** ses cibles : pas de `java.*` en `src/`, pas d'IO fichier, pas de réflexion. Les dépendances plateforme passent par `expect`/`actual` ou par des interfaces implémentées dans les apps.

## Mode de travail

### Profil

- À l'aise avec la syntaxe et les mécaniques Kotlin (`sealed`, `value class`, `when`, lambdas, génériques, coroutines de base). Ne pas expliquer la syntaxe.
- Veut **apprendre les mécanismes de conception que Kotlin rend naturels** : types de résultat et monades (`Result`, `Either`), modélisation des erreurs métier à la Arrow (erreurs typées plutôt qu'exceptions), immutabilité et fonctions pures, hiérarchies scellées comme machines à états, séparation donnée / comportement. Expliquer le **pourquoi** du mécanisme, le problème qu'il résout et ce qu'on perdrait sans lui.
- **Néophyte sur KMP** au-delà du hello world : connaît `expect`/`actual` et le layout, n'a jamais mené de projet KMP poussé. Expliquer les contraintes multiplateformes quand elles apparaissent (ce qui manque en commun, spécificités iOS, sérialisation, threading), au moment où elles se présentent et pas avant.

### Déroulé des tâches

- **Une US = un ticket YouTrack (projet RPG), découpé en plusieurs étapes.** En commençant une US, passer son ticket à l'état **In Progress** dans YouTrack sans attendre de demande, puis créer une branche portant l'id du ticket, de la forme `RPG-<n>-<sujet-court>` (ex. `RPG-38-persistance-elements`), puis préfixer chaque message de commit par cet id (ex. `RPG-38 Écrit le journal des opérations dans l'archive`) pour que YouTrack rattache les commits au ticket.
- **Découper par étape de flux, jamais par fonctionnalité complète.** On construit un mécanisme dans l'ordre où le besoin apparaît, et chaque étape doit expliquer pourquoi la suivante devient nécessaire. Exemple pour les opérations : créer les `Operation`, puis les appliquer à une valeur, puis les refuser proprement, puis les cibler sur un élément, puis les journaliser. Pas de squelette complet d'avance.
- Chaque étape est **petite, compilable et testée** avant de passer à la suivante. Un test par comportement, nommé en phrase lisible.
- **Une étape = un chapitre de session + un commit** : ouvrir un chapitre au début de chaque étape, titré `Étape N · <sujet>`, et la clore par un commit. Ce commit n'est fait qu'**après validation de l'étape par l'utilisateur**, jamais dès la fin de l'implémentation : le travail attend sa relecture sur un working tree modifié.
- **Avant d'écrire** : exposer le choix de conception et ses alternatives en quelques phrases, avec une recommandation. **Après** : un encart pédagogique sur ce que le code illustre. On réfléchit ensemble, l'utilisateur tranche.
- Pas d'anticipation dans le code : pas de champ, de paramètre ou d'abstraction « au cas où ». Si une étape future en aura besoin, le dire, ne pas le coder.
- Hors du commit de fin d'étape, ne jamais commiter sans demande explicite. Ne jamais pusher sans demande explicite.

### Design et interface

- Le design de référence vit dans un canvas Design claude.ai : <https://claude.ai/artifact/AoZuc1Nh67SGgsf8ucDBWG> (« Assistant JdR – Desktop & Mobile »), une ligne de maquettes par thème (Fantasy, Sci-fi). Le consulter avant toute étape qui touche l'interface.
- **Maquette d'abord, code ensuite** : une étape qui ajoute ou change un écran commence par sa maquette dans le canvas, en **Fantasy et Sci-fi** (même si l'app n'implémente que Fantasy), validée par l'utilisateur avant d'écrire le Compose. Le canvas est tenu à jour à chaque étape d'interface.
- Avant de publier dans le canvas, relire les fichiers concernés : l'utilisateur peut l'avoir retouché à la main. En cas de conflit, réappliquer ses changements sur la version en ligne, ne jamais écraser.
- Les maquettes sont une **cible** : elles peuvent montrer des éléments pas encore codés. On ne code d'une maquette que ce qui relève de l'étape en cours.
- En Compose, **factoriser les composants réutilisables** (dans `components/`, couleurs, typographies, gemmes et icônes dans le thème) plutôt que de dupliquer du style dans les écrans ; une valeur de style nouvelle devient un token du thème.

### Langue et style

- Échanges, documentation et messages de commit en **français**. Code, identifiants, noms de types et `@SerialName` en **anglais**.
- Commentaires de code : l'**usage**, pas le raisonnement. Une doc de classe ou de méthode tient en une à trois lignes et ne raconte ni les alternatives écartées ni l'historique. La pédagogie va dans la conversation, jamais dans le code.
- Noms du vocabulaire du core (`Kind`, `Blueprint`, `Element`, `Placement`, `Operation`, `ValueType`, `FieldValue`…) : ceux du doc de conception, tant qu'ils n'y sont pas renommés.
