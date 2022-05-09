SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Math-41-41
Subject [_name=Math, _id=41]
writeFile: /home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state
fixedFile: /home/xushicheng/dataset/defects4j/projects/Math/Math_41_buggy/src/main/java/org/apache/commons/math/stat/descriptive/moment/Variance.java
compilationUnitContent: 
package org.apache.commons.math.stat.descriptive.moment;
import java.io.Serializable;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.stat.descriptive.WeightedEvaluation;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math.util.MathUtils;
/** 
 * Computes the variance of the available values.  By default, the unbiased "sample variance" definitional formula is used: <p> variance = sum((x_i - mean)^2) / (n - 1) </p> <p> where mean is the  {@link Mean} and <code>n</code> is the numberof sample observations.</p> <p> The definitional formula does not have good numerical properties, so this implementation does not compute the statistic using the definitional formula. <ul> <li> The <code>getResult</code> method computes the variance using updating formulas based on West's algorithm, as described in <a href="http://doi.acm.org/10.1145/359146.359152"> Chan, T. F. and J. G. Lewis 1979, <i>Communications of the ACM</i>, vol. 22 no. 9, pp. 526-531.</a></li> <li> The <code>evaluate</code> methods leverage the fact that they have the full array of values in memory to execute a two-pass algorithm. Specifically, these methods use the "corrected two-pass algorithm" from Chan, Golub, Levesque, <i>Algorithms for Computing the Sample Variance</i>, American Statistician, vol. 37, no. 3 (1983) pp. 242-247.</li></ul> Note that adding values using <code>increment</code> or <code>incrementAll</code> and then executing <code>getResult</code> will sometimes give a different, less accurate, result than executing <code>evaluate</code> with the full array of values. The former approach should only be used when the full array of values is not available.</p> <p> The "population variance"  ( sum((x_i - mean)^2) / n ) can also be computed using this statistic.  The <code>isBiasCorrected</code> property determines whether the "population" or "sample" value is returned by the <code>evaluate</code> and <code>getResult</code> methods. To compute population variances, set this property to <code>false.</code> </p> <p> <strong>Note that this implementation is not synchronized.</strong> If multiple threads access an instance of this class concurrently, and at least one of the threads invokes the <code>increment()</code> or <code>clear()</code> method, it must be synchronized externally.</p>
 * @version $Id$
 */
