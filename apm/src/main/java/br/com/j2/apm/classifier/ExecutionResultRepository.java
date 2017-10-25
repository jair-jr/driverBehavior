package br.com.j2.apm.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import br.com.j2.apm.APMException;
import br.com.j2.apm.constants.ClassEnum;
import weka.core.Utils;

public class ExecutionResultRepository implements AutoCloseable{
	
	private static final int IDENTIFIER_CSV_INDEX = 1;
	private static final int SEED_CSV_INDEX = 2;

	private static final String LINE_FEED_CHAR = "\n";
	private static final char FIELD_SEPARATOR_CHAR = ';';
	private static final String LIST_ELEMENT_SEPARATOR_CHAR = ",";
	
	private static final Charset CHARSET = StandardCharsets.UTF_8;
	
	private static final String[] CSV_HEADER = new String[] {
			"executionIdentifier", "identifier", "seed", "crossValidationFold", "timestamp", "sensor", "earthSensor", "coordinate",
			"classifier", "classifierConfigId", "classifierConfig", "numberOfCrossValidationFolds", 
			"numberOfFrames", "classes",
			
			"numberOfInstances", "correctInstancesRate", "incorrectInstancesRate", "unclassifiedInstancesRate",
			"kappaStatistic", "meanAbsoluteError", "rootMeanSquaredError", "relativeAbsoluteError", "rootRelativeSquaredError",
			"confusionMatrix",
			
			"weightedAreaUnderROC", "weightedAreaUnderPRC", "weightedMatthewsCorrelationCoefficient",
			"weightedFMeasure", "weightedTruePositiveRate", "weightedFalsePositiveRate", "weightedPrecision",
			
			"class", "instanceClassificationTimeInNanosAverage", "instanceClassificationTimeInNanosStandardDeviation",
			
			"classAreaUnderROC", "classAreaUnderPRC", "classMatthewsCorrelationCoefficient",
			"classFMeasure", "classTruePositiveRate", "classFalsePositiveRate", "classPrecision"
	};
	
	private Path file;
	
	private Map<String, Set<Integer>> seedsByIdentifier;
	
	private CSVWriter writer;
	
	private Path predictionsHomeDir;
	
	public Path getFile() {
		return file;
	}
	
	public Path getPredictionsHomeDir() {
		return predictionsHomeDir;
	}
	
	public ExecutionResultRepository(final Path file) throws IOException{
		this.file = Objects.requireNonNull(file, "file cannot be null");
		seedsByIdentifier = Collections.synchronizedMap(new HashMap<>());
		open();
	}

	public void addExecutionResult(final ExecutionResult result) throws IOException{
		Objects.requireNonNull(result, "ExecutionResult cannot be null");
		final String identifier = result.getClassifierExecutor().getIdentifier();
		Objects.requireNonNull(identifier, "ClassifierExecutor identifier cannot be null");
		
		writelnsAndFlush(getExecutionResultAsCSVLines(identifier, result));
		
		savePredictions(result);
		
		final Set<Integer> seeds = result.getDetails().stream()
			.map(details -> details.getSeed())
			.collect(Collectors.toSet());
		
		getSeedsByIdentifierSet(identifier).addAll(seeds);
	}
	
	private Path getOrCreatePredictionsSubDirForClassifierExecutorAndSeed(ClassifierExecutor classifierExecutor, int seed) throws IOException{
		final Path dir;
		if(classifierExecutor.isSensorDataset()){
			dir = Paths.get(classifierExecutor.getSensor().getId(), 
					"f" + classifierExecutor.getNumberOfFrames(),
					classifierExecutor.getClassifier().getAbbreviation() + classifierExecutor.getClassifierConfig().getId(),
					"s" + seed);
		}
		else{
			dir = Paths.get(classifierExecutor.getClassifier().getAbbreviation() + classifierExecutor.getClassifierConfig().getId(),
					"s" + seed);			
		}
		
		final Path subDir = predictionsHomeDir.resolve(dir);
		
		if(Files.exists(subDir)){
			return subDir;
		}
		
		synchronized(this){
			if(Files.exists(subDir)){
				return subDir;
			}
			return Files.createDirectories(subDir);
		}
	}
	
	private void savePredictions(ExecutionResult result){
		result.unionPredictionsBySeed()
			.forEach((seed, predictions) -> {
				try{
					final Path predictionsSubDir = getOrCreatePredictionsSubDirForClassifierExecutorAndSeed(result.getClassifierExecutor(), seed);
					
					final String predictionsFile = result.getClassifierExecutor().getIdentifier(Optional.of(seed), Optional.empty()) + ".predictions.csv";
					
					try(final CSVWriter predictionsCSVWriter = createCSVWriter(predictionsSubDir.resolve(predictionsFile));){
						predictions.writeTo(predictionsCSVWriter);
						predictionsCSVWriter.flush();
					}
					catch (Exception e) {
						throw new APMException("Exception while saving predictions. Seed: " + seed + "; ClassifierExecutor: " + result.getClassifierExecutor().getIdentifier(), e);
					}
				}
				catch(IOException e){
					throw new APMException("Error while creating predictions subdir. Seed: " + seed + "; ClassifierExecutor: " + result.getClassifierExecutor().getIdentifier(), e);
				}
			});
	}
		
