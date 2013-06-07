package com.joffrey_bion.weka_parser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.joffrey_bion.file_processor_window.FilePicker;
import com.joffrey_bion.file_processor_window.JFilePickersPanel;
import com.joffrey_bion.file_processor_window.JFileProcessorWindow;
import com.joffrey_bion.file_processor_window.Logger;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String[] str = "bla bla   bli:  blo".split(" +|: +");
        for (String s : str) {
            System.out.println(s);
        }
        if (args.length == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    openWindow();
                }
            });
            return;
        }
    }

    private static void openWindow() {
        // windows system look and feel for the window
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // file pickers source and destination
        final JFilePickersPanel filePickers = new JFilePickersPanel("Weka model (text)", "Output file");
        for (FilePicker fp : filePickers.getInputFilePickers()) {
            fp.addFileTypeFilter(".txt", "Text file");
        }
        for (FilePicker fp : filePickers.getOutputFilePickers()) {
            fp.addFileTypeFilter(".xml", "XML file");
        }
        @SuppressWarnings("serial")
        JFileProcessorWindow frame = new JFileProcessorWindow("Weka to XML converter", "Convert",
                filePickers, null) {
            @Override
            public void process(String[] inPaths, String[] outPaths) {
                this.clearLog();
                Main.process(inPaths[0], outPaths[0], this);
            }
        };
        frame.pack();
        frame.setVisible(true);
    }
    
    private static void process(String wekaFilePath, String outputPath, Logger log) {
        if (wekaFilePath == null || wekaFilePath.equals("")) {
            log.printErr("No input file specified");
        }
        if (outputPath == null || outputPath.equals("")) {
            log.printErr("No output file specified");
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(wekaFilePath));
            LinkedList<TreeLine> lines = new LinkedList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(new TreeLine(line));
            }
            reader.close();
            Tree tree = Tree.createTree(lines);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document doc = documentBuilder.newDocument();
                doc.appendChild(treeToXml(doc, tree));
                writeXml(outputPath, doc);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.printErr(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            log.printErr(e.getMessage());
        }
    }
    
    private static void writeXml(String filePath, Document doc) {
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            // send DOM to file
            FileOutputStream fos = new FileOutputStream(filePath);
            tr.transform(new DOMSource(doc), new StreamResult(fos));
            fos.close();
            System.out.println("Parameters saved.");
        } catch (TransformerException te) {
            System.out.println(te.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }
    
    private static Element treeToXml(Document doc, Tree tree) {
        Element elt;
        if (tree.isLeaf()) {
            elt = doc.createElement("leaf");
            elt.setAttribute("level", tree.getLevel());
        } else {
            elt = doc.createElement("node");
            elt.setAttribute("feature", tree.getFeature());
            elt.setAttribute("threshold", tree.getThreshold().toString());
            elt.appendChild(treeToXml(doc, tree.getLeft()));
            elt.appendChild(treeToXml(doc, tree.getRight()));
        }
        return elt;
    }
}
