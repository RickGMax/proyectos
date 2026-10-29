package cl.uchile.dcc.model.weapons
import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.units.Character
import cl.uchile.dcc.model.actions.Target

/** Common contract implemented by all weapons. */
trait Weapon extends Usable with Target {

  /** Returns whether this weapon can be equipped by the given character. */
  def canBeEquippedBy(character: Character): Boolean

  /** Returns no spell power for weapons without magical capabilities. */
  def spellPower: Option[Double] = None

  /** Returns this weapon's physical attack points. */
  def attackPoints: Int

  /** Returns this weapon's weight. */
  def weight: Double

  /** Returns the playable character that owns this weapon. */
  def owner: Character

  /** Returns this weapon's type. */
  def types: WeaponType
}