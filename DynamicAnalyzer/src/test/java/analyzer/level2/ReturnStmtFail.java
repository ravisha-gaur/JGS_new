package analyzer.level2;

import static org.junit.Assert.assertEquals;

import analyzer.level2.HandleStmt;
import analyzer.level2.SecurityLevel;
import org.junit.Before;
import org.junit.Test;
import tests.testclasses.TestSubClass;
import utils.exceptions.IllegalFlowException;
import utils.logging.L2Logger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ReturnStmtFail {

	Logger logger = L2Logger.getLogger();
	
	@Before
	public void init() {
		HandleStmt.init();
	}

	/**
	 * Tests the NSU policy: 
	 * hs = LOW, int_res1 = LOW, localPC = HIGH.
	 * 
	 * the line "hs.assignReturnLevelToLocal("int_res1") is instrumented to assigns like:
	 * res1 = ExternalClass.returnSometing(): The local variable res1 needs to be upgraded with the 
	 * sec-value of ExternalClass.returnSometing(). But before that can happen, we need to check
	 * if NSU policy holds (remeber: we mustn upgrade low-sec vars in high-sec PC environment. 
	 * Thus, in assignReturnLevelToLocal, we perform the adequate NSU check (checkLocalPC), and since
	 * we have here: local < PC => IllegalFlowException!
	 */
	@Test(expected = IllegalFlowException.class)
	public void returnStmtTestFail() {
		
		logger.log(Level.INFO, "RETURN TEST STARTED");
		
		HandleStmt hs = new HandleStmt();
		hs.initHandleStmtUtils(false);
		hs.addObjectToObjectMap(this);
		hs.pushLocalPC(SecurityLevel.top(), 123);
		hs.addLocal("int_res1");
		
		// Initialize int_res1 !
		hs.initializeVariable("int_res1");
		
		@SuppressWarnings("unused")
		int res1;
		
		
		TestSubClass tsc = new TestSubClass();
		res1 = tsc.methodWithConstReturn();
		assertEquals(SecurityLevel.bottom(), hs.getActualReturnLevel());
		hs.assignReturnLevelToLocal("int_res1");			// IFexception thrown here
		

		hs.popLocalPC(123);
		hs.close();	
	    
		logger.log(Level.INFO, "RETURN TEST FINISHED");
	}
	
	
	@Test
	public void returnStmtTestSuccess() {
		
		logger.log(Level.INFO, "RETURN TEST STARTED");
		
		HandleStmt hs = new HandleStmt();
		hs.initHandleStmtUtils(false);
		hs.addObjectToObjectMap(this);
		hs.pushLocalPC(SecurityLevel.top(), 123);
		hs.addLocal("int_res1");
		
		@SuppressWarnings("unused")
		int res1;
		
		
		TestSubClass tsc = new TestSubClass();
		res1 = tsc.methodWithConstReturn();
		assertEquals(SecurityLevel.bottom(), hs.getActualReturnLevel());
		hs.assignReturnLevelToLocal("int_res1");
		

		hs.popLocalPC(123);
		hs.close();	
	    
		logger.log(Level.INFO, "RETURN TEST FINISHED");
	}

}
