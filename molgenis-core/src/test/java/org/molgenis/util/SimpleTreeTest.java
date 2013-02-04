package org.molgenis.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;

public class SimpleTreeTest
{
	@Test
	public void testCreate()
	{
		String name = "test";
		TestTree tree = new TestTree(name, null);

		assertEquals(name, tree.getName());
		assertFalse(tree.hasChildren());
		assertNotNull(tree.getChildren());
		assertTrue(tree.getChildren().isEmpty());
		assertNull(tree.getParent());
		assertEquals(tree, tree.getRoot());
		assertNotNull(tree.getAllChildren());
		assertTrue(tree.getAllChildren().isEmpty());
		assertNotNull(tree.getAllChildren(true));
		assertEquals(1, tree.getAllChildren(true).size());
		assertEquals(tree, tree.getAllChildren(true).get(0));
		assertNull(tree.getChild("xxx"));
		assertNull(tree.getChild(name));
		assertNull(tree.get("xxx"));
		assertNotNull(tree.get(name));
		assertEquals(tree, tree.get(name));
		assertNull(tree.getValue());
		assertNotNull(tree.getPath(","));
		assertEquals(name, tree.getPath(","));
	}

	@Test
	public void testCreateWithParent()
	{
		TestTree parent = new TestTree("parent", null);
		TestTree child = new TestTree("child", parent);

		assertTrue(parent.hasChildren());
		assertEquals(1, parent.getAllChildren().size());
		assertEquals(child, parent.getAllChildren().get(0));
		assertNotNull(child.getParent());
		assertEquals(child.getParent(), parent);
		assertEquals("parent,child", child.getPath(","));
		assertNotNull(child.getRoot());
		assertEquals(parent, child.getRoot());
	}

	@Test
	public void testSetParent()
	{
		TestTree parent = new TestTree("parent", null);
		TestTree child = new TestTree("child", null);
		child.setParent(parent);

		assertTrue(parent.hasChildren());
		assertEquals(1, parent.getAllChildren().size());
		assertEquals(child, parent.getAllChildren().get(0));
		assertNotNull(child.getParent());
		assertEquals(child.getParent(), parent);
		assertEquals("parent,child", child.getPath(","));
		assertNotNull(child.getRoot());
		assertEquals(parent, child.getRoot());

		TestTree duplicate = new TestTree("child", null);
		try
		{
			duplicate.setParent(parent);
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
			// Expected
		}
	}

	@Test
	public void testGetChild()
	{
		TestTree parent = new TestTree("test", null);
		TestTree child1 = new TestTree("child1", parent);
		TestTree child2 = new TestTree("child2", parent);

		assertEquals(child1, parent.getChild("child1"));
		assertEquals(child2, parent.getChild("child2"));
	}

	@Test
	public void testSetValue()
	{
		String parentValue = "parentValue";
		String childValue = "childValue";

		TestTree parent = new TestTree("parent", null);
		parent.setValue(parentValue);

		TestTree child = new TestTree("child", null);
		child.setValue(childValue);

		child.setParent(parent);
		assertNotNull(parent.getValue());
		assertNotNull(parentValue, parent.getValue());
		assertNotNull(child.getValue());
		assertNotNull(childValue, child.getValue());
	}

	@Test
	public void testRemove()
	{
		TestTree parent = new TestTree("test", null);
		TestTree child1 = new TestTree("child1", parent);
		TestTree child2 = new TestTree("child2", parent);

		assertNotNull(parent.getAllChildren());
		assertEquals(2, parent.getAllChildren().size());

		child1.remove();
		assertNotNull(parent.getAllChildren());
		assertEquals(1, parent.getAllChildren().size());
		assertEquals(child2, parent.getAllChildren().get(0));
		assertNull(parent.getChild("child1"));
	}

	private static class TestTree extends SimpleTree<TestTree>
	{
		private static final long serialVersionUID = 2697117779184102294L;

		public TestTree(String name, TestTree parent)
		{
			super(name, parent);
		}

	}
}
