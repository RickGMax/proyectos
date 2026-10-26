package cl.uchile.dcc.model.weapons
import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.units.Character

/** Common contract implemented by all weapons. */
trait Weapon extends Usable {

  /** Returns this weapon's physical attack points. */
  def attackPoints: Int

  /** Returns this weapon's weight. */
  def weight: Double

  /** Returns the playable character that owns this weapon. */
  def owner: Character

  /** Returns this weapon's type. */
  def types: WeaponType
}