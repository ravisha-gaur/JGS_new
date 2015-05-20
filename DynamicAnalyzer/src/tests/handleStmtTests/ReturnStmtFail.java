package tests.handleStmtTests;

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import logging.L2Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tests.testClasses.TestSubClass;
import analyzer.level2.HandleStmtForTests;
import analyzer.level2.SecurityLevel;
import analyzer.level2.storage.ObjectMap;

public class ReturnStmtFail {

	Logger LOGGER = L2Logger.getLogger();
	
	@Before
	public void init() {
		HandleStmtForTests.init();
	}

	@Test
	public void returnStmtTest() {
		
		LOGGER.log(Level.INFO, "RETURN TEST STARTED");
		
		HandleStmtForTests hs = new HandleStmtForTests();
		hs.addObjectToObjectMap(this);
		hs.setLocalPC(SecurityLevel.LOW);
		hs.addLocal("int_res1");
		hs.addLocal("int_res2");
		hs.addLocal("int_res3");
		
		@SuppressWarnings("unused")
		int res1;
		
		@SuppressWarnings("unused")
		int res2;
		
		@SuppressWarnings("unused")
		int res3;
		
		
		TestSubClass tsc = new TestSubClass();
		res1 = tsc.methodWithConstReturn();
		assertEquals(SecurityLevel.LOW, hs.getActualReturnLevel());
		hs.assignReturnLevelToLocal("int_res1");
		
		res2 = tsc.methodWithLowLocalReturn();
		assertEquals(SecurityLevel.LOW, hs.getActualReturnLevel());
		hs.assignReturnLevelToLocal("int_res2");
		
		res3 = tsc.methodWithHighLocalReturn();
		assertEquals(SecurityLevel.HIGH, hs.getActualReturnLevel());
		hs.assignReturnLevelToLocal("int_res3");

		
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_res1"));
		assertEquals(SecurityLevel.LOW, hs.getLocalLevel("int_res2"));
		assertEquals(SecurityLevel.HIGH, hs.getLocalLevel("int_res3"));
		
	    hs.close();	
	    
	    LOGGER.log(Level.INFO, "RETURN TEST FINISHED");
	}

}
