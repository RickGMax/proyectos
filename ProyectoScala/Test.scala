package cl.uchile.dcc.controller

import munit.FunSuite
import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.potions.{
  HealingPotion, StrengthPotion, ManaPotion, MagicForcePotion,
  Potion, PotionType
}
import cl.uchile.dcc.model.units.{
  Archer, BlackMage, Character, Enemy, GameUnit, Knight,
  MagicCharacter, Thief, WhiteMage
}
import cl.uchile.dcc.model.weapons.{
  Bow, Dagger, MagicWeapon, Staff, Sword, Wand, Weapon, WeaponType
}
import cl.uchile.dcc.model.actions.{
  Action, Attack, ConsumePotion, EquipWeapon, Healing, Meteorite,
  Move, Purification, Source, Target, Thunder, UsableAction
}
import cl.uchile.dcc.model.players.Player
import cl.uchile.dcc.model.map.Panel
import cl.uchile.dcc.model.turns.TurnScheduler


class EntitiesTest extends FunSuite:
  test("all character classes share the character abstraction") {
    val characters: Seq[Character] = Seq(
      new Knight("Knight", 100.0, 10, 50.0),
      new Archer("Archer", 100.0, 10, 50.0),
      new Thief("Thief", 100.0, 10, 50.0),
      new BlackMage("Black mage", 100.0, 10, 50.0, 80.0),
      new WhiteMage("White mage", 100.0, 10, 50.0, 100.0)
    )

    assertEquals(
      characters.map(_.name),
      Seq("Knight", "Archer", "Thief", "Black mage", "White mage")
    )
    assert(characters.forall(_.weapon.isEmpty))
    assert(characters.forall(_.inventory.isEmpty))
  }

  test("both mage classes preserve their mana points") {
    val magicians: Seq[MagicCharacter] = Seq(
      new BlackMage("V", 100.0, 10, 50.0, 80.0),
      new WhiteMage("Gandalf", 100.0, 10, 50.0, 150.0)
    )

    assertEquals(magicians.map(_.manaPoints), Seq(80.0, 150.0))
  }


  test("a character stores its attributes"):
    val character = new Knight(
      "Peter",
      20.0,
      10,
      75.0,
      None,
      Array.empty[Usable]
    )
    assertEquals(character.name,"Peter")
    assertEquals(character.lifePoints,20.0)
    assertEquals(character.defense,10)
    assertEquals(character.weight,75.0)
    assertEquals(character.weapon,None)
    assertEquals(character.inventory.length, 0)


  test("a magic character stores mana points"):
    val magician = new WhiteMage(
      "Gandalf", 80.0, 8, 60.0, 150.0
    )

    assertEquals(magician.name, "Gandalf")
    assertEquals(magician.lifePoints, 80.0)
    assertEquals(magician.defense, 8)
    assertEquals(magician.weight, 60.0)
    assertEquals(magician.weapon, None)
    assertEquals(magician.manaPoints, 150.0)
    assertEquals(magician.inventory.length, 0)

    val character:Character = magician
    val unit:GameUnit = magician

    assertEquals(character.name, "Gandalf")
    assertEquals(unit.name, "Gandalf")

  test("a dagger stores its attributes and owner") {
    val owner = new Thief("Locke", 80.0, 8, 45.0)
    val dagger: Weapon = new Dagger("Assassin dagger", 20, 2.0, owner)

    assertEquals(dagger.name, "Assassin dagger")
    assertEquals(dagger.attackPoints, 20)
    assertEquals(dagger.weight, 2.0)
    assertEquals(dagger.owner, owner)
    assertEquals(dagger.types, WeaponType.Dagger)

    val usable: Usable = dagger
    assertEquals(usable.name, "Assassin dagger")
  }

  test("a bow stores its attributes and owner") {
    val owner = new Archer("Robin", 90.0, 10, 50.0)
    val bow: Weapon = new Bow("Longbow", 25, 4.0, owner)

    assertEquals(bow.name, "Longbow")
    assertEquals(bow.attackPoints, 25)
    assertEquals(bow.weight, 4.0)
    assertEquals(bow.owner, owner)
    assertEquals(bow.types, WeaponType.Bow)

    val usable: Usable = bow
    assertEquals(usable.name, "Longbow")
  }

  test("a enemy stores its attributes"):
    val enemy = new Enemy(
      "Demon",
      50.0,
      15,
      5,
      40.0

    )
    assertEquals(enemy.name, "Demon")
    assertEquals(enemy.lifePoints, 50.0)
    assertEquals(enemy.attackPoints, 15)
    assertEquals(enemy.defense, 5)
    assertEquals(enemy.weight, 40.0)

    val unit: GameUnit = enemy
    assertEquals(unit.name, "Demon")

  test("a potion stores its name and type"):
    val potion = new HealingPotion(
      "Healing potion"
    )
    assertEquals(potion.name, "Healing potion")
    assertEquals(potion.types, PotionType.Heal)
    val usable: Usable = potion
    assertEquals(usable.name, "Healing potion")

  test("all potion classes expose their names and can be used as items") {
    val potions: Seq[Potion] = Seq(
      new HealingPotion("Healing potion"),
      new StrengthPotion("Strength potion"),
      new ManaPotion("Mana potion"),
      new MagicForcePotion("Magic force potion")
    )

    val items: Seq[Usable] = potions

    assertEquals(
      items.map(_.name),
      Seq(
        "Healing potion",
        "Strength potion",
        "Mana potion",
        "Magic force potion"
      )
    )

    assertEquals(
      potions.map(_.types),
      Seq(
        PotionType.Heal,
        PotionType.Strength,
        PotionType.Mana,
        PotionType.MagicForce
      )
    )
  }

  test("a weapon stores its attributes and owner"):
    val owner = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )

    val sword = new Sword(
      "Rebelion",
      30,
      5.0,
      owner,
    )
    assertEquals(sword.name, "Rebelion")
    assertEquals(sword.attackPoints, 30)
    assertEquals(sword.weight, 5.0)
    assertEquals(sword.owner, owner)
    assertEquals(sword.types, WeaponType.Sword)

    val usable:Usable = sword
    assertEquals(usable.name, "Rebelion")

  test("a magic weapon stores magic attack points"):
    val owner = new BlackMage(
      "V", 100.0, 20, 75.0, 150.0
    )

    val staff = new Staff(
      "V staff",
      30,
      40,
      3.0,
      owner
    )

    assertEquals(staff.name, "V staff")
    assertEquals(staff.attackPoints, 30)
    assertEquals(staff.magicAttack, 40)
    assertEquals(staff.weight, 3.0)
    assertEquals(staff.owner, owner)
    assertEquals(staff.types, WeaponType.Staff)

    val usable: Usable = staff
    val weapon: Weapon = staff
    assertEquals(usable.name, "V staff")
    assertEquals(weapon.name, "V staff")


  test("a wand stores physical and magical attack points") {
    val owner = new BlackMage("V", 100.0, 10, 50.0, 80.0)
    val wand: MagicWeapon =
      new Wand("Dark wand", 15, 35, 2.0, owner)

    assertEquals(wand.name, "Dark wand")
    assertEquals(wand.attackPoints, 15)
    assertEquals(wand.magicAttack, 35)
    assertEquals(wand.weight, 2.0)
    assertEquals(wand.owner, owner)
    assertEquals(wand.types, WeaponType.Wand)

    val usable: Usable = wand
    assertEquals(usable.name, "Dark wand")
  }

  test("a character weapon slot can contain a weapon"):
    val character = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )

    val sword = new Sword(
      "Rebelion",
      30,
      5.0,
      character,
    )
    character.equipWeapon(sword)
    assertEquals(character.weapon, Some(sword))
    assertEquals(character.availableActions.map(_.name), Seq("Attack"))
    character.unequipWeapon()
    assertEquals(character.weapon, None)
    assert(character.availableActions.isEmpty)

  test("a character inventory can contain usable item"):
    val character = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )

    val potion = new HealingPotion(
      "Healing potion"
    )

    val sword = new Sword(
      "Rebelion",
      30,
      5.0,
      character,
    )

    character.addToInventory(potion)
    character.addToInventory(sword)
    assertEquals(character.inventory.length, 2)
    assertEquals(character.inventory(0), potion)
    assertEquals(character.inventory(1), sword)

  test("a character protects its inventory from external changes") {
    val potion = new HealingPotion("Healing potion")
    val otherPotion = new ManaPotion("Mana potion")
    val initialItems = Array[Usable](potion)

    val character = new Knight(
      "Dante", 100.0, 20, 75.0,
      None, initialItems
    )

    initialItems(0) = otherPotion
    assertEquals(character.inventory(0), potion)

    val inventoryCopy = character.inventory
    inventoryCopy(0) = otherPotion
    assertEquals(character.inventory(0), potion)
  }

  test("a player is defeated only when all its units are defeated"):
    val character = new Knight(
      "Dante", 100.0, 20, 75.0
      ,None, Array.empty[Usable]
    )
    val enemy = new Enemy("Vergil", 80.0, 20, 10, 40.0)
    val attacker = new Enemy("Mundus", 100.0, 200, 10, 50.0)

    val player = new Player(Seq(character, enemy))

    assertEquals(player.units, Seq[GameUnit](character, enemy))
    assertEquals(player.defeated, false)

    attacker.doAction(new Attack, character)

    assertEquals(character.lifePoints, 0.0)
    assertEquals(player.defeated, false)

    attacker.doAction(new Attack, enemy)

    assertEquals(enemy.lifePoints, 0.0)
    assertEquals(player.defeated, true)


  test("a player without units is defeated"):
    val player = new Player(Seq.empty[GameUnit])

    assertEquals(player.units, Seq.empty[GameUnit])
    assertEquals(player.defeated, true)


  test("a Panel stores coordinates, units and adjacent panels"):
    val character = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )
    val unit: GameUnit = character
    val panelMid = new Panel(2,3,Array(unit),Array.empty[Panel])
    val panelRight = new Panel(3,3,Array.empty[GameUnit],Array.empty[Panel])
    val panelLeft = new Panel(1,3,Array.empty[GameUnit],Array.empty[Panel])

    assertEquals(panelMid.x, 2)
    assertEquals(panelMid.y, 3)
    assertEquals(panelMid.units.length, 1)
    assertEquals(panelMid.units(0), unit)
    assert(panelMid.adjacent.isEmpty)
    panelMid.setAdjacentPanels(Array(panelLeft, panelRight))
    assertEquals(panelMid.adjacent.length,2)
    assertEquals(panelMid.adjacent(0),panelLeft)
    assertEquals(panelMid.adjacent(1),panelRight)


  test("a panel protects its collections from external changes") {
    val first: GameUnit = new Enemy("First", 100.0, 20, 5, 40.0)
    val second: GameUnit = new Enemy("Second", 100.0, 20, 5, 40.0)

    val right = new Panel(
      2, 1, Array.empty[GameUnit], Array.empty[Panel]
    )
    val below = new Panel(
      1, 2, Array.empty[GameUnit], Array.empty[Panel]
    )

    val initialUnits = Array(first)
    val initialAdjacent = Array(right)
    val panel = new Panel(1, 1, initialUnits, initialAdjacent)

    initialUnits(0) = second
    initialAdjacent(0) = below
    assertEquals(panel.units(0), first)
    assertEquals(panel.adjacent(0), right)

    val unitsCopy = panel.units
    val adjacentCopy = panel.adjacent
    unitsCopy(0) = second
    adjacentCopy(0) = below
    assertEquals(panel.units(0), first)
    assertEquals(panel.adjacent(0), right)

    panel.setUnits(initialUnits)
    panel.setAdjacentPanels(initialAdjacent)
    initialUnits(0) = first
    initialAdjacent(0) = right
    assertEquals(panel.units(0), second)
    assertEquals(panel.adjacent(0), below)
  }

  test("a Action stores name and attributes"):
    val attack: Attack = new Attack
    val action:Action = attack
    assertEquals(action.name,"Attack")

  test("a Move stores name and attributes"):
    val move: Move = new Move
    val action: Action = move
    assertEquals(action.name, "Move")

  test("Equip weapon stores name and possibleUsables"):
    val character = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )
    val sword = new Sword(
      "Rebelion",
      30,
      5.0,
      character
    )
    val equipweap: EquipWeapon = new EquipWeapon(Array[Usable](sword))
    val usabaction: UsableAction = equipweap
    assertEquals(usabaction.name, "Equip a weapon")
    assertEquals(usabaction.possibleUsables(0), sword)
    assertEquals(usabaction.possibleUsables.length, 1)

  test("a usable action protects its possible items from external changes") {
    val potion = new HealingPotion("Healing potion")
    val otherPotion = new ManaPotion("Mana potion")
    val initialItems = Array[Usable](potion)

    val action: UsableAction = new ConsumePotion(initialItems)

    initialItems(0) = otherPotion
    assertEquals(action.possibleUsables(0), potion)

    val itemsCopy = action.possibleUsables
    itemsCopy(0) = otherPotion
    assertEquals(action.possibleUsables(0), potion)
  }

  test("Consume potion stores name and possibleUsables"):
    val potion = new HealingPotion(
      "Healing potion"
    )
    val consumep:ConsumePotion = new ConsumePotion(Array[Usable](potion))
    val usabaction:UsableAction = consumep
    assertEquals(usabaction.name,"Consume a potion")
    assertEquals(usabaction.possibleUsables(0),potion)
    assertEquals(usabaction.possibleUsables.length, 1)


  test("a Thunder stores name and attributes"):
    val thunder: Thunder = new Thunder
    val action: Action = thunder
    assertEquals(action.name, "Thunder")



  test("a Meteorite stores name and attributes"):
    val meteorite: Meteorite = new Meteorite
    val action: Action = meteorite
    assertEquals(action.name, "Meteorite")

  test("a Healing stores name and attributes"):
    val healing: Healing = new Healing
    val action: Action = healing
    assertEquals(action.name, "Healing")

  test("a Purification stores name and attributes"):
    val purification: Purification = new Purification
    val action: Action = purification
    assertEquals(action.name, "Purification")



  test("a character calculates its total weight"):
    val character = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )
    assertEquals(character.totalWeight, 75.0)

    val sword = new Sword(
      "Rebelion",
      30,
      5.0,
      character
    )
    character.equipWeapon(sword)
    assertEquals(character.totalWeight, 80.0)

  test("an enemy calculates its total weight"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    assertEquals(enemy.totalWeight, 50.0)

  test("a turn scheduler starts empty"):
    val scheduler = new TurnScheduler

    assert(scheduler.units.isEmpty)


  test("a turn scheduler adds a unit"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val scheduler = new TurnScheduler
    scheduler.addUnit(enemy)
    assertEquals(scheduler.units.length, 1)
    assertEquals(scheduler.units.head,enemy)
    assertEquals(scheduler.barAnalyze(enemy),Some(0.0))

  test("a turn scheduler removes a unit"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val scheduler = new TurnScheduler

    scheduler.addUnit(enemy)
    assertEquals(scheduler.units.length, 1)
    scheduler.removeUnit(enemy)
    assert(scheduler.units.isEmpty)
    assertEquals(scheduler.barAnalyze(enemy),None)

  test("a turn scheduler increases every action bar"):
    val enemy1 = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val enemy2 = new Enemy(
      "Virgilio",
      100.0,
      30,
      10,
      50.0

    )
    val scheduler = new TurnScheduler

    scheduler.addUnit(enemy1)
    scheduler.addUnit(enemy2)

    scheduler.increaseActionBars(15.0)

    assertEquals(scheduler.barAnalyze(enemy1),Some(15.0))
    assertEquals(scheduler.barAnalyze(enemy2),Some(15.0))


  test("a turn scheduler resets one action bar"):
    val enemy1 = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val enemy2 = new Enemy(
      "Virgilio",
      100.0,
      30,
      10,
      50.0

    )
    val scheduler = new TurnScheduler

    scheduler.addUnit(enemy1)
    scheduler.addUnit(enemy2)

    scheduler.increaseActionBars(15.0)
    scheduler.resetActionBar(enemy1)
    

    assertEquals(scheduler.barAnalyze(enemy1), Some(0.0))
    assertEquals(scheduler.barAnalyze(enemy2), Some(15.0))


  test("a turn scheduler detects a completed action bar"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )

    val scheduler = new TurnScheduler
    scheduler.addUnit(enemy)
    scheduler.increaseActionBars(45.0)

    assertEquals(scheduler.hasCompletedBar(enemy),false)
    scheduler.increaseActionBars(10.0)
    assertEquals(scheduler.hasCompletedBar(enemy),true)


  test("completed units are ordered by excess"):
    val enemy1 = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val enemy2 = new Enemy(
      "Virgilio",
      100.0,
      30,
      10,
      40.0

    )
    val enemy3 = new Enemy(
      "Mundus",
      100.0,
      40,
      20,
      100.0
    )

    val scheduler = new TurnScheduler
    scheduler.addUnit(enemy1)
    scheduler.addUnit(enemy2)
    scheduler.addUnit(enemy3)

    scheduler.increaseActionBars(60.0)

    val completed = scheduler.completedUnits

    assertEquals(completed.length,2)
    assertEquals(completed(0),enemy2)
    assertEquals(completed(1),enemy1)


  test("a turn scheduler selects unit with the mayor excess"):
    val enemy1 = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val enemy2 = new Enemy(
      "Virgilio",
      100.0,
      30,
      10,
      40.0

    )
    val scheduler = new TurnScheduler

    scheduler.addUnit(enemy1)
    scheduler.addUnit(enemy2)
    assertEquals(scheduler.nextUnit,None)

    scheduler.increaseActionBars(60.0)

    assertEquals(scheduler.nextUnit,Some(enemy2))


  test("a turn scheduler calculates a maximum action bar"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )

    val scheduler = new TurnScheduler
    scheduler.addUnit(enemy)
    assertEquals(scheduler.maxActionBar(enemy),Some(50.0))


  test("an action bar is completed exactly at its maximum"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )

    val scheduler = new TurnScheduler
    scheduler.addUnit(enemy)
    scheduler.increaseActionBars(50.0)
    assertEquals(scheduler.hasCompletedBar(enemy),true)

  test("A controller selects and stores the next unit"):
    val heavy = new Enemy("Vergil",100.0,30,10, 50.0)
    val light = new Enemy("Mundus",100.0,20,5, 40.0)

    val controller = new GameController(Seq(heavy,light))

    assertEquals(controller.currentUnit, None)

    assertEquals(controller.nextTurn(),Some(light))
    assertEquals(controller.currentUnit,Some(light))

    assertEquals(controller.nextTurn(), Some(heavy))
    assertEquals(controller.currentUnit, Some(heavy))

  test("A controller without units not has next turn"):
    val controller = new GameController(Seq.empty[GameUnit])

    assertEquals(controller.nextTurn(),None)
    assertEquals(controller.currentUnit,None)

  test("A controller rejects a nonpositive total weight"):
    val enemy = new Enemy("Mundus",100.0,20,5, 0.0)
    val controller = new GameController(Seq(enemy))

    intercept[IllegalArgumentException]:
      controller.nextTurn()

    assertEquals(controller.currentUnit, None)

  test(" A controller rejects nonfinite total weight"):
    val enemy = new Enemy("Mundus",100.0,20,5, Double.PositiveInfinity)
    val controller = new GameController(Seq(enemy))

    intercept[IllegalArgumentException]:
      controller.nextTurn()


  test(" resetting the controller restores the initial turn progression"):
    val heavy = new Enemy("Vergil", 100.0, 30, 10, 50.0)
    val light = new Enemy("Mundus", 100.0, 20, 5, 40.0)

    val controller = new GameController(Seq(heavy,light))

    assertEquals(controller.nextTurn(),Some(light))

    controller.resetGame()

    assertEquals(controller.currentUnit, None)

    assertEquals(controller.nextTurn(),Some(light))
    assertEquals(controller.nextTurn(),Some(heavy))

  test(" resetting an empty controller keeps it without an active turn"):
    val controller = new GameController(Seq.empty[GameUnit])

    controller.resetGame()

    assertEquals(controller.currentUnit, None)
    assertEquals(controller.nextTurn(), None)

  test(" an enemy delegates an action with the correct source and target"):
    val enemy = new Enemy("Vergil",100.0,20,5, 50.0)
    val objective = new Enemy("Mundus",100.0,20,5, 40.0)

    var receivedSource: Option[Enemy] = None
    var receivedTarget: Option[Target] = None

    val action: Action = new Action("Recording action") {
      override def executeFromEnemy(source: Enemy,target: Target): Unit ={
        receivedSource = Some(source)
        receivedTarget = Some(target)
      }
    }

    val source:Source = enemy
    val target:Target = objective

    source.doAction(action, target)

    assertEquals(receivedSource,Some(enemy))
    assertEquals(receivedTarget,Some(target))


  test("an enemy rejects an action wihout enemy execution support"):
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val target = new Enemy("Mundus", 100.0, 20, 5, 40.0)

    val action = new Action("Unsupported action"){}

    intercept[UnsupportedOperationException]:
      enemy.doAction(action, target)


  test("action bars include half the equipped weapon weight"):
    val character = new Knight(
      "Dante", 100.0, 20, 75.0,
      None, Array.empty[Usable]
    )

    assertEquals(character.actionBarMaximum, 75.0)

    val sword =
      new Sword("Rebellion", 30, 5.0, character)

    character.equipWeapon(sword)

    assertEquals(character.totalWeight, 80.0)
    assertEquals(character.actionBarMaximum, 77.5)

    val enemy = new Enemy("Demon", 100.0, 20, 5, 78.0)
    val scheduler = new TurnScheduler

    scheduler.addUnit(character)
    scheduler.addUnit(enemy)

    assertEquals(scheduler.maxActionBar(character), Some(77.5))

    scheduler.increaseActionBars(77.0)
    assertEquals(scheduler.hasCompletedBar(character), false)

    scheduler.increaseActionBars(0.5)
    assertEquals(scheduler.hasCompletedBar(character), true)

    scheduler.increaseActionBars(0.5)

    assertEquals(scheduler.completedUnits, Seq[GameUnit](character, enemy))
    assertEquals(scheduler.nextUnit, Some(character))


  test("an enemy attack reduces a character's health"):
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val character = new Knight(
      "Dante", 100.0, 10, 75.0,
      None, Array.empty[Usable]
    )

    val source: Source = enemy
    val action: Action = new Attack
    val target: Target = character

    source.doAction(action, target)

    assertEquals(character.lifePoints, 80.0) // 100 - (30 - 10)
    assertEquals(enemy.lifePoints, 100.0)


  test("defense greater than attack prevents damage"):
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val target = new Enemy("Mundus", 80.0, 20, 40, 40.0)

    enemy.doAction(new Attack, target)

    assertEquals(target.lifePoints, 80.0)


  test("an attack cannot reduce health below zero"):
    val enemy = new Enemy("Vergil", 100.0, 50, 5, 50.0)
    val target = new Enemy("Mundus", 10.0, 20, 5, 40.0)

    enemy.doAction(new Attack, target)

    assertEquals(target.lifePoints, 0.0)


  test("a defeated enemy cannot perform an action"):
    val enemy = new Enemy("Vergil", 0.0, 30, 5, 50.0)
    val target = new Enemy("Mundus", 100.0, 20, 5, 40.0)

    intercept[IllegalArgumentException]:
      enemy.doAction(new Attack, target)

    assertEquals(target.lifePoints, 100.0)


  test("an attack rejects a defeated target"):
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val target = new Enemy("Mundus", 0.0, 20, 5, 40.0)

    intercept[IllegalArgumentException]:
      enemy.doAction(new Attack, target)

    assertEquals(target.lifePoints, 0.0)


  test("an attack rejects an unsupported target"):
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val target = new Target {}

    intercept[UnsupportedOperationException]:
      enemy.doAction(new Attack, target)

  test("an enemy exposes and finds its available actions"):
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val source: Source = enemy

    assertEquals(source.name, "Vergil")
    assertEquals(source.availableActions.map(_.name), Seq("Attack"))
    assertEquals(source.findAction("Attack"), source.availableActions.headOption)
    assertEquals(source.findAction("Unknown"), None)

  test("a defeated enemy has no available actions"):
    val enemy = new Enemy("Vergil", 0.0, 20, 5, 50.0)

    assertEquals(enemy.availableActions, Seq.empty[Action])
    assertEquals(enemy.findAction("Attack"), None)


  test("a controller lists actions for a unit outside its active turn"):
    val heavy = new Enemy("Vergil", 100.0, 30, 10, 50.0)
    val light = new Enemy("Mundus", 100.0, 20, 5, 40.0)
    val controller = new GameController(Seq(heavy, light))

    controller.nextTurn()

    assertEquals(
      controller.availableActions(heavy).map(_.name),
      Seq("Attack")
    )
    assertEquals(controller.currentUnit, Some(light))

  test("a controller reflects changes in available actions"):
    val enemy = new Enemy("Vergil", 100.0, 30, 10, 50.0)
    val controller = new GameController(Seq(enemy))

    assertEquals(
      controller.availableActions(enemy).map(_.name),
      Seq("Attack")
    )

    val attacker = new Enemy("Attacker", 100.0, 200, 0, 50.0)
    attacker.doAction(new Attack, enemy)

    assertEquals(
      controller.availableActions(enemy),
      Seq.empty[Action]
    )
    assertEquals(controller.currentUnit, None)


  test("an enemy defeated after creation cannot perform actions"):
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val target = new Enemy("Mundus", 100.0, 20, 10, 40.0)

    val attacker = new Enemy("Attacker", 100.0, 200, 0, 50.0)
    attacker.doAction(new Attack, enemy)

    intercept[IllegalArgumentException]:
      enemy.doAction(new Attack, target)

    assertEquals(target.lifePoints, 100.0)

  test("a controller rejects a nonpositive action bar maximum") {
    Seq(0.0, -1.0).foreach { maximum =>
      val enemy = new Enemy("Vergil", 100.0, 30, 10, 50.0) {
        override def actionBarMaximum: Double = maximum
      }

      val controller = new GameController(Seq(enemy))

      intercept[IllegalArgumentException] {
        controller.nextTurn()
      }

      assertEquals(controller.currentUnit, None)
    }
  }

  test("a controller executes the active character's attack") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 10, 50.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)

    character.equipWeapon(sword)

    val controller = new GameController(Seq(character, enemy))

    assertEquals(controller.nextTurn(), Some(character))

    controller.executeAction(character, "Attack", enemy)

    assertEquals(enemy.lifePoints, 80.0)
    assertEquals(character.lifePoints, 100.0)
  }

  test("a controller rejects actions before or outside the source's turn") {
    val heavy = new Enemy("Heavy", 100.0, 30, 5, 50.0)
    val light = new Enemy("Light", 100.0, 20, 5, 40.0)
    val controller = new GameController(Seq(heavy, light))

    intercept[IllegalArgumentException] {
      controller.executeAction(heavy, "Attack", light)
    }

    assertEquals(light.lifePoints, 100.0)
    assertEquals(controller.currentUnit, None)

    assertEquals(controller.nextTurn(), Some(light))

    intercept[IllegalArgumentException] {
      controller.executeAction(heavy, "Attack", light)
    }

    assertEquals(light.lifePoints, 100.0)
    assertEquals(controller.currentUnit, Some(light))
  }

  test("a controller rejects an unavailable action during an active turn") {
    val attacker = new Enemy("Attacker", 100.0, 30, 5, 40.0)
    val target = new Enemy("Target", 100.0, 20, 10, 50.0)
    val controller = new GameController(Seq(attacker, target))

    assertEquals(controller.nextTurn(), Some(attacker))

    intercept[IllegalArgumentException] {
      controller.executeAction(attacker, "Unknown", target)
    }

    assertEquals(target.lifePoints, 100.0)
    assertEquals(controller.currentUnit, Some(attacker))
  }

  test("an unarmed character cannot attack") {
    val character = new Knight("Dante", 100.0, 10, 75.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)

    intercept[IllegalArgumentException] {
      character.doAction(new Attack, enemy)
    }

    assertEquals(enemy.lifePoints, 100.0)
    assertEquals(character.lifePoints, 100.0)
    assert(character.availableActions.isEmpty)
  }

  test("a character defeated after creation cannot perform actions") {
    val character = new Knight("Dante", 100.0, 10, 75.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)
    val enemy = new Enemy("Vergil", 100.0, 200, 5, 50.0)

    character.equipWeapon(sword)
    assertEquals(character.availableActions.map(_.name), Seq("Attack"))

    enemy.doAction(new Attack, character)

    assertEquals(character.lifePoints, 0.0)
    assert(character.availableActions.isEmpty)

    intercept[IllegalArgumentException] {
      character.doAction(new Attack, enemy)
    }

    assertEquals(enemy.lifePoints, 100.0)
  }



















  
  
  