package cl.uchile.dcc.model.map

import cl.uchile.dcc.model.units.GameUnit

/** Represents a map panel, its units and its adjacent panels. */
class Panel(
             initialX: Int,
             initialY: Int,
             initialUnits: Array[GameUnit],
             initialAdjacent: Array[Panel]
           ) {
  private val storedX: Int = initialX
  private val storedY: Int = initialY
  private var storedUnits: Array[GameUnit] = initialUnits.clone()
  private var storedAdjacent: Array[Panel] = initialAdjacent.clone()

  /** Returns this panel's horizontal coordinate. */
  def x: Int = storedX

  /** Returns this panel's vertical coordinate. */
  def y: Int = storedY

  /** Returns a copy of this panel's units. */
  def units: Array[GameUnit] = storedUnits.clone()

  /** Returns a copy of this panel's adjacent panels. */
  def adjacent: Array[Panel] = storedAdjacent.clone()

  /** Replaces this panel's units with a copy of the given array. */
  def setUnits(newUnits: Array[GameUnit]): Unit = {
    storedUnits = newUnits.clone()
  }

  /** Replaces this panel's adjacent panels with a copy of the given array. */
  def setAdjacentPanels(panels: Array[Panel]): Unit = {
    storedAdjacent = panels.clone()
  }
}