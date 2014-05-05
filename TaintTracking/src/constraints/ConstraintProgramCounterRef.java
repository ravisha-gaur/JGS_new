package constraints;

public class ConstraintProgramCounterRef extends AConstraintReference {
	
	public ConstraintProgramCounterRef(String signature) {
		super(signature);
	}

	public String toString() {
		return "PC@" + reduceInternalSignature();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ConstraintProgramCounterRef other = (ConstraintProgramCounterRef) obj;
		if (signature == null) {
			if (other.signature != null) return false;
		} else if (!signature.equals(other.signature)) return false;
		return true;
	}
	
	
	
}
