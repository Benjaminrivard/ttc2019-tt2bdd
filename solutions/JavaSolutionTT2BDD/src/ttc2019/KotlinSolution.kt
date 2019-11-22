package ttc2019

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import ttc2019.metamodels.bdd.BDD
import ttc2019.metamodels.bdd.BDDFactory
import ttc2019.metamodels.bdd.impl.BDDFactoryImpl
import ttc2019.metamodels.tt.InputPort
import ttc2019.metamodels.tt.OutputPort
import ttc2019.metamodels.tt.TruthTable
import java.io.IOException
import java.util.*

class KotlinSolution {

    var binaryDecisionDiagram: BDD? = null
    var truthTable: TruthTable? = null
    var outputResource: Resource? = null
    private val service = SolutionHelper

    fun run() {
        /*
		Init the binary decision diagram
        Each TruthTable object should correspond to a BDD object, with the same
        name and equivalent Ports.
		 */
        BDDFactoryImpl.init()
        binaryDecisionDiagram = BDDFactory.eINSTANCE.createBDD()
        binaryDecisionDiagram?.name = truthTable?.name

        service.instance = binaryDecisionDiagram
        /*
            Each InputPort and OutputPort should be mapped to an object of the BDD
            type with the same name, and should be assigned the same name.

         */
        truthTable!!.ports.forEach { port ->
            when (port) {
                is InputPort -> binaryDecisionDiagram!!.ports.add(service.getInputPort(binaryDecisionDiagram!!, port.name))
                is OutputPort -> binaryDecisionDiagram!!.ports.add(service.getOutputPort(binaryDecisionDiagram!!, port.name))
            }
        }

        /*
		 Let the recursion begin
		 */
        val tree = service.getTree(truthTable!!.rows, truthTable!!.ports)
        binaryDecisionDiagram?.tree = tree
    }

    /**
     * Save the transformed binary decision diagram inside the output resource set earlier
     *
     * @throws IOException in case the file is already used
     */
    @Throws(IOException::class)
    fun save() {
        val reg = Resource.Factory.Registry.INSTANCE
        val m = reg.extensionToFactoryMap
        m["xmi"] = XMIResourceFactoryImpl()

        // create a resource

        // Get the first model element and cast it to the right type, in my
        // example everything is hierarchical included in this first node
        outputResource!!.contents.add(binaryDecisionDiagram)

        // now save the content.
        try {
            outputResource!!.save(Collections.EMPTY_MAP)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }
}