	private Set<Integer> getSeedsByIdentifierSet(final String identifier){
		return seedsByIdentifier
				.computeIfAbsent(identifier, key -> Collections.synchronizedSet(new HashSet<>()));
	}
	
	private static String getConfig(ClassifierExecutor classifierExecutor, int seed){
		return classifierExecutor.getClassifier().getClazz().getName() + " " + classifierExecutor.getClassifierConfig().getConfig(seed);
	}
	
	private List<String[]> getExecutionResultAsCSVLines(final String identifier, final ExecutionResult result){
		
		final ClassifierExecutor classifierExecutor = result.getClassifierExecutor();
		
		final String classes = result.getClasses().stream()
				.map(ClassEnum::toString)
				.collect(Collectors.joining(LIST_ELEMENT_SEPARATOR_CHAR));
		
		return result.getDetails().stream()
			.flatMap(details -> {
				final String[] weightedMetricsLine = metricsToCSVLine(details.getWeightedClassifierMetrics()); 
				
				final String[] summaryMetricsLine = summaryMetricsToCSVLine(details.getSummaryClassifierMetrics());
				
				return details.getClasses().stream()
					.map(clazz -> {
						final String[] line = new String[CSV_HEADER.length];
						
						int i = 0;
						
						line[i++] = nullSafeToString(classifierExecutor.getIdentifier(Optional.of(details.getSeed()), Optional.of(details.getCrossValidationFold())));
						line[i++] = nullSafeToString(identifier);
						line[i++] = nullSafeToString(details.getSeed());
						line[i++] = nullSafeToString(details.getCrossValidationFold());
						line[i++] = nullSafeDateToString(details.getTimestamp());
						line[i++] = nullSafeToString(classifierExecutor.getSensor());
						line[i++] = classifierExecutor.getSensor() != null ? String.valueOf(classifierExecutor.getSensor().isEarth()) : "";
						line[i++] = nullSafeToString(classifierExecutor.getCoordinate());
						line[i++] = nullSafeToString(classifierExecutor.getClassifier());
						line[i++] = nullSafeToString(classifierExecutor.getClassifierConfig().getId());
						line[i++] = getConfig(classifierExecutor, details.getSeed());
						line[i++] = nullSafeToString(classifierExecutor.getNumberOfCrossValidationFolds());
						line[i++] = nullSafeToString(classifierExecutor.getNumberOfFrames());
						line[i++] = classes;
						
						System.arraycopy(summaryMetricsLine, 0, line, i, summaryMetricsLine.length);
						i += summaryMetricsLine.length;

						System.arraycopy(weightedMetricsLine, 0, line, i, weightedMetricsLine.length);
						i += weightedMetricsLine.length;
						
						line[i++] = nullSafeToString(clazz);
						line[i++] = nullSafeToString(details.getInstanceClassificationTimeInNanosAverage());
						line[i++] = nullSafeToString(details.getInstanceClassificationTimeInNanosStandardDeviation());
						
						final String[] classifierMetricsLine = metricsToCSVLine(details.getClassifierMetricsByClass(clazz));
						System.arraycopy(classifierMetricsLine, 0, line, i, classifierMetricsLine.length);
						i += classifierMetricsLine.length;
						
						return line;
					});
			})
			.collect(Collectors.toList());
	}
	
	private static String nullSafeToString(Object o){
		if(o == null){
			return "";
		}
		
		if((o instanceof Double) && Utils.isMissingValue((Double) o )){
			return "";
		}
		
		return o.toString();
	}
	
	private static DateFormat createDateFormat(){
		return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}
	
	private static String nullSafeDateToString(Date date){
		return date != null ? createDateFormat().format(date) : "";
	}
	
