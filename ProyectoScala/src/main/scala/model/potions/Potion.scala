package cl.uchile.dcc.model.potions

import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.items.UsableVisitor
import cl.uchile.dcc.model.units.Character
import cl.uchile.dcc.model.actions.{ConsumePotion, Target}


/** Base implementation shared by all potions. */
abstract class Potion(
                       initialName: String,
                       potionType: PotionType
                     ) extends Usable with Target {


  private val storedName: String = initialName
  private val storedType: PotionType = potionType

  /** Consumes this potion through the character performing the action. */
  override def receiveConsumePotion(
                                     source: Character,
                                     action: ConsumePotion
                                   ): Unit = {
    source.consumePotion(this)
  }

  /** Dispatches this potion to the visitor. */
  override def accept(visitor: UsableVisitor): Unit =
    visitor.visitPotion(this)

  /** Returns this potion's name. */
  override def name: String = storedName

  /** Returns this potion's type. */
  def types: PotionType = storedType

  /** Rejects application unless the concrete potion implements its effect. */
  def applyTo(character: Character): Unit = {
    throw new UnsupportedOperationException(
      "This potion's effect is not implemented."
    )
  }
}