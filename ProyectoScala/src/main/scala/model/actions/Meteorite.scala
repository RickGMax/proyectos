package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.units.Character

/** Represents the area spell available to black mages. */
class Meteorite extends Action("Meteorite") {
  /** Returns this spell's mana cost. */
  def manaCost: Double = 50.0

  /** Dispatches casting according to the character's capabilities. */
  override def executeFromCharacter(
                                     source: Character,
                                     target: Target
                                   ): Unit = {
    source.castMeteorite(this, target)
  }
}