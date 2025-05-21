package org.diylc.plugins.compare.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Node;
import org.diylc.plugins.compare.model.ConnectionDiff;
import org.diylc.plugins.compare.model.ComponentDiff;
import org.diylc.plugins.compare.model.CompareResults;
import org.junit.Before;
import org.junit.Test;

public class CompareServiceTest {

    private CompareService compareService;
    private IPlugInPort mockPlugInPort;

    @Before
    public void setUp() {
        mockPlugInPort = mock(IPlugInPort.class);
        compareService = new CompareService(mockPlugInPort);
    }

    @Test
    public void testCompare_IdenticalNetlists() {
        Map<String, IDIYComponent<?>> componentMap = new HashMap<>();
        Netlist netlist1 = createNetlist(componentMap, "Component1", "Node1", "Component2", "Node2");
        Netlist netlist2 = createNetlist(componentMap, "Component1", "Node1", "Component2", "Node2");

        CompareResults results = compareService.compare(netlist1, netlist2);

        assertTrue(results.matches());
        assertTrue(results.connectionDiffs().isEmpty());
        assertTrue(results.componentDiffs().isEmpty());
    }

    @Test
    public void testCompare_MissingConnectionsInSecondNetlist() {
        Map<String, IDIYComponent<?>> componentMap = new HashMap<>();
        // Group will have 3 nodes
        Netlist netlist1 = createNetlist(componentMap, "Component1", "Node1", "Component2", "Node2", "Component3", "Node3");
        Netlist netlist2 = createNetlist(componentMap, "Component1", "Node1"); // Missing group with 2 nodes

        CompareResults results = compareService.compare(netlist1, netlist2);

        assertTrue(!results.matches());
        // 2 components missing = 2 component diffs
        assertEquals(2, results.componentDiffs().size());
        // 3 nodes = 3 connections (1-2, 1-3, 2-3)
        assertEquals(3, results.connectionDiffs().size());
    }

    @Test
    public void testCompare_MissingConnectionsInFirstNetlist() {
        Map<String, IDIYComponent<?>> componentMap = new HashMap<>();
        // Group will have 3 nodes
        Netlist netlist1 = createNetlist(componentMap, "Component1", "Node1");
        Netlist netlist2 = createNetlist(componentMap, "Component1", "Node1", "Component2", "Node2", "Component3", "Node3");

        CompareResults results = compareService.compare(netlist1, netlist2);

        assertTrue(!results.matches());
        // 2 components missing = 2 component diffs
        assertEquals(2, results.componentDiffs().size());
        assertEquals(3, results.connectionDiffs().size());
    }

    @Test
    public void testCompare_CompletelyDifferentNetlists() {
        Map<String, IDIYComponent<?>> componentMap1 = new HashMap<>();
        Map<String, IDIYComponent<?>> componentMap2 = new HashMap<>();
        Netlist netlist1 = createNetlist(componentMap1, "Component1", "Node1", "Component2", "Node2");
        Netlist netlist2 = createNetlist(componentMap2, "Component3", "Node3", "Component4", "Node4");

        CompareResults results = compareService.compare(netlist1, netlist2);

        assertTrue(!results.matches());
        // 4 components missing (2 in each netlist) = 4 component diffs
        assertEquals(4, results.componentDiffs().size());
        // 2 connections in each netlist = 2 connection diffs
        assertEquals(2, results.connectionDiffs().size());
        
        // Verify differences from first netlist
        List<ComponentDiff> firstNetlistComponentDiffs = results.componentDiffs().stream()
            .filter(ComponentDiff::presentInCurrent)
            .toList();
        assertEquals(2, firstNetlistComponentDiffs.size());
        
        // Verify differences from second netlist
        List<ComponentDiff> secondNetlistComponentDiffs = results.componentDiffs().stream()
            .filter(diff -> !diff.presentInCurrent())
            .toList();
        assertEquals(2, secondNetlistComponentDiffs.size());
    }

