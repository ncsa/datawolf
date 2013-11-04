package edu.illinois.ncsa.cyberintegrator.tool.creator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.tool.creator.commandline.CommandLineWizard;
import edu.illinois.ncsa.cyberintegrator.tool.creator.java.JavaWizard;
import edu.illinois.ncsa.domain.Person;

public class ToolCreator extends JFrame {
    private static final long serialVersionUID = 1L;
    private static Logger     logger           = LoggerFactory.getLogger(JavaWizard.class);

    private Person            person           = null;

    public ToolCreator() {
        super("Cyberintegrator Tool Creator");

        JPanel panel = new JPanel(new GridLayout(0, 1));
        add(panel, BorderLayout.CENTER);

        final JTextField txtURL = new JTextField("http://localhost:8888/");
        add(txtURL, BorderLayout.NORTH);

        JButton button = new JButton("Add Person");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!txtURL.getText().trim().equals("")) {
                    PersonWizard wizard = new PersonWizard(ToolCreator.this, "Add Person");
                    if (wizard.open()) {
                        person = wizard.createPerson(txtURL.getText().trim());
                    }
                }
            }
        });
        panel.add(button);

        button = new JButton("Add Command-Line Tool");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!txtURL.getText().trim().equals("") && (person != null)) {
                    CommandLineWizard wizard = new CommandLineWizard(ToolCreator.this, "Cyberintegrator Command Line Wizard");
                    if (wizard.open()) {
                        wizard.createTool(txtURL.getText().trim(), person);
                    }
                }
            }
        });
        panel.add(button);

        button = new JButton("Add Java Tool");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!txtURL.getText().trim().equals("") && (person != null)) {
                    JavaWizard wizard = new JavaWizard(ToolCreator.this, "Cyberintegrator Java Wizard");
                    if (wizard.open()) {
                        wizard.createTool(txtURL.getText().trim(), person);
                    }
                }
            }
        });
        panel.add(button);

        setSize(new Dimension(320, 240));
        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void postTool(File file, String endpoint) {
        String responseStr = null;

        HttpClient httpclient = new DefaultHttpClient();
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }

        try {
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("tool", new FileBody(file));

            HttpPost httppost = new HttpPost(endpoint + "workflowtools");
            httppost.setEntity(reqEntity);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            try {
                responseStr = httpclient.execute(httppost, responseHandler);
                logger.debug("response string" + responseStr);
            } catch (Exception e) {
                logger.error("HTTP post failed", e);
            }
        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
            } catch (Exception e) {
                logger.debug("Could not shutdown connection", e);
            }
        }
    }

    public static void main(String[] args) {
        new GenericXmlApplicationContext("toolcreator.xml");
        new ToolCreator();
    }
}
