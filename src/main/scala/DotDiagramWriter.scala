import java.io.{File, IOException, PrintWriter, Writer}

import com.structurizr.Workspace
import com.structurizr.model.Element
import com.structurizr.view.View
import io.github.livingdocumentation.dotdiagram.DotGraph.Cluster
import io.github.livingdocumentation.dotdiagram.{DotGraph, GoogleChartDotWriter}

import scala.collection.JavaConverters._

class DotDiagramWriter  {
   def generateImages(workspace: Workspace) = {

    workspace.getViews.getEnterpriseContextViews.asScala.foreach(v => getImageFile(v, v.getSoftwareSystem))
    workspace.getViews.getSystemContextViews.asScala.foreach(v => getImageFile(v, null))
    workspace.getViews.getContainerViews.asScala.foreach(v => getImageFile(v, v.getSoftwareSystem))
    workspace.getViews.getComponentViews.asScala.foreach(v => getImageFile(v, v.getContainer))
    workspace.getViews.getDynamicViews.asScala.foreach(v => getImageFile(v, v.getSoftwareSystem))
    //workspace.getViews.getDeploymentViews.asScala.foreach(v => getImageFile(v, v.getSoftwareSystem()))

  }
  var counter=0
  def getImageFile(view : View, system : Element) = {
    counter+=1
    val outputPath = "output/"
    val fileName = view.getName+counter
    val dotFile = new File(outputPath+fileName+".dot")
    val fileWriter = new PrintWriter(dotFile)
    generateDotFile(view, system, fileWriter)
    fileWriter.flush()
    fileWriter.close()

    val googleWriter = new GoogleChartDotWriter(outputPath)
    googleWriter.render(fileName)
    println(outputPath+fileName+".png")
  }

  private def generateDotFile(view: View, softwareSystem: Element, writer: Writer): Unit = {
    try {
      val graph = new DotGraph(view.getName,"LR")
      val digraph = graph.getDigraph

      var cluster :Cluster = null
      if (softwareSystem != null) {
        cluster = digraph.addCluster(softwareSystem.getId)
        cluster.setLabel(softwareSystem.getName)
      }
      import scala.collection.JavaConversions._
      for (elementView <- view.getElements) {
        val element = elementView.getElement
        if (softwareSystem != null && (element.getParent eq softwareSystem)) cluster.addNode(element.getId).setLabel(element.getName)
        else digraph.addNode(element.getId).setLabel(element.getName +"|"+element.getDescription)
      }

      for (relationshipView <- view.getRelationships) {
        val relationship = relationshipView.getRelationship
        digraph.addAssociation(relationship.getSourceId, relationship.getDestinationId).setLabel(relationship.getDescription)
      }
      val output = graph.render.trim
      writer.write(output)
      writer.write(System.lineSeparator)
      writer.write(System.lineSeparator)
    } catch {
      case ioe: IOException =>
        ioe.printStackTrace()
    }
  }

}
