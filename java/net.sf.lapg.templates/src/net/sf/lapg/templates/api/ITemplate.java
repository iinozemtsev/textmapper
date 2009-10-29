package net.sf.lapg.templates.api;

/**
 * Loaded and validated template.
 */
public interface ITemplate extends ILocatedEntity {

	/**
	 * @return template name
	 */
	String getName();

	/**
	 * @return qualified name of template's package
	 */
	String getPackage();

	/**
	 * Evaluates template in context and environment.
	 * @return result of invocation
	 */
	String apply(EvaluationContext context, IEvaluationStrategy env, Object[] arguments) throws EvaluationException;

	/**
	 * @return parameter names
	 */
	String getSignature();

	/**
	 * Returns base template
	 */
	ITemplate getBase();

	/**
	 * Returns base template
	 */
	void setBase(ITemplate base);
}
