package de.unifreiburg.cs.proglang.jgs.typing;

import de.unifreiburg.cs.proglang.jgs.constraints.TypeVars;
import de.unifreiburg.cs.proglang.jgs.jimpleutils.Var;
import de.unifreiburg.cs.proglang.jgs.signatures.Symbol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static de.unifreiburg.cs.proglang.jgs.constraints.TypeVars.TypeVar;

/**
 * Companion class to <code>Environment</code>
 * @author fennell
 *
 */
public class Environments {
    
    public static Environment makeEmpty() {
        return new Environment(Collections.<Var<?>, TypeVar>emptyMap());
    }

    public static Environment fromMap(Map<Var<?>, TypeVars.TypeVar> m) {
        return new Environment(m);
    }


    public static <Level> Environment forParamMap(TypeVars tvars, Map<Symbol.Param<Level>, TypeVar> symbolMapping) {
        Map<Var<?>, TypeVar> m = new HashMap<>();
        for (Map.Entry<Symbol.Param<Level>, TypeVar> e : symbolMapping.entrySet()) {
            m.put(Var.fromParam(e.getKey()), e.getValue());
        }
       return fromMap(m);
    }
}