    @Test
    public void testCompare_MultipleGroups_Identical() {
        Map<String, IDIYComponent<?>> componentMap = new HashMap<>();
        
        // Create first netlist with two groups
        Netlist netlist1 = new Netlist(new ArrayList<>());
        netlist1.add(createGroup(componentMap, "Component1", "Node1", "Component2", "Node2"));
        netlist1.add(createGroup(componentMap, "Component3", "Node3", "Component4", "Node4"));
        
        // Create second netlist with same groups
        Netlist netlist2 = new Netlist(new ArrayList<>());
        netlist2.add(createGroup(componentMap, "Component1", "Node1", "Component2", "Node2"));
        netlist2.add(createGroup(componentMap, "Component3", "Node3", "Component4", "Node4"));

        CompareResults results = compareService.compare(netlist1, netlist2);

        assertTrue(results.matches());
        assertTrue(results.connectionDiffs().isEmpty());
        assertTrue(results.componentDiffs().isEmpty());
    }

    @Test
    public void testCompare_MultipleGroups_DifferentOrder() {
        Map<String, IDIYComponent<?>> componentMap = new HashMap<>();
        
        // Create first netlist with two groups
        Netlist netlist1 = new Netlist(new ArrayList<>());
        netlist1.add(createGroup(componentMap, "Component1", "Node1", "Component2", "Node2"));
        netlist1.add(createGroup(componentMap, "Component3", "Node3", "Component4", "Node4"));
        
        // Create second netlist with same groups but in different order
        Netlist netlist2 = new Netlist(new ArrayList<>());
        netlist2.add(createGroup(componentMap, "Component3", "Node3", "Component4", "Node4"));
        netlist2.add(createGroup(componentMap, "Component1", "Node1", "Component2", "Node2"));

        CompareResults results = compareService.compare(netlist1, netlist2);

        assertTrue(results.matches());
        assertTrue(results.connectionDiffs().isEmpty());
        assertTrue(results.componentDiffs().isEmpty());
    }

    @Test
    public void testCompare_MultipleGroups_ExtraGroup() {
        Map<String, IDIYComponent<?>> componentMap = new HashMap<>();
        // Each group has 2 nodes
        Netlist netlist1 = new Netlist(new ArrayList<>());
        netlist1.add(createGroup(componentMap, "Component1", "Node1", "Component2", "Node2"));
        netlist1.add(createGroup(componentMap, "Component3", "Node3", "Component4", "Node4"));
        Netlist netlist2 = new Netlist(new ArrayList<>());
        netlist2.add(createGroup(componentMap, "Component1", "Node1", "Component2", "Node2"));
        netlist2.add(createGroup(componentMap, "Component3", "Node3", "Component4", "Node4"));
        netlist2.add(createGroup(componentMap, "Component5", "Node5", "Component6", "Node6"));

        CompareResults results = compareService.compare(netlist1, netlist2);

        assertTrue(!results.matches());
        // 2 components in extra group = 2 component diffs
        assertEquals(2, results.componentDiffs().size());
        // 1 connection in extra group = 1 connection diff
        assertEquals(1, results.connectionDiffs().size());
    }

    @Test
    public void testCompare_MultipleGroups_DifferentConnections() {
        Map<String, IDIYComponent<?>> componentMap = new HashMap<>();
        // Each group has 2 nodes
        Netlist netlist1 = new Netlist(new ArrayList<>());
        netlist1.add(createGroup(componentMap, "Component1", "Node1", "Component2", "Node2"));
        netlist1.add(createGroup(componentMap, "Component3", "Node3", "Component4", "Node4"));
        Netlist netlist2 = new Netlist(new ArrayList<>());
        netlist2.add(createGroup(componentMap, "Component1", "Node1", "Component3", "Node3"));
        netlist2.add(createGroup(componentMap, "Component2", "Node2", "Component4", "Node4"));

        CompareResults results = compareService.compare(netlist1, netlist2);

        assertTrue(!results.matches());
        assertEquals(0, results.componentDiffs().size());
        // 2 connections in each group = 4 connection diffs
        assertEquals(4, results.connectionDiffs().size());
    }