public class Variance extends AbstractStorelessUnivariateStatistic implements Serializable, WeightedEvaluation {
  /** 
 * Serializable version identifier 
 */
  private static final long serialVersionUID=-9111962718267217978L;
  /** 
 * SecondMoment is used in incremental calculation of Variance
 */
  protected SecondMoment moment=null;
  /** 
 * Whether or not  {@link #increment(double)} should incrementthe internal second moment. When a Variance is constructed with an external SecondMoment as a constructor parameter, this property is set to false and increments must be applied to the second moment directly.
 */
  protected boolean incMoment=true;
  /** 
 * Whether or not bias correction is applied when computing the value of the statistic. True means that bias is corrected.  See {@link Variance} for details on the formula.
 */
  private boolean isBiasCorrected=true;
  /** 
 * Constructs a Variance with default (true) <code>isBiasCorrected</code> property.
 */
  public Variance(){
    moment=new SecondMoment();
  }
  /** 
 * Constructs a Variance based on an external second moment. When this constructor is used, the statistic may only be incremented via the moment, i.e.,  {@link #increment(double)}does nothing; whereas  {@code m2.increment(value)} incrementsboth  {@code m2} and the Variance instance constructed from it.
 * @param m2 the SecondMoment (Third or Fourth moments workhere as well.)
 */
  public Variance(  final SecondMoment m2){
    incMoment=false;
    this.moment=m2;
  }
  /** 
 * Constructs a Variance with the specified <code>isBiasCorrected</code> property
 * @param isBiasCorrected  setting for bias correction - true meansbias will be corrected and is equivalent to using the argumentless constructor
 */
  public Variance(  boolean isBiasCorrected){
    moment=new SecondMoment();
    this.isBiasCorrected=isBiasCorrected;
  }
  /** 
 * Constructs a Variance with the specified <code>isBiasCorrected</code> property and the supplied external second moment.
 * @param isBiasCorrected  setting for bias correction - true meansbias will be corrected
 * @param m2 the SecondMoment (Third or Fourth moments workhere as well.)
 */
  public Variance(  boolean isBiasCorrected,  SecondMoment m2){
    incMoment=false;
    this.moment=m2;
    this.isBiasCorrected=isBiasCorrected;
  }
  /** 
 * Copy constructor, creates a new  {@code Variance} identicalto the  {@code original}
 * @param original the {@code Variance} instance to copy
 */
  public Variance(  Variance original){
    copy(original,this);
  }
  /** 
 * {@inheritDoc}<p>If all values are available, it is more accurate to use {@link #evaluate(double[])} rather than adding values one at a timeusing this method and then executing  {@link #getResult}, since <code>evaluate</code> leverages the fact that is has the full list of values together to execute a two-pass algorithm. See  {@link Variance}.</p> <p>Note also that when  {@link #Variance(SecondMoment)} is used tocreate a Variance, this method does nothing. In that case, the SecondMoment should be incremented directly.</p>
 */
  @Override public void increment(  final double d){
    if (incMoment) {
      moment.increment(d);
    }
  }
  /** 
 * {@inheritDoc}
 */
  @Override public double getResult(){
    if (moment.n == 0) {
      return Double.NaN;
    }
 else     if (moment.n == 1) {
      return 0d;
    }
 else {
      if (isBiasCorrected) {
        return moment.m2 / (moment.n - 1d);
      }
 else {
        return moment.m2 / (moment.n);
      }
    }
  }
  /** 
 * {@inheritDoc}
 */
  public long getN(){
    return moment.getN();
  }
  /** 
 * {@inheritDoc}
 */
  @Override public void clear(){
    if (incMoment) {
      moment.clear();
    }
  }
  /** 
 * Returns the variance of the entries in the input array, or <code>Double.NaN</code> if the array is empty. <p> See  {@link Variance} for details on the computing algorithm.</p><p> Returns 0 for a single-value (i.e. length = 1) sample.</p> <p> Throws <code>IllegalArgumentException</code> if the array is null.</p> <p> Does not change the internal state of the statistic.</p>
 * @param values the input array
 * @return the variance of the values or Double.NaN if length = 0
 * @throws IllegalArgumentException if the array is null
 */
  @Override public double evaluate(  final double[] values){
    if (values == null) {
      throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
    }
    return evaluate(values,0,values.length);
  }
  /** 
 * Returns the variance of the entries in the specified portion of the input array, or <code>Double.NaN</code> if the designated subarray is empty. <p> See  {@link Variance} for details on the computing algorithm.</p><p> Returns 0 for a single-value (i.e. length = 1) sample.</p> <p> Does not change the internal state of the statistic.</p> <p> Throws <code>IllegalArgumentException</code> if the array is null.</p>
 * @param values the input array
 * @param begin index of the first array element to include
 * @param length the number of elements to include
 * @return the variance of the values or Double.NaN if length = 0
 * @throws IllegalArgumentException if the array is null or the array indexparameters are not valid
 */
  @Override public double evaluate(  final double[] values,  final int begin,  final int length){
    double var=Double.NaN;
    if (test(values,begin,length)) {
      clear();
      if (length == 1) {
        var=0.0;
      }
 else       if (length > 1) {
        Mean mean=new Mean();
        double m=mean.evaluate(values,begin,length);
        var=evaluate(values,m,begin,length);
      }
    }
    return var;
  }
  /** 
 * <p>Returns the weighted variance of the entries in the specified portion of the input array, or <code>Double.NaN</code> if the designated subarray is empty.</p> <p> Uses the formula <pre> &Sigma;(weights[i]*(values[i] - weightedMean)<sup>2</sup>)/(&Sigma;(weights[i]) - 1) </pre> where weightedMean is the weighted mean</p> <p> This formula will not return the same result as the unweighted variance when all weights are equal, unless all weights are equal to 1. The formula assumes that weights are to be treated as "expansion values," as will be the case if for example the weights represent frequency counts. To normalize weights so that the denominator in the variance computation equals the length of the input vector minus one, use <pre> <code>evaluate(values, MathArrays.normalizeArray(weights, values.length)); </code> </pre> <p> Returns 0 for a single-value (i.e. length = 1) sample.</p> <p> Throws <code>IllegalArgumentException</code> if any of the following are true: <ul><li>the values array is null</li> <li>the weights array is null</li> <li>the weights array does not have the same length as the values array</li> <li>the weights array contains one or more infinite values</li> <li>the weights array contains one or more NaN values</li> <li>the weights array contains negative values</li> <li>the start and length arguments do not determine a valid array</li> </ul></p> <p> Does not change the internal state of the statistic.</p> <p> Throws <code>IllegalArgumentException</code> if either array is null.</p>
 * @param values the input array
 * @param weights the weights array
 * @param begin index of the first array element to include
 * @param length the number of elements to include
 * @return the weighted variance of the values or Double.NaN if length = 0
 * @throws IllegalArgumentException if the parameters are not valid
 * @since 2.1
 */
  public double evaluate(  final double[] values,  final double[] weights,  final int begin,  final int length){
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","org.apache.commons.math.stat.descriptive.moment.Variance#double#evaluate#?,double[],double[],int,int START#0");
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","this: ",this);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","values: ",values);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","weights: ",weights);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","begin: ",begin);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","length: ",length);
    double var=Double.NaN;
    if (test(values,weights,begin,length)) {
      clear();
      if (length == 1) {
        var=0.0;
      }
 else {
        if (length > 1) {
          Mean mean=new Mean();
          double m=mean.evaluate(values,weights,begin,length);
          var=evaluate(values,weights,m,begin,length);
        }
      }
    }
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","this: ",this);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","values: ",values);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","weights: ",weights);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","begin: ",begin);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","length: ",length);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","org.apache.commons.math.stat.descriptive.moment.Variance#double#evaluate#?,double[],double[],int,int END#0");
    return (Double)auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","var: ",var);
  }
  /** 
 * <p> Returns the weighted variance of the entries in the the input array.</p> <p> Uses the formula <pre> &Sigma;(weights[i]*(values[i] - weightedMean)<sup>2</sup>)/(&Sigma;(weights[i]) - 1) </pre> where weightedMean is the weighted mean</p> <p> This formula will not return the same result as the unweighted variance when all weights are equal, unless all weights are equal to 1. The formula assumes that weights are to be treated as "expansion values," as will be the case if for example the weights represent frequency counts. To normalize weights so that the denominator in the variance computation equals the length of the input vector minus one, use <pre> <code>evaluate(values, MathArrays.normalizeArray(weights, values.length)); </code> </pre> <p> Returns 0 for a single-value (i.e. length = 1) sample.</p> <p> Throws <code>IllegalArgumentException</code> if any of the following are true: <ul><li>the values array is null</li> <li>the weights array is null</li> <li>the weights array does not have the same length as the values array</li> <li>the weights array contains one or more infinite values</li> <li>the weights array contains one or more NaN values</li> <li>the weights array contains negative values</li> </ul></p> <p> Does not change the internal state of the statistic.</p> <p> Throws <code>IllegalArgumentException</code> if either array is null.</p>
 * @param values the input array
 * @param weights the weights array
 * @return the weighted variance of the values
 * @throws IllegalArgumentException if the parameters are not valid
 * @since 2.1
 */
  public double evaluate(  final double[] values,  final double[] weights){
    return evaluate(values,weights,0,values.length);
  }
  /** 
 * Returns the variance of the entries in the specified portion of the input array, using the precomputed mean value.  Returns <code>Double.NaN</code> if the designated subarray is empty. <p> See  {@link Variance} for details on the computing algorithm.</p><p> The formula used assumes that the supplied mean value is the arithmetic mean of the sample data, not a known population parameter.  This method is supplied only to save computation when the mean has already been computed.</p> <p> Returns 0 for a single-value (i.e. length = 1) sample.</p> <p> Throws <code>IllegalArgumentException</code> if the array is null.</p> <p> Does not change the internal state of the statistic.</p>
 * @param values the input array
 * @param mean the precomputed mean value
 * @param begin index of the first array element to include
 * @param length the number of elements to include
 * @return the variance of the values or Double.NaN if length = 0
 * @throws IllegalArgumentException if the array is null or the array indexparameters are not valid
 */
  public double evaluate(  final double[] values,  final double mean,  final int begin,  final int length){
    double var=Double.NaN;
    if (test(values,begin,length)) {
      if (length == 1) {
        var=0.0;
      }
 else       if (length > 1) {
        double accum=0.0;
        double dev=0.0;
        double accum2=0.0;
        for (int i=begin; i < begin + length; i++) {
          dev=values[i] - mean;
          accum+=dev * dev;
          accum2+=dev;
        }
        double len=length;
        if (isBiasCorrected) {
          var=(accum - (accum2 * accum2 / len)) / (len - 1.0);
        }
 else {
          var=(accum - (accum2 * accum2 / len)) / len;
        }
      }
    }
    return var;
  }
  /** 
 * Returns the variance of the entries in the input array, using the precomputed mean value.  Returns <code>Double.NaN</code> if the array is empty. <p> See  {@link Variance} for details on the computing algorithm.</p><p> If <code>isBiasCorrected</code> is <code>true</code> the formula used assumes that the supplied mean value is the arithmetic mean of the sample data, not a known population parameter.  If the mean is a known population parameter, or if the "population" version of the variance is desired, set <code>isBiasCorrected</code> to <code>false</code> before invoking this method.</p> <p> Returns 0 for a single-value (i.e. length = 1) sample.</p> <p> Throws <code>IllegalArgumentException</code> if the array is null.</p> <p> Does not change the internal state of the statistic.</p>
 * @param values the input array
 * @param mean the precomputed mean value
 * @return the variance of the values or Double.NaN if the array is empty
 * @throws IllegalArgumentException if the array is null
 */
  public double evaluate(  final double[] values,  final double mean){
    return evaluate(values,mean,0,values.length);
  }
  /** 
 * Returns the weighted variance of the entries in the specified portion of the input array, using the precomputed weighted mean value.  Returns <code>Double.NaN</code> if the designated subarray is empty. <p> Uses the formula <pre> &Sigma;(weights[i]*(values[i] - mean)<sup>2</sup>)/(&Sigma;(weights[i]) - 1) </pre></p> <p> The formula used assumes that the supplied mean value is the weighted arithmetic mean of the sample data, not a known population parameter. This method is supplied only to save computation when the mean has already been computed.</p> <p> This formula will not return the same result as the unweighted variance when all weights are equal, unless all weights are equal to 1. The formula assumes that weights are to be treated as "expansion values," as will be the case if for example the weights represent frequency counts. To normalize weights so that the denominator in the variance computation equals the length of the input vector minus one, use <pre> <code>evaluate(values, MathArrays.normalizeArray(weights, values.length), mean); </code> </pre> <p> Returns 0 for a single-value (i.e. length = 1) sample.</p> <p> Throws <code>IllegalArgumentException</code> if any of the following are true: <ul><li>the values array is null</li> <li>the weights array is null</li> <li>the weights array does not have the same length as the values array</li> <li>the weights array contains one or more infinite values</li> <li>the weights array contains one or more NaN values</li> <li>the weights array contains negative values</li> <li>the start and length arguments do not determine a valid array</li> </ul></p> <p> Does not change the internal state of the statistic.</p>
 * @param values the input array
 * @param weights the weights array
 * @param mean the precomputed weighted mean value
 * @param begin index of the first array element to include
 * @param length the number of elements to include
 * @return the variance of the values or Double.NaN if length = 0
 * @throws IllegalArgumentException if the parameters are not valid
 * @since 2.1
 */
  public double evaluate(  final double[] values,  final double[] weights,  final double mean,  final int begin,  final int length){
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","org.apache.commons.math.stat.descriptive.moment.Variance#double#evaluate#?,double[],double[],double,int,int START#0");
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","this: ",this);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","values: ",values);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","weights: ",weights);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","mean: ",mean);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","begin: ",begin);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","length: ",length);
    double var=Double.NaN;
    if (test(values,weights,begin,length)) {
      if (length == 1) {
        var=0.0;
      }
 else {
        if (length > 1) {
          double accum=0.0;
          double dev=0.0;
          double accum2=0.0;
          for (int i=begin; i < begin + length; i++) {
            dev=values[i] - mean;
            accum+=weights[i] * (dev * dev);
            accum2+=weights[i] * dev;
          }
          double sumWts=0;
          for (int i=0; i < weights.length; i++) {
            sumWts+=weights[i];
          }
          if (isBiasCorrected) {
            var=(accum - (accum2 * accum2 / sumWts)) / (sumWts - 1.0);
          }
 else {
            var=(accum - (accum2 * accum2 / sumWts)) / sumWts;
          }
        }
      }
    }
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","this: ",this);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","values: ",values);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","weights: ",weights);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","mean: ",mean);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","begin: ",begin);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","length: ",length);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","org.apache.commons.math.stat.descriptive.moment.Variance#double#evaluate#?,double[],double[],double,int,int END#0");
    return (Double)auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","var: ",var);
  }
  /** 
 * <p>Returns the weighted variance of the values in the input array, using the precomputed weighted mean value.</p> <p> Uses the formula <pre> &Sigma;(weights[i]*(values[i] - mean)<sup>2</sup>)/(&Sigma;(weights[i]) - 1) </pre></p> <p> The formula used assumes that the supplied mean value is the weighted arithmetic mean of the sample data, not a known population parameter. This method is supplied only to save computation when the mean has already been computed.</p> <p> This formula will not return the same result as the unweighted variance when all weights are equal, unless all weights are equal to 1. The formula assumes that weights are to be treated as "expansion values," as will be the case if for example the weights represent frequency counts. To normalize weights so that the denominator in the variance computation equals the length of the input vector minus one, use <pre> <code>evaluate(values, MathArrays.normalizeArray(weights, values.length), mean); </code> </pre> <p> Returns 0 for a single-value (i.e. length = 1) sample.</p> <p> Throws <code>IllegalArgumentException</code> if any of the following are true: <ul><li>the values array is null</li> <li>the weights array is null</li> <li>the weights array does not have the same length as the values array</li> <li>the weights array contains one or more infinite values</li> <li>the weights array contains one or more NaN values</li> <li>the weights array contains negative values</li> </ul></p> <p> Does not change the internal state of the statistic.</p>
 * @param values the input array
 * @param weights the weights array
 * @param mean the precomputed weighted mean value
 * @return the variance of the values or Double.NaN if length = 0
 * @throws IllegalArgumentException if the parameters are not valid
 * @since 2.1
 */
  public double evaluate(  final double[] values,  final double[] weights,  final double mean){
    return evaluate(values,weights,mean,0,values.length);
  }
  /** 
 * @return Returns the isBiasCorrected.
 */
  public boolean isBiasCorrected(){
    return isBiasCorrected;
  }
  /** 
 * @param biasCorrected The isBiasCorrected to set.
 */
  public void setBiasCorrected(  boolean biasCorrected){
    this.isBiasCorrected=biasCorrected;
  }
  /** 
 * {@inheritDoc}
 */
  @Override public Variance copy(){
    Variance result=new Variance();
    copy(this,result);
    return result;
  }
  /** 
 * Copies source to dest. <p>Neither source nor dest can be null.</p>
 * @param source Variance to copy
 * @param dest Variance to copy to
 * @throws NullArgumentException if either source or dest is null
 */
  public static void copy(  Variance source,  Variance dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    dest.moment=source.moment.copy();
    dest.isBiasCorrected=source.isBiasCorrected;
    dest.incMoment=source.incMoment;
  }
}

