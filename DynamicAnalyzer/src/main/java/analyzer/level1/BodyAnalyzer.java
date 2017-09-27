package analyzer.level1;

import de.unifreiburg.cs.proglang.jgs.constraints.TypeViews;
import de.unifreiburg.cs.proglang.jgs.instrumentation.Casts;
import de.unifreiburg.cs.proglang.jgs.instrumentation.MethodTypings;
import soot.*;
import soot.util.Chain;
import utils.dominator.DominatorFinder;
import utils.logging.L1Logger;
import utils.visitor.AnnotationStmtSwitch;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Analyzer is applied to every method.
 * If it's the main method, then ...
 * For each constructor, an new Object and Field Map is inserted into the ObjectMap.
 * For each method, a HandleStmt object is inserted (which contains a local Map 
 * for the Locals and the localPC).
 * Then every Local is inserted into this map.
 * At least it iterates over all Units and calls the appropriate operation
 * At the end (but before the return statement) it calls HandleStmt.close()
 * @author koenigr
 *
 */
public class BodyAnalyzer<L> extends BodyTransformer{

    MethodTypings<L> methodTypings;
	boolean controllerIsActive;
	int expectedException;
	Casts<L> casts;
    public BodyAnalyzer(MethodTypings<L> m,
						Casts<L> c) {
        methodTypings = m;
        this.controllerIsActive = controllerIsActive;
        this.expectedException = expectedException;
        casts = c;
    }

