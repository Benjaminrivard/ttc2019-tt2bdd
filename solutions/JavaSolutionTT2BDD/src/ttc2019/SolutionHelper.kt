package ttc2019

import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.common.util.EList
import ttc2019.metamodels.bdd.BDD
import ttc2019.metamodels.bdd.BDDFactory
import ttc2019.metamodels.bdd.OutputPort
import ttc2019.metamodels.bdd.Tree
import ttc2019.metamodels.tt.Cell
import ttc2019.metamodels.tt.Port
import ttc2019.metamodels.tt.Row
import ttc2019.metamodels.tt.TruthTable

object SolutionHelper {
    /**
     * @param BDD the binary decision diagram
     * @param String the port's name (Identifier in the case of this transformation)
     */
    fun getInputPort(bdd: BDD, portName: String): ttc2019.metamodels.bdd.InputPort {
        bdd.ports.forEach {
            if (portName == it.name) {
                return it as ttc2019.metamodels.bdd.InputPort
            }
        }
        val inputPort = BDDFactory.eINSTANCE.createInputPort()
        inputPort.name = portName
        bdd.ports.add(inputPort)

        return inputPort
    }

    fun getOutputPort(bdd: BDD, portName: String): OutputPort {
        bdd.ports.forEach {
            if (portName == it.name) {
                return it as OutputPort
            }
        }

        val outputPort = BDDFactory.eINSTANCE.createOutputPort()
        outputPort.name = portName
        bdd.ports.add(outputPort)
        return outputPort
    }

    /**
     * Partition of the a sequence of the truthtable
     *
     * @param EList<Row> sequence or subsequence of the rows of a truth table and a port
     * @return a Pair of two sequences : <ul>
     *     <li>First: list of rows where the value of the given port was false</li>
     *     <li>Second: list of rows where the value of the given port was true</li>
     * </ul>
     */
    fun getPartition(rows : EList<Row>, port: Port): Pair<List<Row>, List<Row>> {
        var treeForOne: List<Row> = BasicEList()
        var treeForZero: List<Row> = BasicEList()

        rows.forEach {row ->
            if(!trueCellsByPort(row)[port].isNullOrEmpty()) {
                treeForOne = treeForOne.plus(row)
            }
        }

        rows.forEach {row ->
            if(!falseCellsByPort(row)[port].isNullOrEmpty()) {
                treeForZero = treeForZero.plus(row)
            }
        }
        return Pair(treeForZero, treeForOne)
    }



    fun getTree(bdd : BDD, truthTable: TruthTable) : Tree {

    }

    /**
     * @param Row the row in which the sort should occur
     * @return The map with for key : each port and for value the cells with values of true in the row
     */
    fun trueCellsByPort(row: Row): Map<Port, List<Cell>> {
        return row.cells.associateBy({ it.port }, { row.cells.filter { cell -> cell.isValue } })
    }

    /**
     * @param Row the row in which the sort should occur
     * @return The map with for key : each port and for value the cells with values of false in the row
     */
    fun falseCellsByPort(row: Row): Map<Port, List<Cell>> {
        return row.cells.associateBy({ it.port }, { row.cells.filter { cell -> !cell.isValue } })
    }
}