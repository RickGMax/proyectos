package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.units.Character

/** Represents the purification spell available to white mages. */
class Purification extends Action("Purification") {
  /** Returns this spell's mana cost. */
  def manaCost: Double = 25.0

  /** Dispatches casting according to the character's capabilities. */
  override def executeFromCharacter(
                                     source: Character,
                                     target: Target
                                   ): Unit = {
    source.castPurification(this, target)
  }
}