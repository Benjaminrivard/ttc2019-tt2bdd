package ttc2019

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import ttc2019.metamodels.bdd.*
import ttc2019.metamodels.bdd.impl.BDDFactoryImpl
import ttc2019.metamodels.tt.InputPort
import ttc2019.metamodels.tt.Row
import ttc2019.metamodels.tt.TruthTable
import java.io.IOException
import java.util.*

class KotlinSolution {

    var truthTable: TruthTable? = null
    private var binaryDecisionDiagram: BDD? = null
    var outputResource: Resource? = null
    private var rowsDefinition : Map<InputPort, Int> = HashMap()
    private val service = SolutionHelper

    fun run() {
        /*
		Init the binary decision diagram
		 */
        val bddFactory = BDDFactoryImpl.init()
        binaryDecisionDiagram = bddFactory.createBDD()
        binaryDecisionDiagram!!.name = truthTable!!.name

        /*
		Init the top level tree
		 */
        val tree = bddFactory.createSubtree()
        tree.ownerBDD = binaryDecisionDiagram


        /*
		Each Row should become a Leaf node: the Cells for the OutputPorts will
		become Assignments.
		 */
//        val leafList = truthTable!!.rows.stream().map { row ->
//            val leaf = bddFactory.createLeaf()
//            leaf.ownerBDD = binaryDecisionDiagram
//            createAssignment(bddFactory, ouputPortBdd, row, leaf)
//            leaf
//        }.collect<BasicEList<Leaf>, Any>(Collectors.toCollection(Supplier<BasicEList<Leaf>> { BasicEList() }))

        /*
		One simple	approach is to find a TT InputPort which is (ideally) defined in all the Rows, and
		turn it into an inner node (a Subtree) which points to the equivalent BDD InputPort	and has two Trees
		 */
//        val mostDefinedPort = getCorrespondingPort(mostDefinedPort(truthTable!!), truthTable, binaryDecisionDiagram) as ttc2019.metamodels.bdd.InputPort

        binaryDecisionDiagram!!.tree = tree
    }


    private fun createAssignment(bddFactory: BDDFactory, ouputPortBdd: EList<Port>, row: Row, leaf: Leaf) {
        row.cells.stream().filter { cell -> cell is OutputPort }.forEach { cell ->
            val assignment = bddFactory.createAssignment()
            assignment.owner = leaf
            assignment.isValue = cell.isValue
            val outputPort = ouputPortBdd.stream().filter { port -> port.name == cell.port.name }.findFirst()
            outputPort.ifPresent { port -> assignment.port = port as OutputPort }
            leaf.assignments.add(assignment)
        }
    }


     /**
     *  Get the port that is the most defined
     */
    private fun mostDefinedPort(tt: TruthTable): InputPort? {
       return rowsDefinition.maxBy { it.value }?.key
    }

    /**
     * Valuate the instance's map for number of row's definition for the given port in the truthtable
     *
     * @param tt the truth table
     * @param port the port
     */
    private fun numberOfDefinitionPort(tt: TruthTable, port: InputPort) {
        val numberOfDefinition = tt.rows.stream().filter { row -> row.cells.stream().filter { cell -> cell.port === port }.count() > 1 }.count().toInt()
        rowsDefinition = rowsDefinition.plus(Pair(port, numberOfDefinition))
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
