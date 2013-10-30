package edu.illinois.ncsa.cyberintegrator.tool.creator;

import java.awt.GridBagConstraints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URLEncoder;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.tool.creator.java.JavaWizard;
import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.Person;

public class PersonWizard extends Wizard {
    private static final long serialVersionUID = 1L;
    private static Logger     logger           = LoggerFactory.getLogger(JavaWizard.class);

    private PersonPage        personPage;

    public PersonWizard(JFrame parent, String name) {
        super(parent, name);

        personPage = new PersonPage(this);
        addPage(personPage);
    }

    public Person createPerson(String endpoint) {
        System.out.println("Creating Person");

        HttpClient httpclient = new DefaultHttpClient();
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }

        try {
            Person person = personPage.getPerson();
            endpoint += "persons?firstname=" + URLEncoder.encode(person.getFirstName(), "UTF-8");
            endpoint += "&lastname=" + URLEncoder.encode(person.getLastName(), "UTF-8");
            endpoint += "&email=" + URLEncoder.encode(person.getEmail(), "UTF-8");
            HttpPost httppost = new HttpPost(endpoint);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            try {
                String responseStr = httpclient.execute(httppost, responseHandler);
                logger.debug("response string " + responseStr);
                person.setId(responseStr);
                return person;
            } catch (Exception e) {
                logger.error("HTTP post failed", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Could not create form.", e);
            return null;
        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
            } catch (Exception e) {
                logger.debug("Could not shutdown connection", e);
            }
        }
    }

    class PersonPage extends WizardPage implements KeyListener {
        private static final long serialVersionUID = 1L;
        private JTextField        txtFirstName;
        private JTextField        txtLastName;
        private JTextField        txtEmail;
        private JPasswordField    txtPassword;

        public PersonPage(Wizard wizard) {
            super(wizard);

            add(new JLabel("First Name :"), 0, 0);
            txtFirstName = new JTextField();
            txtFirstName.addKeyListener(this);
            add(txtFirstName, 1, 0, GridBagConstraints.HORIZONTAL);

            add(new JLabel("Last Name :"), 0, 1);
            txtLastName = new JTextField();
            txtLastName.addKeyListener(this);
            add(txtLastName, 1, 1, GridBagConstraints.HORIZONTAL);

            add(new JLabel("E-Mail :"), 0, 2);
            txtEmail = new JTextField();
            txtEmail.addKeyListener(this);
            add(txtEmail, 1, 2, GridBagConstraints.HORIZONTAL);

            add(new JLabel("Password :"), 0, 3);
            txtPassword = new JPasswordField();
            txtPassword.addKeyListener(this);
            add(txtPassword, 1, 3, GridBagConstraints.HORIZONTAL);

            checkUI();
        }

        public Person getPerson() {
            Person person = new Person();
            person.setFirstName(txtFirstName.getText().trim());
            person.setLastName(txtLastName.getText().trim());
            person.setEmail(txtEmail.getText().trim());
            return person;
        }

        public Account getAccount() {
            Account account = new Account();
            account.setUserid(txtFirstName.getText().trim());
            account.setPassword(txtPassword.getText().trim());
            account.setPerson(getPerson());
            return account;
        }

        private void checkUI() {
            if (txtFirstName.getText().trim().equals("")) {
                setStepComplete(false);
                return;
            }
            if (txtLastName.getText().trim().equals("")) {
                setStepComplete(false);
                return;
            }
            if (txtEmail.getText().trim().equals("")) {
                setStepComplete(false);
                return;
            }
            if (txtPassword.getText().trim().equals("")) {
                setStepComplete(false);
                return;
            }

            setStepComplete(true);
        }

        @Override
        public void keyReleased(KeyEvent e) {}

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            checkUI();
        }
    }
}
