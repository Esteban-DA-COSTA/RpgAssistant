# Jdr Assistant — Conception du core

> Document de travail. Il distingue trois statuts :
> **Décidé** (validé explicitement), **Proposé** (piste retenue en discussion, à confirmer) et **Ouvert** (question non tranchée).

## 1. Objectif du core

Le core fournit un modèle générique capable de représenter les fiches, objets, sorts, capacités, etc. de n'importe quel système de JdR, sans embarquer de règles. Les systèmes (D&D en premier) sont apportés par des **modules déclaratifs**.

Le core ne calcule rien à la place du MJ : il stocke, valide la structure des données, synchronise et affiche.

N'importe qui peut créer un module pour son système de jeu, à la manière des systèmes Foundry VTT, sans logique de calcul.

## 2. Décisions

| # | Sujet | Statut | Décision |
|---|-------|--------|----------|
| D1 | Définition / instance | **Décidé** | Une définition est un squelette réutilisable (ex. « Épée longue »). Une instance est une copie dotée d'un cycle de vie (ex. l'épée de Thorin). |
| D2 | Composition | **Décidé** | Un élément porte des valeurs et peut contenir des sous-éléments. Un objet peut vivre seul ou à l'intérieur d'un autre élément. |
| D3 | Valeurs riches | **Décidé** | Les valeurs sont des modèles objet. Le core définit les types de valeurs, qui portent leur fonctionnement (opérations, invariants) et leur état. |
| D4 | Element dans le core | **Décidé** | `Element` est une classe générique du core. |
| D5 | Modules déclaratifs | **Décidé** | Un type comme « PJ » est une **déclaration** du module (données), pas une sous-classe Kotlin. Les modules ne contiennent pas de code compilé. |
| D6 | Modules tiers | **Décidé** | N'importe qui peut créer un module de système de jeu (ex. Pathfinder) : schémas, types de personnages, d'objets, de compétences, bibliothèque de contenus **et interfaces** (layout, style, boutons et leurs actions). Le core interprète le module, côté MJ comme côté mobile. |
| D7 | Modèle de référence | **Décidé** | Inspiré des systèmes Foundry VTT, en moins poussé : pas de logique de calcul ni d'automatisation. |
| D8 | Noms de format | **Décidé** | Chaque sous-classe sérialisée d'une hiérarchie scellée porte un `@SerialName` explicite et stable, de la forme `famille.membre` : famille en minuscules, puis nom Kotlin en camelCase (`resource.damage`, `resource.setTemp`, `element.unknownField`). Une valeur porte le nom de sa famille seul (`resource`, `flag`), et le type de valeur qui la configure porte le même (`{"type": "resource", "min": -10}` dans un Kind). Renommer une classe Kotlin est un refactoring interne ; changer un `@SerialName` est une rupture de format. |
| D9 | Format de l'état | **Décidé** | Un `World` se sérialise en objet JSON dont les clés sont les `ElementId` : l'identifiant reste hors de l'`Element`, et l'unicité tient à la structure. Le décodage vérifie la structure, pas la conformité aux Kinds : un élément incohérent est refusé à la première opération qui le touche (`UnknownKind`, `MissingValue`, `ValueMismatch`). |
| D10 | Kinds d'un module | **Décidé** | Les Kinds d'un module tiennent dans un seul `kinds.json` : une liste de Kinds, chacun portant son `id`. Le chargement décode chaque Kind séparément et rapporte toutes les erreurs d'un coup, repérées par leur position : fichier illisible ou qui n'est pas une liste, Kind mal formé, Kind déclaré plusieurs fois. Une liste plutôt qu'un objet indexé par id, parce qu'une clé JSON en double y serait écrasée sans erreur. |
| D11 | Opérations sur le monde | **Décidé** | `Operation` est une hiérarchie scellée : `UpdateField` (une opération sur un champ d'un élément) et `CreateElement`. Toutes passent par le même `decide` / `evolve`, le même journal et le même `OperationResult`. Une création porte l'élément **complet**, vérifié par `Kind.validate` : le core ne fournit aucune valeur par défaut, ce rôle revient aux Blueprints. L'`ElementId` est choisi par l'émetteur (application optimiste, rejeu du journal) ; le serveur refuse un id déjà pris. |
| D12 | Définition → instance | **Décidé** | Instancier un `Blueprint` **copie** ses valeurs dans un nouvel `Element` (`toElement`), créé ensuite par `CreateElement`. L'élément est autonome : un monde relu n'a pas besoin du compendium, et modifier un Blueprint ne change pas les éléments déjà créés. Un Blueprint porte, comme un élément, les valeurs **complètes** de son Kind : `{"id": "dnd5e:goblin", "kind": "dnd5e:npc", "values": {…}}`. La provenance (`Element.origin`) et les enfants viendront quand un besoin les lira. |
| D13 | Compendium d'un module | **Décidé** | Les Blueprints d'un module vivent dans `compendium/`, en fichiers aux noms libres : chacun contient une liste de Blueprints, et chaque Blueprint porte son `kind`, donc le rangement est l'affaire de l'auteur. Le chargement décode chaque Blueprint séparément, vérifie que son Kind est chargé et qu'il y est conforme (`Kind.validate`), puis rapporte toutes les erreurs d'un coup, repérées par fichier et position : fichier qui n'est pas une liste, Blueprint mal formé, de Kind inconnu, non conforme, ou déclaré plusieurs fois. |
| D14 | Chargement d'un module | **Décidé** | Le manifeste `module.json` ne porte que ce qui est lu : `id` (`ModuleId`) et `title`. Chaque id **déclaré** par le module (Kind, Blueprint) commence par `<id du module>:` ; un `kind` référencé par un Blueprint n'est pas concerné. Le manifeste et les Kinds sont lus indépendamment et leurs erreurs réunies ; le compendium n'est vérifié que contre des Kinds valides, pour ne pas noyer la cause sous des `UnknownKind` dérivés ; le préfixe est vérifié sur le module assemblé. Chaque erreur garde le type de sa source, enveloppé dans `ModuleError`. |
| P7 | UI déclarative riche | Proposé | L'interface d'un module est décrite en données (layout, tokens de style, actions = listes d'opérations, conditions d'affichage, entrées utilisateur). Le core maintient le **vocabulaire**, pas les écrans des modules. |
| P8 | Distribution | Proposé | Les modules sont téléchargés par le mobile **depuis le serveur PC du MJ** en rejoignant la campagne, sans passer par un store. |
| P9 | Système / extension | Proposé | Un *système* définit Kinds, layouts et thème ; une *extension* (bestiaire, homebrew) ajoute du contenu et dépend d'un système. |
| P10 | Rendu compilé | Proposé | Un module reste toujours déclaratif. L'app peut embarquer en plus un **rendu compilé** (widgets Compose) pour certains modules officiels, inséré dans un layout par un nœud `Custom` avec un repli déclaratif obligatoire. Ce rendu ne fait qu'afficher et émettre des opérations (voir section 9.7). |
| P1 | Trois niveaux | Proposé | Type (`Kind`, fourni par le module) → définition (`Blueprint`, compendium) → instance (`Element`, monde). |
| P2 | Modèle unifié | Proposé | Une seule structure (`Element`) ; la distinction « Actor / Item » est un **rôle** porté par le `Kind`, pas deux modèles distincts. |
| P3 | Slots et attributs de lien | Proposé | Les enfants sont rangés dans des slots typés (inventaire, sorts, capacités…). Les attributs de la relation (équipé, préparé, quantité) sont portés par le `Placement`, pas par l'objet. |
| P4 | Opérations typées | Proposé | Toute modification passe par une opération sémantique (`Damage(5)` plutôt que `Set(12)`), validée et ordonnée par le serveur PC, puis journalisée. |
| P5 | Projections | Proposé | Chaque client reçoit une vue de l'état filtrée par son rôle (asymétrie MJ / joueurs). |
| P6 | Pas de valeurs dérivées | Proposé | Pas de type « formule » : il imposerait un langage d'expressions, porte d'entrée d'un moteur de règles. |

## 3. Vocabulaire (noms provisoires)

| Terme | Rôle | Où |
|-------|------|----|
| `Kind` | Schéma d'un type : rôle, champs, slots | Déclaré par un module |
| `FieldDef` | Déclaration d'un champ : clé, `ValueType` + configuration, scope | Dans un `Kind` |
| `FieldKey` | Clé d'un champ (`hp`), unique à l'intérieur de son `Kind` seulement : contrairement à `KindId` et `ElementId`, elle ne désigne rien seule | Dans un `Kind` et un `Element` |
| `SlotDef` | Collection d'enfants typés + attributs de lien | Dans un `Kind` |
| `ValueType` | Type de valeur : configuration, état, opérations | Core |
| `FieldValue` | État d'un champ dans une instance | Core |
| `Blueprint` | Définition réutilisable | Compendium (module ou homebrew MJ) |
| `Element` | Instance vivante | Monde / campagne |
| `Placement` | Lien parent → enfant dans un slot | Monde / campagne |
| `Operation` | Commande typée modifiant une valeur ou la structure | Core |

> Les noms `Kind`, `Blueprint` et `Element` restent ouverts au débat (alternatives évoquées : `Archetype`, `Prototype`, `Template`, `Document`, `Actor` / `Item`).

## 4. Les trois niveaux

```mermaid
flowchart TB
    subgraph Module["Module (ex. D&D)"]
        K["Kind<br/>Arme"]
    end
    subgraph Compendium
        B["Blueprint<br/>Épée longue"]
    end
    subgraph Monde["Monde (campagne)"]
        E["Element<br/>Épée de Thorin"]
    end
    K -- "décrit" --> B
    B -- "instancie" --> E
```

Les trois niveaux ont des cycles de vie différents :

- **Kind** : livré et versionné avec le module, évolue rarement ;
- **Blueprint** : donnée éditable (compendium du module ou homebrew du MJ) ;
- **Element** : état vivant, modifié en permanence pendant la session.

## 5. Répartition du code

```mermaid
flowchart LR
    subgraph Core["Core (Kotlin, commonMain)"]
        direction TB
        Kind
        FieldDef["FieldDef / SlotDef"]
        ValueType
        FieldValue
        Blueprint
        Element
        Placement
        Operation
    end
    subgraph DnD["Module D&D (déclaratif)"]
        direction TB
        KPJ["Kind PJ"]
        KPNJ["Kind PNJ"]
        KArme["Kind Arme"]
        KSort["Kind Sort"]
        SRD["Blueprints SRD"]
        Layouts["Layouts de fiche"]
    end
    subgraph Apps["Apps KMP (PC et mobile)"]
        direction TB
        Serveur["Serveur + journal"]
        Rendu["Rendu générique"]
        Chargeur["Chargeur de modules"]
    end
    DnD -- "utilise les types du core" --> Core
    Core --> Apps
```

- **Core** : code Kotlin écrit une seule fois.
- **Module** : données exprimées avec les types du core, téléchargeables depuis le serveur du MJ (contrainte iOS et politiques des stores : pas de code compilé téléchargé). Peut être écrit par un tiers (voir section 9).
- **Apps** : ne connaissent que le core. Elles affichent une fiche PJ sans savoir ce qu'est un PJ, en lisant les `FieldDef`, `SlotDef` et layouts du module.

Exemple de déclaration côté module (le DSL n'est qu'un outil d'écriture, le résultat est sérialisé) :

```kotlin
val weapon = kind("dnd5e:weapon", role = Role.ITEM) {
    field("damage", Dice, scope = INTRINSIC)
    field("properties", Text(), scope = INTRINSIC)
}

val playerCharacter = kind("dnd5e:pc", role = Role.ACTOR) {
    field("hp", Resource(allowTemp = true), scope = STATE)
    group("abilities") {
        listOf("str", "dex", "con", "int", "wis", "cha")
            .forEach { field(it, Number(min = 1.0, max = 30.0)) }
    }
    slot("inventory", accepts = setOf(weapon)) {
        link("equipped", Flag)
        link("quantity", Number(min = 1.0))
    }
}
```

Une façade typée optionnelle reste possible pour un module embarqué (tests, écran spécifique), sans changer le modèle :

```kotlin
@JvmInline
value class PlayerCharacter(val element: Element) {
    val hp get() = element.resource("hp")
}
```

## 6. Modèle de classes du core

```mermaid
classDiagram
    class Kind {
        +KindId id
        +Int version
        +Role role
        +List~FieldDef~ fields
        +List~SlotDef~ slots
    }
    class FieldDef {
        +FieldKey key
        +ValueType type
        +Scope scope
        +Visibility defaultVisibility
    }
    class SlotDef {
        +String key
        +Set~KindId~ accepts
        +List~FieldDef~ linkFields
    }
    class ValueType {
        <<sealed>>
    }
    class FieldValue {
        <<état immuable>>
    }
    class Blueprint {
        +BlueprintId id
        +KindId kind
        +Map~FieldKey, FieldValue~ values
    }
    class Element {
        +ElementId id
        +KindId kind
        +Origin? origin
        +Map~FieldKey, FieldValue~ values
        +Placement? placement
        +PlayerId? controller
    }
    class Placement {
        +ElementId parent
        +String slot
        +Map~FieldKey, FieldValue~ linkValues
    }
    class Operation {
        <<sealed>>
    }

    Kind "1" *-- "*" FieldDef
    Kind "1" *-- "*" SlotDef
    SlotDef "1" *-- "*" FieldDef : attributs de lien
    FieldDef --> ValueType
    Blueprint ..> Kind : conforme à
    Element ..> Kind : conforme à
    Element ..> Blueprint : origine
    Element "1" *-- "*" FieldValue
    Element "0..1" *-- "1" Placement
    Placement "1" *-- "*" FieldValue
    Placement --> Element : parent
    Operation ..> FieldValue : transforme
```

Notes :

- **Champs dans le code actuel** : tant qu'un champ n'a pas d'autre propriété que son type, `Kind.fields` reste une `Map<FieldKey, ValueType>`, écrite en JSON `"fields": {"hp": {"type": "resource"}}`. `FieldDef` apparaîtra avec le scope ; le format d'un champ changera alors.
- **Scope d'un champ** : `INTRINSIC` (vient de la définition : dégâts, description) ou `STATE` (n'existe que dans l'instance : charges restantes, PV actuels).
- **Composition vs référence** : un sac *contient* une corde (composition, un seul parent, via `Placement`) ; un parchemin *référence* un sort (type de valeur `Reference`).
- **Objet libre** : un `Element` sans `Placement`, ou placé dans un Element « Lieu » / « Réserve ».
- **Empilement** : 50 flèches = une instance avec une quantité, nécessitant des opérations `Split` / `Merge`.

## 7. Types de valeurs

Chaque `ValueType` se décompose en trois parties :

- **configuration** : déclarée dans le `FieldDef` par le module (bornes, options, multiplicité…) ;
- **état** : porté par l'instance, immuable (chaque opération produit un nouvel état) ;
- **opérations** : seules façons légitimes de faire évoluer l'état ; elles garantissent les invariants.

La configuration et le comportement vivent dans la même classe : la sérialisation n'en encode que les propriétés.

```kotlin
@Serializable
sealed interface ValueType {
    fun validate(value: FieldValue): Either<ValueError, FieldValue>        // établit les invariants d'une valeur reçue
    fun decide(value: FieldValue, op: ValueOp): Either<ValueError, ValueOp>  // valide, ou refuse avec une erreur typée
    fun evolve(value: FieldValue, op: ValueOp): FieldValue                  // applique une opération acceptée
}
```

L'ensemble des types est **fermé** (hiérarchie scellée dans le core) : un module compose des types connus, ce qui permet au mobile d'afficher n'importe quel module sans code supplémentaire.

### Catalogue

| Type | Statut | Configuration envisagée | Exemples D&D |
|------|--------|-------------------------|--------------|
| `Text` | **Décidé** | `multiline`, `markdown`, `maxLength` | Nom, notes, description de sort |
| `Number` | **Décidé** | entier / décimal, bornes, affichage signé | Caractéristiques, CA, poids |
| `Flag` (booléen) | **Décidé** | — | Inspiration, harmonisé |
| `Choice` | **Décidé** | source des options, `multiple`, `allowCustom`, `ordered` | Alignement, langues, maîtrise (aucune / demi / maîtrise / expertise) |
| `Resource` | **Décidé** | `min`, max, temporaire, reset | PV, emplacements de sorts, charges, jets contre la mort |
| `Dice` | Proposé | — | Dégâts, dés de vie |
| `Reference` | Proposé | cibles (Kinds), niveau (Blueprint / Element), politique si cible supprimée | Parchemin → sort, familier → maître |
| `Asset` | Proposé | types de média acceptés | Portrait, jeton, illustration |

```mermaid
classDiagram
    class ValueType {
        <<sealed>>
    }
    ValueType <|-- Text
    ValueType <|-- Number
    ValueType <|-- Flag
    ValueType <|-- Choice
    ValueType <|-- Resource
    ValueType <|-- Dice
    ValueType <|-- Reference
    ValueType <|-- Asset

    class Text {
        +Boolean multiline
        +Boolean markdown
        +Int? maxLength
    }
    class Number {
        +Boolean decimal
        +Double? min
        +Double? max
        +Boolean signed
    }
    class Choice {
        +OptionSource source
        +Boolean multiple
        +Boolean allowCustom
        +Boolean ordered
    }
    class Resource {
        +Int min
        +Boolean allowTemp
        +Boolean editableMax
    }
    class Reference {
        +Set~KindId~ targets
        +RefLevel level
        +DanglingPolicy onTargetDeleted
    }
    class Asset {
        +Set~MediaKind~ accepts
    }
```

Exemple d'opérations pour `Resource` :

```kotlin
data class ResourceState(val current: Int, val max: Int, val temp: Int = 0)

sealed interface ResourceOp {
    data class Damage(val amount: Int) : ResourceOp  // consomme temp puis current
    data class Heal(val amount: Int) : ResourceOp    // plafonné à max
    data class SetTemp(val amount: Int) : ResourceOp
    data class SetMax(val max: Int) : ResourceOp
    data object Reset : ResourceOp                    // repos
}
```

### Ce qui n'est pas un type (proposé)

- **Cases à cocher / pastilles** : une `Resource` à petit max ; l'affichage relève du layout.
- **Texte riche** : configuration de `Text`.
- **Groupe** (caractéristiques, monnaie) : regroupement de champs dans le `Kind`, pas un type de valeur — la synchronisation reste au niveau du champ.
- **Valeur dérivée / formule** : exclue (voir P6).

### Frontière avec les règles

Le comportement **structurel** d'un type vit dans le core (les PV ne descendent pas sous le minimum, les dégâts consomment d'abord le temporaire). Les **règles du système** n'y vivent pas (une résistance divise les dégâts par deux) : elles restent l'affaire du MJ.

## 8. Flux d'une opération (proposé)

```mermaid
sequenceDiagram
    participant J as Mobile joueur
    participant S as Serveur PC
    participant L as Journal
    participant MJ as Vue MJ
    participant P as Vues joueurs

    J->>J: apply() optimiste (code commun KMP)
    J->>S: Damage(5) sur "hp"
    S->>S: Vérifie permissions et invariants (ValueType.apply)
    alt Refusée
        S-->>J: Rejet, l'émetteur se recale
    else Acceptée
        S->>L: Événement ordonné
        L-->>MJ: Projection complète
        L-->>P: Projection filtrée par rôle
    end
```

Apports attendus :

- **Commutativité** : deux `Damage` concurrents se composent ; deux `Set` s'écrasent.
- **Historique** lisible (« Thorin : −5 PV par le MJ »), undo, rejeu à la reconnexion.
- **Application optimiste** côté mobile avec la même fonction pure que le serveur.

## 9. Modules de système de jeu

### 9.1 Principe

Un module est un **paquet de données** interprété par le core. Il contient tout ce qui est propre à un système de jeu, interface comprise. Le core fournit un vocabulaire fermé (types de valeurs, primitives de layout, widgets, opérations, tokens de style) ; les auteurs de modules composent avec.

Pourquoi pas de code dans les modules :

- une fonction `@Composable` est du code compilé, elle ne peut pas être transmise comme donnée ;
- iOS interdit l'exécution de code natif téléchargé, et les stores encadrent strictement le code téléchargé ;
- un module venant d'un tiers s'exécute sur le téléphone de chaque joueur : un module déclaratif est **sûr par construction**.

Le principe « pas de règles » rend cette approche réaliste : ce qui exigerait du vrai code (valeurs dérivées, automatisation, résolution d'effets) est hors périmètre.

### 9.2 Correspondance avec Foundry VTT

| Foundry VTT | Jdr Assistant | Remarque |
|---|---|---|
| `system.json` (manifeste) | `module.json` | id, version, compatibilité core, dépendances |
| `template.json` / DataModels | `kinds.json` | Liste des Kinds, avec FieldDef et SlotDef (D10) |
| Compendium packs | `compendium/*.json` | Listes de Blueprints, fichiers aux noms libres (D13) |
| Templates Handlebars | `layouts/*.json` | Arbres de `LayoutNode` |
| CSS | `theme.json` + `assets/` | Tokens, variantes, polices, icônes |
| `lang/*.json` | `i18n/*.json` | Libellés traduisibles |
| JS : logique de fiche | `actions` dans les layouts | Listes d'opérations déclaratives |
| JS : calculs, automatisation | — | Exclus par principe |

### 9.3 Structure d'un paquet (proposé)

```
pf2e/
├── module.json
├── kinds.json
├── compendium/
│   ├── weapons.json
│   ├── spells.json
│   └── ancestries.json
├── layouts/
│   ├── character.sheet.json
│   ├── weapon.compact.json
│   └── weapon.detail.json
├── theme.json
├── i18n/
│   ├── fr.json
│   └── en.json
└── assets/
    ├── fonts/
    └── icons/
```

Manifeste lu aujourd'hui (D14) :

```json
{ "id": "pf2e", "title": "Pathfinder 2e" }
```

Les autres champs arriveront avec leur premier consommateur : `version` (cache des mobiles), `compatibility` (évolution du core), `type` et `dependencies` (extensions, P9), `authors`, `languages`. Un manifeste qui les contient aujourd'hui est refusé (clé inconnue).

Manifeste d'une extension envisagé (homebrew d'une table, proposé) :

```json
{
  "id": "homebrew-table-jeudi",
  "type": "extension",
  "title": "Homebrew de la table du jeudi",
  "version": "0.3.0",
  "compatibility": { "core": { "minimum": "1.0" } },
  "dependencies": [{ "id": "pf2e", "version": ">=1.2" }]
}
```

### 9.4 Interface déclarative (proposé)

L'interface d'un module combine :

- **Layout** : arbre de `LayoutNode` (colonne, ligne, grille, onglets, section, champ, liste d'un slot, libellé…) ;
- **Style** : tokens de thème (couleurs, typographies, arrondis), variantes de composants, assets ;
- **Actions** : listes d'opérations paramétrées, déclenchées par un bouton ;
- **Conditions** : affichage conditionnel (« si `isSpellcaster` ») ;
- **Entrées** : saisie utilisateur réinjectée dans une opération (« demander n, puis `Damage(n)` ») ;
- **Indices d'affichage** (`DisplayHint`) : une `Resource` en barre ou en pastilles, etc.

Chaque Kind d'Item peut fournir plusieurs layouts (`compact` pour une liste, `detail` pour une vue complète).

```json
{
  "type": "Button",
  "label": "Repos long",
  "style": "primary",
  "confirm": "Effectuer un repos long ?",
  "actions": [
    { "op": "resource.reset", "field": "hp" },
    { "op": "resource.reset", "field": "spellSlots.*" },
    { "op": "resource.recover", "field": "hitDice", "fraction": 0.5 },
    { "op": "Notify", "to": "gm", "message": "{name} prend un repos long" }
  ]
}
```

Côté app, un **interpréteur unique** en Compose Multiplatform (PC MJ et mobile) transforme ces descriptions en composables :

```kotlin
@Serializable
sealed interface LayoutNode {
    data class Column(val children: List<LayoutNode>) : LayoutNode
    data class Row(val children: List<LayoutNode>) : LayoutNode
    data class Grid(val columns: Int, val children: List<LayoutNode>) : LayoutNode
    data class Tabs(val tabs: List<Tab>) : LayoutNode
    data class Section(val title: String, val children: List<LayoutNode>) : LayoutNode
    data class Field(val key: FieldKey, val display: DisplayHint? = null) : LayoutNode
    data class SlotList(val slot: String, val itemLayout: LayoutRef, val groupBy: String? = null) : LayoutNode
    data class Button(val label: String, val style: String?, val actions: List<ActionDef>) : LayoutNode
    data class Label(val text: String) : LayoutNode
    data class Custom(val id: String, val fallback: LayoutNode) : LayoutNode
}

@Composable
fun Render(node: LayoutNode, ctx: SheetContext) {
    when (node) {
        is LayoutNode.Column -> Column { node.children.forEach { Render(it, ctx) } }
        is LayoutNode.Field -> FieldWidget(ctx.def(node.key), ctx.value(node.key), node.display, ctx::dispatch)
        is LayoutNode.Button -> ThemedButton(node.label, node.style) { ctx.run(node.actions) }
        // …
    }
}
```

Chaque `ValueType` du core est associé à un widget écrit une fois dans l'app. Les widgets émettent des `Operation`, le flux de validation et de journalisation (section 8) reste inchangé.

### 9.5 Distribution (proposé)

Les modules ne passent pas par un store : ils voyagent du serveur PC du MJ vers les mobiles. Le QR code reste minimal (adresse du serveur, id de campagne, secret d'appairage) ; la liste des modules est obtenue après connexion.

```mermaid
sequenceDiagram
    participant M as Mobile joueur
    participant S as Serveur PC (MJ)

    M->>M: Scan du QR code (adresse, campagne, secret)
    M->>S: Connexion + version de l'app et du core
    S-->>M: Manifeste de campagne (id, version, hash de chaque module)
    M->>M: Comparaison avec le cache local
    loop Modules manquants ou modifiés
        M->>S: Téléchargement du paquet
        S-->>M: Paquet du module
    end
    M->>M: Validation et chargement des modules
    M->>S: Prêt, abonnement aux projections
```

### 9.6 Outillage pour les auteurs (proposé)

- **JSON Schema** publiés pour chaque type de fichier, générés depuis les classes sérialisables du core : autocomplétion et validation dans l'IDE de l'auteur.
- **Validation stricte au chargement** avec des erreurs lisibles (fichier, chemin, cause).
- Plus tard : **rechargement à chaud** dans l'app PC pendant l'écriture d'un module, puis un **éditeur de modules** pour les MJ non développeurs.

### 9.7 Niveaux d'extensibilité

1. **Déclaratif** (socle) : tous les modules, toutes les plateformes.
2. **Rendu compilé dans l'app** (`Custom`, voir P10) : widgets Compose écrits pour des modules officiels, avec repli déclaratif ; non accessibles aux tiers.
2. **Script interprété en sandbox** (ex. Redwood/Treehouse) : porte de sortie éventuelle si le déclaratif atteint ses limites, à condition que le script ne produise que des widgets du catalogue et n'émette que des opérations.

### 9.8 Rendu compilé (proposé)

Certaines interfaces (grimoire avec préparation de sorts, glisser-déposer, visualisations propres à un système) sont difficiles à décrire en données. L'app peut alors embarquer un rendu compilé pour un module officiel comme D&D.

Ce n'est pas un second type de module : le module reste un paquet déclaratif complet, et le rendu compilé est une **couche optionnelle** fournie par l'app.

```json
{
  "type": "Custom",
  "id": "dnd5e.spellbook",
  "fallback": { "type": "SlotList", "slot": "spells", "itemLayout": "spell.compact" }
}
```

Règles :

- **Modèle inchangé** : Kinds, Blueprints, valeurs et opérations viennent du module et du catalogue du core. Le serveur, la validation et la synchronisation ignorent l'existence du rendu compilé.
- **Affichage et opérations seulement** : un rendu compilé reçoit l'état en lecture et `dispatch(Operation)`, comme un widget générique. Il ne calcule rien (voir P6) et ne contourne pas la validation du serveur.
- **Repli obligatoire** : un nœud `Custom` porte toujours un `fallback` déclaratif. Le module reste utilisable sans le code compilé (app pas à jour, système repris par un tiers).
- **Grain du widget** : `Custom` remplace une partie d'un layout, pas une fiche entière. Le reste de l'interface reste déclaratif.

Contraintes :

- Le rendu compilé fait partie du binaire de chaque app (desktop, Android, iOS) : il ne voyage pas depuis le serveur du MJ (voir 9.1), et le faire évoluer demande une nouvelle version de l'app.
- Il est écrit en Compose Multiplatform dans un module dédié (ex. `modules/dnd5e-ui`) dont dépend `app/shared`, une fois pour toutes les plateformes.
- Un besoin couvert par `Custom` qui s'avère générique a vocation à devenir une primitive du vocabulaire déclaratif, disponible pour tous les modules.

## 10. Composants à prévoir (proposé)

- **Visibilité / permissions** par champ et par instance (objet non identifié, piège caché) ; le mobile ne reçoit jamais l'état complet.
- **Compendium** : stockage des Blueprints (module + homebrew).
- **Monde / campagne** : conteneur des instances, frontière de sauvegarde et de sync.
- **Adressage** stable (`elementId` + chemin de champ) pour le ping.
- **Layout** séparé du schéma (voir section 9.4).
- **Migrations** : évolution des Kinds d'une version de module à l'autre et migration des instances existantes.
- **Versioning du format** : chaque version du core annonce les versions du format de module qu'elle sait lire.

## 11. Questions ouvertes

1. **Noms définitifs** : `Kind` / `Blueprint` / `Element`, rôles `Actor` / `Item` ?
2. **Resource** : le max est-il fixé par la définition ou fait-il partie de l'état modifiable par le joueur ?
3. **Dice, Reference, Asset** : à intégrer au catalogue initial ?
4. **Blueprints composés** (ex. paquetage d'explorateur) : à l'instanciation, garder le conteneur ou l'éclater dans l'inventaire ?
5. **Monnaie** : groupe de `Number` sur l'Actor ou objets empilables transférables ?
6. **Permissions d'écriture** : quels champs un joueur peut-il modifier lui-même ?
7. **Cas limites** hors périmètre initial : forme sauvage / métamorphose.
8. **Vocabulaire UI** : liste exacte des primitives de layout, widgets, opérations d'action et tokens de style de la v1.
9. **Format des fichiers de module** : JSON uniquement, ou YAML accepté pour faciliter l'écriture ?
10. **Sécurité** : vérification ou signature des modules transmis par le serveur du MJ ?
