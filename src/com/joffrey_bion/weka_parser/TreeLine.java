package com.joffrey_bion.weka_parser;

/**
 * A {@code TreeLine} is an object representing a line of Weka's decision tree
 * output.
 * <p>
 * Each line represents the son of an internal node, of the form
 * {@code feature <= threshold} (low/left son) or {@code feature > threshold}
 * (high/right son). That is why there are two lines per internal node, corresponding
 * to each son. Also, in the Weka output, the tree has no root for this reason. It
 * starts with the first (left) son of the root.
 * </p>
 * <p>
 * Some of the lines also contain a leaf representing the class to give when reaching
 * this end of the tree. In this case, the internal node's son represented by this
 * {@code TreeLine} is actually a leaf.
 * </p>
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey BION</a>
 */
public class TreeLine {

    private boolean isLowSon;
    private String feature;
    private double threshold;

    private boolean isLeaf;
    private String classAttribute;

    /**
     * Creates a parsed {@code TreeLine} object from the plain {@code String}
     * containing a line of Weka's tree output.
     * 
     * @param line
     *            A line of Weka's tree output.
     */
    public TreeLine(String line) {
        // spaces separate the '|', features, operators and thresholds.
        // colons separate a threshold from a class
        String[] words = line.split(" +|: +");
        // computing the depth of the node, to know where to start in the string
        int depth = 0;
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("|")) {
                depth++;
            } else {
                break;
            }
        }
        feature = words[depth];
        isLowSon = isLowerOrEqualOperator(words[depth + 1]);
        threshold = Double.parseDouble(words[depth + 2]);
        // check whether there is also a leaf at the end of the line
        if (words.length > depth + 3) {
            isLeaf = true;
            classAttribute = words[depth + 3];
        } else {
            isLeaf = false;
            classAttribute = null;
        }
    }

    /**
     * Tells whether {@code operator} is '{@code <=}' or '{@code >}'.
     * 
     * @param operator
     *            One of '{@code <=}' or '{@code >}'.
     * @return {@code true} if '{@code <=}', {@code false} if '{@code >}', throws a
     *         runtime exception otherwise.
     */
    private static boolean isLowerOrEqualOperator(String operator) {
        if (operator.equals("<=")) {
            return true;
        } else if (operator.equals(">")) {
            return false;
        } else {
            throw new RuntimeException("incorrect token '" + operator
                    + "', operator <= or > expected");
        }
    }

    /**
     * Returns whether this line is a left son (corresponding to
     * {@code feature <= threshold}) or not.
     * 
     * @return Whether this line is a left (low) son.
     */
    public boolean isLowSon() {
        return isLowSon;
    }

    /**
     * Returns the feature that this line's internal node represents.
     * 
     * @return The feature that this line's internal node represents.
     */
    public String getFeature() {
        return feature;
    }

    /**
     * Returns the threshold that this line's internal node represents.
     * 
     * @return The feature that this line's internal node represents.
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Returns whether this line contains a leaf or not.
     * 
     * @return Whether the internal node's son represented by this {@code TreeLine}
     *         is a leaf.
     */
    public boolean isLeaf() {
        return isLeaf;
    }

    /**
     * Returns the class associated with the leaf contained in this line (if any).
     * You can use {@link #isLeaf()} to ensure that.
     * 
     * @return The class represented by the leaf, or null if this line does not
     *         contain a leaf.
     */
    public String getClassAttribute() {
        return classAttribute;
    }

    /**
     * Returns whether the specified line is the corresponding sibling of this line.
     * If this {@code TreeLine} is the left (respectively right) son of an internal
     * node, and {@code line} is the right (respectively left) son of the same
     * internal node, this method returns {@code true}. In any other case, it returns
     * false.
     * 
     * @param line
     *            The {@code TreeLine} to test.
     * @return whether the specified line is the corresponding sibling of this line.
     */
    public boolean matches(TreeLine line) {
        boolean same = feature.equals(line.feature) && threshold == line.threshold;
        return same && ((isLowSon && !line.isLowSon) || (!isLowSon && line.isLowSon));
    }
}
