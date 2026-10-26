package cl.uchile.dcc.model.actions
import cl.uchile.dcc.model.items.Usable

/** Represents the potion consumption action. */
class ConsumePotion(
                     possibleUsables: Array[Usable]
                   ) extends UsableAction("Consume a potion",possibleUsables)