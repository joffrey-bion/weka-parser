package com.joffrey_bion.weka_parser;
import java.util.LinkedList;

public class Tree {

    private boolean isLeaf;
    private String level;

    private Tree left;
    private Tree right;
    private String feature;
    private Double threshold;

    private Tree(String level) {
        this.isLeaf = true;
        this.level = level;
    }

    private Tree(String feature, double threshold) {
        this.isLeaf = false;
        this.feature = feature;
        this.threshold = threshold;
        this.left = null;
        this.right = null;
    }

    public static Tree createTree(LinkedList<TreeLine> lines) {
        if (lines == null || lines.size() == 0) {
            throw new IllegalArgumentException("The list cannot be null or empty");
        }
        TreeLine first = lines.pollFirst();
        if (!first.isLeft()) {
            throw new RuntimeException("There should be a left side first");
        }
        Tree tree = new Tree(first.getFeature(), first.getThreshold());
        LinkedList<TreeLine> leftChildren = null, rightChildren = null;
        boolean addLeft;
        if (first.hasLeaf()) {
            tree.left = new Tree(first.getLevel());
            addLeft = false;
        } else {
            leftChildren = new LinkedList<>();
            addLeft = true;
        }
        for (TreeLine line : lines) {
            if (line.matches(first)) {
                addLeft = false;
                if (line.hasLeaf()) {
                    tree.right = new Tree(line.getLevel());
                    break;
                } else {
                    rightChildren = new LinkedList<>();
                    continue;
                }
            }
            if (addLeft) {
                leftChildren.add(line);
            } else if (rightChildren != null) {
                rightChildren.add(line);
            }
        }
        if (tree.left == null) {
            tree.left = createTree(leftChildren);
        }
        if (tree.right == null) {
            tree.right = createTree(rightChildren);
        }
        return tree;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public String getLevel() {
        return level;
    }

    public Tree getLeft() {
        return left;
    }

    public Tree getRight() {
        return right;
    }

    public String getFeature() {
        return feature;
    }

    public Double getThreshold() {
        return threshold;
    }
}
