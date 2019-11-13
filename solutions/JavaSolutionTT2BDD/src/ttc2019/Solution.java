package ttc2019;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import ttc2019.metamodels.bdd.*;
import ttc2019.metamodels.bdd.impl.BDDFactoryImpl;
import ttc2019.metamodels.tt.InputPort;
import ttc2019.metamodels.tt.Row;
import ttc2019.metamodels.tt.TruthTable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Solution {

	private TruthTable truthTable;
	private BDD binaryDecisionDiagram;
	private Resource outputResource;
	private Map<InputPort, Integer> rowsDefinition = new HashMap<>();

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
		/*
		Init the binary decision diagram
		 */
		BDDFactory bddFactory = BDDFactoryImpl.init();
		binaryDecisionDiagram = bddFactory.createBDD();
		binaryDecisionDiagram.setName(truthTable.getName());

		EList<Port> inputPorts = truthTable.getPorts().stream()
												 .filter(Solution::isInputPort)
												 .map(port -> ttPortToBddPort(port, binaryDecisionDiagram))
												 .collect(Collectors.toCollection(BasicEList::new));

		EList<Port> ouputPortBdd = truthTable.getPorts().stream()
                                                        .filter(port -> !Solution.isInputPort(port))
                                                        .map(port -> ttPortToBddPort(port, binaryDecisionDiagram))
                                                        .collect(Collectors.toCollection(BasicEList::new));

		EList<Leaf> leafList = truthTable.getRows().stream().map(row -> {
			Leaf leaf = bddFactory.createLeaf();
			leaf.setOwnerBDD(binaryDecisionDiagram);
			row.getCells().forEach(cell ->  {
				Assignment assignment = bddFactory.createAssignment();
				assignment.setOwner(leaf);
				Optional<Port> outputPort = ouputPortBdd.stream().filter(port -> port.getName().equals(cell.getPort().getName())).findFirst();
				outputPort.ifPresent(port -> assignment.setPort((OutputPort) port));
			});
			return leaf;
		}).collect(Collectors.toCollection(BasicEList::new));

		ttc2019.metamodels.bdd.InputPort mostDefinedPort = (ttc2019.metamodels.bdd.InputPort) getCorrespondingPort(mostDefinedPort(truthTable), truthTable, binaryDecisionDiagram);

		Tree tree = bddFactory.createSubtree();
        tree.setOwnerBDD(binaryDecisionDiagram);


		binaryDecisionDiagram.setTree(tree);
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

	private static ttc2019.metamodels.bdd.Port getCorrespondingPort(ttc2019.metamodels.tt.Port port, TruthTable tt, BDD binaryDecisionDiagram){
      Optional<ttc2019.metamodels.tt.Port> result = tt.getPorts().stream().filter(ttport -> ttport.getName().equals(port.getName())).findFirst();
      if(result.isPresent()) {
          return ttPortToBddPort(port, binaryDecisionDiagram);
      } else {
          throw new RuntimeException(String.format("Port %s not present in the given truth table", port.getName()));
      }
    }

	private static Optional<ttc2019.metamodels.tt.InputPort> getTTPortDefinedInAllRow(ttc2019.metamodels.tt.TruthTable tt) {
	    return tt.getPorts().stream().filter(Solution::isInputPort).filter(port -> {
	        EList<Row> portDefinedIn = tt.getRows().stream().filter( row -> row.getCells().stream().filter(cell -> cell.getPort() == port).count() >1).collect(Collectors.toCollection(BasicEList::new));
	        return portDefinedIn.size() == tt.getRows().size();
        }).map( port -> (InputPort) port).findFirst();
	}

	private InputPort mostDefinedPort(TruthTable tt) {
		tt.getPorts().stream().filter(Solution::isInputPort).forEach(port -> numberOfDefinitionPort(tt, (InputPort) port));
		return Collections.max(rowsDefinition.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
	}

	/**
	 * Valuate the instance's map for number of row's definition for the given port in the truthtable
	 *
	 * @param tt the truth table
	 * @param port the port
	 */
	private void numberOfDefinitionPort(TruthTable tt, InputPort port) {
		int numberOfDefinition = (int) tt.getRows().stream().filter(row -> row.getCells().stream().filter(cell -> cell.getPort() == port).count() > 1 ).count();
		rowsDefinition.putIfAbsent(port, numberOfDefinition);
	}

	private static EList<Row> listRowFor(ttc2019.metamodels.tt.Port port, TruthTable tt, boolean value) {
		/* return tt.getRows().stream().filter( row -> {
			return true;
		}); */
	}



	private static boolean isInputPort(ttc2019.metamodels.tt.Port port) {
		return port instanceof InputPort;
	}


	/**
	 * Save the transformed binary decision diagram inside the output resource set earlier
	 *
	 * @throws IOException in case the file is already used
	 */
	public void save() throws IOException {
        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("xmi", new XMIResourceFactoryImpl());

        // Obtain a new resource set
        ResourceSet resSet = new ResourceSetImpl();

        // create a resource

        // Get the first model element and cast it to the right type, in my
        // example everything is hierarchical included in this first node
        getOutputResource().getContents().add(binaryDecisionDiagram);

        // now save the content.
        try {
            getOutputResource().save(Collections.EMPTY_MAP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
}
