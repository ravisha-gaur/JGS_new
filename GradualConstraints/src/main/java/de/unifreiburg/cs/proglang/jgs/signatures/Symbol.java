package de.unifreiburg.cs.proglang.jgs.signatures;

import de.unifreiburg.cs.proglang.jgs.constraints.CTypes;
import de.unifreiburg.cs.proglang.jgs.constraints.CTypes.CType;
import de.unifreiburg.cs.proglang.jgs.constraints.TypeDomain;
import de.unifreiburg.cs.proglang.jgs.constraints.TypeViews;
import de.unifreiburg.cs.proglang.jgs.constraints.TypeViews.TypeView;
import de.unifreiburg.cs.proglang.jgs.signatures.SymbolViews.SymbolView;

import java.util.*;
/**
 * JGS Symbols that occur in method signatures. They correspond to Jimples Parameters, @return, or literals
 */
public abstract class Symbol<Level> {
    /**
     * Create a parameter symbol from a type and a position.
     * <p>
     * Why not use ParameterRef? Because it only implements pointer equality and there is no way to get to the
     * ParameterRefs of a SootMethod (that I know of)
     */
    public static <Level> Param<Level> param(int position) {
        if (position < 0) {
            throw new IllegalArgumentException(String.format("Negative position for parameter: %d", position));
        }
        return new Param<>(position);
    }



    // TODO-performance: make a singleton out of this
    public static <Level> Symbol<Level> ret() {
        return new Return<>();
    }

    public static <Level> Symbol<Level> literal(TypeView<Level> t) {
        return new Literal<>(t);
    }

    @Override
    public abstract String toString();

    // TODO: wrong place for this operation. It only performs a lookup and fails if the element is not found. this should be simply a map with guarded lookup..
    public abstract CType<Level> toCType(Map<Symbol<Level>, CType<Level>> tvarMapping);

    public abstract SymbolView<Level> inspect();

//    protected abstract Optional<TypeVar> asTypeVar(TypeVars tvars);

    public static class Param<Level> extends Symbol<Level> {
        public final int position;

        private Param(int position) {
            super();
            this.position = position;
        }

        @Override
        public String toString() {
            return String.format("@param%d", this.position);
        }

        @Override
        public CType<Level> toCType(Map<Symbol<Level>, CType<Level>> tvarMapping) {
            CType<Level> result = tvarMapping.get(this);
            if (result == null) throw new NoSuchElementException(String.format(
                            "No mapping for %s: %s",
                            this,
                            tvarMapping.toString()));
            return result;
        }

        @Override
        public SymbolView<Level> inspect() {
            return new SymbolViews.Param<Level>(position);
        }

//        @Override
//        protected Optional<TypeVar> asTypeVar(TypeVars tvars) {
//            return Optional.of(tvars.param(Var.fromParam(this)));
//        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Param<?> param = (Param<?>) o;

            return position == param.position;

        }

        @Override
        public int hashCode() {
            return position;
        }
    }

    public static class Return<Level> extends Symbol<Level> {

        private Return() {
        }

        @Override
        public String toString() {
            return "@return";
        }

        @Override
        public CType<Level> toCType(Map<Symbol<Level>, CType<Level>> tvarMapping) {
            CType<Level> result = tvarMapping.get(this);
            if (result == null) throw new NoSuchElementException(String.format(
                    "No mapping for %s: %s",
                    this,
                    tvarMapping.toString()));
            return result;
        }

        @Override
        public SymbolView<Level> inspect() {
            return new SymbolViews.Return<Level>();
        }

//        @Override
//        protected Optional<TypeVar> asTypeVar(TypeVars tvars) {
//            return Optional.of(tvars.ret());
//        }

        @Override
        // TODO-performance: should be a singleton
        public boolean equals(Object other) {
            return this.getClass().equals(other.getClass());
        }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

    }

    public static class Literal<Level> extends Symbol<Level> {
        final TypeView<Level> me;

        private Literal(TypeView<Level> me) {
            super();
            this.me = me;
        }

        @Override
        public String toString() {
            return me.toString();
        }

        @Override
        public CType<Level> toCType(Map<Symbol<Level>, CType<Level>> tvarMapping) {
            return CTypes.literal(this.me);
        }

        @Override
        public SymbolView<Level> inspect() {
            return new SymbolViews.Literal<Level>(this.me);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Literal<?> literal = (Literal<?>) o;

            return !(me != null ? !me.equals(literal.me) : literal.me != null);

        }

        @Override
        public int hashCode() {
            return me != null ? me.hashCode() : 0;
        }
    }
}
