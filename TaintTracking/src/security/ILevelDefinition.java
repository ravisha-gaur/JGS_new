package security;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import annotation.IAnnotationDAO;
import constraints.IConstraint;

public interface ILevelDefinition {

	public int compare(ILevel level1, ILevel level2);

	public Set<IConstraint> extractConstraints(IAnnotationDAO dao, String signature);

	public List<ILevel> extractEffects(IAnnotationDAO dao);

	public ILevel extractFieldLevel(IAnnotationDAO dao);

	public List<ILevel> extractParameterLevels(IAnnotationDAO dao);

	public ILevel extractReturnLevel(IAnnotationDAO dao);

	public Class<? extends Annotation> getAnnotationClassConstraints();

	public Class<? extends Annotation> getAnnotationClassEffects();

	public Class<? extends Annotation> getAnnotationClassFieldLevel();

	public Class<? extends Annotation> getAnnotationClassParameterLevel();

	public Class<? extends Annotation> getAnnotationClassReturnLevel();

	public ILevel getDefaultVariableLevel();

	public ILevel getGreatesLowerBoundLevel();

	public ILevel getGreatestLowerBoundLevel(ILevel level1, ILevel level2);

	public ILevel getLeastUpperBoundLevel();

	public ILevel getLeastUpperBoundLevel(ILevel level1, ILevel level2);

	public ILevel[] getLevels();

	public List<ILevel> getLibraryClassWriteEffects(String className);

	public Set<IConstraint> getLibraryConstraints(String methodName, List<String> parameterTypes, String returnType, String declaringClassName, String signature);
	
	public Set<IConstraint> getLibraryConstraints(String className);

	public ILevel getLibraryFieldLevel(String fieldName, String declaringClassName, String signature);

	public List<ILevel> getLibraryMethodWriteEffects(String methodName, List<String> parameterTypes, String declaringClassName, String signature);

	public List<ILevel> getLibraryParameterLevel(String methodName, List<String> parameterTypes, String declaringClassName, String signature);

	public ILevel getLibraryReturnLevel(String methodName, List<String> parameterTypes, String declaringClassName, String signature, List<ILevel> levels);

}
