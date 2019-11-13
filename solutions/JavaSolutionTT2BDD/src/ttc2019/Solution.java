package ttc2019;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import ttc2019.metamodels.bdd.*;
import ttc2019.metamodels.bdd.impl.BDDFactoryImpl;
import ttc2019.metamodels.tt.InputPort;
import ttc2019.metamodels.tt.Row;
import ttc2019.metamodels.tt.TruthTable;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class Solution {

	private TruthTable truthTable;
	private BDD binaryDecisionDiagram;
	private Resource outputResource;

	public TruthTable getTruthTable() {
		return truthTable;
	}

	public void setTruthTable(final TruthTable tt) {
		this.truthTable = tt;
	}

	public void setOutputResource(final Resource outputResource) {
		this.outputResource = outputResource;
	}

	public Resource getOutputResource() {
		return outputResource;
	}

	public void run() {
		BDDFactory bddFactory = BDDFactoryImpl.init();
		BDD bdd = bddFactory.createBDD();

		EList<Port> inputPorts = truthTable.getPorts().stream()
												 .filter(Solution::isInputPort)
												 .map(port -> ttPortToBddPort(port, bdd))
												 .collect(Collectors.toCollection(BasicEList::new));


		EList<Port> ouputPortBdd = truthTable.getPorts().stream()
                                                        .filter(port -> !Solution.isInputPort(port))
                                                        .map(port -> ttPortToBddPort(port, bdd))
                                                        .collect(Collectors.toCollection(BasicEList::new));

		EList<Leaf> leafList = truthTable.getRows().stream().map(row -> {
			Leaf leaf = bddFactory.createLeaf();
			leaf.setOwnerBDD(bdd);
			row.getCells().forEach(cell ->  {
				Assignment assignment = bddFactory.createAssignment();
				assignment.setOwner(leaf);
				Optional<Port> outputPort = ouputPortBdd.stream().filter(port -> port.getName().equals(cell.getPort().getName())).findFirst();
				outputPort.ifPresent(port -> assignment.setPort((OutputPort) port));
			});
			return leaf;
		}).collect(Collectors.toCollection(BasicEList::new));







	}

	private static ttc2019.metamodels.bdd.Port ttPortToBddPort(ttc2019.metamodels.tt.Port port, BDD binaryDecisionDiagram) {
		BDDFactory bddFactory = BDDFactoryImpl.eINSTANCE;
		ttc2019.metamodels.bdd.Port bddPort;
		if(isInputPort(port)) {
			bddPort = bddFactory.createInputPort();
			bddPort.setName(port.getName());
			bddPort.setOwner(binaryDecisionDiagram);
		} else {
			bddPort = bddFactory.createOutputPort();
			bddPort.setOwner(binaryDecisionDiagram);
			bddPort.setName(port.getName());
		}

		return bddPort;
	}

	private static Optional<ttc2019.metamodels.tt.InputPort> getTTDefinedInAllRow(ttc2019.metamodels.tt.TruthTable tt) {
	    return tt.getPorts().stream().filter(Solution::isInputPort).filter(port -> {
	        EList<Row> portDefinedIn = tt.getRows().stream().filter( row -> row.getCells().stream().filter(cell -> cell.getPort() == port).count() >1).collect(Collectors.toCollection(BasicEList::new));
	        return portDefinedIn.size() == tt.getRows().size();
        }).map( port -> (InputPort) port).findFirst();
	}



	private static boolean isInputPort(ttc2019.metamodels.tt.Port port) {
		return port instanceof InputPort;
	}


	public void save() throws IOException {
		outputResource.save(null);
	}
}
