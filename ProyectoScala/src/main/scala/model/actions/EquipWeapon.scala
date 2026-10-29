package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.units.Character

/** Represents the weapon equipment action. */
class EquipWeapon(
                   possibleUsables: Array[Usable]
                 ) extends UsableAction("Equip a weapon", possibleUsables) {

  /** Dispatches equipment to the selected target. */
  override def executeFromCharacter(
                                     source: Character,
                                     target: Target
                                   ): Unit = {
    target.receiveEquipWeapon(source, this)
  }
}