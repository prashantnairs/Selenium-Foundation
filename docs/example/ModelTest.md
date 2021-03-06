﻿| **ModelTest.java** | [ExamplePage.java](ExamplePage.md) | [TableComponent.java](TableComponent.md) | [TableRowComponent.java](TableRowComponent.md) | [FrameComponent.java](FrameComponent.md) |

# Sample Code

###### ModelTest.java
```java
package com.nordstrom.automation.selenium.model;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.LinkedListeners;
import com.nordstrom.automation.testng.ListenerChain;

@InitialPage(ExamplePage.class)
@Listeners({ListenerChain.class})
@LinkedListeners({DriverManager.class, ExecutionFlowController.class})
public class ModelTest {
	
	private static final String TITLE = "Example Page";
	private static final String[] PARAS = {"This is paragraph one.", "This is paragraph two.", "This is paragraph three."};
	private static final String[] HEADINGS = {"Firstname", "Lastname", "Age"};
	private static final String[][] CONTENT = {{"Jill", "Smith", "50"}, {"Eve", "Jackson", "94"}, {"John", "Doe", "80"}};
	private static final String FRAME_A = "Frame A";
	private static final String FRAME_A_ID = "frame-a";
	private static final String FRAME_B = "Frame B";
	private static final String FRAME_B_ID = "frame-b";
	private static final String FRAME_C = "Frame C";
	private static final String FRAME_C_ID = "frame-c";
	private static final String TABLE_ID = "t1";
	
	@Test
	public void testBasicPage() {
		ExamplePage page = getPage();
		assertEquals(page.getTitle(), TITLE);
	}
	
	@Test
	public void testParagraphs() {
		ExamplePage page = getPage();
		List<String> paraList = page.getParagraphs();
		assertEquals(paraList.size(), 3);
		assertEquals(paraList.toArray(), PARAS);
	}
	
	@Test
	public void testTable() {
		ExamplePage page = getPage();
		TableComponent component = page.getTable();
		verifyTable(component);
	}

	/**
	 * Verify the contents of the specified table component
	 * 
	 * @param component table component to be verified
	 */
	private static void verifyTable(TableComponent component) {
		assertEquals(component.getHeadings().toArray(), HEADINGS);
		List<List<String>> content = component.getContent();
		assertEquals(content.size(), 3);
		assertEquals(content.get(0).toArray(), CONTENT[0]);
		assertEquals(content.get(1).toArray(), CONTENT[1]);
		assertEquals(content.get(2).toArray(), CONTENT[2]);
	}
	
	@Test
	public void testFrameByElement() {
		ExamplePage page = getPage();
		FrameComponent component = page.getFrameByElement();
		assertEquals(component.getPageContent(), FRAME_A);
	}

	@Test
	public void testFrameByIndex() {
		ExamplePage page = getPage();
		FrameComponent component = page.getFrameByIndex();
		assertEquals(component.getPageContent(), FRAME_B);
	}

	@Test
	public void testFrameById() {
		ExamplePage page = getPage();
		FrameComponent component = page.getFrameById();
		assertEquals(component.getPageContent(), FRAME_C);
	}
	
	@Test
	public void testComponentList() {
		ExamplePage page = getPage();
		List<TableComponent> componentList = page.getTableList();
		verifyTable(componentList.get(0));
	}
	
	@Test
	public void testComponentMap() {
		ExamplePage page = getPage();
		Map<Object, TableComponent> componentMap = page.getTableMap();
		verifyTable(componentMap.get(TABLE_ID));
	}
	
	@Test
	public void testFrameList() {
		ExamplePage page = getPage();
		List<FrameComponent> frameList = page.getFrameList();
		assertEquals(frameList.size(), 3);
		assertEquals(frameList.get(0).getPageContent(), FRAME_A);
		assertEquals(frameList.get(1).getPageContent(), FRAME_B);
		assertEquals(frameList.get(2).getPageContent(), FRAME_C);
	}

	@Test
	public void testFrameMap() {
		ExamplePage page = getPage();
		Map<Object, FrameComponent> frameMap = page.getFrameMap();
		assertEquals(frameMap.size(), 3);
		assertEquals(frameMap.get(FRAME_A_ID).getPageContent(), FRAME_A);
		assertEquals(frameMap.get(FRAME_B_ID).getPageContent(), FRAME_B);
		assertEquals(frameMap.get(FRAME_C_ID).getPageContent(), FRAME_C);
	}

	private ExamplePage getPage() {
		return (ExamplePage) DriverManager.getInitialPage();
	}
}
```