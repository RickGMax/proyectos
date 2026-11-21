package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.units.Character

/** Represents the thunder spell available to black mages. */
class Thunder extends Action("Thunder") {
  /** Returns this spell's mana cost. */
  def manaCost: Double = 20.0

  /** Dispatches casting according to the character's capabilities. */
  override def executeFromCharacter(
                                     source: Character,
                                     target: Target
                                   ): Unit = {
    source.castThunder(this, target)
  }
}