    @Test
    public void testCompare_NonPolarizedComponent_SwappedNodes() {
        Map<String, IDIYComponent<?>> componentMap = new HashMap<>();
        
        // Create first netlist with non-polarized component in two groups
        Netlist netlist1 = new Netlist(new ArrayList<>());
        netlist1.add(createGroup(componentMap, "Resistor1", "Node1", "Component2", "Node2"));
        netlist1.add(createGroup(componentMap, "Resistor1", "Node2", "Component3", "Node3"));
        
        // Create second netlist with same connections but different node order
        Netlist netlist2 = new Netlist(new ArrayList<>());
        netlist2.add(createGroup(componentMap, "Resistor1", "Node2", "Component2", "Node2"));
        netlist2.add(createGroup(componentMap, "Resistor1", "Node1", "Component3", "Node3"));

        // Make Resistor1 non-polarized
        IDIYComponent<?> resistor = componentMap.get("Resistor1");
        when(resistor.isPolarized()).thenReturn(false);

        CompareResults results = compareService.compare(netlist1, netlist2);

        assertTrue(results.matches());
        assertTrue(results.connectionDiffs().isEmpty());
        assertTrue(results.componentDiffs().isEmpty());
    }

    @Test
    public void testCompare_PolarizedComponent_SwappedNodes() {
        // Create first netlist with polarized component in two groups
        Netlist netlist1 = new Netlist(new ArrayList<>());
        
        // Create diode for netlist1
        IDIYComponent<?> diode1 = mock(IDIYComponent.class);
        when(diode1.getName()).thenReturn("Diode1");
        when(diode1.getControlPointCount()).thenReturn(2);
        when(diode1.getControlPoint(0)).thenReturn(new Point2D.Double(0, 0));
        when(diode1.getControlPoint(1)).thenReturn(new Point2D.Double(0, 0));
        when(diode1.isControlPointSticky(0)).thenReturn(true);
        when(diode1.isControlPointSticky(1)).thenReturn(true);
        when(diode1.isPolarized()).thenReturn(true);
        when(diode1.toString()).thenReturn("Diode1");
        when(diode1.getControlPointNodeName(0)).thenReturn("Anode");
        when(diode1.getControlPointNodeName(1)).thenReturn("Cathode");
        
        // Create Component2 for netlist1
        IDIYComponent<?> component2_1 = mock(IDIYComponent.class);
        when(component2_1.getName()).thenReturn("Component2");
        when(component2_1.getControlPointNodeName(0)).thenReturn("Node2");
        when(component2_1.getControlPointCount()).thenReturn(1);
        when(component2_1.getControlPoint(0)).thenReturn(new Point2D.Double(0, 0));
        when(component2_1.isControlPointSticky(0)).thenReturn(true);
        when(component2_1.toString()).thenReturn("Component2");
        when(component2_1.isPolarized()).thenReturn(false);
        
        // Create Component3 for netlist1
        IDIYComponent<?> component3_1 = mock(IDIYComponent.class);
        when(component3_1.getName()).thenReturn("Component3");
        when(component3_1.getControlPointNodeName(0)).thenReturn("Node3");
        when(component3_1.getControlPointCount()).thenReturn(1);
        when(component3_1.getControlPoint(0)).thenReturn(new Point2D.Double(0, 0));
        when(component3_1.isControlPointSticky(0)).thenReturn(true);
        when(component3_1.toString()).thenReturn("Component3");
        when(component3_1.isPolarized()).thenReturn(false);
        
        // Create groups for netlist1
        Group group1 = new Group();
        Node node1 = new Node(diode1, 0); // Anode
        Node node2 = new Node(component2_1, 0);
        group1.getNodes().add(node1);
        group1.getNodes().add(node2);
        netlist1.add(group1);
        
        Group group2 = new Group();
        Node node3 = new Node(diode1, 1); // Cathode
        Node node4 = new Node(component3_1, 0);
        group2.getNodes().add(node3);
        group2.getNodes().add(node4);
        netlist1.add(group2);
        
        // Create second netlist with same connections but different node order
        Netlist netlist2 = new Netlist(new ArrayList<>());
        
        // Create diode for netlist2
        IDIYComponent<?> diode2 = mock(IDIYComponent.class);
        when(diode2.getName()).thenReturn("Diode1");
        when(diode2.getControlPointCount()).thenReturn(2);
        when(diode2.getControlPoint(0)).thenReturn(new Point2D.Double(0, 0));
        when(diode2.getControlPoint(1)).thenReturn(new Point2D.Double(0, 0));
        when(diode2.isControlPointSticky(0)).thenReturn(true);
        when(diode2.isControlPointSticky(1)).thenReturn(true);
        when(diode2.isPolarized()).thenReturn(true);
        when(diode2.toString()).thenReturn("Diode1");
        when(diode2.getControlPointNodeName(0)).thenReturn("Anode");
        when(diode2.getControlPointNodeName(1)).thenReturn("Cathode");
        
        // Create Component2 for netlist2
        IDIYComponent<?> component2_2 = mock(IDIYComponent.class);
        when(component2_2.getName()).thenReturn("Component2");
        when(component2_2.getControlPointNodeName(0)).thenReturn("Node2");
        when(component2_2.getControlPointCount()).thenReturn(1);
        when(component2_2.getControlPoint(0)).thenReturn(new Point2D.Double(0, 0));
        when(component2_2.isControlPointSticky(0)).thenReturn(true);
        when(component2_2.toString()).thenReturn("Component2");
        when(component2_2.isPolarized()).thenReturn(false);
        
        // Create Component3 for netlist2
        IDIYComponent<?> component3_2 = mock(IDIYComponent.class);
        when(component3_2.getName()).thenReturn("Component3");
        when(component3_2.getControlPointNodeName(0)).thenReturn("Node3");
        when(component3_2.getControlPointCount()).thenReturn(1);
        when(component3_2.getControlPoint(0)).thenReturn(new Point2D.Double(0, 0));
        when(component3_2.isControlPointSticky(0)).thenReturn(true);
        when(component3_2.toString()).thenReturn("Component3");
        when(component3_2.isPolarized()).thenReturn(false);
        
        // Create groups for netlist2 with swapped nodes
        Group group3 = new Group();
        Node node5 = new Node(diode2, 1); // Cathode
        Node node6 = new Node(component2_2, 0);
        group3.getNodes().add(node5);
        group3.getNodes().add(node6);
        netlist2.add(group3);
        
        Group group4 = new Group();
        Node node7 = new Node(diode2, 0); // Anode
        Node node8 = new Node(component3_2, 0);
        group4.getNodes().add(node7);
        group4.getNodes().add(node8);
        netlist2.add(group4);

        // Debug: Print the groups and verify diode polarity
        System.out.println("Netlist1 groups:");
        for (Group g : netlist1.getGroups()) {
            System.out.println("Group: " + g);
            for (Node n : g.getNodes()) {
                System.out.println("  Node: " + n.getComponent().getName() + 
                    " " + n.getComponent().getControlPointNodeName(n.getPointIndex()) +
                    " (polarized: " + n.getComponent().isPolarized() + ")");
            }
        }
        System.out.println("\nNetlist2 groups:");
        for (Group g : netlist2.getGroups()) {
            System.out.println("Group: " + g);
            for (Node n : g.getNodes()) {
                System.out.println("  Node: " + n.getComponent().getName() + 
                    " " + n.getComponent().getControlPointNodeName(n.getPointIndex()) +
                    " (polarized: " + n.getComponent().isPolarized() + ")");
            }
        }

        CompareResults results = compareService.compare(netlist1, netlist2);

        // Debug: Print comparison results
        System.out.println("\nComparison results:");
        System.out.println("Matches: " + results.matches());
        System.out.println("Component diffs: " + results.componentDiffs().size());
        System.out.println("Connection diffs: " + results.connectionDiffs().size());
        for (ConnectionDiff diff : results.connectionDiffs()) {
            System.out.println("  " + diff.fromComponent() + " " + diff.fromNodeName() + 
                " -> " + diff.toComponent() + " " + diff.toNodeName() + 
                " (present in current: " + diff.presentInCurrent() + ")");
        }

        assertTrue(!results.matches());
        assertEquals(0, results.componentDiffs().size());
        
        // Since connections are reported in both directions, we expect 4 differences
        assertEquals(4, results.connectionDiffs().size());
        
        // Verify the specific differences
        List<ConnectionDiff> diffs = results.connectionDiffs();
        boolean foundCathodeToComponent3 = false;
        boolean foundComponent3ToCathode = false;
        boolean foundComponent2ToAnode = false;
        boolean foundAnodeToComponent2 = false;
        boolean foundCathodeToComponent2 = false;
        boolean foundComponent2ToCathode = false;
        boolean foundAnodeToComponent3 = false;
        boolean foundComponent3ToAnode = false;
        
        for (ConnectionDiff diff : diffs) {
            // Check Diode1 Cathode <-> Component3 Node3
            if (diff.fromComponent().equals("Diode1") && 
                diff.fromNodeName().equals("Cathode") && 
                diff.toComponent().equals("Component3") &&
                diff.toNodeName().equals("Node3") &&
                diff.presentInCurrent()) {
                foundCathodeToComponent3 = true;
            }
            if (diff.fromComponent().equals("Component3") && 
                diff.fromNodeName().equals("Node3") && 
                diff.toComponent().equals("Diode1") &&
                diff.toNodeName().equals("Cathode") &&
                diff.presentInCurrent()) {
                foundComponent3ToCathode = true;
            }
            
            // Check Component2 Node2 <-> Diode1 Anode
            if (diff.fromComponent().equals("Component2") && 
                diff.fromNodeName().equals("Node2") && 
                diff.toComponent().equals("Diode1") &&
                diff.toNodeName().equals("Anode") &&
                diff.presentInCurrent()) {
                foundComponent2ToAnode = true;
            }
            if (diff.fromComponent().equals("Diode1") && 
                diff.fromNodeName().equals("Anode") && 
                diff.toComponent().equals("Component2") &&
                diff.toNodeName().equals("Node2") &&
                diff.presentInCurrent()) {
                foundAnodeToComponent2 = true;
            }
            
            // Check Diode1 Cathode <-> Component2 Node2 (not present)
            if (diff.fromComponent().equals("Diode1") && 
                diff.fromNodeName().equals("Cathode") && 
                diff.toComponent().equals("Component2") &&
                diff.toNodeName().equals("Node2") &&
                !diff.presentInCurrent()) {
                foundCathodeToComponent2 = true;
            }
            if (diff.fromComponent().equals("Component2") && 
                diff.fromNodeName().equals("Node2") && 
                diff.toComponent().equals("Diode1") &&
                diff.toNodeName().equals("Cathode") &&
                !diff.presentInCurrent()) {
                foundComponent2ToCathode = true;
            }
            
            // Check Diode1 Anode <-> Component3 Node3 (not present)
            if (diff.fromComponent().equals("Diode1") && 
                diff.fromNodeName().equals("Anode") && 
                diff.toComponent().equals("Component3") &&
                diff.toNodeName().equals("Node3") &&
                !diff.presentInCurrent()) {
                foundAnodeToComponent3 = true;
            }
            if (diff.fromComponent().equals("Component3") && 
                diff.fromNodeName().equals("Node3") && 
                diff.toComponent().equals("Diode1") &&
                diff.toNodeName().equals("Anode") &&
                !diff.presentInCurrent()) {
                foundComponent3ToAnode = true;
            }
        }
        
        // Check that we found at least one direction of each connection
        assertTrue("Should detect difference in Diode1 Cathode <-> Component3 Node3 (present in current)", 
            foundCathodeToComponent3 || foundComponent3ToCathode);
        assertTrue("Should detect difference in Component2 Node2 <-> Diode1 Anode (present in current)", 
            foundComponent2ToAnode || foundAnodeToComponent2);
        assertTrue("Should detect difference in Diode1 Cathode <-> Component2 Node2 (not present in current)", 
            foundCathodeToComponent2 || foundComponent2ToCathode);
        assertTrue("Should detect difference in Diode1 Anode <-> Component3 Node3 (not present in current)", 
            foundAnodeToComponent3 || foundComponent3ToAnode);
    }

