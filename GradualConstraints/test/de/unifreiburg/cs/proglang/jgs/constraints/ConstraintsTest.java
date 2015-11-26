package de.unifreiburg.cs.proglang.jgs.constraints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.unifreiburg.cs.proglang.jgs.constraints.CTypes.CType;
import de.unifreiburg.cs.proglang.jgs.constraints.TypeDomain.Type;
import de.unifreiburg.cs.proglang.jgs.constraints.TypeVars.TypeVar;
import de.unifreiburg.cs.proglang.jgs.constraints.secdomains.LowHigh.Level;

import static de.unifreiburg.cs.proglang.jgs.constraints.CTypes.*;
import static de.unifreiburg.cs.proglang.jgs.TestDomain.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ConstraintsTest {

    private TypeVars tvars;
    private TypeVar h1, h2, l1, l2, d1, d2, p1, p2;
    private CType<Level> ch1, ch2, cl1, cl2, cd1, cd2, cp1, cp2;
    private Map<TypeVar, Type<Level>> ass;

    // Particular Sets of variables Variable
    private Set<CType<Level>> allVariables;
    private Set<CType<Level>> nonDyn;
    private Set<CType<Level>> allStatic;

    @Before
    public void setUp() {
        tvars = new TypeVars("");
        h1 = tvars.fresh("h1");
        h2 = tvars.fresh("h2");
        l1 = tvars.fresh("l1");
        l2 = tvars.fresh("l2");
        d1 = tvars.fresh("d1");
        d2 = tvars.fresh("d2");
        p1 = tvars.fresh("p1");
        p2 = tvars.fresh("p2");
        ass = new HashMap<>();
        ass.put(h1, THIGH);
        ass.put(h2, THIGH);
        ass.put(l1, TLOW);
        ass.put(l2, TLOW);
        ass.put(d1, DYN);
        ass.put(d2, DYN);
        ass.put(p1, PUB);
        ass.put(p2, PUB);

        cl1 = variable(l1);
        ch1 = variable(h1);
        cl2 = variable(l2);
        ch2 = variable(h2);
        cd1 = variable(d1);
        cd2 = variable(d2);
        cp1 = variable(p1);
        cp2 = variable(p2);

        allVariables =
            new HashSet<>(Arrays.asList(cl1,
                                        ch1,
                                        cl2,
                                        ch2,
                                        cd1,
                                        cd2,
                                        cp1,
                                        cp2));

        nonDyn = new HashSet<>(allVariables);
        nonDyn.removeAll(Arrays.asList(cd1, cd2));

        allStatic = new HashSet<>(nonDyn);
        allStatic.removeAll(Arrays.asList(cp1, cp2));
    }


    @Test
    public void testLe() {
        Constraint<Level> LleH = leC(cl1, ch1);
        Assignment<Level> a = new Assignment<>(ass);
        assertThat(a, satisfies(LleH));

        Constraint<Level> HleL = leC(ch1, cl1);
        assertThat(a, not(satisfies(HleL)));

        Constraint<Level> HleHigh = leC(ch1, literal(THIGH));
        assertThat(a,satisfies(HleHigh));

        for (CType<Level> t : allVariables) {
            Constraint<Level> c = leC(literal(PUB), t);
            assertThat("all: pub <= " + t.toString(), c, is(satisfiedBy(a)));
        }

        for (CType<Level> t : nonDyn) {
            Constraint<Level> c = leC(literal(DYN), t);
            assertThat("nonDyn: ? /<= " + t.toString(), c, not(is(satisfiedBy(a))));
        }

        Set<CType<Level>> allStatic = new HashSet<>(nonDyn);
        allStatic.removeAll(Arrays.asList(cp1, cp2));
        for (CType<Level> t : nonDyn) {
            Constraint<Level> c = leC(literal(DYN), t);
            assertThat(String.format("static %s /<= t", t.toString()),
                    c, not(is(satisfiedBy(a))));
        }
    }

    @Test
    public void testComp() {

        Assignment<Level> a = new Assignment<>(ass);
        for (CType<Level> t : allVariables) {
            Constraint<Level> c = compC(literal(PUB), t);
            assertThat("all: pub ~ " + t.toString(), c, is(satisfiedBy(a)));
            c = compC(t, literal(PUB));
            assertThat(String.format("all: %s ~ pub", t.toString()),
                    c, is(satisfiedBy(a)));
        }

        for (CType<Level> t1 : allStatic) {
            for (CType<Level> t2 : allStatic) {
                Constraint<Level> c = compC(t1, t2);
                assertThat(String.format("static: %s ~ %s",
                                t1.toString(),
                                t2.toString()),
                        c, is(satisfiedBy(a)));
                c = compC(t2, t1);
                assertThat(String.format("static: %s ~ %s",
                                t2.toString(),
                                t1.toString()),
                        c, is(satisfiedBy(a)));
            }
        }

        for (CType<Level> t : allStatic) {
            assertThat(String.format("%s /~ ?", t.toString()),
                    compC(t, cd1), not(is(satisfiedBy(a))));
            assertThat(String.format("? /~ %s", t.toString()),
                    compC(cd1, t), not(is(satisfiedBy(a))));
        }

        assertThat("d1 ~ d2", compC(cd1, cd2), is(satisfiedBy(a)));
        assertThat("d2 ~ d1", compC(cd2, cd1), is(satisfiedBy(a)));

    }

    @Test
    public void testDImpl() {
        Assignment<Level> a = new Assignment<>(ass);

        for (CType<Level> t1 : nonDyn) {
            for (CType<Level> t2 : allVariables) {
                assertThat(String.format("%s -->? %s",
                                t1.toString(),
                                t2.toString()),
                        dimplC(t1, t2), is(satisfiedBy(a)));
            }
        }

        for (CType<Level> t1 : allStatic) {
            assertThat(String.format("static: ? /-->? %s",
                            t1.toString()),
                    dimplC(cd1, t1), not(is(satisfiedBy(a))));
        }
        
        assertThat("? -->? ?", dimplC(cd1, cd2), is(satisfiedBy(a)));
        assertThat("? -->? pub", dimplC(cd1, cp2), is(satisfiedBy(a)));

    }

    @Test
    public void testEquals() {
        Constraint<Level> c1 = leC(cd1, ch2);
        Constraint<Level> c12 = leC(cd1, ch2);
        Constraint<Level> c2 = leC(cd2, cl2);

        assertThat(c1, is(c1));
        assertThat(c1, is(c12));
        assertThat(c1, not(is(c2)));
    }

}
