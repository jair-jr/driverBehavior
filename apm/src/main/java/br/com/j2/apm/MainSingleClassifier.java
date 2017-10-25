package br.com.j2.apm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.opencsv.CSVWriter;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.unsupervised.attribute.RemoveByName;

public class MainSingleClassifier {
	
	static class Parameters{
		int cExp;
		int gExp;
		int kernel;
		
		Instances instances;
		boolean normalized;
	}
	
	private static final Instances readDataset(String arffFile) throws IOException {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(arffFile), StandardCharsets.UTF_8));){
			final Instances instances = new Instances(reader);
			instances.setClassIndex(instances.numAttributes() - 1);
			return instances;
		}		
	}

	public static void main(String[] args) throws Exception{
		final String datasetFileNormalizado = "va_acelerometroTerra_nq8_normalizado.arff";
		//final String datasetFile = "va_acelerometroTerra_nq8.arff";
		
		//final Instances instances = readDataset(datasetFile);
		final Instances normalizedInstances = readDataset(datasetFileNormalizado);
				
		//final int[] cExp = new int[] {-5, -3, -1, 0, 1, 3, 5, 7, 9, 11, 13, 15};
		//final int[] gExp = new int[] {-15, -13, -11, -9, -7, -5, -3, -1, 0, 1, 3, 5};
		final int[] cExp = new int[] {-11, -9, -7, -5, -3, -1, 0, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21};
		final int[] gExp = new int[] {17, 19, 21, 23, 25, 27};
		
		final List<Parameters> parameters = new ArrayList<>();
		for(int i = 0; i < cExp.length; i++){
			for(int j = 0; j < gExp.length; j++){
				for(int k = 0; k < 4; k++){
//					final Parameters p1 = new Parameters();
//					p1.cExp = cExp[i];
//					p1.gExp = gExp[j];
//					p1.kernel = k;
//					p1.instances = instances;
//					p1.normalized = false;
					
					
					final Parameters p2 = new Parameters();
					p2.cExp = cExp[i];
					p2.gExp = gExp[j];
					p2.kernel = k;
					p2.instances = normalizedInstances;
					p2.normalized = true;

//					parameters.add(p1);
					parameters.add(p2);
				}
			}
		}
		
		final List<String[]> csvLines = evaluateModels(parameters);
		
		try(final Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("svm.csv"), StandardCharsets.UTF_8));
			final CSVWriter csvWriter = new CSVWriter(w, ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER)){			
			csvWriter.writeAll(csvLines);
		}
				
	}

	private static List<String[]> evaluateModels(final List<Parameters> parameters) {
		final int total = parameters.size();
		final AtomicInteger sum = new AtomicInteger(0);
		
		final RemoveByName rm = new RemoveByName();
		rm.setExpression("quadro0");
		
		final List<String[]> csvLines = parameters.parallelStream()
			.map(p -> {
				try {
					final double c = Math.pow(2, p.cExp);
					final double gamma = Math.pow(2, p.gExp);
					
					final LibSVM libSVM = new LibSVM();
						libSVM.setOptions(Utils.splitOptions("-S 0 -K " + p.kernel + " -C " + c + " -G " + gamma + " -W \"1 1 1 1 1 1 1\" -seed 10"));
					
					final FilteredClassifier fc = new FilteredClassifier();
					fc.setFilter(rm);
					fc.setClassifier(libSVM);
					
					final Evaluation evaluation = new Evaluation(p.instances);
					evaluation.crossValidateModel(fc, p.instances, 10, new Random(10));
					
					System.out.println(sum.incrementAndGet() / (double) total);
					
					return new String[]{String.valueOf(p.cExp),
							String.valueOf(p.gExp),
							String.valueOf(p.kernel),
							String.valueOf(p.normalized),
							String.valueOf(evaluation.pctCorrect())};
				} 
				catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException(e);
				}
			})
			.collect(Collectors.toList());
		return csvLines;
	}
}