    @Test
    public void testCompare_MixedPolarizedAndNonPolarized() {
        Map<String, IDIYComponent<?>> componentMap = new HashMap<>();
        
        // Create first netlist with both polarized and non-polarized components
        Netlist netlist1 = new Netlist(new ArrayList<>());
        netlist1.add(createGroup(componentMap, "Resistor1", "Node1", "Diode1", "Anode"));
        netlist1.add(createGroup(componentMap, "Resistor1", "Node2", "Component3", "Node3"));
        netlist1.add(createGroup(componentMap, "Diode1", "Cathode", "Component4", "Node4"));
        
        // Create second netlist with same connections but different node order for non-polarized
        Netlist netlist2 = new Netlist(new ArrayList<>());
        netlist2.add(createGroup(componentMap, "Resistor1", "Node2", "Diode1", "Anode"));
        netlist2.add(createGroup(componentMap, "Resistor1", "Node1", "Component3", "Node3"));
        netlist2.add(createGroup(componentMap, "Diode1", "Cathode", "Component4", "Node4"));

        // Make Resistor1 non-polarized and Diode1 polarized
        IDIYComponent<?> resistor = componentMap.get("Resistor1");
        IDIYComponent<?> diode = componentMap.get("Diode1");
        when(resistor.isPolarized()).thenReturn(false);
        when(diode.isPolarized()).thenReturn(true);

        CompareResults results = compareService.compare(netlist1, netlist2);

        assertTrue(results.matches());
        assertTrue(results.connectionDiffs().isEmpty());
        assertTrue(results.componentDiffs().isEmpty());
    }

