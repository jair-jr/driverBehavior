package br.com.j2.apm.classifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.NormalizableDistance;
import weka.core.Option;
import weka.core.Range;
import weka.core.Utils;
import weka.core.neighboursearch.PerformanceStats;

/**
 * 
 * Calculates the distance between two instances using the {@link DTW} algorithm.
 * 
 * If the instance has more than one attribute, the distance is the simple mean
 * of the DTW distances of each attribute. 
 * 
 * Boa parte do código foi copiado de {@link NormalizableDistance}.
 * 
 *  <!-- options-start -->
 * <p> Valid options are: <p/>
 * 
 * <pre> -R &lt;col1,col2-col4,...&gt;
 *  Specifies list of columns to used in the calculation of the 
 *  distance. 'first' and 'last' are valid indices.
 *  (default: first-last)</pre>
 * 
 * <pre> -V
 *  Invert matching sense of column indices.</pre>
 * 
 <!-- options-end --> 

 * @author pma029
 *
 */
public class DTWDistanceFunction implements DistanceFunction, Serializable {

	/**
	 * Serial version id to avoid warning
	 */
	private static final long serialVersionUID = -6122544363344080508L;

	/** The range of attributes to use for calculating the distance. */
	private Range attributeIndices = new Range("first-last");

	/** Whether all the necessary preparations have been done. */
	private boolean validated;
	
	/** the instances used internally. */
	private Instances instances = null;

	/** The boolean flags, whether an attribute will be used or not. */
	private boolean[] activeIndices;

	/**
	 * Invalidates the distance function, Instances must be still set.
	 */
	public DTWDistanceFunction() {
		invalidate();
	}
	
	/**
	 * Initializes the distance function and automatically initializes the
	 * ranges.
	 * 
	 * @param data
	 *            the instances the distance function should work on
	 */
	public DTWDistanceFunction(Instances data) {
		setInstances(data);
	}

	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		final List<Option> result = new ArrayList<Option>();

		result.add(new Option(
				"\tSpecifies list of columns to used in the calculation of the \n"
						+ "\tdistance. 'first' and 'last' are valid indices.\n" + "\t(default: first-last)",
				"R", 1, "-R <col1,col2-col4,...>"));

		result.add(new Option("\tInvert matching sense of column indices.", "V", 0, "-V"));

