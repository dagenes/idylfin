/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Adapted from the forth-order Runge-Kutta method for solving ODE. See <a
 * href="http://en.wikipedia.org/wiki/Runge-Kutta_methods">here </a> for the
 * maths. It is a very robust integrator and should be used before trying more
 * specialised methods.
 */
public class RungeKuttaIntegrator1D extends Integrator1D<Double, Double> {
  private static final double DEF_TOL = 1e-10;
  private static final double STEP_SIZE_LIMIT = 1e-50;
  private static final int DEF_MIN_STEPS = 10;
  private final double _absTol, _relTol;
  private final int _minSteps;

  public RungeKuttaIntegrator1D(final double absTol, final double relTol, final int minSteps) {
    if (absTol < 0.0) {
      throw new IllegalArgumentException("Absolute Tolerance must be greater than zero");
    }
    if (relTol < 0.0) {
      throw new IllegalArgumentException("Relative Tolerance must be grater than zero");
    }
    if (minSteps < 1) {
      throw new IllegalArgumentException("Must have minimum of 1 step");
    }
    _absTol = absTol;
    _relTol = relTol;
    _minSteps = minSteps;
  }

  public RungeKuttaIntegrator1D(final double tol, final int minSteps) {
    this(tol, tol, minSteps);
  }

  public RungeKuttaIntegrator1D(final double atol, final double rtol) {
    this(atol, rtol, DEF_MIN_STEPS);
  }

  public RungeKuttaIntegrator1D(final double tol) {
    this(tol, tol, DEF_MIN_STEPS);
  }

  public RungeKuttaIntegrator1D(final int minSteps) {
    this(DEF_TOL, minSteps);
  }

  public RungeKuttaIntegrator1D() {
    this(DEF_TOL, DEF_MIN_STEPS);

  }

  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {

    final double h = (upper - lower) / _minSteps;
    double f1, f2, f3, x;
    x = lower;
    f1 = f.evaluate(x);

    double result = 0.0;
    for (int i = 0; i < _minSteps; i++) {
      f2 = f.evaluate(x + h / 2.0);
      f3 = f.evaluate(x + h);

      result += calculateRungeKuttaFourthOrder(f, x, h, f1, f2, f3);
      f1 = f3;
      x += h;
    }
    return result;
  }

  private double calculateRungeKuttaFourthOrder(final Function1D<Double, Double> f, final double x, final double h, final double fl, final double fm, final double fu) {

    final double f1 = f.evaluate(x + 0.25 * h);
    final double f2 = f.evaluate(x + 0.75 * h);
    final double ya = h * (fl + 4.0 * fm + fu) / 6.0;
    final double yb = h * (fl + 2.0 * fm + 4.0 * (f1 + f2) + fu) / 12.0;

    final double diff = Math.abs(ya - yb);
    final double abs = Math.max(Math.abs(ya), Math.abs(yb));

    if (diff < _absTol + _relTol * abs) {
      return yb + (yb - ya) / 15.0;
    }

    // can't keep halving the step size
    if (h < STEP_SIZE_LIMIT) {
      return yb + (yb - ya) / 15.0;
    }

    return calculateRungeKuttaFourthOrder(f, x, h / 2.0, fl, f1, fm) + calculateRungeKuttaFourthOrder(f, x + h / 2.0, h / 2.0, fm, f2, fu);
  }

}
