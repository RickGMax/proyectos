package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.units.Character

/** Represents the healing spell available to white mages. */
class Healing extends Action("Healing") {
  /** Returns this spell's mana cost. */
  def manaCost: Double = 15.0

  /** Dispatches casting according to the character's capabilities. */
  override def executeFromCharacter(
                                     source: Character,
                                     target: Target
                                   ): Unit = {
    source.castHealing(this, target)
  }
}