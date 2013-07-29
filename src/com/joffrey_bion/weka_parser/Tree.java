package com.joffrey_bion.weka_parser;

import java.util.LinkedList;

/**
 * A {@code Tree} object can be viewed as a subtree of a decision tree. It is either
 * an internal node or a leaf.
 * <p>
 * If it is a leaf, then it represents a class. If it is an internal node, then it
 * represents a feature that has to be compared to a threshold.
 * </p>
 * <p>
 * On a given sample, if the specified feature of the sample is lower than or equal
 * to the threshold, then we move to the left (low) son. Otherwise, we move to the
 * right (high) son. Anyway, we carry on until a leaf is reached, giving the
 * class of the tested sample.
 * </p>
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey BION</a>
 */
class Tree {

    private boolean isLeaf;
    private String classAttribute;

    private Tree lowSon;
    private Tree highSon;
    private String feature;
    private Double threshold;

    /**
     * Creates a new leaf.
     * 
     * @param classAttribute
     *            The class associated with this leaf.
     */
    private Tree(String classAttribute) {
        this.isLeaf = true;
        this.classAttribute = classAttribute;
    }

    /**
     * Creates a new internal node.
     * 
     * @param feature
     *            The String representing the feature that has to be tested.
     * @param threshold
     *            The threshold to choose between lowSon or highSon. If the feature
     *            is less than or equal to the threshold, then the lowSon has to be
     *            considered, otherwise the highSon branch is followed.
     */
    private Tree(String feature, double threshold) {
        this.isLeaf = false;
        this.feature = feature;
        this.threshold = threshold;
        this.lowSon = null;
        this.highSon = null;
    }

    /**
     * Creates a {@code Tree} according to the given parsed {@link TreeLine}s.
     * 
     * @param lines
     *            The {@code TreeLine}s that were read in the Weka file, and parsed.
     * @return A {@code Tree} corresponding to these lines.
     */
    public static Tree createTree(LinkedList<TreeLine> lines) {
        if (lines == null || lines.size() == 0) {
            throw new IllegalArgumentException("The list cannot be null or empty");
        }
        // there are no actual nodes in the Weka file, just the 2 sons, starting with
        // the left one
        TreeLine first = lines.pollFirst();
        if (!first.isLowSon()) {
            throw new RuntimeException("There should be a left (<=) side first");
        }
        Tree tree = new Tree(first.getFeature(), first.getThreshold());
        // These lists don't need to be initialized if the corresponding son is a
        // leaf.
        LinkedList<TreeLine> leftDescendantsLines = null, rightDescendantsLines = null;
        boolean addLeft;
        if (first.isLeaf()) {
            // the left side is a leaf, no left descendants to store
            tree.lowSon = new Tree(first.getClassAttribute());
            addLeft = false;
        } else {
            // the left side is an internal node so we expect left descendants
            leftDescendantsLines = new LinkedList<>();
            addLeft = true;
        }
        for (TreeLine line : lines) {
            // we stop adding left descendants when we find the matching right
            // sibling
            if (line.matches(first)) {
                addLeft = false;
                if (line.isLeaf()) {
                    // the matching right sibling is a leaf, no right descendants
                    tree.highSon = new Tree(line.getClassAttribute());
                    break;
                } else {
                    // the matching right sibling is an internal node
                    rightDescendantsLines = new LinkedList<>();
                    continue;
                }
            }
            if (addLeft) {
                // we are in the left descendants
                leftDescendantsLines.add(line);
            } else if (rightDescendantsLines != null) {
                // we are in the right descendants
                rightDescendantsLines.add(line);
            }
        }
        // recursive calls on the descendants if the sons are not leaves
        if (tree.lowSon == null) {
            tree.lowSon = createTree(leftDescendantsLines);
        }
        if (tree.highSon == null) {
            tree.highSon = createTree(rightDescendantsLines);
        }
        return tree;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public String getClassAttribute() {
        return classAttribute;
    }

    public Tree getLowSon() {
        return lowSon;
    }

    public Tree getHighSon() {
        return highSon;
    }

    public String getFeature() {
        return feature;
    }

    public Double getThreshold() {
        return threshold;
    }
}
