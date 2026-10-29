package cl.uchile.dcc.model.weapons

import cl.uchile.dcc.model.units.Character

/** Represents a bow owned by a playable character. */
class Bow(
           name: String,
           attackPoints: Int,
           weight: Double,
           owner: Character
         ) extends AbstractWeapon(name, attackPoints, weight, owner, WeaponType.Bow) {

  /** Checks the character's permission to equip this bow. */
  override def canBeEquippedBy(character: Character): Boolean =
    character.canEquip(this)
}