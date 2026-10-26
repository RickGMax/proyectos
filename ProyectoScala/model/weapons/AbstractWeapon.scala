package cl.uchile.dcc.model.weapons
import cl.uchile.dcc.model.units.Character

/** Shared implementation for physical and magical weapons. */
abstract class AbstractWeapon(
                               initialName: String,
                               initialAttackPoints: Int,
                               initialWeight: Double,
                               initialOwner: Character,
                               weaponType: WeaponType
                             ) extends Weapon {

  private val storedName: String = initialName
  private val storedAttackPoints: Int = initialAttackPoints
  private val storedWeight: Double = initialWeight
  private val storedOwner: Character = initialOwner
  private val storedType: WeaponType = weaponType

  /** Returns this weapon's name. */
  override def name: String = storedName

  /** Returns this weapon's physical attack points. */
  override def attackPoints: Int = storedAttackPoints

  /** Returns this weapon's weight. */
  override def weight: Double = storedWeight

  /** Returns the playable character that owns this weapon. */
  override def owner: Character = storedOwner

  /** Returns this weapon's type. */
  override def types: WeaponType = storedType
}