fixedFile: /home/xushicheng/dataset/defects4j/projects/Math/Math_41_buggy/src/main/java/org/apache/commons/math/stat/descriptive/moment/Mean.java
compilationUnitContent: 
package org.apache.commons.math.stat.descriptive.moment;
import java.io.Serializable;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math.stat.descriptive.WeightedEvaluation;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.commons.math.util.MathUtils;
/** 
 * <p>Computes the arithmetic mean of a set of values. Uses the definitional formula:</p> <p> mean = sum(x_i) / n </p> <p>where <code>n</code> is the number of observations. </p> <p>When  {@link #increment(double)} is used to add data incrementally from astream of (unstored) values, the value of the statistic that {@link #getResult()} returns is computed using the following recursiveupdating algorithm: </p> <ol> <li>Initialize <code>m = </code> the first value</li> <li>For each additional value, update using <br> <code>m = m + (new value - m) / (number of observations)</code></li> </ol> <p> If  {@link #evaluate(double[])} is used to compute the mean of an arrayof stored values, a two-pass, corrected algorithm is used, starting with the definitional formula computed using the array of stored values and then correcting this by adding the mean deviation of the data values from the arithmetic mean. See, e.g. "Comparison of Several Algorithms for Computing Sample Means and Variances," Robert F. Ling, Journal of the American Statistical Association, Vol. 69, No. 348 (Dec., 1974), pp. 859-866. </p> <p> Returns <code>Double.NaN</code> if the dataset is empty. </p> <strong>Note that this implementation is not synchronized.</strong> If multiple threads access an instance of this class concurrently, and at least one of the threads invokes the <code>increment()</code> or <code>clear()</code> method, it must be synchronized externally.
 * @version $Id$
 */
