import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.*;

import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;


public class LibraryApp extends JFrame
{
	private final String XSDFile = "library.xsd";
	private final String XMLFile = "library.xml";
	private Document document;
	private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
	public LibraryApp() {
        setTitle("Система управления библиотекой");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        int res = loadXML();
        if (res != 0)
        {
        	if (res == 1)
        	{
        		JOptionPane.showMessageDialog(this, "Ошибка валидации XML. Приложение будет закрыто.", "Error", JOptionPane.ERROR_MESSAGE);
        	}
        	else
        	{
        		JOptionPane.showMessageDialog(this, "Ошибка загрузки XML. Приложение будет закрыто.", "Error", JOptionPane.ERROR_MESSAGE);
        	}            
            System.exit(1);
        }
        
        initComponents();
        refreshTable();
    }

    private void refreshTable()
    {
    	tableModel.setRowCount(0);
    	NodeList nList = document.getElementsByTagName("book");
    	for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;
                tableModel.addRow(new Object[]{
                        elem.getAttribute("id"),
                        getTagValue("title", elem),
                        getTagValue("author", elem),
                        getTagValue("year", elem),
                        getTagValue("price", elem),
                        getTagValue("category", elem),
                        elem.getAttribute("totalCount"),
                        elem.getAttribute("availableCount")
                });
            }
        }
	}
    
    private String getTagValue(String tag, Element element) {
        NodeList nl = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node n = nl.item(0);
        return n.getNodeValue();
    }

	private void initComponents()
    {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnAdd = new JButton("Добавить книгу");
        btnAdd.addActionListener(e -> addBookDialog());
        
        JButton btnPrice = new JButton("Переоценка");
        
        JButton btnCheckout = new JButton("Выдать книгу");

        JLabel lblSearch = new JLabel("Поиск:");
        searchField = new JTextField(20);
        JButton btnSearch = new JButton("Найти");
        JButton btnReset = new JButton("Сброс");

        controlPanel.add(btnAdd);
        controlPanel.add(btnPrice);
        controlPanel.add(btnCheckout);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(lblSearch);
        controlPanel.add(searchField);
        controlPanel.add(btnSearch);
        controlPanel.add(btnReset);

        add(controlPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Название", "Автор", "Год", "Цена", "Категория", "Всего", "В наличии"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

	private void addBookDialog()
	{
		JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField catField = new JTextField();
        JTextField idField = new JTextField();
        JTextField totalField = new JTextField();

        Object[] message = {
                "ID:", idField,
                "Title:", titleField,
                "Author:", authorField,
                "Year:", yearField,
                "Price:", priceField,
                "Category:", catField,
                "Total count:", totalField
        };
        System.out.print(11);
        int option = JOptionPane.showConfirmDialog(null, message, "Добавить книгу", JOptionPane.OK_CANCEL_OPTION);
	}
	
	private int loadXML() {
        try 
        {
        	validateXML();
        	
        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            
            document = dBuilder.parse(new File(XMLFile));
            document.getDocumentElement().normalize();
            return 0;

        } 
        catch (SAXException e) {
            System.out.println("XML file is not valid");
            return 1;
        } 
        catch (IOException | ParserConfigurationException e)
        {
        	e.printStackTrace();
        	return 2;
        }
    }
	
	private int validateXML() throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema sc = factory.newSchema(new File(XSDFile));
            
        Validator validator = sc.newValidator();
        Source xmlSource = new StreamSource(new File(XMLFile));
        validator.validate(xmlSource);
        return 0;    
	}
}