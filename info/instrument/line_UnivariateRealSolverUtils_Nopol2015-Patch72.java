package org.apache.commons.math.analysis.solvers;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
/** 
 * Utility routines for  {@link UnivariateRealSolver} objects.
 * @version $Revision$ $Date$
 */
public class UnivariateRealSolverUtils {
  /** 
 * Default constructor.
 */
  private UnivariateRealSolverUtils(){
    super();
  }
  /** 
 * Convenience method to find a zero of a univariate real function.  A default solver is used. 
 * @param f the function.
 * @param x0 the lower bound for the interval.
 * @param x1 the upper bound for the interval.
 * @return a value where the function is zero.
 * @throws ConvergenceException if the iteration count was exceeded
 * @throws FunctionEvaluationException if an error occurs evaluatingthe function
 * @throws IllegalArgumentException if f is null or the endpoints do notspecify a valid interval
 */
  public static double solve(  UnivariateRealFunction f,  double x0,  double x1) throws ConvergenceException, FunctionEvaluationException {
    setup(f);
    return LazyHolder.FACTORY.newDefaultSolver().solve(f,x0,x1);
  }
  /** 
 * Convenience method to find a zero of a univariate real function.  A default solver is used. 
 * @param f the function
 * @param x0 the lower bound for the interval
 * @param x1 the upper bound for the interval
 * @param absoluteAccuracy the accuracy to be used by the solver
 * @return a value where the function is zero
 * @throws ConvergenceException if the iteration count is exceeded
 * @throws FunctionEvaluationException if an error occurs evaluating thefunction
 * @throws IllegalArgumentException if f is null, the endpoints do not specify a valid interval, or the absoluteAccuracy is not valid for the default solver
 */
  public static double solve(  UnivariateRealFunction f,  double x0,  double x1,  double absoluteAccuracy) throws ConvergenceException, FunctionEvaluationException {
    setup(f);
    UnivariateRealSolver solver=LazyHolder.FACTORY.newDefaultSolver();
    solver.setAbsoluteAccuracy(absoluteAccuracy);
    return solver.solve(f,x0,x1);
  }
  /** 
 * This method attempts to find two values a and b satisfying <ul> <li> <code> lowerBound <= a < initial < b <= upperBound</code> </li> <li> <code> f(a) * f(b) < 0 </code></li> </ul> If f is continuous on <code>[a,b],</code> this means that <code>a</code> and <code>b</code> bracket a root of f. <p> The algorithm starts by setting  <code>a := initial -1; b := initial +1,</code> examines the value of the function at <code>a</code> and <code>b</code> and keeps moving the endpoints out by one unit each time through a loop that terminates  when one of the following happens: <ul> <li> <code> f(a) * f(b) < 0 </code> --  success!</li> <li> <code> a = lower </code> and <code> b = upper</code>  -- ConvergenceException </li> <li> <code> Integer.MAX_VALUE</code> iterations elapse  -- ConvergenceException </li> </ul></p> <p> <strong>Note: </strong> this method can take  <code>Integer.MAX_VALUE</code> iterations to throw a  <code>ConvergenceException.</code>  Unless you are confident that there is a root between <code>lowerBound</code> and <code>upperBound</code> near <code>initial,</code> it is better to use  {@link #bracket(UnivariateRealFunction,double,double,double,int)},  explicitly specifying the maximum number of iterations.</p>
 * @param function the function
 * @param initial initial midpoint of interval being expanded tobracket a root
 * @param lowerBound lower bound (a is never lower than this value)
 * @param upperBound upper bound (b never is greater than thisvalue)
 * @return a two element array holding {a, b}
 * @throws ConvergenceException if a root can not be bracketted
 * @throws FunctionEvaluationException if an error occurs evaluating thefunction
 * @throws IllegalArgumentException if function is null, maximumIterationsis not positive, or initial is not between lowerBound and upperBound
 */
  public static double[] bracket(  UnivariateRealFunction function,  double initial,  double lowerBound,  double upperBound) throws ConvergenceException, FunctionEvaluationException {
    return bracket(function,initial,lowerBound,upperBound,Integer.MAX_VALUE);
  }
  /** 
 * This method attempts to find two values a and b satisfying <ul> <li> <code> lowerBound <= a < initial < b <= upperBound</code> </li> <li> <code> f(a) * f(b) <= 0 </code> </li> </ul> If f is continuous on <code>[a,b],</code> this means that <code>a</code> and <code>b</code> bracket a root of f. <p> The algorithm starts by setting  <code>a := initial -1; b := initial +1,</code> examines the value of the function at <code>a</code> and <code>b</code> and keeps moving the endpoints out by one unit each time through a loop that terminates  when one of the following happens: <ul> <li> <code> f(a) * f(b) <= 0 </code> --  success!</li> <li> <code> a = lower </code> and <code> b = upper</code>  -- ConvergenceException </li> <li> <code> maximumIterations</code> iterations elapse  -- ConvergenceException </li></ul></p>
 * @param function the function
 * @param initial initial midpoint of interval being expanded tobracket a root
 * @param lowerBound lower bound (a is never lower than this value)
 * @param upperBound upper bound (b never is greater than thisvalue)
 * @param maximumIterations maximum number of iterations to perform
 * @return a two element array holding {a, b}.
 * @throws ConvergenceException if the algorithm fails to find a and bsatisfying the desired conditions
 * @throws FunctionEvaluationException if an error occurs evaluating the function
 * @throws IllegalArgumentException if function is null, maximumIterationsis not positive, or initial is not between lowerBound and upperBound
 */
  public static double[] bracket(  UnivariateRealFunction function,  double initial,  double lowerBound,  double upperBound,  int maximumIterations) throws ConvergenceException, FunctionEvaluationException {
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int START#0");
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#170");
    if (function == null) {
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#171");
      throw MathRuntimeException.createIllegalArgumentException("function is null");
    }
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#173");
    if (maximumIterations <= 0) {
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#174");
      throw MathRuntimeException.createIllegalArgumentException("bad value for maximum iterations number: {0}",maximumIterations);
    }
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#177");
    if (initial < lowerBound || initial > upperBound || lowerBound >= upperBound) {
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#178");
      throw MathRuntimeException.createIllegalArgumentException("invalid bracketing parameters:  lower bound={0},  initial={1}, upper bound={2}",lowerBound,initial,upperBound);
    }
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#182");
    double a=initial;
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#183");
    double b=initial;
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#184");
    double fa;
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#185");
    double fb;
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#186");
    int numIterations=0;
    do {
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#189");
      a=Math.max(a - 1.0,lowerBound);
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#190");
      b=Math.min(b + 1.0,upperBound);
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#191");
      fa=function.value(a);
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#193");
      fb=function.value(b);
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#194");
      numIterations++;
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#195");
    }
 while ((fa * fb > 0.0) && (numIterations < maximumIterations) && ((a > lowerBound) || (b < upperBound)));
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#198");
    if (((fa * fb)) != (lowerBound)) {
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#199");
      if (fa * fb >= 0.0) {
        auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#200");
        throw new ConvergenceException("number of iterations={0}, maximum iterations={1}, " + "initial={2}, lower bound={3}, upper bound={4}, final a value={5}, " + "final b value={6}, f(a)={7}, f(b)={8}",numIterations,maximumIterations,initial,lowerBound,upperBound,a,b,fa,fb);
      }
    }
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/PatchCheck/tmp/Math/85//line/Nopol2015-Patch72_line","org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils#double[]#bracket#?,UnivariateRealFunction,double,double,double,int#208");
    return new double[]{a,b};
  }
  /** 
 * Compute the midpoint of two values.
 * @param a first value.
 * @param b second value.
 * @return the midpoint. 
 */
  public static double midpoint(  double a,  double b){
    return (a + b) * .5;
  }
  /** 
 * Checks to see if f is null, throwing IllegalArgumentException if so.
 * @param f  input function
 * @throws IllegalArgumentException if f is null
 */
  private static void setup(  UnivariateRealFunction f){
    if (f == null) {
      throw MathRuntimeException.createIllegalArgumentException("function is null");
    }
  }
  /** 
 * Holder for the factory. <p>We use here the Initialization On Demand Holder Idiom.</p>
 */
private static class LazyHolder {
    /** 
 * Cached solver factory 
 */
    private static final UnivariateRealSolverFactory FACTORY=UnivariateRealSolverFactory.newInstance();
  }
}
