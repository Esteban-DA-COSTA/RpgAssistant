package com.estebandacosta.jdrassistant.core

import kotlin.jvm.JvmInline

/** Identifiant stable d'un Kind, préfixé par son module (ex. `dnd5e:pc`). */
@JvmInline
value class KindId(val value: String)
