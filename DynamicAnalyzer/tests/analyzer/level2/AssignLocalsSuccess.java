package analyzer.level2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import analyzer.level2.HandleStmtForTests;
import analyzer.level2.SecurityLevel;
import org.junit.Before;
import org.junit.Test;
import tests.testClasses.TestSubClass;
import utils.logging.L2Logger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AssignLocalsSuccess {
	
	Logger logger = L2Logger.getLogger();
	
	@Before
	public void init() {
		HandleStmtForTests.init();
	}

	@Test
	public void assignConstantToLocal() {
		
		logger.log(Level.INFO, "ASSIGN CONSTANT TO LOCAL TEST STARTED");
		
		HandleStmtForTests hs = new HandleStmtForTests();
		hs.addLocal("int_x", SecurityLevel.LOW);
		
		/*
		 *  int x = c;
		 *  1. Check if Level(x) >= lpc
		 *  2. Assign level of lpc to local
		 */
		assertEquals(SecurityLevel.LOW, hs.setLevelOfLocal("int_x")); // x = LOW, lpc = LOW
		
		hs.makeLocalHigh("int_x");
		assertEquals(SecurityLevel.LOW, hs.setLevelOfLocal("int_x")); // x = HIGH, lpc = LOW
		
		hs.makeLocalHigh("int_x");
		hs.pushLocalPC(SecurityLevel.HIGH, 123);
		assertEquals(SecurityLevel.HIGH, hs.setLevelOfLocal("int_x")); //x = HIGH,lpc = HIGH
		
		hs.popLocalPC(123);
		hs.close();

		logger.log(Level.INFO, "ASSIGN CONSTANT TO LOCAL TEST FINISHED");
	}
	
	@Test
	public void assignLocalsToLocal() {
		
		logger.log(Level.INFO, "ASSIGN LOCALS TO LOCAL TEST STARTED");
		
		HandleStmtForTests hs = new HandleStmtForTests();
		hs.addLocal("int_x");
		hs.addLocal("int_y");
		hs.addLocal("int_z", SecurityLevel.HIGH);
		
		/*
		 *  Assign Locals to Local
		 *  int x = y + z;
		 *  1. Check if Level(x) >= lpc
		 *  2. Assign Join(y, z, lpc) to x
		 */
		hs.pushLocalPC(SecurityLevel.LOW, 123);
		// x = LOW, lpc = LOW
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_x"));
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_y"));
		assertEquals(SecurityLevel.HIGH, hs.getLocalLevel("int_z"));
		assertEquals(SecurityLevel.LOW, hs.getLocalPC());
		assertEquals(SecurityLevel.LOW, hs.addLevelOfLocal("int_y"));
		assertEquals(SecurityLevel.HIGH, hs.addLevelOfLocal("int_z"));
		assertEquals(SecurityLevel.HIGH, hs.setLevelOfLocal("int_x"));
		
		hs.makeLocalLow("int_z");
		// x = HIGH, lpc = LOW
		assertEquals(SecurityLevel.HIGH, hs.getLocalLevel("int_x"));
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_y"));
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_z"));
		assertEquals(SecurityLevel.LOW, hs.getLocalPC());
		assertEquals(SecurityLevel.LOW, hs.addLevelOfLocal("int_y"));
		assertEquals(SecurityLevel.LOW, hs.addLevelOfLocal("int_z"));
		assertEquals(SecurityLevel.LOW, hs.setLevelOfLocal("int_x"));
		
		hs.pushLocalPC(SecurityLevel.HIGH, 123);
		hs.makeLocalHigh("int_x");
		// x = HIGH, lpc = HIGH
		assertEquals(SecurityLevel.HIGH, hs.getLocalLevel("int_x"));
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_y"));
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_z"));
		assertEquals(SecurityLevel.HIGH, hs.getLocalPC());
		assertEquals(SecurityLevel.LOW, hs.addLevelOfLocal("int_y"));
		assertEquals(SecurityLevel.LOW, hs.addLevelOfLocal("int_z"));
		assertEquals(SecurityLevel.HIGH, hs.setLevelOfLocal("int_x"));
		
		hs.popLocalPC(123);
		hs.popLocalPC(123);
		hs.close();	

		logger.log(Level.INFO, "ASSIGN CONSTANT TO LOCAL TEST FINISHED");
	}
	
	@Test
	public void assignFieldToLocal() {
		
		logger.log(Level.INFO, "ASSIGN FIELD TO LOCAL TEST STARTED");
	    
		HandleStmtForTests hs = new HandleStmtForTests();

		hs.addObjectToObjectMap(this);
		hs.addFieldToObjectMap(this, "String_field");
		hs.addLocal("String_local");
		
		/*
		 * Assign Field to Local
		 * 1. Check if Level(local) >= lpc
		 * 2. Assign Level(field) to Level(local)
		 */
		// local = LOW, lpc = LOW, field = LOW
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("String_local"));
		assertEquals(SecurityLevel.LOW, hs.getFieldLevel(this, "String_field"));
		assertEquals(SecurityLevel.LOW, hs.getLocalPC());
		assertEquals(SecurityLevel.LOW, hs.addLevelOfField(this, "String_field"));
		assertEquals(SecurityLevel.LOW, hs.setLevelOfLocal("String_local"));
		
		// local = HIGH, lpc = LOW, field = LOW
		hs.makeLocalHigh("String_local");
		assertEquals(SecurityLevel.HIGH, hs.getLocalLevel("String_local"));
		assertEquals(SecurityLevel.LOW, hs.getFieldLevel(this, "String_field"));
		assertEquals(SecurityLevel.LOW, hs.getLocalPC());
		assertEquals(SecurityLevel.LOW, hs.addLevelOfField(this, "String_field"));
		assertEquals(SecurityLevel.LOW, hs.setLevelOfLocal("String_local"));
		
		// local = LOW, lpc = LOW, field = LOW
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("String_local"));
		assertEquals(SecurityLevel.LOW, hs.getFieldLevel(this, "String_field"));
		assertEquals(SecurityLevel.LOW, hs.getLocalPC());
		assertEquals(SecurityLevel.LOW, hs.addLevelOfField(this, "String_field"));
		assertEquals(SecurityLevel.LOW, hs.setLevelOfLocal("String_local"));
		
		hs.close();	

		logger.log(Level.INFO, "ASSIGN METHOD RESULT TO LOCAL TEST FINISHED");
	}
	
	@Test
	public void assignNewObjectToLocal() {
		
		logger.log(Level.INFO, "ASSIGN FIELD TO LOCAL TEST STARTED");
		
		HandleStmtForTests hs = new HandleStmtForTests();
		hs.addLocal("TestSubClass_xy");
		
		/*
		 *  Assign new Object
		 *  check(xy) >= lpc
		 *  lpc -> xy
		 *  add new Object to ObjectMap
		 */
		
		
		TestSubClass xy = new TestSubClass();
		assertTrue(hs.containsObjectInObjectMap(xy));
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("TestSubClass_xy"));
		
		hs.setLevelOfLocal("TestSubClass_xy", SecurityLevel.HIGH);
		hs.setLevelOfLocal("TestSubClass_xy");
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("TestSubClass_xy"));
		
		hs.close();	

		logger.log(Level.INFO, "ASSIGN NEW OBJECT TO LOCAL TEST FINISHED");
	}
	
	@SuppressWarnings("unused")
	@Test
	public void assignMethodResultToLocal() {

		logger.log(Level.INFO, "ASSIGN METHOD RESULT TO LOCAL TEST STARTED");
		
		HandleStmtForTests hs = new HandleStmtForTests();
		
		/*
		 * Assign method (result)
		 */
		int res;
		TestSubClass xy = new TestSubClass();
		hs.addLocal("int_res");
		hs.addLocal("TestSubClass_xy");
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_res"));

		hs.addLevelOfLocal("TestSubClass_xy");
		hs.setLevelOfLocal("int_res");
		res = xy.methodWithConstReturn();
		hs.assignReturnLevelToLocal("int_res");
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_res"));

		hs.addLevelOfLocal("TestSubClass_xy");
		hs.setLevelOfLocal("int_res");
		res = xy.methodWithLowLocalReturn();
		hs.assignReturnLevelToLocal("int_res");
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_res"));

		hs.addLevelOfLocal("TestSubClass_xy");
		hs.setLevelOfLocal("int_res");
		res = xy.methodWithHighLocalReturn();
		hs.assignReturnLevelToLocal("int_res");
		assertEquals(SecurityLevel.HIGH, hs.getLocalLevel("int_res"));
		
		hs.close();	
	    

		logger.log(Level.INFO, "ASSIGN METHOD RESULT TO LOCAL TEST FINISHED");
	}
	
	@Test
	public void assignArgumentToLocal() {

		logger.log(Level.INFO, "ASSIGN METHOD RESULT TO LOCAL TEST STARTED");
		
		HandleStmtForTests hs = new HandleStmtForTests();
		
		/*
		 * Assign argument
		 */
		
		hs.addLocal("int_a");
		hs.addLocal("int_b", SecurityLevel.HIGH);
		hs.addLocal("int_c");
		
		hs.addLocal("int_res");
		
		hs.storeArgumentLevels("int_a", "int_b", "int_c");

		hs.assignArgumentToLocal(0, "int_res");
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_res"));
		hs.assignArgumentToLocal(1, "int_res");
		assertEquals(SecurityLevel.HIGH, hs.getLocalLevel("int_res"));
		hs.assignArgumentToLocal(2, "int_res");
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_res"));
		
		hs.close();	
	    

		logger.log(Level.INFO, "ASSIGN METHOD RESULT TO LOCAL TEST FINISHED");
	}
	
	@SuppressWarnings("unused")
	@Test
	public void assignConstantAndLocalToLocal() {
		
		logger.log(Level.INFO, "ASSIGN CONSTANT AND LOCAL TO LOCAL SUCCESS TEST STARTED");
				
		HandleStmtForTests hs = new HandleStmtForTests();
		
		/*
		 * x++; or x += 1;  or x = x + 1;
		 */
		
		hs.addLocal("int_x");
		int x = 0;

		hs.addLevelOfLocal("TestSubClass_xy");
		hs.setLevelOfLocal("int_res"); // Just ignore the constants
		x++;
		
		hs.close();
				
		logger.log(Level.INFO, "ASSIGN CONSTANT AND LOCAL TO LOCAL SUCCESS TEST FINISHED");
	}

}