    private Netlist createNetlist(Map<String, IDIYComponent<?>> componentMap, String... componentNodePairs) {
        Netlist netlist = new Netlist(new ArrayList<>());
        netlist.add(createGroup(componentMap, componentNodePairs));
        return netlist;
    }

    private Group createGroup(Map<String, IDIYComponent<?>> componentMap, String... componentNodePairs) {
        Group group = new Group();
        
        for (int i = 0; i < componentNodePairs.length; i += 2) {
            String componentName = componentNodePairs[i];
            String nodeName = componentNodePairs[i + 1];
            
            IDIYComponent<?> component = componentMap.computeIfAbsent(componentName, name -> {
                IDIYComponent<?> c = mock(IDIYComponent.class);
                when(c.getName()).thenReturn(name);
                when(c.getControlPointNodeName(0)).thenReturn(nodeName);
                when(c.getControlPointCount()).thenReturn(1);
                when(c.getControlPoint(0)).thenReturn(new Point2D.Double(0, 0));
                when(c.isControlPointSticky(0)).thenReturn(true);
                when(c.toString()).thenReturn(name);
                // Default to non-polarized
                when(c.isPolarized()).thenReturn(false);
                return c;
            });
            // For the same component name, update the node name for this instance
            when(component.getControlPointNodeName(0)).thenReturn(nodeName);
            Node node = new Node(component, 0);
            group.getNodes().add(node);
        }
        
        return group;
    }
} 
