package org.Evaluation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class EvaluationResultTest {
	
	private EvaluationResult evaluationResult;

	@Before
	public void preparation() {
		List<String> true_positives = new ArrayList<String>();
		true_positives.add("A_correct");
		true_positives.add("B_correct");
		true_positives.add("C_correct");
		
		List<String> total_retrieved = new ArrayList<String>();
		total_retrieved.add("A_correct");
		total_retrieved.add("B_correct");
		total_retrieved.add("C_correct");
		total_retrieved.add("O");
		total_retrieved.add("P");
		total_retrieved.add("Q");
		total_retrieved.add("R");
		total_retrieved.add("S");
		total_retrieved.add("T");
		total_retrieved.add("U");
		total_retrieved.add("V");
		total_retrieved.add("W");
		total_retrieved.add("X");
		total_retrieved.add("Y");
		total_retrieved.add("Z");
		
		List<String> total_correct = new ArrayList<String>();
		total_correct.add("A_correct");
		total_correct.add("B_correct");
		total_correct.add("C_correct");
		total_correct.add("D_correct");
		total_correct.add("E_correct");
		
		evaluationResult = new EvaluationResult(true_positives, total_retrieved, total_correct);
	}
	
	@Test
	public void testPrecision() {
		
		double precision = evaluationResult.getPrecision();
		
		assertEquals(precision, 3.0 / 15.0 , 0.00001);
	}
	
	@Test
	public void testRecall() {
		
		double recall = evaluationResult.getRecall();
		
		assertEquals(recall, 3.0 / 5.0 , 0.00001);
	}
	
	@Test
	public void testFMeasure() {
		
		double fMeasure = evaluationResult.getFMeasure();
		double expected = 2.0 * (3.0 / 15.0) * (3.0 / 5.0) / (3.0 / 15.0 + 3.0 / 5.0);
		
		assertEquals(fMeasure, expected , 0.00001);
	}

}
