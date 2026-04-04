package com.in28minutes.webservices.songrec.evaluation.runner;

import com.in28minutes.webservices.songrec.evaluation.evaluator.RecommendationEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
public class EvaluationRunner implements CommandLineRunner {
  private final RecommendationEvaluator evaluator;

  @Override
  public void run(String... args) throws Exception {
    evaluator.runEvaluation();
  }
}
