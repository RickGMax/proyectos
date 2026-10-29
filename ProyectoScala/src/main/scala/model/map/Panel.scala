package cl.uchile.dcc.model.map

import cl.uchile.dcc.model.units.{BlackMage, GameUnit}
import cl.uchile.dcc.model.actions.{Meteorite, Move, Target}


/** Represents a map panel, its units and its adjacent panels. */
class Panel(
             initialX: Int,
             initialY: Int,
             initialUnits: Array[GameUnit],
             initialAdjacent: Array[Panel]
           ) extends Target {
  private val storedX: Int = initialX
  private val storedY: Int = initialY
  private var storedUnits: Array[GameUnit] = Array.empty[GameUnit]
  private var storedAdjacent: Array[Panel] = initialAdjacent.clone()

  initialUnits.clone().foreach(addUnit)

  /** Returns this panel's horizontal coordinate. */
  def x: Int = storedX

  /** Returns this panel's vertical coordinate. */
  def y: Int = storedY

  /** Returns a copy of this panel's units. */
  def units: Array[GameUnit] = storedUnits.clone()

  /** Returns a copy of this panel's adjacent panels. */
  def adjacent: Array[Panel] = storedAdjacent.clone()

  /** Returns whether this unit is present in the panel. */
  def containsUnit(unit: GameUnit): Boolean =
    storedUnits.exists(_ eq unit)

  /** Places a unit here and removes it from its previous panel. */
  def addUnit(unit: GameUnit): Unit = {
    unit.currentPanel
      .filterNot(_ eq this)
      .foreach(_.removeUnit(unit))

    if (!containsUnit(unit)) {
      storedUnits = storedUnits :+ unit
    }

    unit.updatePanel(Some(this))
  }

  /** Removes a unit and clears its position when it belongs to this panel. */
  def removeUnit(unit: GameUnit): Unit = {
    storedUnits = storedUnits.filterNot(_ eq unit)

    if (unit.currentPanel.exists(_ eq this)) {
      unit.updatePanel(None)
    }
  }

  /** Replaces this panel's units and updates their positions. */
  def setUnits(newUnits: Array[GameUnit]): Unit = {
    val replacements = newUnits.clone()

    storedUnits.clone().foreach(removeUnit)
    replacements.foreach(addUnit)
  }

  /** Replaces this panel's adjacent panels with a copy of the given array. */
  def setAdjacentPanels(panels: Array[Panel]): Unit = {
    storedAdjacent = panels.clone()
  }

  /** Validates all units before applying Meteorite damage to each one. */
  override def receiveMeteorite(
                                 source: BlackMage,
                                 action: Meteorite,
                                 damage: Double
                               ): Unit = {
    val targets = storedUnits.distinct

    require(
      damage.isFinite && damage >= 0.0,
      "Magical damage must be finite and non-negative."
    )
    require(
      targets.forall(_.lifePoints > 0.0),
      "Meteorite cannot affect defeated units."
    )

    targets.foreach(_.receiveMeteoriteDamage(source, action, damage))
  }

  /** Receives a moving unit after its movement requirements are validated. */
  override def receiveMove(source: GameUnit, action: Move): Unit = {
    source.moveTo(this)
  }
}