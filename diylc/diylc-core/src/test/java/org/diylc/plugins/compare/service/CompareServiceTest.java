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