    /**
	 * internalTransform is an internal soot method, which is called somewhere along the way.
	 * for us, it serves as an entry point to the instrumentation process
	 */
	@Override
	protected void internalTransform(Body arg0, String arg1,
				@SuppressWarnings("rawtypes") Map arg2) {
		SootMethod sootMethod;
		Body body;


		/*
		 * Chain<Unit> units contains for TestProgramm Simple:
		 * 
		 * [r0 := @parameter0: java.lang.String[], r1 = "Hello World", 
		 * $r2 = staticinvoke <utils.analyzer.DynamicLabel: java.lang.Object makeHigh(java.lang.Object)>(r1),
		 * r3 = (java.lang.String) $r2, $r4 = <java.lang.System: java.io.PrintStream out>, 
		 * virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.String)>(r3), return]
		 * Invoke HandleStmt in method main
		 */
		Chain<Unit> units;
		
		/*
		 * Chain<Local> locals contains for TestProgramm Simple:
		 * 
		 * [r0, r1, $r2, r3, $r4]
		 */
		Chain<Local> locals;
		AnnotationStmtSwitch stmtSwitch;
		
		
		/*
		 * hain<SootField> fields contains for TestProgramm Simple:
		 *
		 * []		//empty
		 */
		Chain<SootField> fields;


		Logger logger = L1Logger.getLogger();
		
		logger.log(Level.INFO, "\n BODYTRANSFORM STARTED: {0}",
				arg0.getMethod().getName());
	
		body = arg0;
		sootMethod = body.getMethod();
		fields = sootMethod.getDeclaringClass().getFields();

		stmtSwitch = new AnnotationStmtSwitch(body);

		DominatorFinder.init(body);

		// für jeden methodenbody wird der Bodyanalyzer einmal "ausgeführt"
        // Intraprozedurale Methode
        // Body-Analyz implementiert Analyse in EINER Methode. Neu aufgerufen für jede Methode
		JimpleInjector.setBody(body);

		// hand over exactly those Maps that contain Instantiation, Statement and Locals for the currently analyzed method
		JimpleInjector.setStaticAnalaysisResults(methodTypings.getVarTyping(sootMethod),
                                                 methodTypings.getCxTyping(sootMethod),
                                                 // We set the default type to dyn; our RT-system is able to handle untracked variables.
                                                 methodTypings.getSingleInstantiation(sootMethod, new TypeViews.Dyn<>()),
                                                 casts);

		units = body.getUnits();


		// invokeHS should be at the beginning of every method-body. 
		// It creates a map for locals.
		JimpleInjector.invokeHS();
		JimpleInjector.addNeededLocals();

		// We have to initialize the run-time system at the very beginning.
		// That is, either
		// - at the beginning of the clinit of the Main class (which is // the class that contains the main method), *if it exists*, or
		// - if there is no clinit, at the beginning of main
		// TODO: the run-time system should inititalize itself lazily, perhaps (i.e., on-demand)
		if (isFirstApplicationMethodToRun(sootMethod)) {
			JimpleInjector.initHS();
		}

        JimpleInjector.initHandleStmtUtils(controllerIsActive, expectedException);

		/*
		 * If the method is the constructor, the newly created object
		 * has to be added to the ObjectMap and its fields are added to the
		 * new object
		 */
		if (sootMethod.getName().equals("<init>")) {
			logger.log(Level.INFO, "Entering <init>");
			JimpleInjector.addInstanceObjectToObjectMap();
						
			// Add all instance fields to ObjectMap
			Iterator<SootField> fIt = fields.iterator();
			while (fIt.hasNext()) {
				SootField item = fIt.next();
				if (!item.isStatic()) {
					JimpleInjector.addInstanceFieldToObjectMap(item);
				}
			}
						
		} else if (sootMethod.getName().equals("<clinit>")) {
			logger.log(Level.INFO, "Entering <clinit>");
			SootClass sc = sootMethod.getDeclaringClass();
			JimpleInjector.addClassObjectToObjectMap(sc);
						
			// Add all static fields to ObjectMap
			Iterator<SootField> fIt = fields.iterator();
			while (fIt.hasNext()) {
				SootField item = fIt.next();
				if (item.isStatic()) {
					JimpleInjector.addStaticFieldToObjectMap(item);
				} 
			}
		}
				

		// Add all locals to LocalMap except the locals which 
		// are inserted for analysis purposes.
		// locals are not added anymore all at the beginning. Instead, they are added only when needed.
		/*Iterator<Local> lit = locals.iterator();
		while (lit.hasNext()) {
			Local item = lit.next();
			if (!(item.getName() == "local_for_Strings") 
					&& !(item.getName() == "local_for_String_Arrays")
					&& !(item.getName() == "local_for_Strings2") 
					&& !(item.getName() == "local_for_Strings3") 
					&& !(item.getName() == "local_for_Objects") 
					&& !(item.getName() == "local_level")
					&& !(item.getName() == "hs")) {
				//JimpleInjector.addLocal(item);
			}
		}*/

				
				
		Iterator<Unit> uit = units.iterator();
		while (uit.hasNext()) {
			Unit item = uit.next();
			
			// Check if the statements is a postdominator for an IfStmt.
			if (DominatorFinder.containsStmt(item)) {
				JimpleInjector.exitInnerScope(item);
				logger.log(Level.INFO, "Exit inner scope with identity {0}", 
					DominatorFinder.getIdentityForUnit(item));

				DominatorFinder.removeStmt(item);
			}
			
			// Add further statements using JimpleInjector.
			item.apply(stmtSwitch);
		}
		
		// Apply all changes.
		JimpleInjector.addUnitsToChain();			
		
		JimpleInjector.closeHS();
	}

	/**
	 * Return the very first method (of the app) to run.
	 * Either clinit of the
	 * Main-class, or main.
	 */
	private boolean isFirstApplicationMethodToRun(SootMethod firstMethodCand) {
		// TODO: this does not work, because "no main method set".. why?
	    /*SootClass mainClass = Scene.v().getMainMethod().getDeclaringClass();
		for (SootMethod m : mainClass.getMethods()) {
			if (m.getName().equals("<clinit>")) {
			    return m;
			}
		}
		return Scene.v().getMainMethod();
		*/
	    if (firstMethodCand.isMain()) {
			for (SootMethod m : firstMethodCand.getDeclaringClass().getMethods()) {
				if (m.getName().equals("<clinit>")) {
					return false;
				}
			}
			// there is no clinit in the main-class
			return true;
		} else if (firstMethodCand.getName().equals("<clinit>")){
			for (SootMethod m : firstMethodCand.getDeclaringClass().getMethods()) {
				if (m.isMain()) {
					return true; // we are clinit, and in the main class
				}
			}
			// we are another clinit, not in the main class
			return false;
		} else {
			return false; // neither clinit nor main method
		}
	}
}
