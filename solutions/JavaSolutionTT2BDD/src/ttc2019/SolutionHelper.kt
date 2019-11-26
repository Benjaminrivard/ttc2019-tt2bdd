package ttc2019

import org.eclipse.emf.common.util.BasicEList
import ttc2019.metamodels.bdd.*
import ttc2019.metamodels.tt.Cell
import ttc2019.metamodels.tt.Port
import ttc2019.metamodels.tt.Row
import ttc2019.metamodels.tt.TTFactory

object SolutionHelper {

    var instance: BDD? = null

    /**
     * @param BDD the binary decision diagram
     * @param String the port's name (Identifier in the case of this transformation)
     * @return the corresponding input port
     */
    fun getInputPort(bdd: BDD, portName: String): InputPort {
        bdd.ports.forEach {
            if (portName == it.name) {
                return it as InputPort
            }
        }
        val inputPort = BDDFactory.eINSTANCE.createInputPort()
        inputPort.name = portName
        inputPort.owner = bdd
        bdd.ports.add(inputPort)

        return inputPort
    }

    /**
     * @param BDD the binary decision diagram
     * @param String the port's name (Identifier in the case of this transformation)
     *@return the corresponding output port
     */
    fun getOutputPort(bdd: BDD, portName: String): OutputPort {
        bdd.ports.forEach {
            if (portName == it.name) {
                return it as OutputPort
            }
        }
        val outputPort = BDDFactory.eINSTANCE.createOutputPort()
        outputPort.name = portName
        outputPort.owner = bdd
        bdd.ports.add(outputPort)
        return outputPort
    }

    /**
     * Each Row should become a Leaf node: the Cells for the OutputPorts will
     * become Assignments.
     */
    fun rowToLeaf(row: Row): Leaf {
        val leaf = BDDFactory.eINSTANCE.createLeaf()
        val cells = row.cells.filter { cell -> cell.port !is OutputPort }.map { cell -> cell }
        cells.forEach { cell ->
            when (cell.port) {
                is ttc2019.metamodels.tt.OutputPort -> leaf.assignments.add(cellToAssignement(cell, leaf))
            }
        }
        instance?.let {
            leaf.ownerBDD = it
        }
        return leaf
    }

    /**
     * Transform a cell to an assignement
     */
    fun cellToAssignement(cell: Cell, leaf: Leaf): Assignment {
        val assignment = BDDFactory.eINSTANCE.createAssignment()
        val port = getOutputPort(instance!!, cell.port.name)
        assignment.isValue = cell.isValue
        assignment.port = port
        assignment.owner = leaf
        port.assignments.add(assignment)
        return assignment
    }

    fun tripleToTree(triple: Triple<Cell, Tree, Tree>): Tree {
        val tree = BDDFactory.eINSTANCE.createSubtree()
        val port = getInputPort(instance!!, triple.first.port.name)
        port.name = triple.first.port.name
        tree.port = port
        tree.treeForZero = triple.second
        tree.treeForOne = triple.third
        port.subtrees.add(tree)

        return tree
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
    fun getPartition(rows: List<Row>, port: Port): Pair<List<Row>, List<Row>> {
        var treeForOne: List<Row> = BasicEList()
        var treeForZero: List<Row> = BasicEList()

        rows.forEach { row ->
            if (!(trueCellsByPort(row)[port].isNullOrEmpty())) {
                treeForOne = treeForOne.plus(row)
            }
        }

        rows.forEach { row ->
            if (!(falseCellsByPort(row)[port].isNullOrEmpty())) {
                treeForZero = treeForZero.plus(row)
            }
        }
        return Pair(treeForZero, treeForOne)
    }

    /**
     * One simple	approach is to find a TT InputPort which is (ideally) defined in all the Rows, and
     * turn it into an inner node (a Subtree) which points to the equivalent BDD InputPort	and has two Trees
     */
    fun getTree(rows: List<Row>, ports: List<Port>): Tree {
        /**
         *  Get the port that is the most defined
         */
        val port = getPort(ports, rows)

        if (port != null) {
            //Select a cell that defines the value
            var _cell: Cell = TTFactory.eINSTANCE.createCell()
            for (row in rows) {
                for (cell in row.cells) {
                    if (cell.port == port)
                        _cell = cell
                }
            }

            //Partition the provided collection of rows
            val part = getPartition(rows, port)

            //Define a new collection of usable ports for the resulting partitionings
            val newPorts = ports.minus(port)

            // Build the resulting tuple : the tree structure is created recursively
            val first: Tree
            val second: Tree

            first = when (part.first.size) {
                1 -> rowToLeaf(part.first[0])
                else -> getTree(part.first, newPorts)
            }

            second = when (part.second.size) {
                1 -> rowToLeaf(part.second[0])
                else -> getTree(part.second, newPorts)
            }

            val triple = Triple(_cell, first, second)
            return tripleToTree(triple)
        } else {
            throw Exception()
        }
    }

    /**
     * Among the usable ports, select one where the value is defined for all rows
     * @param List<Port> the sequence of ports
     * @param List<Row> the sequence of rows
     *
     * @return the port fit the criteria
     */
    fun getPort(ports: List<Port>, rows: List<Row>): Port? {
        return ports.find { port ->
            return@find rows.filter { row ->
                row.cells.filter { cell ->
                    cell.port == port
                }.isNotEmpty()
            }.size == rows.size
        }
    }

    /**
     * @param Row the row in which the sort should occur
     * @return The map with for key : each port and for value the cells with values of true in the row
     */
    fun trueCellsByPort(row: Row): Map<Port, List<Cell>> {
        var partition: Map<Port, List<Cell>> = LinkedHashMap()
        val ports: Set<Port> = row.cells.map { cell -> cell.port }.toSet()

        ports.forEach { port ->
            val cells = row.cells.filter { cell -> cell.isValue && cell.port == port }
            val pair = Pair(port, cells)
            partition = partition.plus(pair)
        }

        return partition
    }

    /**
     * @param Row the row in which the sort should occur
     * @return The map with for key : each port and for value the cells with values of false in the row
     */
    fun falseCellsByPort(row: Row): Map<Port, List<Cell>> {
        var partition: Map<Port, List<Cell>> = LinkedHashMap()
        val ports: Set<Port> = row.cells.map { cell -> cell.port }.toSet()

        ports.forEach { port ->
            val cells = row.cells.filter { cell -> !cell.isValue && cell.port == port }
            val pair = Pair(port, cells)
            partition = partition.plus(pair)
        }

        return partition
    }
}