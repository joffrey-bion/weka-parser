package com.joffrey_bion.weka_parser;

public class TreeLine {
    
    private int depth;
    private boolean isLeft;
    private String feature;
    private double threshold;
    
    private boolean hasLeaf;
    private String level;
    
    // LinearAccelerationAxis0StdDev <= 4.285762
    // |   |   AccelerometerAxis0StdDev > 1.377941
    // |   |   |   |   GravityAxis1Avg <= -4.270108
    // |   |   |   |   |   AccelerometerAxis1Avg <= -4.683059: Light (2.0)

    public TreeLine(String line) {
        String[] words = line.split(" +|: +");
        depth = 0;
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("|")) {
                depth++;
            } else {
                break;
            }
        }
        feature = words[depth];
        isLeft = isLeftSon(words[depth+1]);
        threshold = Double.parseDouble(words[depth + 2]);
        if (words.length > depth + 3) {
            hasLeaf = true;
            level = words[depth + 3];
        } else {
            hasLeaf = false;
        }
    }

    private static boolean isLeftSon(String operator) {
        if (operator.equals("<=")) {
            return true;
        } else if (operator.equals(">")) {
            return false;
        } else {
            throw new RuntimeException("incorrect token '" + operator + "' operator <= or > expected");
        }
    }

    public int getDepth() {
        return depth;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public String getFeature() {
        return feature;
    }

    public double getThreshold() {
        return threshold;
    }

    public boolean hasLeaf() {
        return hasLeaf;
    }

    public String getLevel() {
        return level;
    }
    
    public boolean matches(TreeLine line) {
        boolean same = feature.equals(line.feature) && threshold == line.threshold;
        return same && ((isLeft && !line.isLeft) || (!isLeft && line.isLeft));
    }
}