		return Collections.enumeration(result);
	}

	/**
	 * Parses a given list of options.
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		final String tmpStr = Utils.getOption('R', options);

		if (tmpStr.length() != 0) {
			setAttributeIndices(tmpStr);
		} else {
			setAttributeIndices("first-last");
		}

		setInvertSelection(Utils.getFlag('V', options));
	}

	/**
	 * Gets the current settings.
	 * 
	 * @return an array of strings suitable for passing to setOptions()
	 */
	@Override
	public String[] getOptions() {
		final List<String> result = new ArrayList<>();

		result.add("-R");
		result.add(getAttributeIndices());

		if (getInvertSelection()) {
			result.add("-V");
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Sets the instances.
	 * 
	 * @param insts
	 *            the instances to use
	 */
	@Override
	public void setInstances(Instances insts) {
		instances = insts;
	    invalidate();
	}

	/**
	 * returns the instances currently set.
	 * 
	 * @return the current instances
	 */
	@Override
	public Instances getInstances() {
		return instances;
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String attributeIndicesTipText() {
		return "Specify range of attributes to act on. " + "This is a comma separated list of attribute indices, with "
				+ "\"first\" and \"last\" valid values. Specify an inclusive "
				+ "range with \"-\". E.g: \"first-3,5,6-10,last\".";
	}

	/**
	 * Sets the range of attributes to use in the calculation of the distance.
	 * The indices start from 1, 'first' and 'last' are valid as well. E.g.:
	 * first-3,5,6-last
	 * 
	 * @param value
	 *            the new attribute index range
	 */
	@Override
	public void setAttributeIndices(String value) {
		attributeIndices.setRanges(value);
		invalidate();
	}

	/**
	 * Gets the range of attributes used in the calculation of the distance.
	 * 
	 * @return the attribute index range
	 */
	@Override
	public String getAttributeIndices() {
		return attributeIndices.getRanges();
	}
	
	/**
	 * invalidates all initializations.
	 */
	protected void invalidate() {
		validated = false;
	}
	
	/**
	 * performs the initializations if necessary.
	 */
	protected void validate() {
		if (!validated) {
			initialize();
			validated = true;
		}
	}
	
	/**
	 * initializes the ranges and the attributes being used.
	 */
	protected void initialize() {
		initializeAttributeIndices();
		//initializeRanges();
	}
	
	/**
	 * initializes the attribute indices.
	 */
	protected void initializeAttributeIndices() {
		attributeIndices.setUpper(instances.numAttributes() - 1);
		activeIndices = new boolean[instances.numAttributes()];
		for (int i = 0; i < activeIndices.length; i++) {
			activeIndices[i] = attributeIndices.isInRange(i);
		}
	}
	
	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String invertSelectionTipText() {
		return "Set attribute selection mode. If false, only selected "
				+ "attributes in the range will be used in the distance calculation; if "
				+ "true, only non-selected attributes will be used for the calculation.";
	}

	/**
	 * Sets whether the matching sense of attribute indices is inverted or not.
	 * 
	 * @param value
	 *            if true the matching sense is inverted
	 */
	@Override
	public void setInvertSelection(boolean value) {
		attributeIndices.setInvert(value);
		invalidate();
	}

	/**
	 * Gets whether the matching sense of attribute indices is inverted or not.
	 * 
	 * @return true if the matching sense is inverted
	 */
	@Override
	public boolean getInvertSelection() {
		return attributeIndices.getInvert();
	}

	/**
	 * Calculates the distance between two instances.
	 * 
	 * @param first
	 *            the first instance
	 * @param second
	 *            the second instance
	 * @return the distance between the two given instances
	 */
	@Override
	public double distance(Instance first, Instance second) {
		return distance(first, second, null);
	}

	/**
	 * Calculates the distance between two instances.
	 * 
	 * @param first
	 *            the first instance
	 * @param second
	 *            the second instance
	 * @param stats
	 *            the performance stats object
	 * @return the distance between the two given instances
	 */
	@Override
	public double distance(Instance first, Instance second, PerformanceStats stats) {
		return distance(first, second, Double.POSITIVE_INFINITY, stats);
	}

	/**
	 * Calculates the distance between two instances. Offers speed up (if the
	 * distance function class in use supports it) in nearest neighbour search
	 * by taking into account the cutOff or maximum distance. Depending on the
	 * distance function class, post processing of the distances by
	 * postProcessDistances(double []) may be required if this function is used.
	 * 
	 * @param first
	 *            the first instance
	 * @param second
	 *            the second instance
	 * @param cutOffValue
	 *            If the distance being calculated becomes larger than
	 *            cutOffValue then the rest of the calculation is discarded.
	 * @return the distance between the two given instances or
	 *         Double.POSITIVE_INFINITY if the distance being calculated becomes
	 *         larger than cutOffValue.
	 */
	@Override
	public double distance(Instance first, Instance second, double cutOffValue) {
		return distance(first, second, cutOffValue, null);
	}

	/**
	 * Calculates the distance between two instances. Offers speed up (if the
	 * distance function class in use supports it) in nearest neighbour search
	 * by taking into account the cutOff or maximum distance. Depending on the
	 * distance function class, post processing of the distances by
	 * postProcessDistances(double []) may be required if this function is used.
	 * 
	 * @param first
	 *            the first instance
	 * @param second
	 *            the second instance
	 * @param cutOffValue
	 *            If the distance being calculated becomes larger than
	 *            cutOffValue then the rest of the calculation is discarded.
	 * @param stats
	 *            the performance stats object
	 * @return the distance between the two given instances or
	 *         Double.POSITIVE_INFINITY if the distance being calculated becomes
	 *         larger than cutOffValue.
	 */
	@Override
	public double distance(Instance first, Instance second, double cutOffValue, PerformanceStats stats) {
		validate();
		
		final int classIndex = instances.classIndex();
		
		double sum = 0;
		int attributesCount = 0;
		
		for(int i = 0; i < activeIndices.length; i++){
			if(!activeIndices[i]){
				continue;
			}
			if(i == classIndex){
				continue;
			}
			
			final DTW dtw = new DTW(getSequence(first.stringValue(i)), getSequence(second.stringValue(i)));
			dtw.calculate();
			sum += dtw.getMinimumDistance();
			
			if(stats != null){
				stats.incrCoordCount();
			}
			attributesCount++;
		}
		
		return sum / attributesCount;
	}
	
	private static double[] getSequence(String attributeValue){
		if(attributeValue == null){
			return new double[0];
		}
		
		return Stream.of(attributeValue.split(";"))
			.mapToDouble(s -> Double.parseDouble(s))
			.toArray();
	}

	/**
	 * Does nothing, derived classes may override it though.
	 * 
	 * @param distances
	 *            the distances to post-process
	 */
	@Override
	public void postProcessDistances(double[] distances) {
	}

	/**
	 * Update the distance function (if necessary) for the newly added instance.
	 * (não tenho certeza se isso se aplica a essa DistanceFunction, mas eu implementei porque
	 * {@link NormalizableDistance} implementa)
	 * @param ins
	 *            the instance to add
	 */
	@Override
	public void update(Instance ins) {
	    validate();
	}

	@Override
	public void clean() {
		instances = new Instances(instances, 0);
	}

}
