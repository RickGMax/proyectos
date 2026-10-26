package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.units.{Character, Enemy}

/** Dispatches a physical attack from its source to its target. */
class Attack extends Action("Attack") {
  /** Dispatches a character's physical attack to the selected target. */
  override def executeFromCharacter(
                                     source: Character,
                                     target: Target
                                   ): Unit = target.receiveCharacterAttack(source, this)

  /**
   * Dispatches an enemy's attack to the selected target.
   *
   * @param source the attacking enemy
   * @param target the entity receiving the attack
   */
  override def executeFromEnemy(source: Enemy,
                                target: Target
                               ): Unit = target.receiveEnemyAttack(source, this)
}
