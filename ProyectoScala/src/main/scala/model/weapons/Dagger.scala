package cl.uchile.dcc.model.weapons

import cl.uchile.dcc.model.units.Character

/** Represents a dagger owned by a playable character. */
class Dagger(
              name: String,
              attackPoints: Int,
              weight: Double,
              owner: Character
            ) extends AbstractWeapon(name, attackPoints, weight, owner, WeaponType.Dagger) {

  /** Checks the character's permission to equip this dagger. */
  override def canBeEquippedBy(character: Character): Boolean =
    character.canEquip(this)
}