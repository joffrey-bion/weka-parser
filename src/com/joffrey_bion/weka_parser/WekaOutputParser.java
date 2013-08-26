package com.joffrey_bion.weka_parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.SwingUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.joffrey_bion.generic_guis.LookAndFeel;
import com.joffrey_bion.generic_guis.file_picker.FilePicker;
import com.joffrey_bion.generic_guis.file_picker.JFilePickersPanel;
import com.joffrey_bion.generic_guis.file_processor.JFileProcessorWindow;
import com.joffrey_bion.utils.xml.XmlHelper;

/**
 * This program parses Weka's output tree and writes it to an XML file representing
 * the same decision tree. It is designed to parse decision trees with continuous
 * attributes only (each accepting a threshold).
 * <p>
 * <b>Important:</b> make sure all attributes names (including the class attribute
 * name and values) do not contain spaces.
 * </p>
 * 
 * @author <a href="mailto:joffrey.bion@gmail.com">Joffrey BION</a>
 */
public class WekaOutputParser {

    private static final int ARG_SOURCE = 0;
    private static final int ARG_DEST = 1;

    private static final String TYPE_LEAF = "leaf";
    private static final String TYPE_NODE = "node";
    private static final String ATT_NODE_TYPE = "type";
    private static final String ATT_CLASS = "class";
    private static final String ATT_FEATURE = "feature";
    private static final String ATT_THRESHOLD = "threshold";
    private static final String TAG_ROOT = "root";
    private static final String TAG_LEFT_SON = "left";
    private static final String TAG_RIGHT_SON = "right";

    /**
     * Choose between GUI or console version according to the number of arguments.
     * 
     * @param args
     *            No arguments will start the GUI, otherwise two filenames have to be
     *            specified: source, then destination.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    openWindow();
                }
            });
        } else if (args.length == 2) {
            process(args[ARG_SOURCE], args[ARG_DEST]);
        } else {
            System.out.println("Usage: WekaOutputParser.jar <source-file> <dest-file>");
        }
    }

    /**
     * Starts the GUI.
     */
    private static void openWindow() {
        LookAndFeel.setSystemLookAndFeel();
        // file pickers source and destination
        final JFilePickersPanel filePickers = new JFilePickersPanel("Weka model (text)",
                "Output file");
        for (FilePicker fp : filePickers.getInputFilePickers()) {
            fp.addFileTypeFilter(".txt", "Text file");
        }
        for (FilePicker fp : filePickers.getOutputFilePickers()) {
            fp.addFileTypeFilter(".xml", "XML file");
        }
        @SuppressWarnings("serial")
        JFileProcessorWindow frame = new JFileProcessorWindow("Weka to XML converter", filePickers,
                null, "Convert") {
            @Override
            public void process(String[] inPaths, String[] outPaths, int processBtnIndex) {
                this.clearLog();
                WekaOutputParser.process(inPaths[0], outPaths[0]);
            }
        };
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Parses the given Weka output and writes it to an XML file representing a
     * decision tree.
     * 
     * @param wekaFilePath
     *            The path to the input Weka file.
     * @param outputPath
     *            The path to the output XML file.
     */
    private static void process(String wekaFilePath, String outputPath) {
        if (wekaFilePath == null || wekaFilePath.equals("")) {
            System.err.println("No input file specified");
            return;
        }
        if (outputPath == null || outputPath.equals("")) {
            System.err.println("No output file specified");
            return;
        }
        try {
            System.out.println("Opening file '" + wekaFilePath + "'...");
            BufferedReader reader = new BufferedReader(new FileReader(wekaFilePath));
            System.out.println("Parsing lines...");
            LinkedList<TreeLine> lines = new LinkedList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(new TreeLine(line));
            }
            reader.close();
            System.out.println("Creating tree...");
            Tree tree = Tree.createTree(lines);
            System.out.println("Writing XML output file...");
            Document doc = XmlHelper.createEmptyDomDocument();
            doc.appendChild(treeToXml(doc, tree, TAG_ROOT));
            XmlHelper.writeXml(outputPath, doc);
            System.out.println("XML successfully written in '" + outputPath + "'");
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Converts the specified {@link Tree} into an XML {@link Element}.
     * 
     * @param doc
     *            The {@code Document} the returned {@code Element} will be part of.
     * @param tree
     *            The {@code Tree} to convert.
     * @param tagName
     *            The tag name of the created element.
     * @return An XML {@code Element} representing the specified {@code Tree}.
     */
    private static Element treeToXml(Document doc, Tree tree, String tagName) {
        Element elt = doc.createElement(tagName);
        if (tree.isLeaf()) {
            elt.setAttribute(ATT_NODE_TYPE, TYPE_LEAF);
            elt.setAttribute(ATT_CLASS, tree.getClassAttribute());
        } else {
            elt.setAttribute(ATT_NODE_TYPE, TYPE_NODE);
            elt.setAttribute(ATT_FEATURE, tree.getFeature());
            elt.setAttribute(ATT_THRESHOLD, tree.getThreshold().toString());
            elt.appendChild(treeToXml(doc, tree.getLowSon(), TAG_LEFT_SON));
            elt.appendChild(treeToXml(doc, tree.getHighSon(), TAG_RIGHT_SON));
        }
        return elt;
    }
}