	private String[] summaryMetricsToCSVLine(SummaryClassifierMetrics summaryClassifierMetrics) {
		return new String[]{
			summaryClassifierMetrics != null ? nullSafeToString(summaryClassifierMetrics.getNumberOfInstances()) : "",
			summaryClassifierMetrics != null ? nullSafeToString(summaryClassifierMetrics.getCorrectInstancesRate()) : "",
			summaryClassifierMetrics != null ? nullSafeToString(summaryClassifierMetrics.getIncorrectInstancesRate()) : "",
			summaryClassifierMetrics != null ? nullSafeToString(summaryClassifierMetrics.getUnclassifiedInstancesRate()) : "",
			summaryClassifierMetrics != null ? nullSafeToString(summaryClassifierMetrics.getKappaStatistic()) : "",
			summaryClassifierMetrics != null ? nullSafeToString(summaryClassifierMetrics.getMeanAbsoluteError()) : "",
			summaryClassifierMetrics != null ? nullSafeToString(summaryClassifierMetrics.getRootMeanSquaredError()) : "",
			summaryClassifierMetrics != null ? nullSafeToString(summaryClassifierMetrics.getRelativeAbsoluteError()) : "",
			summaryClassifierMetrics != null ? nullSafeToString(summaryClassifierMetrics.getRootRelativeSquaredError()) : "",
			summaryClassifierMetrics != null ? Arrays.stream(summaryClassifierMetrics.getConfusionMatrix())
					.flatMapToDouble(array -> Arrays.stream(array))
					.mapToObj(d -> String.valueOf(d))
					.collect(Collectors.joining(LIST_ELEMENT_SEPARATOR_CHAR)): ""
		};
	}

	private static String[] metricsToCSVLine(ClassifierMetrics classifierMetrics){		
		return new String[]{
			classifierMetrics != null ? nullSafeToString(classifierMetrics.getAreaUnderROC()) : "",
			classifierMetrics != null ? nullSafeToString(classifierMetrics.getAreaUnderPRC()) : "",
			classifierMetrics != null ? nullSafeToString(classifierMetrics.getMatthewsCorrelationCoefficient()) : "",
			classifierMetrics != null ? nullSafeToString(classifierMetrics.getFMeasure()) : "",
			classifierMetrics != null ? nullSafeToString(classifierMetrics.getTruePositiveRate()) : "",
			classifierMetrics != null ? nullSafeToString(classifierMetrics.getFalsePositiveRate()) : "",
			classifierMetrics != null ? nullSafeToString(classifierMetrics.getPrecision()) : ""
		};
	}
		
	private static String emptySafeString(String s){
		return !isEmptyOrNull(s) ? s : null;
	}
	
	private static Integer emptySafeIntegerValueOf(String s){
		return !isEmptyOrNull(s) ? Integer.valueOf(s) : null;
	}

	private static boolean isEmptyOrNull(String s) {
		return s == null || s.equals("");
	}

	private void open() throws IOException{
		if(Files.exists(file)){
			readExecutionResults();
			createCSVWriter();
		}
		else{
			createCSVWriter();
			writelnsAndFlush(Collections.singletonList(CSV_HEADER));
		}
		
		predictionsHomeDir = createNewDir("predictions");
	}
	
	private Path createNewDir(String dirPrefix) throws IOException{
		final Path baseDir = file.toAbsolutePath().getParent();
		final DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		final String now = df.format(new Date());
		int i = 0;
		String iStr = "";
		while(true){
			final Path dir = baseDir.resolve(dirPrefix + "_" + now + iStr);
			if(!Files.exists(dir)){
				return Files.createDirectories(dir);
			}
			i++;
			iStr = "_" + i;
		}
	}

	private CSVWriter createCSVWriter(Path filePath) throws FileNotFoundException{
		return new CSVWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath.toFile(), true), CHARSET)), 
				FIELD_SEPARATOR_CHAR, 
				CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.NO_ESCAPE_CHARACTER,
				LINE_FEED_CHAR);
	}
	
	private void createCSVWriter() throws FileNotFoundException{
		writer = createCSVWriter(file);
	}
	
	private void readExecutionResults() throws IOException{
		try(final CSVReader reader = createCSVReader()){
			reader.readAll().stream()
				.forEach(line -> getSeedsByIdentifierSet(emptySafeString(line[IDENTIFIER_CSV_INDEX])).add(emptySafeIntegerValueOf(line[SEED_CSV_INDEX])));
		}		
	}
	
	/*
	 * package level for testing only
	 */
	CSVReader createCSVReader() throws FileNotFoundException {
		return new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(file.toFile()), CHARSET)), 
				FIELD_SEPARATOR_CHAR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, 1);
	}

	private void writelnsAndFlush(List<String[]> lines) throws IOException{
		synchronized(writer){
			lines.stream().forEachOrdered(writer::writeNext);
			writer.flush();
		}
	}

	public void close() throws IOException {
		if(writer != null){
			writer.close();
		}
	}

	public Collection<Integer> findSeedsByIdentifier(String identifier){
		Objects.requireNonNull(identifier, "identifier cannot be null");
		
		final Set<Integer> seeds = seedsByIdentifier.get(identifier);
		if(seeds == null){
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(seeds);
	}
	
	public int getNumberOfIdentifiers(){
		return seedsByIdentifier.size();
	}

}