public class Mean extends AbstractStorelessUnivariateStatistic implements Serializable, WeightedEvaluation {
  /** 
 * Serializable version identifier 
 */
  private static final long serialVersionUID=-1296043746617791564L;
  /** 
 * First moment on which this statistic is based. 
 */
  protected FirstMoment moment;
  /** 
 * Determines whether or not this statistic can be incremented or cleared. <p> Statistics based on (constructed from) external moments cannot be incremented or cleared.</p>
 */
  protected boolean incMoment;
  /** 
 * Constructs a Mean. 
 */
  public Mean(){
    incMoment=true;
    moment=new FirstMoment();
  }
  /** 
 * Constructs a Mean with an External Moment.
 * @param m1 the moment
 */
  public Mean(  final FirstMoment m1){
    this.moment=m1;
    incMoment=false;
  }
  /** 
 * Copy constructor, creates a new  {@code Mean} identicalto the  {@code original}
 * @param original the {@code Mean} instance to copy
 */
  public Mean(  Mean original){
    copy(original,this);
  }
  /** 
 * {@inheritDoc}
 */
  @Override public void increment(  final double d){
    if (incMoment) {
      moment.increment(d);
    }
  }
  /** 
 * {@inheritDoc}
 */
  @Override public void clear(){
    if (incMoment) {
      moment.clear();
    }
  }
  /** 
 * {@inheritDoc}
 */
  @Override public double getResult(){
    return moment.m1;
  }
  /** 
 * {@inheritDoc}
 */
  public long getN(){
    return moment.getN();
  }
  /** 
 * Returns the arithmetic mean of the entries in the specified portion of the input array, or <code>Double.NaN</code> if the designated subarray is empty. <p> Throws <code>IllegalArgumentException</code> if the array is null.</p> <p> See  {@link Mean} for details on the computing algorithm.</p>
 * @param values the input array
 * @param begin index of the first array element to include
 * @param length the number of elements to include
 * @return the mean of the values or Double.NaN if length = 0
 * @throws IllegalArgumentException if the array is null or the array indexparameters are not valid
 */
  @Override public double evaluate(  final double[] values,  final int begin,  final int length){
    if (test(values,begin,length)) {
      Sum sum=new Sum();
      double sampleSize=length;
      double xbar=sum.evaluate(values,begin,length) / sampleSize;
      double correction=0;
      for (int i=begin; i < begin + length; i++) {
        correction+=values[i] - xbar;
      }
      return xbar + (correction / sampleSize);
    }
    return Double.NaN;
  }
  /** 
 * Returns the weighted arithmetic mean of the entries in the specified portion of the input array, or <code>Double.NaN</code> if the designated subarray is empty. <p> Throws <code>IllegalArgumentException</code> if either array is null.</p> <p> See  {@link Mean} for details on the computing algorithm. The two-pass algorithmdescribed above is used here, with weights applied in computing both the original estimate and the correction factor.</p> <p> Throws <code>IllegalArgumentException</code> if any of the following are true: <ul><li>the values array is null</li> <li>the weights array is null</li> <li>the weights array does not have the same length as the values array</li> <li>the weights array contains one or more infinite values</li> <li>the weights array contains one or more NaN values</li> <li>the weights array contains negative values</li> <li>the start and length arguments do not determine a valid array</li> </ul></p>
 * @param values the input array
 * @param weights the weights array
 * @param begin index of the first array element to include
 * @param length the number of elements to include
 * @return the mean of the values or Double.NaN if length = 0
 * @throws IllegalArgumentException if the parameters are not valid
 * @since 2.1
 */
  public double evaluate(  final double[] values,  final double[] weights,  final int begin,  final int length){
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","org.apache.commons.math.stat.descriptive.moment.Mean#double#evaluate#?,double[],double[],int,int START#0");
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","this: ",this);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","values: ",values);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","weights: ",weights);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","begin: ",begin);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","length: ",length);
    if (test(values,weights,begin,length)) {
      Sum sum=new Sum();
      double sumw=sum.evaluate(weights,begin,length);
      double xbarw=sum.evaluate(values,weights,begin,length) / sumw;
      double correction=0;
      for (int i=begin; i < begin + length; i++) {
        correction+=weights[i] * (values[i] - xbarw);
      }
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","this: ",this);
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","values: ",values);
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","weights: ",weights);
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","begin: ",begin);
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","length: ",length);
      auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","org.apache.commons.math.stat.descriptive.moment.Mean#double#evaluate#?,double[],double[],int,int END#0");
      return (Double)auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","xbarw + (correction / sumw): ",xbarw + (correction / sumw));
    }
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","this: ",this);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","values: ",values);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","weights: ",weights);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","begin: ",begin);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","length: ",length);
    auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","org.apache.commons.math.stat.descriptive.moment.Mean#double#evaluate#?,double[],double[],int,int END#0");
    return (Double)auxiliary.Dumper.write("/home/xushicheng/eclipse-workspace/InPaFer/tmp/Math/41//state/initial_state","Double.NaN: ",Double.NaN);
  }
  /** 
 * Returns the weighted arithmetic mean of the entries in the input array. <p> Throws <code>IllegalArgumentException</code> if either array is null.</p> <p> See  {@link Mean} for details on the computing algorithm. The two-pass algorithmdescribed above is used here, with weights applied in computing both the original estimate and the correction factor.</p> <p> Throws <code>IllegalArgumentException</code> if any of the following are true: <ul><li>the values array is null</li> <li>the weights array is null</li> <li>the weights array does not have the same length as the values array</li> <li>the weights array contains one or more infinite values</li> <li>the weights array contains one or more NaN values</li> <li>the weights array contains negative values</li> </ul></p>
 * @param values the input array
 * @param weights the weights array
 * @return the mean of the values or Double.NaN if length = 0
 * @throws IllegalArgumentException if the parameters are not valid
 * @since 2.1
 */
  public double evaluate(  final double[] values,  final double[] weights){
    return evaluate(values,weights,0,values.length);
  }
  /** 
 * {@inheritDoc}
 */
  @Override public Mean copy(){
    Mean result=new Mean();
    copy(this,result);
    return result;
  }
  /** 
 * Copies source to dest. <p>Neither source nor dest can be null.</p>
 * @param source Mean to copy
 * @param dest Mean to copy to
 * @throws NullArgumentException if either source or dest is null
 */
  public static void copy(  Mean source,  Mean dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    dest.incMoment=source.incMoment;
    dest.moment=source.moment.copy();
  }
}

Compile Subject [_name=Math, _id=41]
Subject [_name=Math, _id=41] Compile Success! 

