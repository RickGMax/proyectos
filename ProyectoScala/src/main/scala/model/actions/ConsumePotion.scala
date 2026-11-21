package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.units.Character

/** Represents the potion consumption action. */
class ConsumePotion(possibleUsables: Array[Usable])
  extends UsableAction("Consume a potion", possibleUsables) {

  /** Dispatches consumption to the selected target. */
  override def executeFromCharacter(
                                     source: Character,
                                     target: Target
                                   ): Unit = {
    target.receiveConsumePotion(source, this)
  }
}