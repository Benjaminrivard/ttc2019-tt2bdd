package ttc2019

import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.common.util.EList
import ttc2019.metamodels.bdd.*
import ttc2019.metamodels.bdd.OutputPort
import ttc2019.metamodels.tt.*
import ttc2019.metamodels.tt.Port

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

    fun rowToLeaf(row: Row) : Leaf {
        val leaf = BDDFactory.eINSTANCE.createLeaf()
        val cells = row.cells.filter { cell -> cell.port !is OutputPort }.map { cell -> cell}
        cells.forEach {
            cell -> leaf.assignments.add(cellToAssignement(cell))
        }
        return leaf
    }

    fun cellToAssignement (cell: Cell) : Assignment {
        val assignment = BDDFactory.eINSTANCE.createAssignment()
        val port = BDDFactory.eINSTANCE.createOutputPort()
        port.name = cell.port.name
        assignment.isValue = cell.isValue
        assignment.port = port

        return assignment
    }

    fun tripleToTree(triple: Triple<Cell, Tree, Tree>): Tree {
        val tree = BDDFactory.eINSTANCE.createSubtree()
        val port = BDDFactory.eINSTANCE.createInputPort()
        port.name = triple.first.port.name
        tree.port = port
        tree.treeForZero = triple.second
        tree.treeForOne = triple.third
        return tree
    }

    fun cellToSubTree(cell: Cell) : Subtree {
        val tree = BDDFactory.eINSTANCE.createSubtree()
        val port = BDDFactory.eINSTANCE.createInputPort()
        port.name = cell.port.name
        tree.port = port
        return tree
    }

    fun getNode(cell: Cell, tree: Tree) {
        //cell.owner.owner.
    }

    fun collectAllNodes(tree: Tree) : Set<Tree> {
        if (tree.ownerSubtreeForZero is Row) {
            if(tree.ownerSubtreeForOne is Row) {
                return emptySet()
            } else {
                return collectAllNodes(tree.ownerSubtreeForZero)
            }
        } else {
            if(tree.ownerSubtreeForOne is Row) {
                return collectAllNodes(tree.ownerSubtreeForZero)
            } else {
                return collectAllNodes(tree.ownerSubtreeForZero).union(collectAllNodes(tree.ownerSubtreeForOne))
            }
        }
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
    fun getPartition(rows : List<Row>, port: Port): Pair<List<Row>, List<Row>> {
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

    fun getTree(rows : List<Row>, ports : List<Port>) : Triple<Cell,Tree, Tree> {
        val port = getPort(ports, rows)
        port?.let {
            //Select a cell that defines the value
            var _cell : Cell  = TTFactory.eINSTANCE.createCell()
            for(row in rows) {
               for (cell in row.cells) {
                   if (cell.port == port)
                   _cell = cell
               }
            }

            //Partition the provided collection of rows
            val part  = getPartition(rows, port)

            //Define a new collection of usable ports for the resulting partitionings
            val newPorts = ports.minus(port)

            // Build the resulting tuple : the tree structure is created recursively
            var first : Tree = BDDFactory.eINSTANCE.createSubtree()
            var second : Tree = BDDFactory.eINSTANCE.createSubtree()

            when(part.first.size) {
                1 -> first = rowToLeaf(part.first[0])
                else -> getTree(part.first, newPorts)
            }

            when(part.second.size) {
                1 -> second = rowToLeaf(part.second[0])
                else -> getTree(part.second, newPorts)
            }

            return Triple(_cell, first, second)
        }
    }

    /**
     * Among the usable ports, select one where the value is defined for all rows
     */
    fun getPort(ports : List<Port>, rows : List<Row>) : Port? {
        return ports.find { port ->
           return@find rows.filter { row ->
                row.cells.filter {
                    cell -> cell.port == port
                }.isNotEmpty()
            }.size == rows.size
        }
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