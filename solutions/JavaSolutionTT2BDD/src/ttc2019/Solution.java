package ttc2019;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;

import ttc2019.metamodels.bdd.BDD;
import ttc2019.metamodels.bdd.BDDFactory;
import ttc2019.metamodels.bdd.BDDPackage;
import ttc2019.metamodels.bdd.Port;
import ttc2019.metamodels.bdd.impl.BDDFactoryImpl;
import ttc2019.metamodels.tt.*;

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

		EList<ttc2019.metamodels.bdd.Port> inputPorts = truthTable.getPorts().stream()
												 .filter(Solution::isInputPort)
												 .map(port -> ttPortToBddPort(port, bdd))
												 .collect(Collectors.toCollection(BasicEList::new));


		EList<ttc2019.metamodels.bdd.Port> ouputPortBdd = truthTable.getPorts().stream()
																				.filter(port -> !Solution.isInputPort(port))
																				.map(port -> ttPortToBddPort(port, bdd))
																				.collect(Collectors.toCollection(BasicEList::new));

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

	private static void makeAssignment( ) {

	}


	private static boolean isInputPort(ttc2019.metamodels.tt.Port port) {
		return port instanceof InputPort;
	}


	public void save() throws IOException {
		outputResource.save(null);
	}
}
