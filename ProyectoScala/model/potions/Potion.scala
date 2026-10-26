package cl.uchile.dcc.model.potions

import cl.uchile.dcc.model.items.Usable

/** Base implementation shared by all potions. */
abstract class Potion(
                       initialName: String,
                       potionType: PotionType
                     ) extends Usable {

  private val storedName: String = initialName
  private val storedType: PotionType = potionType

  /** Returns this potion's name. */
  override def name: String = storedName

  /** Returns this potion's type. */
  def types: PotionType = storedType
}