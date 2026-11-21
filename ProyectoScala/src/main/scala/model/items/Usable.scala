package cl.uchile.dcc.model.items

/** Common contract for items that characters can use. */
trait Usable {
  /** Returns this item's name. */
  def name: String

  /** Dispatches this item to the corresponding visitor method. */
  def accept(visitor: UsableVisitor): Unit
}