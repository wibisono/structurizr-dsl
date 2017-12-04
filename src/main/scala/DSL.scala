import java.io.File

import PlainStructurizr.views
import com.structurizr.Workspace
import com.structurizr.model.{Component, Container}
import com.structurizr.view.PaperSize

object DSL {

  trait Element {

  }

  case class Worksheet(name: String, description: String,
                       persons: List[DPerson],
                       softwareSystems: List[DSoftwareSystem],
                       relations: List[DRelation]
                      )

  case class DPerson(name: String, description: String) extends Element

  case class DSoftwareSystem(name: String, description: String, containers: List[DContainer]) extends Element

  case class DContainer(name: String, description: String, technology: String, components: List[DComponent]) extends Element

  case class DComponent(name: String, description: String) extends Element

  case class DRelation(source: Element, destination: Element, description: String)

  def generate(worksheet: Worksheet) = {
    val workspace = new Workspace(worksheet.name, worksheet.description)
    val model = workspace.getModel

    val personMap: Map[DPerson, com.structurizr.model.Person] = worksheet.persons.map(person => (person, model.addPerson(person.name, person.description))).toMap

    val systemMap: Map[DSoftwareSystem, com.structurizr.model.SoftwareSystem] = worksheet.softwareSystems.map(system => (system, model.addSoftwareSystem(system.name, system.description))).toMap

    val containerMap: Map[DContainer, Container] = worksheet.softwareSystems.flatMap(dSystem => dSystem.containers.map(dContainer => (dContainer, systemMap(dSystem)
      .addContainer(dContainer.name, dContainer.description, dContainer.technology)))).toMap

    val componentMap: Map[DComponent, Component] = containerMap.flatMap {
      case (dContainer, container) => dContainer.components.map(dComponent => (dComponent, container.addComponent(dComponent.name, dComponent.description)))
    }

    worksheet.relations.foreach {
      case DRelation(aPerson: DPerson, aSystem: DSoftwareSystem, description: String) =>
        personMap(aPerson).uses(systemMap(aSystem), description)
      case DRelation(aPerson: DPerson, aContainer: DContainer, description: String) =>
        personMap(aPerson).uses(containerMap(aContainer), description)
      case DRelation(aPerson: DPerson, aComponent: DComponent, description: String) =>
        personMap(aPerson).uses(componentMap(aComponent), description)
      case DRelation(aContainer: DContainer, bContainer:DContainer, description: String) =>
        containerMap(aContainer).uses(containerMap(bContainer), description)
    }

    (workspace, systemMap.values, containerMap.values, personMap.values)
  }

}

object Test extends App {

  import DSL._

  val person1 = DPerson("Customer", "Of the system")

  val myComponent = DComponent("Inventory","Inventory Component")
  val myContainer = DContainer("Container One", "My Container 1", "Play", List(myComponent))
  val myContainer1 = DContainer("Container Two", "My Container 2", "Play 2", List.empty)
  val mySystem = DSoftwareSystem("My System", "My Sys desc", List(myContainer, myContainer1))

  val personUses = DRelation(person1, myContainer, "uses")
  val containerUses = DRelation(myContainer, myContainer1, "needs another container")

  val worksheet: Worksheet = Worksheet("Something", "description",
    List(person1),
    List(mySystem),
    List(personUses, containerUses))

  val (workspace, systems, containers, persons) = generate(worksheet)

  val enterpriseContextView = workspace.getViews.createEnterpriseContextView("EnterpriseContext", "The enterprise context diagram for the Internet Banking System.")
  enterpriseContextView.addAllElements()
  enterpriseContextView.setPaperSize(PaperSize.A5_Landscape)

  val systemContextView = workspace.getViews.createSystemContextView(systems.head, systems.head.getCanonicalName, systems.head.getDescription)
  systemContextView.addNearestNeighbours(systems.head)
  systemContextView.setPaperSize(PaperSize.A5_Landscape)

  val containerView = workspace.getViews.createContainerView(systems.head, systems.head.getName, systems.head.getDescription)
  containerView.add(persons.head)
  containerView.addAllContainers()
  containerView.add(systems.head)
  containerView.setPaperSize(PaperSize.A5_Landscape)


  val componentView = workspace.getViews.createComponentView(containers.head, containers.head.getName, containers.head.getDescription)
  componentView.addAllContainers
  componentView.addAllComponents
  componentView.setPaperSize(PaperSize.A5_Landscape)

  new File("output").listFiles().foreach(_.delete())
  val dotWriter = new DotDiagramWriter
  dotWriter.generateImages(workspace)





}

