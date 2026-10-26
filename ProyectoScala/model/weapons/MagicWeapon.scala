package cl.uchile.dcc.model.weapons
import cl.uchile.dcc.model.units.Character

/** Base implementation shared by magical weapons. */
abstract class MagicWeapon(
                            name: String,
                            attackPoints: Int,
                            initialMagicAttack: Int,
                            weight: Double,
                            owner: Character,
                            weaponType: WeaponType
                          ) extends AbstractWeapon(name, attackPoints, weight, owner, weaponType) {

  private val storedMagicAttack: Int = initialMagicAttack

  /** Returns this weapon's magical attack points. */
  def magicAttack: Int = storedMagicAttack
}