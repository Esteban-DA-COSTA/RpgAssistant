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

- **Découper par étape de flux, jamais par fonctionnalité complète.** On construit un mécanisme dans l'ordre où le besoin apparaît, et chaque étape doit expliquer pourquoi la suivante devient nécessaire. Exemple pour les opérations : créer les `Operation`, puis les appliquer à une valeur, puis les refuser proprement, puis les cibler sur un élément, puis les journaliser. Pas de squelette complet d'avance.
- Chaque étape est **petite, compilable et testée** avant de passer à la suivante. Un test par comportement, nommé en phrase lisible.
- **Un chapitre de session par étape** : en ouvrir un au début de chaque étape, titré `Étape N · <sujet>`.
- **Avant d'écrire** : exposer le choix de conception et ses alternatives en quelques phrases, avec une recommandation. **Après** : un encart pédagogique sur ce que le code illustre. On réfléchit ensemble, l'utilisateur tranche.
- Pas d'anticipation dans le code : pas de champ, de paramètre ou d'abstraction « au cas où ». Si une étape future en aura besoin, le dire, ne pas le coder.
- Ne jamais commiter ni pusher sans demande explicite. Le travail se termine sur un working tree modifié, laissé pour relecture.

### Langue et style

- Échanges, documentation et messages de commit en **français**. Code, identifiants, noms de types et `@SerialName` en **anglais**.
- Commentaires de code : l'**usage**, pas le raisonnement. Une doc de classe ou de méthode tient en une à trois lignes et ne raconte ni les alternatives écartées ni l'historique. La pédagogie va dans la conversation, jamais dans le code.
- Noms du vocabulaire du core (`Kind`, `Blueprint`, `Element`, `Placement`, `Operation`, `ValueType`, `FieldValue`…) : ceux du doc de conception, tant qu'ils n'y sont pas renommés.
